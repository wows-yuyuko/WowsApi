package com.shinoaki.wows.api.endpoint;

import com.shinoaki.wows.api.auth.AuthKind;
import com.shinoaki.wows.api.auth.Credential;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 凭证触发参数：本次请求解析出指定种类凭证时才输出（如 in_garage 跟随 access_token）。
 *
 * @param key   参数名
 * @param value 参数值（原样拼接）
 * @param kind  触发凭证的种类
 * @author Xun
 */
public record CredentialParam(String key, String value, AuthKind kind) implements QueryParam {

    public CredentialParam {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(value, "value");
        Objects.requireNonNull(kind, "kind");
    }

    @Override
    public String render(Map<String, String> params, List<Credential> credentials) {
        for (Credential credential : credentials) {
            if (credential.kind() == kind) {
                return value;
            }
        }
        return null;
    }
}
