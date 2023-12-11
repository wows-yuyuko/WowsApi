package com.shinoaki.wows.api.vortex;

import com.fasterxml.jackson.databind.JsonNode;
import com.shinoaki.wows.api.data.ShipInfo;
import com.shinoaki.wows.api.error.BasicException;
import com.shinoaki.wows.api.type.WowsBattlesType;
import com.shinoaki.wows.api.utils.DateUtils;
import com.shinoaki.wows.api.vortex.ship.VortexShipInfo;

import java.util.*;

/**
 * @param type          战斗类型
 * @param accountId     账号ID
 * @param hiddenProfile 是否隐藏了战绩
 * @param name          用户名
 * @param shipMap       战舰信息
 * @param created_at    账号创建时间
 * @param activated_at
 * @param recordTime    系统记录时间
 * @author Xun
 * @date 2023/4/9 18:43 星期日
 */
public record VortexUserShip(WowsBattlesType type, long accountId, boolean hiddenProfile, String name, Map<Long, VortexShipInfo> shipMap, double created_at,
                             double activated_at, long recordTime) {

    public List<ShipInfo> toShipInfoList() {
        return this.shipMap.entrySet().stream().map(map ->
                ShipInfo.to(map.getKey(), map.getValue(), recordTime)).toList();
    }

    public static VortexUserShip parse(WowsBattlesType type, JsonNode node) throws BasicException {
        //判断status
        BasicException.status(node);
        Iterator<Map.Entry<String, JsonNode>> iterator = node.get("data").fields();
        if (iterator.hasNext()) {
            Map.Entry<String, JsonNode> map = iterator.next();
            return parse(type, Long.parseLong(map.getKey()), map.getValue());
        }
        return null;
    }

    private static VortexUserShip parse(WowsBattlesType type, long accountId, JsonNode node) throws BasicException {
        String name = node.get("name").asText();
        JsonNode hiddenProfile = node.get("hidden_profile");
        if (hiddenProfile != null && hiddenProfile.asBoolean()) {
            //用户隐藏了战绩
            return new VortexUserShip(type, accountId, true, name, Map.of(), 0, 0, 0L);
        }
        Map<Long, VortexShipInfo> shipMap = new HashMap<>();
        Iterator<Map.Entry<String, JsonNode>> iterator = node.get("statistics").fields();
        while (iterator.hasNext()) {
            var map = iterator.next();
            shipMap.put(Long.parseLong(map.getKey()), VortexShipInfo.parse(map.getValue().get(type.name().toLowerCase(Locale.ROOT))));
        }
        return new VortexUserShip(type, accountId, false, name, shipMap, node.get("created_at").asDouble(),
                node.get("activated_at").asDouble(),
                DateUtils.toEpochMilli());
    }
}
