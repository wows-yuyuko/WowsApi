package com.shinoaki.wows.api.auth;

import java.util.Map;

/**
 * 官方开发者API 令牌（查询参数 {@code application_id}），服务器级作用域。
 * <p>
 * 实例即服务器：莱服实例必须配莱服自己签发的 key（WG key 无效，已验证）。
 *
 * @param value 令牌值（日志中掩码显示）
 * @author Xun
 */
public record ApplicationId(String value) implements Credential {

    public ApplicationId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("application_id 不能为空");
        }
    }

    public static ApplicationId of(String value) {
        return new ApplicationId(value);
    }

    @Override
    public AuthKind kind() {
        return AuthKind.APPLICATION_ID;
    }

    @Override
    public CredentialScope scope() {
        return CredentialScope.SERVER;
    }

    @Override
    public Map<String, String> queryParams() {
        return Map.of("application_id", value);
    }

    @Override
    public String toString() {
        return "ApplicationId[value=***]";
    }
}
