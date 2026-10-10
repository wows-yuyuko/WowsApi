package com.shinoaki.wows.api.vortex.account.statistics;

import com.shinoaki.wows.api.error.BasicException;
import com.shinoaki.wows.api.vortex.ship.VortexShipInfo;
import lombok.Data;
import tools.jackson.databind.JsonNode;

import java.util.Map;

/**
 * 账号战绩汇总，对应 {@code /api/accounts/{id}/} 的 {@code data.<accountId>.statistics}。
 * <p>
 * 各战斗类型(pvp/pve/pvp_solo/pvp_div2/pvp_div3/rank_solo/rank_div2/rank_div3/rank_old_*)为同构的
 * {@link VortexShipInfo} 数据块（80 个字段）；{@code seasons} 为按赛季三层 key 的同类数据；
 * {@code rank_info} 为排位赛进度；{@code mark_of_excellence} 为优秀标记。
 * <p>
 * https://vortex.worldofwarships.asia/api/accounts/2022515210/
 *
 * @author Xun
 */
@Data
public class VortexUserInfoStatistics {
    private VortexShipInfo pvp;
    private VortexShipInfo pve;
    private VortexMarkOfExcellence mark_of_excellence;
    /**
     * 赛季 -> 分组 -> 段位 -> 排位进度
     */
    private Map<String, Map<String, Map<String, VortexRankInfo>>> rank_info;
    private Basic basic;
    private VortexShipInfo pvp_solo;
    private VortexShipInfo rank_old_div3;
    private VortexShipInfo rank_old_div2;
    private VortexShipInfo rank_solo;
    /**
     * 赛季 -> 分组 -> 战斗类型 -> 战斗数据
     */
    private VortexSeasons seasons;
    private VortexShipInfo rank_old_solo;
    private VortexShipInfo pvp_div2;
    private VortexShipInfo rank_div3;
    private VortexShipInfo rank_div2;
    private VortexShipInfo pvp_div3;
    private String mastery_sign;

    public static VortexUserInfoStatistics parse(JsonNode node) throws BasicException {
        if (node == null || node.isEmpty()) {
            return empty();
        }
        VortexUserInfoStatistics statistics = new VortexUserInfoStatistics();
        statistics.setPvp(infoCheckEmpty(node.path("pvp")));
        statistics.setPve(infoCheckEmpty(node.path("pve")));
        statistics.setMark_of_excellence(VortexMarkOfExcellence.parse(node.path("mark_of_excellence")));
        statistics.setRank_info(VortexRankInfo.parseAll(node.path("rank_info")));
        statistics.setBasic(Basic.parse(node.path("basic")));
        statistics.setPvp_solo(infoCheckEmpty(node.path("pvp_solo")));
        statistics.setRank_old_div3(infoCheckEmpty(node.path("rank_old_div3")));
        statistics.setRank_old_div2(infoCheckEmpty(node.path("rank_old_div2")));
        statistics.setRank_solo(infoCheckEmpty(node.path("rank_solo")));
        statistics.setSeasons(VortexSeasons.parse(node.path("seasons")));
        statistics.setRank_old_solo(infoCheckEmpty(node.path("rank_old_solo")));
        statistics.setPvp_div2(infoCheckEmpty(node.path("pvp_div2")));
        statistics.setRank_div3(infoCheckEmpty(node.path("rank_div3")));
        statistics.setRank_div2(infoCheckEmpty(node.path("rank_div2")));
        statistics.setPvp_div3(infoCheckEmpty(node.path("pvp_div3")));
        statistics.setMastery_sign(node.path("mastery_sign").asString(""));
        return statistics;
    }

    private static VortexUserInfoStatistics empty() {
        VortexUserInfoStatistics statistics = new VortexUserInfoStatistics();
        statistics.setPvp(VortexShipInfo.defaultValue());
        statistics.setPve(VortexShipInfo.defaultValue());
        statistics.setMark_of_excellence(VortexMarkOfExcellence.empty());
        statistics.setRank_info(Map.of());
        statistics.setBasic(Basic.empty());
        statistics.setPvp_solo(VortexShipInfo.defaultValue());
        statistics.setRank_old_div3(VortexShipInfo.defaultValue());
        statistics.setRank_old_div2(VortexShipInfo.defaultValue());
        statistics.setRank_solo(VortexShipInfo.defaultValue());
        statistics.setSeasons(VortexSeasons.empty());
        statistics.setRank_old_solo(VortexShipInfo.defaultValue());
        statistics.setPvp_div2(VortexShipInfo.defaultValue());
        statistics.setRank_div3(VortexShipInfo.defaultValue());
        statistics.setRank_div2(VortexShipInfo.defaultValue());
        statistics.setPvp_div3(VortexShipInfo.defaultValue());
        statistics.setMastery_sign("");
        return statistics;
    }

    private static VortexShipInfo infoCheckEmpty(JsonNode node) throws BasicException {
        if (node.isEmpty() || node.isNull()) {
            return VortexShipInfo.defaultValue();
        }
        return VortexShipInfo.parse(node);
    }
}
