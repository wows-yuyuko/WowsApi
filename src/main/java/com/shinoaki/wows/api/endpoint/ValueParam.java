package com.shinoaki.wows.api.endpoint;

import com.shinoaki.wows.api.util.UrlCodec;

import java.util.Map;
import java.util.Objects;

/**
 * 值查询参数（3.0 重写）：值来自 {@link EndpointRequest#params()}，如
 * {@code account_id={accountId}}、{@code search={search}}。
 *
 * @param key     参数名
 * @param param   取值参数名
 * @param encoded 是否按 encodeURIComponent 语义编码（自由文本如搜索词 = true；数字 ID、extra 串 = false）
 * @author Xun
 */
public record ValueParam(String key, String param, boolean encoded) implements QueryParam {

    public ValueParam {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(param, "param");
    }

    @Override
    public String render(Map<String, String> params) {
        String value = params.get(param);
        if (value == null) {
            return null;
        }
        return encoded ? UrlCodec.encodeComponent(value) : value;
    }
}
