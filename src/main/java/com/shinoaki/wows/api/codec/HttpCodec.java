package com.shinoaki.wows.api.codec;

import com.shinoaki.wows.api.error.BasicException;
import com.shinoaki.wows.api.error.HttpThrowableStatus;
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
     * 当前请求超时时间(秒),供各请求通道读取。
     */
    static int requestTimeout() {
        return requestTimeout;
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
                if (encoding.contains("gzip")) {
                    return new String(HttpCodec.unGzip(response.body()), StandardCharsets.UTF_8);
                }
                if (encoding.contains("deflate")) {
                    return new String(HttpCodec.unDeflate(response.body()), StandardCharsets.UTF_8);
                }
                return new String(response.body(), StandardCharsets.UTF_8);
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
