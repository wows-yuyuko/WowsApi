package com.shinoaki.wows.api.vortex;

import com.shinoaki.wows.api.data.ShipInfo;
import com.shinoaki.wows.api.error.BasicException;
import com.shinoaki.wows.api.error.HiddenProfileException;
import com.shinoaki.wows.api.type.WowsBattlesType;
import com.shinoaki.wows.api.utils.DateUtils;
import com.shinoaki.wows.api.vortex.ship.VortexShipInfo;
import com.shinoaki.wows.api.vortex.ship.VortexShipMarkOfExcellence;
import com.shinoaki.wows.api.vortex.ship.VortexShipStatistics;
import tools.jackson.databind.JsonNode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 账号战舰列表，对应 {@code /api/accounts/{id}/ships/{type}/}。
 * <p>
 * 响应字段：{@code data.<accountId>} 下的 name / created_at / activated_at / visibility_settings，
 * 以及 {@code statistics.<shipId>} 下的 {@code <type>} 数据块、mastery_sign、mark_of_excellence。
 * <p>
 * 莱服(korabli.su)有两点不同：{@code data.<accountId>} 里没有 {@code visibility_settings}（按 false 处理）、
 * 单船条目里没有 {@code mastery_sign}/{@code mark_of_excellence}（按 ""/空处理）。
 *
 * @param type          战斗类型（含莱服专属类型，见 {@link WowsBattlesType#servers()}）
 * @param accountId     账号ID
 * @param hiddenProfile 是否隐藏了战绩
 * @param name          用户名
 * @param shipMap       战舰信息(战舰ID -> 战绩)
 * @param created_at    账号创建时间
 * @param activated_at  账号激活时间
 * @param recordTime    系统记录时间
 * @param visibility_settings 战绩可见性设置
 * @author Xun
 * @date 2023/4/9 18:43 星期日
 */
public record VortexUserShip(WowsBattlesType type, long accountId, boolean hiddenProfile, String name, Map<Long, VortexShipStatistics> shipMap,
                             double created_at, double activated_at, long recordTime, boolean visibility_settings) {

    public List<ShipInfo> toShipInfoList() {
        return this.shipMap.entrySet().stream().map(map ->
                ShipInfo.to(map.getKey(), map.getValue(), recordTime)).toList();
    }

    /**
     * 战斗类型的战舰列表入口（莱服专属类型直接传 {@link WowsBattlesType} 里的对应值，如 {@code RATING_SOLO}）。
     */
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
            var info = VortexShipInfo.parse(map.getValue().get(type.code()));
            var masterySign = map.getValue().path("mastery_sign").asString("");
            var markOfExcellence = VortexShipMarkOfExcellence.parse(map.getValue().path("mark_of_excellence"));
            shipMap.put(Long.parseLong(map.getKey()), new VortexShipStatistics(info, masterySign, markOfExcellence));
        }
        return new VortexUserShip(type, accountId, false, name, shipMap, node.path("created_at").asDouble(0),
                node.path("activated_at").asDouble(),
                DateUtils.toEpochMilli(),
                node.path("visibility_settings").asBoolean(false));
    }
}
