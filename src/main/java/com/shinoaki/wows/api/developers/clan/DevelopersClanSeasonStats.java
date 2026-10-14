package com.shinoaki.wows.api.developers.clan;

import com.shinoaki.wows.api.developers.warships.DevelopersMeta;
import com.shinoaki.wows.api.error.BasicException;
import com.shinoaki.wows.api.utils.JsonUtils;
import tools.jackson.databind.JsonNode;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * /wows/clans/seasonstats/ 军团战赛季战绩：账号ID -&gt; 玩家数据。
 * <p>
 * 莱服无该接口（返回 404，端点已声明不支持）。
 *
 * @author Xun
 */
public record DevelopersClanSeasonStats(DevelopersMeta meta, Map<Long, DevelopersClanSeasonPlayer> players) {

    public static DevelopersClanSeasonStats parse(JsonNode node) throws BasicException {
        BasicException.status(node);
        DevelopersMeta meta = JsonUtils.json().parse(node.path("meta"), DevelopersMeta.class);
        Map<Long, DevelopersClanSeasonPlayer> players = new LinkedHashMap<>();
        for (var entry : node.path("data").properties()) {
            if (entry.getValue() == null || entry.getValue().isNull()) {
                continue;
            }
            players.put(Long.parseLong(entry.getKey()), DevelopersClanSeasonPlayer.parse(entry.getValue()));
        }
        return new DevelopersClanSeasonStats(meta, players);
    }
}
