package com.shinoaki.wows.api.developers.clan;

import com.shinoaki.wows.api.error.BasicException;
import tools.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;

/**
 * 军团战赛季战绩里的单个玩家：各赛季数据块列表。
 *
 * @author Xun
 */
public record DevelopersClanSeasonPlayer(long account_id, List<DevelopersClanSeasonBlock> seasons) {

    public static DevelopersClanSeasonPlayer parse(JsonNode node) throws BasicException {
        List<DevelopersClanSeasonBlock> seasons = new ArrayList<>();
        for (JsonNode item : node.path("seasons")) {
            seasons.add(DevelopersClanSeasonBlock.parse(item));
        }
        return new DevelopersClanSeasonPlayer(node.path("account_id").asLong(0), seasons);
    }
}
