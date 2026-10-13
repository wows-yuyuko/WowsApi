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
 * 端点定义（3.0 重写）：URL / 参数 / 认证槽位 / 兼容性 / 解析器 / 监控 key 的唯一来源。
 * <p>
 * 端点定义<b>不含主机与（dev 通道的）命名空间前缀</b>——由 {@code ServerProfile} 注入；
 * 每个端点一个稳定的 {@link #id()}，字段漂移监控以此为基线键（graphql 多 schema 天然区分）。
 *
 * @param id             端点 id，如 {@code vortex.account.info} / {@code dev.account.info}
 * @param channel        通道（决定主机与后续差异化策略）
 * @param path           路径模板；dev 通道为命名空间内路径（如 {@code /account/info/}），其余通道含完整前缀。
 *                       {@code {name}} 占位符以 URI 组件语义编码后替换，取值来自请求参数。
 * @param namespace      dev 通道的命名空间（其它通道为 null）
 * @param auth           认证槽位（顺序即注入顺序）
 * @param query          查询参数声明（顺序即渲染顺序）
 * @param servers        支持该端点的服务器集合；空集 = 不做端点级限制（仅受通道级限制，如 CN 无 dev API）
 * @param throttleEmpty  限流 TOLERANT 策略下的返回空值；null = 不提供（仍抛异常）
 * @param notFoundEmpty  非官方API服务器（无 dev API，如 CN）返回 404 时的返回空值（旧语义保留）；null = 404 一律抛异常
 * @param body           请求体渲染函数；null = GET，非 null = POST（Content-Type: application/json）
 * @param parser         响应解析器
 * @param <T>            解析结果类型
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
     * dev 通道端点：路径为命名空间内路径，前缀由 ServerProfile 按服务器注入。
     */
    public static <T> Endpoint<T> dev(String id, DevNamespace namespace, String path, List<AuthSlot> auth,
                                      List<QueryParam> query, Parser<T> parser) {
        return new Endpoint<>(id, Channel.DEV, path, namespace, auth, query, Set.of(), null, null, null, parser);
    }

    /**
     * 限定支持的服务器集合（不调用 = 全部支持，仅受通道级限制）。
     */
    public Endpoint<T> withServers(WowsServer... servers) {
        return new Endpoint<>(id, channel, path, namespace, auth, query, Set.of(servers), throttleEmpty, notFoundEmpty, body, parser);
    }

    /**
     * 设置限流 TOLERANT 策略下的返回空值（如搜索：限流返回空表）。
     */
    public Endpoint<T> withThrottleEmpty(Supplier<T> empty) {
        return new Endpoint<>(id, channel, path, namespace, auth, query, servers, empty, notFoundEmpty, body, parser);
    }

    /**
     * 设置非 API 服务器（无 dev API，如 CN）404 时的返回空值。
     */
    public Endpoint<T> withNotFoundEmpty(Supplier<T> empty) {
        return new Endpoint<>(id, channel, path, namespace, auth, query, servers, throttleEmpty, empty, body, parser);
    }

    /**
     * 设置为 POST 端点（请求体由参数渲染；Content-Type: application/json）。
     */
    public Endpoint<T> post(Function<Map<String, String>, String> body) {
        return new Endpoint<>(id, channel, path, namespace, auth, query, servers, throttleEmpty, notFoundEmpty, body, parser);
    }
}
