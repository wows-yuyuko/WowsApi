package com.shinoaki.wows.api.developers;

import com.shinoaki.wows.api.data.ShipInfo;
import com.shinoaki.wows.api.developers.warships.DevelopersMeta;
import com.shinoaki.wows.api.developers.warships.DevelopersShipBattleInfo;
import com.shinoaki.wows.api.error.BasicException;
import com.shinoaki.wows.api.type.WowsBattlesType;
import com.shinoaki.wows.api.utils.DateUtils;
import com.shinoaki.wows.api.utils.JsonUtils;
import tools.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * @author Xun
 * @date 2023/4/24 14:59 星期一
 */
public record DevelopersUserShip(DevelopersMeta developersMeta, long accountId, List<DevelopersShipBattleInfo> infoList) {

    public static DevelopersUserShip parse(JsonNode node) throws BasicException {
        //判断status
        BasicException.status(node);
        DevelopersMeta developersMeta =   JsonUtils.json().parse(node.get("meta"), DevelopersMeta.class);
        for (var map:node.get("data").properties()){
            List<DevelopersShipBattleInfo> developersShipBattleInfoList = DevelopersShipBattleInfo.parse(map.getValue());
            return new DevelopersUserShip(developersMeta, Long.parseLong(map.getKey()), developersShipBattleInfoList);
        }
        if (developersMeta.hidden() != null) {
            return new DevelopersUserShip(developersMeta, developersMeta.hidden()[0], List.of());
        }
        return new DevelopersUserShip(developersMeta, -1L, List.of());
    }

    public Map<WowsBattlesType, List<ShipInfo>> toShipInfoMap() {
        Map<WowsBattlesType, List<ShipInfo>> map = new EnumMap<>(WowsBattlesType.class);
        for (DevelopersShipBattleInfo info : infoList) {
            for (var entry : info.shipBattleTypeMap().entrySet()) {
                if (entry.getValue() != null) {
                    map.computeIfAbsent(entry.getKey(), list -> new ArrayList<>()).add(ShipInfo.to(info.ship_id(), entry.getValue(), info.last_battle_time(),
                            DateUtils.toEpochMilli()));
                }
            }
        }
        return map;
    }
}
