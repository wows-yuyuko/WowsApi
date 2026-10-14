package com.shinoaki.wows.api.auth;

/**
 * 端点认证槽位：种类 + 是否必填。必填未命中 → 发送前抛 {@code MissingCredentialException}；可选未命中 → 匿名。
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
