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
 * 唯一请求管线：全部端点的唯一出口。
 * 流程：兼容性检查 → 凭证解析（均发送前 fast-fail）→ URL 组装 → 发送（HostPolicy）→ 解码 → 监控挂钩 → parser。
 * 限流：429/503 在 {@link RateLimitPolicy#TOLERANT} 且端点声明空值时返回空值，否则抛出。
 *
 * @author Xun
 */
@Slf4j
public final class HttpPipeline {

    /**
     * 限流状态码（国服搜索接口即 503 高频限流）。
     */
    private static final Set<Integer> THROTTLE_STATUS = Set.of(429, 503);

    private final ServerProfile profile;
    private final CredentialStore credentials;

    public HttpPipeline(ServerProfile profile, CredentialStore credentials) {
        this.profile = Objects.requireNonNull(profile, "profile");
        this.credentials = Objects.requireNonNull(credentials, "credentials");
    }

    /**
     * 当前实例的服务器档案（视图方法读取服务器事实用）。
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
        HttpRequest httpRequest = buildRequest(endpoint, buildUri(endpoint, request, resolved), request, resolved);
        String json;
        try {
            HttpResponse<byte[]> response = HostPolicy.send(HttpClientProvider.client(), httpRequest);
            json = ResponseCodec.decode(response);
        } catch (HttpStatusException e) {
            //非 API 服务器（CN）查无数据返回 404 → 按端点声明返回空值（旧语义）
            if (e.getStatusCode() == 404 && endpoint.notFoundEmpty() != null && !profile.devApiSupported()) {
                return endpoint.notFoundEmpty().get();
            }
            if (request.policy() == RateLimitPolicy.TOLERANT
                    && THROTTLE_STATUS.contains(e.getStatusCode())
                    && endpoint.throttleEmpty() != null) {
                log.warn("端点 {} 命中限流（HTTP {}），按 TOLERANT 策略返回空值", endpoint.id(), e.getStatusCode());
                return endpoint.throttleEmpty().get();
            }
            throw e;
        }
        //字段漂移监视挂钩：按 endpoint id 记录（默认关闭）
        if (SchemaRecorder.enabled()) {
            SchemaRecorder.record(endpoint.id(), json);
        }
        return parse(endpoint, request, json);
    }

    /**
     * 服务器兼容性检查（发送前 fast-fail）：CN 无 dev API；端点可声明服务器支持面（如 RU 无战舰徽章）。
     */
    private void checkServer(Endpoint<?> endpoint) throws UnsupportedOnServerException {
        if (endpoint.channel() == Channel.DEV && !profile.devApiSupported()) {
            throw new UnsupportedOnServerException("端点 " + endpoint.id(), profile.server(), "该服务器没有官方开发者API");
        }
        if (!endpoint.servers().isEmpty() && !endpoint.servers().contains(profile.server())) {
            throw new UnsupportedOnServerException("端点 " + endpoint.id(), profile.server(), "该端点仅支持: " + endpoint.servers());
        }
    }

    /**
     * URL 组装：主机 + 路径（dev 前缀 + 模板渲染）+ 查询串（凭证先、端点参数随后）。
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
            String value = param.render(request.params(), credentials);
            if (value == null) {
                continue;
            }
            //统一 key=value（空值也带 =，如军团排行的 season= ——与旧实现一致）
            query.add(param.key() + "=" + value);
        }
        if (!query.isEmpty()) {
            url.append('?').append(String.join("&", query));
        }
        return URI.create(url.toString());
    }

    /**
     * 路径模板渲染：{@code {name}} 按 URI 组件编码替换；缺参数属编程错误，抛异常。
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
     * 构造 HTTP 请求：通道头 + 凭证头 + 请求级超时；声明了 body 即 POST。
     */
    private static HttpRequest buildRequest(Endpoint<?> endpoint, URI uri, EndpointRequest request, List<Credential> credentials) {
        //只声明 gzip/deflate：本库未实现 br 解压，声明 br 可能导致解析失败
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
        if (endpoint.body() != null) {
            builder.header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(endpoint.body().apply(request.params())));
        }
        for (Credential credential : credentials) {
            credential.headers().forEach(builder::setHeader);
        }
        return builder.build();
    }

    /**
     * 响应解析：字符串 → JsonNode → 端点解析器（Jackson 异常统一包装）。
     */
    private <T> T parse(Endpoint<T> endpoint, EndpointRequest request, String json) throws BasicException {
        JsonNode node = JsonUtils.json().parseResponse(json);
        try {
            return endpoint.parser().parse(node, request, profile);
        } catch (JacksonException e) {
            throw new ResponseParseException("响应解析失败: " + e.getMessage(), e);
        }
    }
}
