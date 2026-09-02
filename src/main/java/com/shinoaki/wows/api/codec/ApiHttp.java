package com.shinoaki.wows.api.codec;

import com.shinoaki.wows.api.error.BasicException;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

/**
 * 官方开发者API请求通道(server.api() 域名,即 /wows/** 与莱服 /mk/**)。
 * <p>
 * 与 {@link VortexHttp} 分离:开发者API的服务器防护策略与 vortex 不同,请求头/UA与后续
 * 针对官方API的改动( 处理、鉴权等)只需修改本类,不影响 vortex 通道。
 * <p>
 * 发送请求经由共用的 {@link HttpTransport}。cookie管理+重定向跟随<b>按主机开启</b>,
 * 目前仅 api.korabli.su(莱服官方API,存在 307 + bp_chl  握手)自动启用,其它主机的请求
 * 走普通快速通道、零额外开销;未来若其它服务器需要同样处理,调用
 * {@link HttpTransport#enableCookieRedirect(String)} 即可。调用方即使使用默认的
 * {@code HttpClient.newBuilder().build()}(不跟随重定向、无 cookie handler)也能正常工作。
 *
 * @author Xun
 */
public final class ApiHttp {

    private ApiHttp() {
    }

    /**
     * 构造官方API请求。
     * <p>
     * 携带稳定的浏览器标识:一方面兼容历史行为;另一方面 korabli  重放要求前后请求指纹一致,
     * 使用固定UA与请求头可保证握手稳定通过。
     */
    public static HttpRequest request(URI uri) {
        //注意：只声明 gzip/deflate，不声明 br（brotli），因为本库未实现br解压；若声明br服务端可能返回br导致解析失败
        return HttpRequest.newBuilder().uri(uri)
                .setHeader("Accept-Encoding", "gzip, deflate")
                .setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.0.0 Safari/537.36")
                .setHeader("Sec-Ch-Ua", "\"Google Chrome\";v=\"125\", \"Chromium\";v=\"125\", \"Not.A/Brand\";v=\"24\"")
                .setHeader("Sec-Ch-Ua-Mobile", "?0")
                .setHeader("Sec-Ch-Ua-Platform", "\"Windows\"")
                .setHeader("Sec-Fetch-Mode", "navigate")
                .setHeader("Sec-Fetch-Dest", "document")
                .setHeader("Sec-Fetch-User", "?1")
                .setHeader("Sec-Fetch-Site", "none")
                .GET()
                .timeout(Duration.ofSeconds(HttpCodec.requestTimeout()))
                .build();
    }

    /**
     * 同步发送请求,详见 {@link HttpTransport#send(HttpClient, HttpRequest)}。
     */
    public static HttpResponse<byte[]> send(HttpClient client, HttpRequest request) throws BasicException {
        return HttpTransport.send(client, request);
    }

    /**
     * 异步发送请求,详见 {@link HttpTransport#sendAsync(HttpClient, HttpRequest)}。
     */
    public static CompletableFuture<HttpResponse<byte[]>> sendAsync(HttpClient client, HttpRequest request) {
        return HttpTransport.sendAsync(client, request);
    }

    /**
     * 校验状态码并解码响应体(与{@link HttpCodec#response(HttpResponse)}一致)。
     */
    public static String response(HttpResponse<byte[]> response) throws BasicException {
        return HttpCodec.response(response);
    }
}
