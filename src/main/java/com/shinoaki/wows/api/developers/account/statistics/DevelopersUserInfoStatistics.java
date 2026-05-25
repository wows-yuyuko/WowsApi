package com.shinoaki.wows.api.developers.account.statistics;

import com.shinoaki.wows.api.developers.warships.type.DevelopersShipBattleType;
import com.shinoaki.wows.api.error.BasicException;
import com.shinoaki.wows.api.utils.WowsUtils;
import tools.jackson.databind.JsonNode;


public record DevelopersUserInfoStatistics(DevelopersShipBattleType clan, int distance, DevelopersShipBattleType pve_div3, DevelopersShipBattleType pve_div2,
                                           JsonNode oper_div, DevelopersShipBattleType pvp, JsonNode club, DevelopersShipBattleType pve_solo,
                                           DevelopersShipBattleType pvp_solo, JsonNode oper_div_hard, int battles, DevelopersShipBattleType pve,
                                           DevelopersShipBattleType pvp_div3, DevelopersShipBattleType pvp_div2, JsonNode oper_solo,
                                           DevelopersShipBattleType rank_div3, DevelopersShipBattleType rank_div2, DevelopersShipBattleType rank_solo) {
    public static DevelopersUserInfoStatistics parse(JsonNode node) throws BasicException {
        return new DevelopersUserInfoStatistics(
                checkShipInfo(node.path("clan")),
                WowsUtils.json(node.path("distance"), 0),
                checkShipInfo(node.path("pve_div3")),
                checkShipInfo(node.path("pve_div2")),
                checkInfo(node.path("oper_div")),
                checkShipInfo(node.path("pvp")),
                checkInfo(node.path("club")),
                checkShipInfo(node.path("pve_solo")),
                checkShipInfo(node.path("pvp_solo")),
                checkInfo(node.path("oper_div_hard")),
                WowsUtils.json(node.path("battles"), 0),
                checkShipInfo(node.path("pve")),
                checkShipInfo(node.path("pvp_div3")),
                checkShipInfo(node.path("pvp_div2")),
                checkInfo(node.path("oper_solo")),
                checkShipInfo(node.path("rank_div3")),
                checkShipInfo(node.path("rank_div2")),
                checkShipInfo(node.path("rank_solo")));

    }

    private static DevelopersShipBattleType checkShipInfo(JsonNode node) throws BasicException {
        if (node == null || node.isNull() || node.isEmpty()) {
            return DevelopersShipBattleType.empty();
        }
        return DevelopersShipBattleType.parse(node);
    }

    private static JsonNode checkInfo(JsonNode node) {
        if (node == null || node.isNull() || node.isEmpty()) {
            return null;
        }
        return node;
    }
}
