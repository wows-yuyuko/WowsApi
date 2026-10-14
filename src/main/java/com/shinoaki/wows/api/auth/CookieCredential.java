package com.shinoaki.wows.api.auth;

import java.util.Map;

/**
 * 预留：cookie 类认证（请求头 {@code Cookie}，账号级）。与 WAF cookie（HostPolicy 的 bp_chl 307 握手）分层，
 * 合并发送逻辑待首个用例接入；当前无端点声明 COOKIE 槽位，配置后暂不会注入。
 *
 * @param accountId 凭证绑定的账号
 * @param value     cookie 值（日志中掩码显示）
 * @author Xun
 */
public record CookieCredential(long accountId, String value) implements Credential {

    public CookieCredential {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("cookie 不能为空");
        }
    }

    public static CookieCredential of(long accountId, String value) {
        return new CookieCredential(accountId, value);
    }

    @Override
    public AuthKind kind() {
        return AuthKind.COOKIE;
    }

    @Override
    public CredentialScope scope() {
        return CredentialScope.ACCOUNT;
    }

    @Override
    public Map<String, String> headers() {
        return Map.of("Cookie", value);
    }

    @Override
    public String toString() {
        return "CookieCredential[accountId=" + accountId + ", value=***]";
    }
}
