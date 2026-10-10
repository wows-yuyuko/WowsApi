package com.shinoaki.wows.api.developers.warships;

import com.shinoaki.wows.api.developers.account.DevelopersUserInfoPrivate;
import com.shinoaki.wows.api.developers.warships.type.DevelopersOperBattleType;
import com.shinoaki.wows.api.developers.warships.type.DevelopersShipBattleType;
import com.shinoaki.wows.api.error.BasicException;
import tools.jackson.databind.JsonNode;

import java.util.*;

public record DevelopersShipOperBattleInfo(Map<String, DevelopersOperBattleType> shipBattleTypeMap, long last_battle_time, long account_id, int distance,
                                           long updated_at, int battles, long ship_id, DevelopersShipBattleType pvp,
                                           DevelopersUserInfoPrivate privateInfo) {
    private static final String[] OPER_TYPE = new String[]{"oper_div", "oper_div_hard", "oper_solo"};

    public static List<DevelopersShipOperBattleInfo> parse(JsonNode node) throws BasicException {
        List<DevelopersShipOperBattleInfo> list = new ArrayList<>();
        for (var data : node) {
            Map<String, DevelopersOperBattleType> shipBattleTypeMap = new HashMap<>();
            for (var type : OPER_TYPE) {
                JsonNode jsonNode = data.get(type.toLowerCase(Locale.ROOT));
                shipBattleTypeMap.put(type, DevelopersOperBattleType.parse(jsonNode));
            }
            list.add(new DevelopersShipOperBattleInfo(shipBattleTypeMap, data.path("last_battle_time").asLong(), data.path("account_id").asLong(), data.get(
                    "distance").asInt(), data.path("updated_at").asLong(), data.path("battles").asInt(), data.path("ship_id").asLong(),
                    DevelopersShipBattleType.parse(data.path("pvp")),
                    DevelopersUserInfoPrivate.parse(data.path("private"))));
        }
        return list;
    }
}