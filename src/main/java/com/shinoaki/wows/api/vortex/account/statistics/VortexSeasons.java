package com.shinoaki.wows.api.vortex.account.statistics;

import com.shinoaki.wows.api.error.BasicException;
import com.shinoaki.wows.api.vortex.ship.VortexShipInfo;
import tools.jackson.databind.JsonNode;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 赛季战绩，对应 {@code /api/accounts/{id}/} 中 {@code statistics.seasons}。
 * <p>
 * 原始结构为三层动态 key：{@code seasons[赛季][分组][战斗类型]}，值为与 pvp 同构的战斗数据块，
 * 例：{@code {"1028": {"0": {"rank_solo": {...80 个字段...}}}}}
 *
 * @param seasons 赛季 -> 分组 -> 战斗类型 -> 战斗数据
 * @author Xun
 */
public record VortexSeasons(Map<String, Map<String, Map<String, VortexShipInfo>>> seasons) {

    public static VortexSeasons parse(JsonNode node) throws BasicException {
        Map<String, Map<String, Map<String, VortexShipInfo>>> result = new LinkedHashMap<>();
        if (node == null || node.isMissingNode() || node.isNull() || !node.isObject()) {
            return empty();
        }
        for (Map.Entry<String, JsonNode> season : node.properties()) {
            Map<String, Map<String, VortexShipInfo>> groups = new LinkedHashMap<>();
            for (Map.Entry<String, JsonNode> group : season.getValue().properties()) {
                Map<String, VortexShipInfo> types = new LinkedHashMap<>();
                for (Map.Entry<String, JsonNode> type : group.getValue().properties()) {
                    types.put(type.getKey(), VortexShipInfo.parse(type.getValue()));
                }
                groups.put(group.getKey(), types);
            }
            result.put(season.getKey(), groups);
        }
        return new VortexSeasons(result);
    }

    public static VortexSeasons empty() {
        return new VortexSeasons(Map.of());
    }

    /**
     * 跨分组取某个赛季某个战斗类型的数据（取第一个命中的分组）。
     */
    public VortexShipInfo stats(String season, String battleType) {
        Map<String, Map<String, VortexShipInfo>> groups = seasons.get(season);
        if (groups == null) {
            return VortexShipInfo.defaultValue();
        }
        for (Map<String, VortexShipInfo> types : groups.values()) {
            VortexShipInfo info = types.get(battleType);
            if (info != null) {
                return info;
            }
        }
        return VortexShipInfo.defaultValue();
    }

    public boolean isEmpty() {
        return seasons.isEmpty();
    }
}
