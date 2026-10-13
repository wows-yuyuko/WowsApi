package com.shinoaki.wows.testkit;

import java.util.HashMap;
import java.util.Map;

/**
 * 假上游单主机路由表：按路径 / 查询参数 / 请求体回放预置响应。
 * <p>
 * 匹配顺序：graphql（按请求体 schema + 语言）→ extra 路由 → id 路由（account_id / clan_id）→ 精确路径；
 * 全不命中 = 404（因此"打错主机/通道"会被测试暴露）。
 *
 * @author Xun
 */
public final class FakeRouteTable {

    private final Map<String, Canned> exact = new HashMap<>();
    private final Map<String, Canned> perId = new HashMap<>();
    private final Map<String, Canned> perExtra = new HashMap<>();
    private final Map<String, Canned> graphql = new HashMap<>();

    /**
     * 精确路径响应。
     */
    public void exact(String path, String body) {
        exact.put(path, new Canned(body, 200, false));
    }

    /**
     * 精确路径 + gzip 编码响应。
     */
    public void gzip(String path, String body) {
        exact.put(path, new Canned(body, 200, true));
    }

    /**
     * 精确路径 + 指定状态码（无响应体，如 429/503）。
     */
    public void status(String path, int status) {
        exact.put(path, new Canned(null, status, false));
    }

    /**
     * 按查询里的 {@code account_id} / {@code clan_id} 路由（同路径多账号）。
     */
    public void perId(String path, long id, String body) {
        perId.put(path + "#" + id, new Canned(body, 200, false));
    }

    /**
     * 按查询的 {@code extra} 路由（同路径不同 extra，如战舰统计 vs 全量战绩）。
     */
    public void perExtra(String path, String extra, String body) {
        perExtra.put(path + "#" + extra, new Canned(body, 200, false));
    }

    /**
     * graphql 路由：同 URL 多 schema，按请求体区分 schema（getGlossData / Lootbox / Items）+ 语言。
     */
    public void graphql(String schema, String language, String body) {
        graphql.put(schema + "|" + language, new Canned(body, 200, false));
    }

    Canned find(String rawPath, Map<String, String> query, String body) {
        if (rawPath.equals("/api/graphql/glossary/")) {
            String schema = body.contains("getGlossData") ? "getGlossData" : body.contains("Lootbox") ? "Lootbox" : "Items";
            String language = body.contains("\"zh-sg\"") ? "zh-sg" : "ru";
            return graphql.get(schema + "|" + language);
        }
        String extra = query.get("extra");
        if (extra != null) {
            Canned canned = perExtra.get(rawPath + "#" + extra);
            if (canned != null) {
                return canned;
            }
        }
        String id = query.get("account_id");
        if (id == null) {
            id = query.get("clan_id");
        }
        if (id != null) {
            Canned canned = perId.get(rawPath + "#" + id);
            if (canned != null) {
                return canned;
            }
        }
        return exact.get(rawPath);
    }

    /**
     * 预置响应（body 为 null 表示无响应体）。
     */
    public record Canned(String body, int status, boolean gzip) {
    }
}
