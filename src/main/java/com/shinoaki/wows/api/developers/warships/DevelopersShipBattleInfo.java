package com.shinoaki.wows.api.developers.warships;

import com.fasterxml.jackson.databind.JsonNode;
import com.shinoaki.wows.api.developers.warships.type.DevelopersShipBattleType;
import com.shinoaki.wows.api.error.BasicException;
import com.shinoaki.wows.api.type.WowsBattlesType;

import java.util.*;

/**
 * @author Xun
 * @date 2023/4/24 20:54 星期一
 */
public record DevelopersShipBattleInfo(Map<WowsBattlesType, DevelopersShipBattleType> shipBattleTypeMap, long last_battle_time, long account_id, int distance,
                                       long updated_at, int battles, long ship_id) {
    public static List<DevelopersShipBattleInfo> parse(JsonNode node) throws BasicException {
        List<DevelopersShipBattleInfo> list = new ArrayList<>();
        for (var data : node) {
            Map<WowsBattlesType, DevelopersShipBattleType> shipBattleTypeMap = new EnumMap<>(WowsBattlesType.class);
            for (var type : WowsBattlesType.values()) {
                JsonNode jsonNode = data.get(type.name().toLowerCase(Locale.ROOT));
                shipBattleTypeMap.put(type, DevelopersShipBattleType.parse(jsonNode));
            }
            list.add(new DevelopersShipBattleInfo(shipBattleTypeMap, data.get("last_battle_time").asLong(), data.get("account_id").asLong(), data.get(
                    "distance").asInt(), data.get("updated_at").asLong(), data.get("battles").asInt(), data.get("ship_id").asLong()));
        }
        return list;
    }
}
