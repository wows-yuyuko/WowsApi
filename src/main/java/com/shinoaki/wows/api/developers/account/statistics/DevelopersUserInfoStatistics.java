package com.shinoaki.wows.api.developers.account.statistics;

import com.fasterxml.jackson.databind.JsonNode;
import com.shinoaki.wows.api.developers.warships.type.DevelopersShipBattleType;
import com.shinoaki.wows.api.error.BasicException;
import com.shinoaki.wows.api.utils.WowsUtils;
import lombok.Data;

@Data
public class DevelopersUserInfoStatistics {
    private DevelopersShipBattleType clan;
    private int distance;
    private DevelopersShipBattleType pve_div3;
    private DevelopersShipBattleType pve_div2;
    private JsonNode oper_div;
    private DevelopersShipBattleType pvp;
    private JsonNode club;
    private DevelopersShipBattleType pve_solo;
    private DevelopersShipBattleType pvp_solo;
    private JsonNode oper_div_hard;
    private int battles;
    private DevelopersShipBattleType pve;
    private DevelopersShipBattleType pvp_div3;
    private DevelopersShipBattleType pvp_div2;
    private JsonNode oper_solo;
    private DevelopersShipBattleType rank_div3;
    private DevelopersShipBattleType rank_div2;
    private DevelopersShipBattleType rank_solo;


    public static DevelopersUserInfoStatistics parse(JsonNode node) throws BasicException {
        DevelopersUserInfoStatistics statistics = new DevelopersUserInfoStatistics();
        statistics.setClan(checkShipInfo(node.get("clan")));
        statistics.setDistance(WowsUtils.json(node.get("distance"), 0));
        statistics.setPve_div3(checkShipInfo(node.get("pve_div3")));
        statistics.setPve_div2(checkShipInfo(node.get("pve_div2")));
        statistics.setOper_div(checkInfo(node.get("oper_div")));
        statistics.setPvp(checkShipInfo(node.get("pvp")));
        statistics.setClub(checkInfo(node.get("club")));
        statistics.setPve_solo(checkShipInfo(node.get("pve_solo")));
        statistics.setPvp_solo(checkShipInfo(node.get("pvp_solo")));
        statistics.setOper_div_hard(checkInfo(node.get("oper_div_hard")));
        statistics.setBattles(WowsUtils.json(node.get("battles"), 0));
        statistics.setPve(checkShipInfo(node.get("pve")));
        statistics.setPvp_div3(checkShipInfo(node.get("pvp_div3")));
        statistics.setPvp_div2(checkShipInfo(node.get("pvp_div2")));
        statistics.setOper_solo(checkInfo(node.get("oper_solo")));
        statistics.setRank_div3(checkShipInfo(node.get("rank_div3")));
        statistics.setRank_div2(checkShipInfo(node.get("rank_div2")));
        statistics.setRank_solo(checkShipInfo(node.get("rank_solo")));
        return statistics;
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
