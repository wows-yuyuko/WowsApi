package com.shinoaki.wows.api.error;

import com.shinoaki.wows.api.auth.AuthKind;

/**
 * 必填凭证缺失：发送前 fast-fail（不产生网络请求）。可选槽位未命中不属于异常，走匿名请求。
 *
 * @author Xun
 */
public class MissingCredentialException extends BasicException {

    private final String endpointId;
    private final AuthKind kind;

    public MissingCredentialException(String endpointId, AuthKind kind) {
        super(HttpThrowableStatus.MISSING_CREDENTIAL,
                "端点 " + endpointId + " 缺少必填凭证: " + kind + "（可通过 WowsApi.credentials(...) 配置，或调用级覆盖）");
        this.endpointId = endpointId;
        this.kind = kind;
    }

    /**
     * 缺失凭证的端点 id，如 {@code dev.account.info}。
     */
    public String getEndpointId() {
        return endpointId;
    }

    /**
     * 缺失的凭证种类。
     */
    public AuthKind getKind() {
        return kind;
    }
}
