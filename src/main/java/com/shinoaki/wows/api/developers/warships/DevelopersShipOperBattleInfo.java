package com.shinoaki.wows.api.developers.warships;

import com.shinoaki.wows.api.developers.account.DevelopersUserInfoPrivate;
import com.shinoaki.wows.api.developers.warships.type.DevelopersOperBattleType;
import com.shinoaki.wows.api.developers.warships.type.DevelopersShipBattleType;
import com.shinoaki.wows.api.error.BasicException;
import tools.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 官方 {@code /wows/ships/stats/} 全量战绩（extra 见 {@code WowsBattlesType.DEVELOPERS_ALL_STATS_EXTRA}）的单船数据。
 * <p>
 * 解析<b>不限定战斗类型</b>：以响应里实际出现的对象块为准，按块形状分流——
 * 带 {@code wins_by_tasks} 的块（oper_* 家族）进 {@link #operBattleTypeMap()}，
 * 其余常规块（pvp / pve_* / rank_* / clan 等）进 {@link #battleTypeMap()}，
 * 块内字段分别由 {@link DevelopersOperBattleType} / {@link DevelopersShipBattleType} 承接。
 *
 * @author Xun
 */
public record DevelopersShipOperBattleInfo(Map<String, DevelopersShipBattleType> battleTypeMap,
                                           Map<String, DevelopersOperBattleType> operBattleTypeMap,
                                           long last_battle_time, long account_id, int distance,
                                           long updated_at, int battles, long ship_id,
                                           DevelopersUserInfoPrivate privateInfo) {

    public static List<DevelopersShipOperBattleInfo> parse(JsonNode node) throws BasicException {
        List<DevelopersShipOperBattleInfo> list = new ArrayList<>();
        for (var data : node) {
            Map<String, DevelopersShipBattleType> battleTypeMap = new LinkedHashMap<>();
            Map<String, DevelopersOperBattleType> operBattleTypeMap = new LinkedHashMap<>();
            for (var field : data.properties()) {
                //只有对象块才算战斗数据块：跳过数字字段与 private 私有块
                if (!field.getValue().isObject() || "private".equals(field.getKey())) {
                    continue;
                }
                if (field.getValue().has("wins_by_tasks")) {
                    operBattleTypeMap.put(field.getKey(), DevelopersOperBattleType.parse(field.getValue()));
                } else {
                    battleTypeMap.put(field.getKey(), DevelopersShipBattleType.parse(field.getValue()));
                }
            }
            list.add(new DevelopersShipOperBattleInfo(battleTypeMap, operBattleTypeMap,
                    data.path("last_battle_time").asLong(), data.path("account_id").asLong(), data.get("distance").asInt(),
                    data.path("updated_at").asLong(), data.path("battles").asInt(), data.path("ship_id").asLong(),
                    DevelopersUserInfoPrivate.parse(data.path("private"))));
        }
        return list;
    }

    /**
     * pvp 块（默认块）的便捷访问；等价于 {@code battleTypeMap().get("pvp")}。
     */
    public DevelopersShipBattleType pvp() {
        return battleTypeMap.get("pvp");
    }
}
