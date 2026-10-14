package com.shinoaki.wows.api.developers.clan;

import com.shinoaki.wows.api.developers.warships.type.DevelopersShipBattleType;
import com.shinoaki.wows.api.error.BasicException;
import tools.jackson.databind.JsonNode;

/**
 * 军团战某赛季的数据块：赛季ID + 战斗统计（字段集与随机战块同构，复用 {@link DevelopersShipBattleType}）。
 *
 * @author Xun
 */
public record DevelopersClanSeasonBlock(long season_id, DevelopersShipBattleType stats) {

    public static DevelopersClanSeasonBlock parse(JsonNode node) throws BasicException {
        return new DevelopersClanSeasonBlock(node.path("season_id").asLong(0), DevelopersShipBattleType.parse(node));
    }
}
