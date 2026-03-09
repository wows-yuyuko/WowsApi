package com.shinoaki.wows.api.vortex.clan.base.info;

import lombok.Data;
import tools.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;

/**
 * 公会建筑信息
 *
 * @author Xun
 * @date 2022/07/06 星期三 10:00
 */
@Data
public class VortexClanBuildingsInfo {

    /**
     * null
     */
    private Long clanId;
    /**
     * null
     */
    private Integer buildingsId;
    /**
     * 数组 list<String>
     */
    private String buildingsModifiers;
    /**
     * null
     */
    private String buildingsName;
    /**
     * null
     */
    private Integer buildingsLevel;

    public static List<VortexClanBuildingsInfo> clan(long clanId, JsonNode node) {
        List<VortexClanBuildingsInfo> infoList = new ArrayList<>();
        for (var x:node.properties()){
            VortexClanBuildingsInfo info = new VortexClanBuildingsInfo();
            info.setClanId(clanId);
            info.setBuildingsName(x.getValue().get("name").asString());
            info.setBuildingsLevel(x.getValue().get("level").asInt());
            info.setBuildingsId(x.getValue().get("id").asInt());
            info.setBuildingsModifiers(x.getValue().get("modifiers").toString());
            infoList.add(info);
        }
        return infoList;
    }
}
