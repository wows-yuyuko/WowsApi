package com.shinoaki.wows.api.auth;

import java.util.Map;

/**
 * 凭证（sealed）：新增认证种类 = +1 个 record，核心零改动。
 * kind 决定槽位匹配；scope 决定匹配方式；注入点 {@link #queryParams()} / {@link #headers()}；实现均掩码 toString。
 *
 * @author Xun
 */
public sealed interface Credential permits ApplicationId, AccessToken, ShareToken, CookieCredential {

    /**
     * 凭证种类。
     */
    AuthKind kind();

    /**
     * 凭证作用域。
     */
    CredentialScope scope();

    /**
     * 注入点 1：URL 查询参数。
     */
    default Map<String, String> queryParams() {
        return Map.of();
    }

    /**
     * 注入点 2：请求头（cookie 走这里）。
     */
    default Map<String, String> headers() {
        return Map.of();
    }
}
