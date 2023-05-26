package com.shinoaki.wows.api.developers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.shinoaki.wows.api.data.ShipInfo;
import com.shinoaki.wows.api.developers.warships.DevelopersMeta;
import com.shinoaki.wows.api.developers.warships.DevelopersShipBattleInfo;
import com.shinoaki.wows.api.error.StatusException;
import com.shinoaki.wows.api.type.WowsBattlesType;
import com.shinoaki.wows.api.utils.DateUtils;
import com.shinoaki.wows.api.utils.JsonUtils;

import java.util.*;

/**
 * @author Xun
 * @date 2023/4/24 14:59 星期一
 */
public record DevelopersUserShip(DevelopersMeta developersMeta, long accountId, List<DevelopersShipBattleInfo> infoList) {

    public static DevelopersUserShip parse(JsonNode node) throws StatusException, JsonProcessingException {
        //判断status
        StatusException.status(node);
        DevelopersMeta developersMeta = new JsonUtils().parse(node.get("meta"), DevelopersMeta.class);
        Iterator<Map.Entry<String, JsonNode>> iterator = node.get("data").fields();
        if (iterator.hasNext()) {
            Map.Entry<String, JsonNode> map = iterator.next();
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
                map.computeIfAbsent(entry.getKey(), list -> new ArrayList<>()).add(ShipInfo.to(info.ship_id(), entry.getValue(), info.last_battle_time(),
                        DateUtils.toEpochMilli()));
            }
        }
        return map;
    }
}
