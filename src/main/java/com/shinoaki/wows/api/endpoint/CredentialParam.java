package com.shinoaki.wows.api.endpoint;

import com.shinoaki.wows.api.auth.AuthKind;
import com.shinoaki.wows.api.auth.Credential;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 凭证触发的字面量查询参数（3.0 重写）：仅当本次请求解析出指定种类的凭证时才输出该参数。
 * <p>
 * 用于表达旧实现里"参数与凭证绑定出现"的语义——例如官方API 战舰统计的 {@code in_garage=1}
 * 只在带 {@code access_token} 的请求上出现（旧实现把两者绑在一起，这里显式建模）。
 *
 * @param key   参数名
 * @param value 参数值（原样拼接）
 * @param kind  触发凭证的种类（本次请求解析出该种类凭证时输出）
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
