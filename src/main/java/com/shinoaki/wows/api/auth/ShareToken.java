package com.shinoaki.wows.api.auth;

import java.util.Map;

/**
 * vortex 分享凭证（查询参数 {@code ac}，账号级）：只对该账号有效，换账号必须换 token。
 *
 * @param accountId 凭证绑定的账号
 * @param value     凭证值（日志中掩码显示）
 * @author Xun
 */
public record ShareToken(long accountId, String value) implements Credential {

    public ShareToken {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("share token 不能为空");
        }
    }

    public static ShareToken of(long accountId, String value) {
        return new ShareToken(accountId, value);
    }

    @Override
    public AuthKind kind() {
        return AuthKind.SHARE_TOKEN;
    }

    @Override
    public CredentialScope scope() {
        return CredentialScope.ACCOUNT;
    }

    @Override
    public Map<String, String> queryParams() {
        return Map.of("ac", value);
    }

    @Override
    public String toString() {
        return "ShareToken[accountId=" + accountId + ", value=***]";
    }
}
