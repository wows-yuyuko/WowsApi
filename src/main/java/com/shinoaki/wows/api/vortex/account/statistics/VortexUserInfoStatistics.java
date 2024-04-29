package com.shinoaki.wows.api.vortex.account.statistics;

import com.fasterxml.jackson.databind.JsonNode;
import com.shinoaki.wows.api.error.BasicException;
import com.shinoaki.wows.api.vortex.ship.VortexShipInfo;
import lombok.Data;

/**
 * https://vortex.worldofwarships.asia/api/accounts/2022515210/
 */
@Data
public class VortexUserInfoStatistics {
    private VortexShipInfo pvp;
    private JsonNode rank_info;
    private Basic basic;
    private VortexShipInfo pvp_solo;
    private VortexShipInfo rank_old_div3;
    private VortexShipInfo rank_old_div2;
    private VortexShipInfo rank_solo;
    private JsonNode seasons;
    private VortexShipInfo rank_old_solo;
    private VortexShipInfo pvp_div2;
    private VortexShipInfo rank_div3;
    private VortexShipInfo rank_div2;
    private VortexShipInfo pvp_div3;

    public static VortexUserInfoStatistics parse(JsonNode node) throws BasicException {
        if (node == null || node.isEmpty()) {
            return empty();
        }
        VortexUserInfoStatistics statistics = new VortexUserInfoStatistics();
        statistics.setPvp(infoCheckEmpty(node.get("pvp")));
        statistics.setRank_info(node.get("rank_info"));
        statistics.setBasic(Basic.parse(node.get("basic")));
        statistics.setPvp_solo(infoCheckEmpty(node.get("pvp_solo")));
        statistics.setRank_old_div3(infoCheckEmpty(node.get("rank_old_div3")));
        statistics.setRank_old_div2(infoCheckEmpty(node.get("rank_old_div2")));
        statistics.setRank_solo(infoCheckEmpty(node.get("rank_solo")));
        statistics.setSeasons(node.get("seasons"));
        statistics.setRank_old_solo(infoCheckEmpty(node.get("rank_old_solo")));
        statistics.setPvp_div2(infoCheckEmpty(node.get("pvp_div2")));
        statistics.setRank_div3(infoCheckEmpty(node.get("rank_div3")));
        statistics.setRank_div2(infoCheckEmpty(node.get("rank_div2")));
        statistics.setPvp_div3(infoCheckEmpty(node.get("pvp_div3")));
        return statistics;
    }

    private static VortexUserInfoStatistics empty() {
        VortexUserInfoStatistics statistics = new VortexUserInfoStatistics();
        statistics.setPvp(VortexShipInfo.defaultValue());
        statistics.setRank_info(null);
        statistics.setBasic(Basic.empty());
        statistics.setPvp_solo(VortexShipInfo.defaultValue());
        statistics.setRank_old_div3(VortexShipInfo.defaultValue());
        statistics.setRank_old_div2(VortexShipInfo.defaultValue());
        statistics.setRank_solo(VortexShipInfo.defaultValue());
        statistics.setSeasons(null);
        statistics.setRank_old_solo(VortexShipInfo.defaultValue());
        statistics.setPvp_div2(VortexShipInfo.defaultValue());
        statistics.setRank_div3(VortexShipInfo.defaultValue());
        statistics.setRank_div2(VortexShipInfo.defaultValue());
        statistics.setPvp_div3(VortexShipInfo.defaultValue());
        return statistics;
    }

    private static VortexShipInfo infoCheckEmpty(JsonNode node) throws BasicException {
        if (node.isEmpty() || node.isNull()) {
            return VortexShipInfo.defaultValue();
        }
        return VortexShipInfo.parse(node);
    }
}
