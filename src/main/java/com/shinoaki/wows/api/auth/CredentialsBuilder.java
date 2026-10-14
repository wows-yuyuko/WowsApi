package com.shinoaki.wows.api.auth;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 凭证配置构建器：{@code WowsApi.credentials(c -> c.applicationId(...).shareToken(...))} 的载体。
 * 从现有凭证表播种；重复配置同 kind + 同账号 = 覆盖。
 *
 * @author Xun
 */
public final class CredentialsBuilder {

    private ApplicationId applicationId;
    private final Map<Long, AccessToken> accessTokens = new LinkedHashMap<>();
    private final Map<Long, ShareToken> shareTokens = new LinkedHashMap<>();
    private final Map<Long, CookieCredential> cookies = new LinkedHashMap<>();

    private CredentialsBuilder() {
    }

    public static CredentialsBuilder from(CredentialStore store) {
        CredentialsBuilder builder = new CredentialsBuilder();
        builder.applicationId = store.applicationId();
        builder.accessTokens.putAll(store.accessTokens());
        builder.shareTokens.putAll(store.shareTokens());
        builder.cookies.putAll(store.cookies());
        return builder;
    }

    /**
     * 官方开发者API 令牌（服务器级；莱服必须用莱服自己的 key）。
     */
    public CredentialsBuilder applicationId(String value) {
        this.applicationId = new ApplicationId(value);
        return this;
    }

    /**
     * 官方开发者API 用户态令牌（账号级，解锁 private.*）。
     */
    public CredentialsBuilder accessToken(long accountId, String value) {
        accessTokens.put(accountId, new AccessToken(accountId, value));
        return this;
    }

    /**
     * vortex 分享凭证（账号级，只对该账号有效）。
     */
    public CredentialsBuilder shareToken(long accountId, String value) {
        shareTokens.put(accountId, new ShareToken(accountId, value));
        return this;
    }

    /**
     * cookie 类凭证（账号级，预留——当前无端点声明 COOKIE 槽位）。
     */
    public CredentialsBuilder cookie(long accountId, String value) {
        cookies.put(accountId, new CookieCredential(accountId, value));
        return this;
    }

    public CredentialStore build() {
        return new CredentialStore(applicationId, accessTokens, shareTokens, cookies);
    }
}
