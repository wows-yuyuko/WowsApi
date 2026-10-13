package com.shinoaki.wows.api.codec;

import com.shinoaki.wows.api.error.BasicException;
import com.shinoaki.wows.api.transport.HostPolicy;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

/**
 * 低层HTTP传输的<b>历史兼容入口</b>——实现已迁移到 3.0 重写底座的 {@link HostPolicy}。
 * <p>
 * 原有的「cookie 管理 + 重定向跟随 + 429 限流退避，按主机开关（默认仅 api.korabli.su）」逻辑
 * 逐字保留在 {@link HostPolicy} 中，本类所有方法仅做委托，新旧代码行为一致。
 * <p>
 * 新代码请直接使用 {@link HostPolicy}；本类将在 3.0 断代清理时删除。
 *
 * @author Xun
 */
public final class HttpTransport {

    private HttpTransport() {
    }

    /**
     * 为指定主机启用cookie管理+重定向跟随,详见{@link HostPolicy#enableCookieRedirect(String)}。
     */
    public static void enableCookieRedirect(String host) {
        HostPolicy.enableCookieRedirect(host);
    }

    /**
     * 取消指定主机的cookie管理+重定向跟随,详见{@link HostPolicy#disableCookieRedirect(String)}。
     */
    public static void disableCookieRedirect(String host) {
        HostPolicy.disableCookieRedirect(host);
    }

    /**
     * 同步发送请求并返回最终响应,详见{@link HostPolicy#send(HttpClient, HttpRequest)}。
     */
    public static HttpResponse<byte[]> send(HttpClient client, HttpRequest request) throws BasicException {
        return HostPolicy.send(client, request);
    }

    /**
     * 异步发送请求,详见{@link HostPolicy#sendAsync(HttpClient, HttpRequest)}。
     */
    public static CompletableFuture<HttpResponse<byte[]>> sendAsync(HttpClient client, HttpRequest request) {
        return HostPolicy.sendAsync(client, request);
    }
}
