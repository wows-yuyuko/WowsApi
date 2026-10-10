package com.shinoaki.wows.api.vortex.account.statistics;

import tools.jackson.databind.JsonNode;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 评级战进度，对应 {@code /api/accounts/{id}/} 里 {@code statistics.rating_info.<赛季>}（莱服独有）。
 * <p>
 * 例：{@code {"1001": {"league_best": "platinum", "league_best_rating": 88, "rating": 2104}}}；
 * 没有打过评级时 {@code league_best} 为 {@code null}、两个数值为 0/缺省。
 *
 * @param rating             当前评分
 * @param league_best        历史最高联赛名（如 platinum/gold，没有时为 ""）
 * @param league_best_rating 历史最高联赛时的评分
 * @author Xun
 */
public record VortexRatingInfo(long rating, String league_best, long league_best_rating) {

    public static VortexRatingInfo parse(JsonNode node) {
        if (node == null || node.isEmpty() || node.isNull()) {
            return empty();
        }
        return new VortexRatingInfo(node.path("rating").asLong(0),
                node.path("league_best").asString(""),
                node.path("league_best_rating").asLong(0));
    }

    /**
     * 解析整个 {@code rating_info}：赛季ID -> 评级进度。
     */
    public static Map<String, VortexRatingInfo> parseAll(JsonNode node) {
        Map<String, VortexRatingInfo> map = new LinkedHashMap<>();
        if (node == null || node.isEmpty() || node.isNull()) {
            return map;
        }
        for (var entry : node.properties()) {
            map.put(entry.getKey(), parse(entry.getValue()));
        }
        return map;
    }

    public static VortexRatingInfo empty() {
        return new VortexRatingInfo(0, "", 0);
    }
}
