package com.shinoaki.wows.api.developers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.shinoaki.wows.api.developers.warships.DevelopersMeta;
import com.shinoaki.wows.api.developers.warships.DevelopersShipBattleInfo;
import com.shinoaki.wows.api.error.StatusException;
import com.shinoaki.wows.api.utils.JsonUtils;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author Xun
 * @date 2023/4/24 14:59 星期一
 */
public record DevelopersUserShip(DevelopersMeta developersMeta, long accountId, List<DevelopersShipBattleInfo> infoList) {

    public static DevelopersUserShip parse(JsonNode node) throws StatusException, JsonProcessingException {
        //判断status
        JsonNode status = node.get("status");
        if (!status.isNull() && status.asText().equalsIgnoreCase("ok")) {
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
        //抛出解析status 异常的问题
        throw new StatusException(status);
    }
}
