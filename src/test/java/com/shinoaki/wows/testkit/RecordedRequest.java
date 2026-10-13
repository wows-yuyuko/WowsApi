package com.shinoaki.wows.testkit;

import com.sun.net.httpserver.Headers;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 假上游记录到的一次请求（{@link FakeUpstream}）。
 *
 * @param method  请求方法
 * @param rawPath 原始路径（未解码，保留 %XX 形态）
 * @param query   查询参数（原样、不解码）
 * @param headers 请求头
 * @param body    请求体（UTF-8）
 * @author Xun
 */
public record RecordedRequest(String method, String rawPath, Map<String, String> query, Headers headers, String body) {

    /**
     * 原始查询串（按请求顺序重建；与真实发送的查询串等值，编码值保持原样）。
     */
    public String rawQuery() {
        if (query.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder("?");
        boolean first = true;
        for (var entry : query.entrySet()) {
            if (!first) {
                sb.append('&');
            }
            first = false;
            sb.append(entry.getKey()).append('=').append(entry.getValue());
        }
        return sb.toString();
    }

    /**
     * 查询参数副本（便于断言，Map.equals 与顺序无关）。
     */
    public Map<String, String> queryMap() {
        return new LinkedHashMap<>(query);
    }
}
