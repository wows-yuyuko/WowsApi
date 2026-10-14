package com.shinoaki.wows.api.developers.seasons;

import com.shinoaki.wows.api.error.BasicException;
import com.shinoaki.wows.api.utils.JsonUtils;
import tools.jackson.databind.JsonNode;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * /wows/seasons/info/ 排位赛季：赛季ID -&gt; 赛季信息（含各联赛与段位）。
 *
 * @param account_tier 参赛所需服役记录等级
 * @param start_at     开始时间戳
 * @param finish_at    结束时间戳
 * @param close_at     关闭时间戳
 * @param leagues      联赛ID -&gt; 联赛
 * @author Xun
 */
public record DevelopersRankedSeason(int season_id, String season_name, int account_tier, long start_at, long finish_at,
                                     long close_at, Map<Long, DevelopersRankedLeague> leagues) {

    public static Map<Long, DevelopersRankedSeason> parse(JsonNode node) throws BasicException {
        BasicException.status(node);
        Map<Long, DevelopersRankedSeason> map = new LinkedHashMap<>();
        for (var entry : node.path("data").properties()) {
            map.put(Long.parseLong(entry.getKey()), JsonUtils.json().parse(entry.getValue(), DevelopersRankedSeason.class));
        }
        return map;
    }
}
