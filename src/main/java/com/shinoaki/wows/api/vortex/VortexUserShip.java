package com.shinoaki.wows.api.vortex;

import com.shinoaki.wows.api.data.ShipInfo;
import com.shinoaki.wows.api.error.BasicException;
import com.shinoaki.wows.api.error.HiddenProfileException;
import com.shinoaki.wows.api.type.WowsBattlesType;
import com.shinoaki.wows.api.utils.DateUtils;
import com.shinoaki.wows.api.vortex.ship.VortexShipInfo;
import com.shinoaki.wows.api.vortex.ship.VortexShipStatistics;
import tools.jackson.databind.JsonNode;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
public record VortexUserShip(WowsBattlesType type, long accountId, boolean hiddenProfile, String name, Map<Long, VortexShipStatistics> shipMap, double created_at,
                             double activated_at, long recordTime) {

    public List<ShipInfo> toShipInfoList() {
        return this.shipMap.entrySet().stream().map(map ->
                ShipInfo.to(map.getKey(), map.getValue(), recordTime)).toList();
    }

    public static VortexUserShip parse(WowsBattlesType type, JsonNode node) throws BasicException {
        //判断status
        BasicException.status(node);
        for (var map : node.path("data").properties()) {
            return parse(type, Long.parseLong(map.getKey()), map.getValue());
        }
        return null;
    }

    private static VortexUserShip parse(WowsBattlesType type, long accountId, JsonNode node) throws BasicException {
        String name = node.path("name").asString();
        var hidden = node.path("hidden_profile");
        if (!hidden.isMissingNode() && hidden.asBoolean()) {
            throw new HiddenProfileException(accountId);
        }
        Map<Long, VortexShipStatistics> shipMap = new HashMap<>();
        for (var map : node.path("statistics").properties()) {
            var info = VortexShipInfo.parse(map.getValue().get(type.name().toLowerCase(Locale.ROOT)));
            var masterySign = map.getValue().path("mastery_sign").asString("");
            shipMap.put(Long.parseLong(map.getKey()), new VortexShipStatistics(info, masterySign));
        }
        return new VortexUserShip(type, accountId, false, name, shipMap, node.path("created_at").asDouble(0),
                node.path("activated_at").asDouble(),
                DateUtils.toEpochMilli());
    }
}
