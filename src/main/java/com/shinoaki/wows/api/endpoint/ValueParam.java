package com.shinoaki.wows.api.endpoint;

import com.shinoaki.wows.api.auth.Credential;
import com.shinoaki.wows.api.util.UrlCodec;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 值查询参数：值来自请求参数，encoded = 是否按 encodeURIComponent 编码（自由文本 true；数字 ID / extra false）。
 *
 * @param key     参数名
 * @param param   取值参数名
 * @param encoded 是否编码
 * @author Xun
 */
public record ValueParam(String key, String param, boolean encoded) implements QueryParam {

    public ValueParam {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(param, "param");
    }

    @Override
    public String render(Map<String, String> params, List<Credential> credentials) {
        String value = params.get(param);
        if (value == null) {
            return null;
        }
        return encoded ? UrlCodec.encodeComponent(value) : value;
    }
}
