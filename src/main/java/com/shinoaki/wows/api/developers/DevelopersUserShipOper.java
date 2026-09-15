package com.shinoaki.wows.api.developers;

import com.shinoaki.wows.api.developers.warships.DevelopersMeta;
import com.shinoaki.wows.api.developers.warships.DevelopersShipOperBattleInfo;
import com.shinoaki.wows.api.error.BasicException;
import com.shinoaki.wows.api.utils.JsonUtils;
import tools.jackson.databind.JsonNode;

import java.util.List;

/**
 * @author Xun
 * @date 2023/4/24 14:59 星期一
 */
public record DevelopersUserShipOper(DevelopersMeta developersMeta, long accountId, List<DevelopersShipOperBattleInfo> infoList) {

    public static DevelopersUserShipOper parse(JsonNode node) throws BasicException {
        //判断status
        BasicException.status(node);
        DevelopersMeta developersMeta = JsonUtils.json().parse(node.path("meta"), DevelopersMeta.class);
        for (var map : node.path("data").properties()) {
            List<DevelopersShipOperBattleInfo> developersShipBattleInfoList = DevelopersShipOperBattleInfo.parse(map.getValue());
            return new DevelopersUserShipOper(developersMeta, Long.parseLong(map.getKey()), developersShipBattleInfoList);
        }
        if (developersMeta.hidden() != null) {
            return new DevelopersUserShipOper(developersMeta, developersMeta.hidden()[0], List.of());
        }
        return new DevelopersUserShipOper(developersMeta, -1L, List.of());
    }
}
