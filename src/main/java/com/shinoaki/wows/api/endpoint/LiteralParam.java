package com.shinoaki.wows.api.endpoint;

import java.util.Map;
import java.util.Objects;

/**
 * 字面量查询参数（3.0 重写）：值固定，如 {@code extra=clan}、{@code type=clans}。
 *
 * @param key   参数名
 * @param value 参数值（原样拼接，不编码）
 * @author Xun
 */
public record LiteralParam(String key, String value) implements QueryParam {

    public LiteralParam {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(value, "value");
    }

    @Override
    public String render(Map<String, String> params) {
        return value;
    }
}
