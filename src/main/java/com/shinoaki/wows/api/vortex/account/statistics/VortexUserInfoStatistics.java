package com.shinoaki.wows.api.vortex.account.statistics;

import com.shinoaki.wows.api.error.BasicException;
import com.shinoaki.wows.api.type.WowsBattlesType;
import com.shinoaki.wows.api.vortex.ship.VortexShipInfo;
import lombok.Data;
import tools.jackson.databind.JsonNode;

import java.util.Map;

/**
 * 账号战绩汇总，对应 {@code /api/accounts/{id}/} 的 {@code data.<accountId>.statistics}。
 * <p>
 * 各战斗类型(pvp/pve/pvp_solo/pvp_div2/pvp_div3/rank_solo/rank_div2/rank_div3/rank_old_*)为同构的
 * {@link VortexShipInfo} 数据块（其它服 80 个字段、莱服 98 个）；{@code seasons} 为按赛季三层 key 的同类数据；
 * {@code rank_info} 为排位赛进度；{@code mark_of_excellence} 为优秀标记。
 * <p>
 * <b>莱服(korabli.su)额外多出</b>：{@code brawl}、{@code clan}、{@code oper_mm}、{@code rank}、{@code rank_old}、
 * {@code rating}、{@code rating_div}、{@code rating_solo} 八个数据块与 {@code rating_info}（评级战进度）；
 * 同时莱服没有 {@code mastery_sign}/{@code mark_of_excellence}（精通点改为 {@link Basic#mastery_rank()}）。
 * 取这些块建议直接用 {@link #block(WowsBattlesType)}。
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
    /**
     * 乱斗(莱服)
     */
    private VortexShipInfo brawl;
    /**
     * 军团战(莱服，账号汇总里有、没有对应的单船接口)
     */
    private VortexShipInfo clan;
    /**
     * 行动模式-匹配(莱服)
     */
    private VortexShipInfo oper_mm;
    /**
     * 新排位(莱服)
     */
    private VortexShipInfo rank;
    /**
     * 旧排位(莱服)
     */
    private VortexShipInfo rank_old;
    /**
     * 评级战(莱服)
     */
    private VortexShipInfo rating;
    /**
     * 评级战-组队(莱服)
     */
    private VortexShipInfo rating_div;
    /**
     * 评级战-单人(莱服)
     */
    private VortexShipInfo rating_solo;
    /**
     * 赛季ID -> 评级战进度(莱服)
     */
    private Map<String, VortexRatingInfo> rating_info;

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
        //---- 莱服专属 ----
        statistics.setBrawl(infoCheckEmpty(node.path("brawl")));
        statistics.setClan(infoCheckEmpty(node.path("clan")));
        statistics.setOper_mm(infoCheckEmpty(node.path("oper_mm")));
        statistics.setRank(infoCheckEmpty(node.path("rank")));
        statistics.setRank_old(infoCheckEmpty(node.path("rank_old")));
        statistics.setRating(infoCheckEmpty(node.path("rating")));
        statistics.setRating_div(infoCheckEmpty(node.path("rating_div")));
        statistics.setRating_solo(infoCheckEmpty(node.path("rating_solo")));
        statistics.setRating_info(VortexRatingInfo.parseAll(node.path("rating_info")));
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
        statistics.setBrawl(VortexShipInfo.defaultValue());
        statistics.setClan(VortexShipInfo.defaultValue());
        statistics.setOper_mm(VortexShipInfo.defaultValue());
        statistics.setRank(VortexShipInfo.defaultValue());
        statistics.setRank_old(VortexShipInfo.defaultValue());
        statistics.setRating(VortexShipInfo.defaultValue());
        statistics.setRating_div(VortexShipInfo.defaultValue());
        statistics.setRating_solo(VortexShipInfo.defaultValue());
        statistics.setRating_info(Map.of());
        return statistics;
    }

    /**
     * 按战斗类型取数据块，省得写一长串 switch（含莱服专属类型，如 {@code BRAWL}）。
     * <p>
     * 注意：响应里没有的块（如 {@code rating_*} 在其它服、{@code pve_solo} 系在任何服的 vortex）返回的是
     * 全 0 的默认值，不会抛异常，也不会是 null。
     *
     * @param type 战斗类型
     * @return 该类型的战斗数据块，取不到时返回全 0 的默认值
     */
    public VortexShipInfo block(WowsBattlesType type) {
        return switch (type) {
            case PVP -> pvp;
            case PVP_SOLO -> pvp_solo;
            case PVP_DIV2 -> pvp_div2;
            case PVP_DIV3 -> pvp_div3;
            case RANK_SOLO -> rank_solo;
            case PVE -> pve;
            case OPER_MM -> oper_mm;
            case BRAWL -> brawl;
            case CLAN -> clan;
            case RANK -> rank;
            case RANK_DIV2 -> rank_div2;
            case RANK_DIV3 -> rank_div3;
            case RANK_OLD -> rank_old;
            case RANK_OLD_SOLO -> rank_old_solo;
            case RANK_OLD_DIV2 -> rank_old_div2;
            case RANK_OLD_DIV3 -> rank_old_div3;
            case RATING -> rating;
            case RATING_SOLO -> rating_solo;
            case RATING_DIV -> rating_div;
            //pve_solo/pve_div2/pve_div3 只在官方开发者API 出现，vortex 账号汇总里没有
            case PVE_SOLO, PVE_DIV2, PVE_DIV3 -> VortexShipInfo.defaultValue();
        };
    }

    private static VortexShipInfo infoCheckEmpty(JsonNode node) throws BasicException {
        if (node.isEmpty() || node.isNull()) {
            return VortexShipInfo.defaultValue();
        }
        return VortexShipInfo.parse(node);
    }
}
