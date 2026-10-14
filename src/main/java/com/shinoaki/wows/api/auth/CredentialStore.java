package com.shinoaki.wows.api.auth;

import java.util.Map;

/**
 * 凭证表（随 WowsApi 实例走，不可变）：applicationId 服务器级 + 三类账号级表（accountId 精确命中）。
 *
 * @param applicationId 服务器级令牌（至多一条）
 * @param accessTokens  账号级 dev 用户态令牌
 * @param shareTokens   账号级 vortex 分享凭证
 * @param cookies       账号级 cookie 凭证（预留）
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
