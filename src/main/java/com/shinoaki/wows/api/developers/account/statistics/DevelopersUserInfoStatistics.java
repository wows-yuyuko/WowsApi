package com.shinoaki.wows.api.developers.account.statistics;

import com.fasterxml.jackson.databind.JsonNode;
import com.shinoaki.wows.api.developers.warships.type.DevelopersShipBattleType;
import com.shinoaki.wows.api.error.BasicException;
import com.shinoaki.wows.api.utils.WowsUtils;

public record DevelopersUserInfoStatistics(DevelopersShipBattleType clan, int distance, DevelopersShipBattleType pve_div3, DevelopersShipBattleType pve_div2,
                                           JsonNode oper_div, DevelopersShipBattleType pvp, JsonNode club, DevelopersShipBattleType pve_solo,
                                           DevelopersShipBattleType pvp_solo, JsonNode oper_div_hard, int battles, DevelopersShipBattleType pve,
                                           DevelopersShipBattleType pvp_div3, DevelopersShipBattleType pvp_div2, JsonNode oper_solo,
                                           DevelopersShipBattleType rank_div3, DevelopersShipBattleType rank_div2, DevelopersShipBattleType rank_solo) {
    public static DevelopersUserInfoStatistics parse(JsonNode node) throws BasicException {
        return new DevelopersUserInfoStatistics(
                checkShipInfo(node.get("clan")),
                WowsUtils.json(node.get("distance"), 0),
                checkShipInfo(node.get("pve_div3")),
                checkShipInfo(node.get("pve_div2")),
                checkInfo(node.get("oper_div")),
                checkShipInfo(node.get("pvp")),
                checkInfo(node.get("club")),
                checkShipInfo(node.get("pve_solo")),
                checkShipInfo(node.get("pvp_solo")),
                checkInfo(node.get("oper_div_hard")),
                WowsUtils.json(node.get("battles"), 0),
                checkShipInfo(node.get("pve")),
                checkShipInfo(node.get("pvp_div3")),
                checkShipInfo(node.get("pvp_div2")),
                checkInfo(node.get("oper_solo")),
                checkShipInfo(node.get("rank_div3")),
                checkShipInfo(node.get("rank_div2")),
                checkShipInfo(node.get("rank_solo")));

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
