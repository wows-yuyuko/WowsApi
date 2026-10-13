package com.shinoaki.wows.api.transport;

import java.net.ProxySelector;
import java.net.http.HttpClient;
import java.time.Duration;

/**
 * 共享 {@link HttpClient} 与传输级配置（超时 / 代理）的唯一持有者（3.0 重写底座）。
 * <p>
 * 本类合并自原 {@code HttpCodec} 的客户端管理逻辑，行为与原实现一致：
 * <ul>
 *     <li>全局共享同一个 client（JDK HttpClient 线程安全且自带连接池，避免重建 selector/executor 与连接）；</li>
 *     <li>连接超时取自{@link #requestTimeout()}，{@link #updateTimeout(int)} 后下次取用时自动重建；</li>
 *     <li>代理选择器 {@link #updateProxySelector(ProxySelector)} 变更后同样在下次取用时重建；</li>
 *     <li>该实例不跟随重定向、不挂 CookieHandler——重定向与会话 Cookie 由 {@link HostPolicy} 按主机处理。</li>
 * </ul>
 * 迁移期旧入口 {@code HttpCodec.client()/updateTimeout/updateProxySelector} 已委托到本类，
 * 新旧代码共用同一实例与同一份配置。
 *
 * @author Xun
 */
public final class HttpClientProvider {

    private HttpClientProvider() {
    }

    /**
     * 请求超时（秒），缺省 3；请求级与连接级超时都以此为准。
     */
    private static volatile int requestTimeout = 3;

    /**
     * 共享客户端当前绑定的超时(秒),与{@link #requestTimeout}不一致时触发重建。
     */
    private static volatile int clientTimeout = Integer.MIN_VALUE;

    private static volatile HttpClient client;

    /**
     * 全局代理选择器（null = 全部直连）。
     */
    private static volatile ProxySelector proxySelector;

    /**
     * 共享客户端当前绑定的代理选择器,与{@link #proxySelector}不一致时触发重建。
     */
    private static volatile ProxySelector clientProxy;

    /**
     * 更新请求超时（秒）。影响后续请求（请求级 timeout 与 client 连接超时），进行中的请求不受影响。
     */
    public static void updateTimeout(int timeoutSeconds) {
        requestTimeout = timeoutSeconds;
    }

    public static int requestTimeout() {
        return requestTimeout;
    }

    /**
     * 更新全局代理选择器（null = 全部直连）。
     * <p>
     * 只影响本方法之后从{@link #client()}取用的实例;进行中的请求仍持有旧实例。
     */
    public static void updateProxySelector(ProxySelector selector) {
        proxySelector = selector;
    }

    public static ProxySelector proxySelector() {
        return proxySelector;
    }

    /**
     * 全局共享的 {@link HttpClient},库内统一使用本实例。
     *
     * @return 共享HttpClient
     */
    public static HttpClient client() {
        HttpClient shared = client;
        int timeout = requestTimeout;
        ProxySelector proxy = proxySelector;
        if (shared == null || clientTimeout != timeout || clientProxy != proxy) {
            synchronized (HttpClientProvider.class) {
                if (client == null || clientTimeout != timeout || clientProxy != proxy) {
                    var builder = HttpClient.newBuilder()
                            .connectTimeout(Duration.ofSeconds(timeout));
                    if (proxy != null) {
                        builder.proxy(proxy);
                    }
                    client = builder.build();
                    clientTimeout = timeout;
                    clientProxy = proxy;
                }
                shared = client;
            }
        }
        return shared;
    }
}
