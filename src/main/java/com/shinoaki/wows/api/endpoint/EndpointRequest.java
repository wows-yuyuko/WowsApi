package com.shinoaki.wows.api.endpoint;

import com.shinoaki.wows.api.auth.Credential;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 一次端点请求的参数载体（3.0 重写）。
 * <p>
 * {@link #accountId()} 有两个用途：路径/查询参数里的账号取值来源，
 * 以及<b>账号级凭证的匹配依据</b>（仅当与凭证绑定账号一致才注入）。
 * 非账号端点（搜索、军团详情等）为 null，账号级凭证一律不注入。
 *
 * @param accountId 本次请求的账号（可为 null）
 * @param params    路径占位符与查询参数取值（键名与端点声明一一对应）
 * @param overrides 调用级覆盖凭证（优先于实例配置）
 * @param policy    限流容错策略
 * @author Xun
 */
public record EndpointRequest(
        Long accountId,
        Map<String, String> params,
        List<Credential> overrides,
        RateLimitPolicy policy
) {

    public EndpointRequest {
        Objects.requireNonNull(params, "params");
        Objects.requireNonNull(overrides, "overrides");
        Objects.requireNonNull(policy, "policy");
        params = Map.copyOf(params);
        overrides = List.copyOf(overrides);
    }

    /**
     * 账号请求（自动带 {@code accountId} 参数）。
     */
    public static EndpointRequest account(long accountId) {
        return account(accountId, Map.of());
    }

    /**
     * 账号请求，附带额外参数（如 {@code extra}）。
     */
    public static EndpointRequest account(long accountId, Map<String, String> params) {
        Map<String, String> merged = new LinkedHashMap<>(params);
        merged.put("accountId", String.valueOf(accountId));
        return new EndpointRequest(accountId, merged, List.of(), RateLimitPolicy.STRICT);
    }

    /**
     * 非账号请求（搜索、军团详情等）。
     */
    public static EndpointRequest of(Map<String, String> params) {
        return new EndpointRequest(null, params, List.of(), RateLimitPolicy.STRICT);
    }

    /**
     * 调用级凭证覆盖（逃生门），优先于实例配置；账号级覆盖需账号一致才生效。
     */
    public EndpointRequest credentials(Credential... credentials) {
        return new EndpointRequest(accountId, params, List.of(credentials), policy);
    }

    /**
     * 限流容错策略（默认 STRICT）。
     */
    public EndpointRequest policy(RateLimitPolicy policy) {
        return new EndpointRequest(accountId, params, overrides, policy);
    }

    /**
     * 取参数值；缺失返回 null（可选参数）。
     */
    public String param(String name) {
        return params.get(name);
    }

    /**
     * 取数值型参数（如 {@code clanId}）；缺失或非数字属编程错误，直接抛异常。
     */
    public long longParam(String name) {
        String value = params.get(name);
        if (value == null) {
            throw new IllegalArgumentException("请求缺少参数: " + name);
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("请求参数不是数字: " + name + "=" + value, e);
        }
    }
}
