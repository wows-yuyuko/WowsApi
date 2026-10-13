package com.shinoaki.wows.api.auth;

import java.util.Map;

/**
 * 凭证表（3.0 重写）：随 {@code WowsApi} 实例走，不可变。
 * <p>
 * 客户端的实例即服务器边界——凭证表互不可见，跨服串用结构上不可能。
 * 同 kind 可配多条账号级凭证，按 accountId 精确命中。
 *
 * @param applicationId 服务器级令牌（至多一条）
 * @param accessTokens  账号级 dev 用户态令牌（accountId -&gt; 凭证）
 * @param shareTokens   账号级 vortex 分享凭证（accountId -&gt; 凭证）
 * @param cookies       账号级 cookie 凭证（预留，accountId -&gt; 凭证）
 * @author Xun
 */
public record CredentialStore(
        ApplicationId applicationId,
        Map<Long, AccessToken> accessTokens,
        Map<Long, ShareToken> shareTokens,
        Map<Long, CookieCredential> cookies
) {

    public static final CredentialStore EMPTY = new CredentialStore(null, Map.of(), Map.of(), Map.of());

    public CredentialStore {
        accessTokens = Map.copyOf(accessTokens);
        shareTokens = Map.copyOf(shareTokens);
        cookies = Map.copyOf(cookies);
    }

    public static CredentialsBuilder builder() {
        return CredentialsBuilder.from(EMPTY);
    }
}
