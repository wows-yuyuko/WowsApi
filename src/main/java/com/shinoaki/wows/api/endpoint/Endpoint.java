package com.shinoaki.wows.api.endpoint;

import com.shinoaki.wows.api.auth.AuthSlot;
import com.shinoaki.wows.api.server.DevNamespace;
import com.shinoaki.wows.api.transport.Channel;
import com.shinoaki.wows.api.type.WowsServer;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 端点定义：URL / 参数 / 认证槽位 / 兼容性 / 解析器 / 监控键的唯一来源。
 * 不含主机与 dev 前缀（由 ServerProfile 注入）；id 稳定，作字段漂移监控的基线键。
 *
 * @param id            端点 id（如 {@code vortex.account.info}），监控基线键
 * @param channel       通道（决定主机与差异化策略）
 * @param path          路径模板；{@code {name}} 占位符按 URI 组件编码替换；dev 通道为命名空间内路径
 * @param namespace     dev 通道命名空间（其它通道为 null）
 * @param auth          认证槽位（顺序即注入顺序）
 * @param query         查询参数（顺序即渲染顺序）
 * @param servers       支持的服务器集合；空集 = 不限制（仅受通道级限制）
 * @param throttleEmpty 限流 TOLERANT 下的空值；null = 抛异常
 * @param notFoundEmpty 非 dev 服务器 404 时的空值；null = 抛异常
 * @param body          请求体渲染；null = GET，非 null = POST（json）
 * @param parser        响应解析器
 * @param <T>           解析结果类型
 * @author Xun
 */
public record Endpoint<T>(
        String id,
        Channel channel,
        String path,
        DevNamespace namespace,
        List<AuthSlot> auth,
        List<QueryParam> query,
        Set<WowsServer> servers,
        Supplier<T> throttleEmpty,
        Supplier<T> notFoundEmpty,
        Function<Map<String, String>, String> body,
        Parser<T> parser
) {

    public Endpoint {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(channel, "channel");
        Objects.requireNonNull(path, "path");
        Objects.requireNonNull(auth, "auth");
        Objects.requireNonNull(query, "query");
        Objects.requireNonNull(servers, "servers");
        Objects.requireNonNull(parser, "parser");
        if (channel == Channel.DEV && namespace == null) {
            throw new IllegalArgumentException("dev 通道端点必须声明命名空间: " + id);
        }
        if (channel != Channel.DEV && namespace != null) {
            throw new IllegalArgumentException("非 dev 通道端点不应声明命名空间: " + id);
        }
        if (!path.startsWith("/")) {
            throw new IllegalArgumentException("端点路径必须以 / 开头: " + id + " -> " + path);
        }
        auth = List.copyOf(auth);
        query = List.copyOf(query);
        servers = servers.isEmpty() ? Set.of() : Collections.unmodifiableSet(EnumSet.copyOf(servers));
    }

    /**
     * 无查询参数的端点（vortex / clans 通道）。
     */
    public static <T> Endpoint<T> of(String id, Channel channel, String path, List<AuthSlot> auth, Parser<T> parser) {
        return new Endpoint<>(id, channel, path, null, auth, List.of(), Set.of(), null, null, null, parser);
    }

    /**
     * 带查询参数的端点（vortex / clans 通道）。
     */
    public static <T> Endpoint<T> of(String id, Channel channel, String path, List<AuthSlot> auth,
                                     List<QueryParam> query, Parser<T> parser) {
        return new Endpoint<>(id, channel, path, null, auth, query, Set.of(), null, null, null, parser);
    }

    /**
     * dev 通道端点（路径为命名空间内路径，前缀由 ServerProfile 注入）。
     */
    public static <T> Endpoint<T> dev(String id, DevNamespace namespace, String path, List<AuthSlot> auth,
                                      List<QueryParam> query, Parser<T> parser) {
        return new Endpoint<>(id, Channel.DEV, path, namespace, auth, query, Set.of(), null, null, null, parser);
    }

    /**
     * 限定支持的服务器（不调用 = 全部支持，仅受通道级限制）。
     */
    public Endpoint<T> withServers(WowsServer... servers) {
        return new Endpoint<>(id, channel, path, namespace, auth, query, Set.of(servers), throttleEmpty, notFoundEmpty, body, parser);
    }

    /**
     * 设置限流 TOLERANT 下的空值（如搜索返回空表）。
     */
    public Endpoint<T> withThrottleEmpty(Supplier<T> empty) {
        return new Endpoint<>(id, channel, path, namespace, auth, query, servers, empty, notFoundEmpty, body, parser);
    }

    /**
     * 设置非 dev 服务器 404 时的空值。
     */
    public Endpoint<T> withNotFoundEmpty(Supplier<T> empty) {
        return new Endpoint<>(id, channel, path, namespace, auth, query, servers, throttleEmpty, empty, body, parser);
    }

    /**
     * 声明为 POST 端点（请求体由参数渲染，Content-Type: application/json）。
     */
    public Endpoint<T> post(Function<Map<String, String>, String> body) {
        return new Endpoint<>(id, channel, path, namespace, auth, query, servers, throttleEmpty, notFoundEmpty, body, parser);
    }
}
