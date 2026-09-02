package com.shinoaki.wows.api.codec;

import com.shinoaki.wows.api.error.BasicException;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

/**
 * vortex请求通道(server.vortex()/server.clans() 域名)。
 * <p>
 * 与 {@link ApiHttp} 分离:vortex 接口使用独立的请求头/UA,后续 vortex 侧的改动
 * (请求头、代理、鉴权等)只修改本类,不影响官方API通道。
 * <p>
 * 发送请求经由共用的 {@link HttpTransport}。cookie管理+重定向跟随<b>按主机开启</b>,
 * 当前 vortex/clans 各主机(含 korabli)均未注册,直接走普通快速通道、零额外开销;
 * 未来若某 vortex 主机出现类似防护,调用 {@link HttpTransport#enableCookieRedirect(String)} 即可。
 *
 * @author Xun
 */
public final class VortexHttp {

    private VortexHttp() {
    }

    /**
     * 构造vortex请求,携带浏览器标识以兼容各站点的基本校验。
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
