package com.shinoaki.wows.api.auth;

/**
 * 端点声明的认证槽位（3.0 重写）：种类 + 是否必填。
 * <p>
 * 必填槽位未命中 → 发送前抛 {@code MissingCredentialException}（fast-fail）；
 * 可选槽位未命中 → 匿名请求（vortex 公开数据、dev 不带 access_token 即此路径）。
 *
 * @author Xun
 */
public record AuthSlot(AuthKind kind, boolean required) {

    public static AuthSlot required(AuthKind kind) {
        return new AuthSlot(kind, true);
    }

    public static AuthSlot optional(AuthKind kind) {
        return new AuthSlot(kind, false);
    }
}
