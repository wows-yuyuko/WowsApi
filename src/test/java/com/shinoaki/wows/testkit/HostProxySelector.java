package com.shinoaki.wows.testkit;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.List;
import java.util.Map;

/**
 * 按 host 后缀路由的代理选择器：命中规则的域名走代理，其余直连。
 * <p>
 * 规则形如 {@code worldofwarships.asia -> 127.0.0.1:7890}，匹配该域名及其所有子域
 * （{@code api.}/{@code vortex.}/{@code clans.} 等）。
 *
 * @author Xun
 */
public final class HostProxySelector extends ProxySelector {

    /**
     * host 后缀 -> 代理地址
     */
    private final Map<String, InetSocketAddress> proxyByDomain;

    public HostProxySelector(Map<String, InetSocketAddress> proxyByDomain) {
        this.proxyByDomain = Map.copyOf(proxyByDomain);
    }

    public Map<String, InetSocketAddress> proxyByDomain() {
        return proxyByDomain;
    }

    @Override
    public List<Proxy> select(URI uri) {
        String host = uri == null ? null : uri.getHost();
        if (host != null) {
            for (Map.Entry<String, InetSocketAddress> entry : proxyByDomain.entrySet()) {
                if (host.equals(entry.getKey()) || host.endsWith("." + entry.getKey())) {
                    return List.of(new Proxy(Proxy.Type.HTTP, entry.getValue()));
                }
            }
        }
        return List.of(Proxy.NO_PROXY);
    }

    @Override
    public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
        //代理连接失败由 HttpClient 在请求上抛出，这里无需额外处理
    }

    @Override
    public String toString() {
        if (proxyByDomain.isEmpty()) {
            return "HostProxySelector(全部直连)";
        }
        return "HostProxySelector(" + proxyByDomain + ")";
    }
}
