package com.shinoaki.wows.api.auth;

import java.util.Map;

/**
 * 预留：cookie 类认证（请求头 {@code Cookie}），账号级作用域（与 access_token / share token 同族）。
 * <p>
 * 分层约定：WAF cookie（api.korabli.su 的 {@code bp_chl} 307 握手）属于传输层、由
 * {@code HostPolicy} 管理，与本凭证无关；同请求两者并存时由 HostPolicy 合并为一条 Cookie 头
 * （待首个用例接入时实现）。
 * <p>
 * 当前库内尚无端点声明 {@link AuthKind#COOKIE} 槽位，因此本凭证配置后暂不会注入任何请求；
 * 新增用例 = 端点上声明槽位 + 实现合并逻辑，核心解析器零改动。
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
