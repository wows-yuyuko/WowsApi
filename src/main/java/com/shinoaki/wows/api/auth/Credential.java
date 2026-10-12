package com.shinoaki.wows.api.auth;

import java.util.Map;

/**
 * 凭证（3.0 重写，sealed 家族）：新增认证种类 = 本包 +1 个 record，核心代码零改动。
 * <p>
 * 设计要点：
 * <ul>
 *     <li>{@link #kind()} 决定通道绑定与端点槽位匹配（见 {@link AuthKind}）；</li>
 *     <li>{@link #scope()} 决定匹配方式（服务器级 / 账号级，见 {@link CredentialScope}）；</li>
 *     <li>注入点两类：{@link #queryParams()}（URL 查询参数）与 {@link #headers()}（请求头，cookie 走这里）；</li>
 *     <li>所有实现 override {@code toString()} 掩码 token，防止日志泄漏。</li>
 * </ul>
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
     * 注入点 1：附加到 URL 查询参数的值（顺序跟随端点槽位声明顺序）。
     */
    default Map<String, String> queryParams() {
        return Map.of();
    }

    /**
     * 注入点 2：附加到请求头的值（cookie 走这里）。
     */
    default Map<String, String> headers() {
        return Map.of();
    }
}
