package com.shinoaki.wows.api.transport;

import com.shinoaki.wows.api.auth.Credential;
import com.shinoaki.wows.api.auth.CredentialResolver;
import com.shinoaki.wows.api.auth.CredentialStore;
import com.shinoaki.wows.api.endpoint.Endpoint;
import com.shinoaki.wows.api.endpoint.EndpointRequest;
import com.shinoaki.wows.api.endpoint.QueryParam;
import com.shinoaki.wows.api.endpoint.RateLimitPolicy;
import com.shinoaki.wows.api.error.BasicException;
import com.shinoaki.wows.api.error.HttpStatusException;
import com.shinoaki.wows.api.error.ResponseParseException;
import com.shinoaki.wows.api.error.UnsupportedOnServerException;
import com.shinoaki.wows.api.monitor.SchemaRecorder;
import com.shinoaki.wows.api.server.ServerProfile;
import com.shinoaki.wows.api.util.UrlCodec;
import com.shinoaki.wows.api.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * 唯一请求管线（3.0 重写）：所有端点的唯一出口，结构上不可绕过。
 * <p>
 * 一次调用生命周期：
 * <pre>
 * 视图方法 → EndpointRequest
 *          → 服务器兼容性检查（发送前 fast-fail）
 *          → 凭证解析（发送前 fast-fail）
 *          → URL 组装（ServerProfile 注入主机与 dev 前缀；凭证与端点参数注入查询串）
 *          → 通道头 + 凭证头 → 发送（HostPolicy：cookie/重定向/429 按主机）
 *          → 解码（ResponseCodec）→ 监控挂钩（endpoint id 键）
 *          → parser → T
 * </pre>
 * 失败语义：网络/状态码/解析异常分层不变；限流（429/503）在 {@link RateLimitPolicy#TOLERANT}
 * 且端点声明了容错空值时返回空值，否则抛出。
 *
 * @author Xun
 */
@Slf4j
public final class HttpPipeline {

    /**
     * 视为"限流"的状态码：429 与 503（国服搜索接口即 503 高频限流）。
     */
    private static final Set<Integer> THROTTLE_STATUS = Set.of(429, 503);

    private final ServerProfile profile;
    private final CredentialStore credentials;

    public HttpPipeline(ServerProfile profile, CredentialStore credentials) {
        this.profile = Objects.requireNonNull(profile, "profile");
        this.credentials = Objects.requireNonNull(credentials, "credentials");
    }

    /**
     * 当前实例的服务器档案（供视图方法读取服务器事实，如 account/info 的 extra 串）。
     */
    public ServerProfile profile() {
        return profile;
    }

    /**
     * 执行一次端点请求。
     */
    public <T> T execute(Endpoint<T> endpoint, EndpointRequest request) throws BasicException {
        checkServer(endpoint);
        List<Credential> resolved = CredentialResolver.resolve(credentials, endpoint.id(), endpoint.auth(),
                request.accountId(), request.overrides());
        HttpRequest httpRequest = buildRequest(buildUri(endpoint, request, resolved), resolved);
        String json;
        try {
            HttpResponse<byte[]> response = HostPolicy.send(HttpClientProvider.client(), httpRequest);
            json = ResponseCodec.decode(response);
        } catch (HttpStatusException e) {
            if (request.policy() == RateLimitPolicy.TOLERANT
                    && THROTTLE_STATUS.contains(e.getStatusCode())
                    && endpoint.emptyValue() != null) {
                log.warn("端点 {} 命中限流（HTTP {}），按 TOLERANT 策略返回空值", endpoint.id(), e.getStatusCode());
                return endpoint.emptyValue().get();
            }
            throw e;
        }
        //字段漂移监视的采集挂钩：新管线按 endpoint id 记录（默认关闭，未开启时只有一次 ThreadLocal 读取）
        if (SchemaRecorder.enabled()) {
            SchemaRecorder.record(endpoint.id(), json);
        }
        return parse(endpoint, request, json);
    }

    /**
     * 服务器兼容性检查：CN 不提供官方开发者API（发送前 fast-fail，不查凭证、不发请求）。
     */
    private void checkServer(Endpoint<?> endpoint) throws UnsupportedOnServerException {
        if (endpoint.channel() == Channel.DEV && !profile.devApiSupported()) {
            throw new UnsupportedOnServerException("端点 " + endpoint.id(), profile.server(), "该服务器没有官方开发者API");
        }
    }

    /**
     * URL 组装：主机（ServerProfile）→ 路径（dev 前缀 + 模板渲染）→ 查询串（凭证参数在前，端点参数随后）。
     */
    private URI buildUri(Endpoint<?> endpoint, EndpointRequest request, List<Credential> credentials) {
        StringBuilder url = new StringBuilder(profile.host(endpoint.channel()));
        String path = endpoint.path();
        if (endpoint.channel() == Channel.DEV) {
            path = profile.devPrefix(endpoint.namespace()) + path.substring(1);
        }
        url.append(renderPath(path, request.params()));
        List<String> query = new ArrayList<>();
        for (Credential credential : credentials) {
            credential.queryParams().forEach((key, value) -> query.add(key + "=" + value));
        }
        for (QueryParam param : endpoint.query()) {
            String value = param.render(request.params());
            if (value == null) {
                continue;
            }
            query.add(value.isEmpty() ? param.key() : param.key() + "=" + value);
        }
        if (!query.isEmpty()) {
            url.append('?').append(String.join("&", query));
        }
        return URI.create(url.toString());
    }

    /**
     * 路径模板渲染：{@code {name}} 占位符以 URI 组件语义编码后替换；缺失参数属编程错误，直接抛异常。
     */
    private static String renderPath(String path, Map<String, String> params) {
        StringBuilder result = new StringBuilder(path.length() + 16);
        int index = 0;
        while (index < path.length()) {
            char c = path.charAt(index);
            if (c != '{') {
                result.append(c);
                index++;
                continue;
            }
            int end = path.indexOf('}', index);
            if (end < 0) {
                throw new IllegalArgumentException("路径模板缺少 '}' : " + path);
            }
            String name = path.substring(index + 1, end);
            String value = params.get(name);
            if (value == null) {
                throw new IllegalArgumentException("路径模板缺少参数 '" + name + "' : " + path);
            }
            result.append(UrlCodec.encodeComponent(value));
            index = end + 1;
        }
        return result.toString();
    }

    /**
     * 构造 HTTP 请求：通道头（三个通道目前一致）+ 凭证头 + 请求级超时。
     */
    private static HttpRequest buildRequest(URI uri, List<Credential> credentials) {
        //只声明 gzip/deflate，不声明 br（brotli）——本库未实现 br 解压，声明后服务端可能返回 br 导致解析失败
        HttpRequest.Builder builder = HttpRequest.newBuilder().uri(uri)
                .setHeader("Accept-Encoding", "gzip, deflate")
                .setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.0.0 Safari/537.36")
                .setHeader("Sec-Ch-Ua", "\"Google Chrome\";v=\"125\", \"Chromium\";v=\"125\", \"Not.A/Brand\";v=\"24\"")
                .setHeader("Sec-Ch-Ua-Mobile", "?0")
                .setHeader("Sec-Ch-Ua-Platform", "\"Windows\"")
                .setHeader("Sec-Fetch-Mode", "navigate")
                .setHeader("Sec-Fetch-Dest", "document")
                .setHeader("Sec-Fetch-User", "?1")
                .setHeader("Sec-Fetch-Site", "none")
                .timeout(Duration.ofSeconds(HttpClientProvider.requestTimeout()))
                .GET();
        for (Credential credential : credentials) {
            credential.headers().forEach(builder::setHeader);
        }
        return builder.build();
    }

    /**
     * 响应解析：字符串 → JsonNode（失败带响应摘要）→ 端点解析器；解析器内 Jackson 异常统一包装。
     */
    private static <T> T parse(Endpoint<T> endpoint, EndpointRequest request, String json) throws BasicException {
        JsonNode node = JsonUtils.json().parseResponse(json);
        try {
            return endpoint.parser().parse(node, request);
        } catch (JacksonException e) {
            throw new ResponseParseException("响应解析失败: " + e.getMessage(), e);
        }
    }
}
