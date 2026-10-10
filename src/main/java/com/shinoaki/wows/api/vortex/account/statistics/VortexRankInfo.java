package com.shinoaki.wows.api.vortex.account.statistics;

import tools.jackson.databind.JsonNode;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 排位赛进度，对应 {@code /api/accounts/{id}/} 中 {@code statistics.rank_info} 的最内层节点。
 * <p>
 * 原始结构为三层动态 key：{@code rank_info[赛季][分组][段位]}，
 * 例：{@code {"1024": {"0": {"3": {"rank": 10, "best_rank_in_sprint": 10, "victories": 0,
 * "max_stars_total": 0, "battles": 0, "stars": 0, "qualification_status": 0, "rank_best": 10, "stage": 1}}}}}
 *
 * @param rank                 当前排名
 * @param best_rank_in_sprint  本次冲刺最好排名
 * @param victories            胜利场次
 * @param max_stars_total      累计星数上限
 * @param battles              场次
 * @param stars                当前星数
 * @param qualification_status 资格状态
 * @param rank_best            最好排名
 * @param stage                阶段
 * @author Xun
 */
public record VortexRankInfo(int rank, int best_rank_in_sprint, int victories, int max_stars_total, int battles,
                             int stars, int qualification_status, int rank_best, int stage) {

    public static VortexRankInfo parse(JsonNode node) {
        return new VortexRankInfo(node.path("rank").asInt(0),
                node.path("best_rank_in_sprint").asInt(0),
                node.path("victories").asInt(0),
                node.path("max_stars_total").asInt(0),
                node.path("battles").asInt(0),
                node.path("stars").asInt(0),
                node.path("qualification_status").asInt(0),
                node.path("rank_best").asInt(0),
                node.path("stage").asInt(0));
    }

    public static VortexRankInfo empty() {
        return new VortexRankInfo(0, 0, 0, 0, 0, 0, 0, 0, 0);
    }

    /**
     * 解析整个 rank_info 节点。
     *
     * @return 赛季 -> 分组 -> 段位 -> 进度
     */
    public static Map<String, Map<String, Map<String, VortexRankInfo>>> parseAll(JsonNode node) {
        Map<String, Map<String, Map<String, VortexRankInfo>>> result = new LinkedHashMap<>();
        if (node == null || node.isMissingNode() || node.isNull() || !node.isObject()) {
            return result;
        }
        for (Map.Entry<String, JsonNode> season : node.properties()) {
            Map<String, Map<String, VortexRankInfo>> groups = new LinkedHashMap<>();
            for (Map.Entry<String, JsonNode> group : season.getValue().properties()) {
                Map<String, VortexRankInfo> divisions = new LinkedHashMap<>();
                for (Map.Entry<String, JsonNode> division : group.getValue().properties()) {
                    divisions.put(division.getKey(), parse(division.getValue()));
                }
                groups.put(group.getKey(), divisions);
            }
            result.put(season.getKey(), groups);
        }
        return result;
    }
}
