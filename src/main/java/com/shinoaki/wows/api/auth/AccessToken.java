package com.shinoaki.wows.api.auth;

import java.util.Map;

/**
 * 官方开发者API 用户态令牌（查询参数 {@code access_token}），账号级作用域；
 * 配置后 {@code private.*} 私有数据才会返回。
 *
 * @param accountId 凭证绑定的账号（仅对该账号的请求注入）
 * @param value     令牌值（日志中掩码显示）
 * @author Xun
 */
public record AccessToken(long accountId, String value) implements Credential {

    public AccessToken {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("access_token 不能为空");
        }
    }

    public static AccessToken of(long accountId, String value) {
        return new AccessToken(accountId, value);
    }

    @Override
    public AuthKind kind() {
        return AuthKind.ACCESS_TOKEN;
    }

    @Override
    public CredentialScope scope() {
        return CredentialScope.ACCOUNT;
    }

    @Override
    public Map<String, String> queryParams() {
        return Map.of("access_token", value);
    }

    @Override
    public String toString() {
        return "AccessToken[accountId=" + accountId + ", value=***]";
    }
}
