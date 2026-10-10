package com.shinoaki.wows.api.codec;

import com.shinoaki.wows.api.error.BasicException;
import com.shinoaki.wows.api.error.HttpThrowableStatus;
import com.shinoaki.wows.api.monitor.SchemaRecorder;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

/**
 * 底层HTTP编码/解码工具,同时作为历史兼容入口。
 * <p>
 * 请求发送请按业务通道使用 {@link ApiHttp}(官方开发者API,内置cookie与重定向处理)或
 * {@link VortexHttp}(vortex接口)。本类的 request/requestApi/send/sendAsync 方法保留原签名,
 * 分别委托给对应通道,保证旧调用方在 api.korabli.su 等带防护的服务器上也能正常工作。
 *
 * @author Xun
 * @date 2023/3/18 14:31 星期六
 */
@Slf4j
public class HttpCodec {
    private HttpCodec() {

    }

    private static int requestTimeout = 3;

    public static void updateTimeout(int timeoutSeconds) {
        requestTimeout = timeoutSeconds;
    }

    /**
     * 当前请求超时时间(秒),供各请求通道与资源类读取,统一以本处配置为准。
     */
    public static int requestTimeout() {
        return requestTimeout;
    }

    /**
     * 共享客户端当前绑定的超时(秒),与{@link #requestTimeout}不一致时触发重建。
     */
    private static volatile int clientTimeout = Integer.MIN_VALUE;

    private static volatile HttpClient client;

    /**
     * 全局共享的 {@link HttpClient},库内统一使用本实例。
     * <p>
     * JDK 的 HttpClient 线程安全且自带连接池,复用同一个实例可避免每次请求都重建
     * selector/executor 线程与 TCP、TLS 连接(高频请求下开销显著)。
     * <b>因此库内不再各自 {@code HttpClient.newHttpClient()},一律通过本方法获取。</b>
     * <p>
     * 该实例不跟随重定向、不挂 CookieHandler:重定向与会话 Cookie 由{@link HttpTransport}
     * 按主机自行处理,此处若自动跟随重定向会破坏 api.korabli.su 的 307 握手。
     * <p>
     * 连接超时取自{@link #requestTimeout},调用{@link #updateTimeout(int)}后会在下次取用时
     * 重建为新的超时;重建不影响进行中的请求(它们仍持有旧实例)。
     *
     * @return 共享HttpClient
     */
    public static HttpClient client() {
        HttpClient shared = client;
        int timeout = requestTimeout;
        if (shared == null || clientTimeout != timeout) {
            synchronized (HttpCodec.class) {
                if (client == null || clientTimeout != timeout) {
                    client = HttpClient.newBuilder()
                            .connectTimeout(Duration.ofSeconds(timeout))
                            .build();
                    clientTimeout = timeout;
                }
                shared = client;
            }
        }
        return shared;
    }

    public static final String CONTENT_ENCODING = "Content-Encoding";

    /**
     * 构造官方开发者API请求(已携带浏览器标识并支持重放,详见 {@link ApiHttp#request(URI)})。
     */
    public static HttpRequest requestApi(URI uri) {
        return ApiHttp.request(uri);
    }

    /**
     * 构造vortex请求(浏览器标识等,详见 {@link VortexHttp#request(URI)})。
     */
    public static HttpRequest request(URI uri) {
        return VortexHttp.request(uri);
    }

    public static CompletableFuture<HttpResponse<byte[]>> sendAsync(HttpClient client, HttpRequest request) {
        return ApiHttp.sendAsync(client, request);
    }

    /**
     * 发送请求。已升级为自动管理cookie并跟随重定向,见 {@link ApiHttp#send(HttpClient, HttpRequest)}。
     */
    public static HttpResponse<byte[]> send(HttpClient client, HttpRequest request) throws BasicException {
        return ApiHttp.send(client, request);
    }


    public static String fromDataAsString(Map<String, String> fromData) {
        StringBuilder builder = new StringBuilder();
        Charset charset = StandardCharsets.UTF_8;
        fromData.forEach((k, v) -> {
            if (!builder.isEmpty()) {
                builder.append("&");
            }
            builder.append(URLEncoder.encode(k, charset)).append("=").append(URLEncoder.encode(v, charset));
        });
        return builder.toString();
    }

    /**
     * url特殊字符解析
     *
     * @param s url信息
     * @return 结果
     */
    public static String encodeURIComponent(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8)
                .replace("+", "%20")
                .replace("%21", "!")
                .replace("%27", "'")
                .replace("%28", "(")
                .replace("%29", ")")
                .replace("%7E", "~");
    }

    public static String decodeURIComponent(String s) {
        return URLDecoder.decode(s, StandardCharsets.UTF_8);
    }


    public static String response(HttpResponse<byte[]> response) throws BasicException {
        try {
            if (response.statusCode() == 200) {
                var encoding = response.headers().firstValue(HttpCodec.CONTENT_ENCODING).orElse("").toLowerCase(Locale.ROOT);
                final String json;
                if (encoding.contains("gzip")) {
                    json = new String(HttpCodec.unGzip(response.body()), StandardCharsets.UTF_8);
                } else if (encoding.contains("deflate")) {
                    json = new String(HttpCodec.unDeflate(response.body()), StandardCharsets.UTF_8);
                } else {
                    json = new String(response.body(), StandardCharsets.UTF_8);
                }
                //字段漂移监视的采集挂钩:默认关闭,未开启时只有一次 ThreadLocal 读取
                if (SchemaRecorder.enabled()) {
                    SchemaRecorder.record(response.uri(), json);
                }
                return json;
            }
        } catch (IOException e) {
            log.error("网络请求响应异常！", e);
            throw new BasicException(HttpThrowableStatus.HTTP_IO, e);
        }
        throw new BasicException(HttpThrowableStatus.HTTP_STATUS, "http状态码异常 code=" + response.statusCode());
    }

    public static byte[] unGzip(byte[] bytes) throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             GZIPInputStream gzipInputStream = new GZIPInputStream(new ByteArrayInputStream(bytes))) {
            byte[] buffer = new byte[8192];
            int n;
            while ((n = gzipInputStream.read(buffer)) >= 0) {
                out.write(buffer, 0, n);
            }
            return out.toByteArray();
        }
    }

    public static byte[] unDeflate(byte[] bytes) throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             InflaterInputStream in = new InflaterInputStream(new ByteArrayInputStream(bytes))) {
            byte[] buffer = new byte[8192];
            int n;
            while ((n = in.read(buffer)) >= 0) {
                out.write(buffer, 0, n);
            }
            return out.toByteArray();
        }
    }
}
