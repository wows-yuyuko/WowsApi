package com.shinoaki.wows.api.auth;

import com.shinoaki.wows.api.error.MissingCredentialException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 凭证解析（发送前）：按端点槽位解析本次请求实际注入的凭证。
 * 规则：调用级覆盖 &gt; 实例配置；账号级仅当请求账号一致才命中；必填未命中抛 {@link MissingCredentialException}，可选跳过。
 *
 * @author Xun
 */
public final class CredentialResolver {

    private CredentialResolver() {
    }

    /**
     * 按槽位顺序解析（可选槽位未命中则跳过）。
     */
    public static List<Credential> resolve(CredentialStore store, String endpointId, List<AuthSlot> slots,
                                           Long accountId, List<Credential> overrides) throws MissingCredentialException {
        List<Credential> resolved = new ArrayList<>(slots.size());
        for (AuthSlot slot : slots) {
            Credential credential = find(store, slot.kind(), accountId, overrides);
            if (credential == null) {
                if (slot.required()) {
                    throw new MissingCredentialException(endpointId, slot.kind());
                }
                continue;
            }
            resolved.add(credential);
        }
        return resolved;
    }

    private static Credential find(CredentialStore store, AuthKind kind, Long accountId, List<Credential> overrides) {
        for (Credential candidate : overrides) {
            if (candidate.kind() == kind && matches(candidate, accountId)) {
                return candidate;
            }
        }
        return switch (kind) {
            case APPLICATION_ID -> store.applicationId();
            case ACCESS_TOKEN -> lookup(store.accessTokens(), accountId);
            case SHARE_TOKEN -> lookup(store.shareTokens(), accountId);
            case COOKIE -> lookup(store.cookies(), accountId);
        };
    }

    /**
     * 账号级凭证仅当请求账号一致时可命中；请求不带账号一律不注入。
     */
    private static <T extends Credential> T lookup(Map<Long, T> credentials, Long accountId) {
        return accountId == null ? null : credentials.get(accountId);
    }

    /**
     * 服务器级覆盖任意请求可命中；账号级需账号一致。
     */
    private static boolean matches(Credential credential, Long accountId) {
        return switch (credential) {
            case ApplicationId ignored -> true;
            case AccessToken token -> accountId != null && accountId == token.accountId();
            case ShareToken token -> accountId != null && accountId == token.accountId();
            case CookieCredential cookie -> accountId != null && accountId == cookie.accountId();
        };
    }
}
