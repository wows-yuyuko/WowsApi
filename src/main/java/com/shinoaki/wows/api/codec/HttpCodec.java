package com.shinoaki.wows.api.codec;

import com.shinoaki.wows.api.error.BasicException;
import com.shinoaki.wows.api.monitor.SchemaRecorder;
import com.shinoaki.wows.api.transport.HttpClientProvider;
import com.shinoaki.wows.api.transport.ResponseCodec;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

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

    /**
     * 更新请求超时（秒）。已委托到 3.0 重写底座的共享配置 {@link HttpClientProvider}。
     */
    public static void updateTimeout(int timeoutSeconds) {
        HttpClientProvider.updateTimeout(timeoutSeconds);
    }

    /**
     * 当前请求超时时间(秒),供各请求通道与资源类读取,统一以本处配置为准。
     */
    public static int requestTimeout() {
        return HttpClientProvider.requestTimeout();
    }

    /**
     * 更新全局代理选择器（null = 全部直连）。
     * <p>
     * 已委托到 {@link HttpClientProvider};设置后共享客户端会在下次取用时自动重建。
     * 测试配置见 {@code com.shinoaki.wows.testkit.TestConfig}。
     */
    public static void updateProxySelector(ProxySelector selector) {
        HttpClientProvider.updateProxySelector(selector);
    }

    public static ProxySelector proxySelector() {
        return HttpClientProvider.proxySelector();
    }

    /**
     * 全局共享的 {@link HttpClient},库内统一使用本实例。
     * <p>
     * 实现已迁移到 {@link HttpClientProvider}（共享实例、超时/代理集中管理、变更自动重建），
     * 本方法保留为历史兼容入口，新旧代码取到的是同一个实例。
     * <p>
     * 该实例不跟随重定向、不挂 CookieHandler:重定向与会话 Cookie 由{@code HostPolicy}
     * 按主机自行处理,此处若自动跟随重定向会破坏 api.korabli.su 的 307 握手。
     *
     * @return 共享HttpClient
     */
    public static HttpClient client() {
        return HttpClientProvider.client();
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
        String json = ResponseCodec.decode(response);
        //字段漂移监视的采集挂钩:默认关闭,未开启时只有一次 ThreadLocal 读取
        if (SchemaRecorder.enabled()) {
            SchemaRecorder.record(response.uri(), json);
        }
        return json;
    }

    public static byte[] unGzip(byte[] bytes) throws IOException {
        return ResponseCodec.unGzip(bytes);
    }

    public static byte[] unDeflate(byte[] bytes) throws IOException {
        return ResponseCodec.unDeflate(bytes);
    }
}
