package com.shinoaki.wows.api.auth;

import com.shinoaki.wows.api.error.MissingCredentialException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 凭证解析（3.0 重写，发送前执行）：把端点声明的认证槽位解析为本次请求实际注入的凭证列表。
 * <p>
 * 规则（与方案 §4.2 一致）：
 * <ul>
 *     <li><b>覆盖顺序</b>：调用级覆盖 &gt; 实例配置（{@link CredentialStore}）；</li>
 *     <li><b>通道过滤</b>：kind 固定归属通道——端点只声明自己通道的槽位，vortex 凭证永远不会被注入 dev 端点；</li>
 *     <li><b>账号级匹配</b>：仅当本次请求账号与凭证绑定账号一致才注入，不一致视同未配置；
 *     请求不带账号（搜索、军团列表等）时账号级凭证一律不注入；</li>
 *     <li><b>缺失语义</b>：必填未命中 → {@link MissingCredentialException}；可选未命中 → 跳过（匿名）。</li>
 * </ul>
 *
 * @author Xun
 */
public final class CredentialResolver {

    private CredentialResolver() {
    }

    /**
     * 按端点槽位解析凭证。
     *
     * @param store     实例凭证表
     * @param endpointId 端点 id（仅用于错误消息）
     * @param slots     端点声明的认证槽位（顺序即注入顺序）
     * @param accountId 本次请求的账号（可为 null = 非账号请求）
     * @param overrides 调用级覆盖凭证（优先于实例配置）
     * @return 按槽位顺序解析出的凭证列表（可选槽位未命中则跳过）
     * @throws MissingCredentialException 必填槽位未命中
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
     * 覆盖凭证的作用域匹配：服务器级任意请求可覆盖；账号级需账号一致。
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
