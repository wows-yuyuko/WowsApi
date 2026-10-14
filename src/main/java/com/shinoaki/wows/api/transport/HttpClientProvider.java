package com.shinoaki.wows.api.transport;

import java.net.ProxySelector;
import java.net.http.HttpClient;
import java.time.Duration;

/**
 * 共享 HttpClient 与传输级配置（超时 / 代理）的唯一持有者：全局单例（线程安全、自带连接池），
 * 超时或代理变更后下次取用时自动重建。不跟随重定向、不挂 CookieHandler（HostPolicy 按主机处理）。
 * 旧入口 {@code HttpCodec} 已委托到本类。
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
     * 更新请求超时（秒，须为正数）。影响后续请求，进行中的请求不受影响。
     */
    public static void updateTimeout(int timeoutSeconds) {
        if (timeoutSeconds <= 0) {
            throw new IllegalArgumentException("timeout 必须为正数: " + timeoutSeconds);
        }
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
