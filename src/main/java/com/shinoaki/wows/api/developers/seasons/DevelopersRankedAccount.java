package com.shinoaki.wows.api.developers.seasons;

import com.shinoaki.wows.api.developers.warships.DevelopersMeta;
import com.shinoaki.wows.api.error.BasicException;
import com.shinoaki.wows.api.error.HttpThrowableStatus;
import com.shinoaki.wows.api.utils.JsonUtils;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;

import java.util.Map;

/**
 * /wows/seasons/accountinfo/ 排位玩家战绩：段位进度（rank_info）+ 各赛季统计（seasons）。
 *
 * @param rank_info 赛季ID -&gt; 联赛 -&gt; 冲刺 -&gt; 段位进度
 * @param seasons   赛季ID -&gt; 分组键 -&gt; 战斗数据
 * @author Xun
 */
public record DevelopersRankedAccount(DevelopersMeta meta, long accountId,
                                      Map<Long, Map<Long, Map<Long, DevelopersRankedRankInfo>>> rank_info,
                                      Map<Long, Map<Long, DevelopersRankedShipSeasonBlock>> seasons) {

    public DevelopersRankedAccount {
        rank_info = rank_info == null ? Map.of() : rank_info;
        seasons = seasons == null ? Map.of() : seasons;
    }

    public static DevelopersRankedAccount parse(JsonNode node) throws BasicException {
        BasicException.status(node);
        DevelopersMeta meta = JsonUtils.json().parse(node.path("meta"), DevelopersMeta.class);
        for (var entry : node.path("data").properties()) {
            long accountId = Long.parseLong(entry.getKey());
            JsonNode value = entry.getValue();
            if (value == null || value.isNull()) {
                return new DevelopersRankedAccount(meta, accountId, Map.of(), Map.of());
            }
            Map<Long, Map<Long, Map<Long, DevelopersRankedRankInfo>>> rankInfo = JsonUtils.json().parse(
                    value.path("rank_info"), new TypeReference<Map<Long, Map<Long, Map<Long, DevelopersRankedRankInfo>>>>() {
                    });
            Map<Long, Map<Long, DevelopersRankedShipSeasonBlock>> seasons = JsonUtils.json().parse(
                    value.path("seasons"), new TypeReference<Map<Long, Map<Long, DevelopersRankedShipSeasonBlock>>>() {
                    });
            return new DevelopersRankedAccount(meta, accountId, rankInfo, seasons);
        }
        if (meta.hidden() != null && meta.hidden().length > 0) {
            return new DevelopersRankedAccount(meta, meta.hidden()[0], Map.of(), Map.of());
        }
        throw new BasicException(HttpThrowableStatus.DATA_STATUS, "排位玩家战绩响应为空");
    }
}
