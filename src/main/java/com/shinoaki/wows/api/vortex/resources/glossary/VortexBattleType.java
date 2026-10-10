package com.shinoaki.wows.api.vortex.resources.glossary;

import lombok.Data;
import tools.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;

/**
 * 战斗类型(PVP/排位/行动…)，对应 getGlossData 的 {@code battleTypes[]}。
 *
 * @author Xun
 */
@Data
public class VortexBattleType {
    private String id;
    private String name;
    private String title;
    /**
     * 战绩接口里使用的名字(如 pvp)
     */
    private String dossierName;
    private VortexBattleTypeIcons icons;

    public static List<VortexBattleType> parseList(JsonNode node) {
        List<VortexBattleType> list = new ArrayList<>();
        for (JsonNode type : node) {
            VortexBattleType data = new VortexBattleType();
            data.setId(type.path("id").asString(""));
            data.setName(type.path("name").asString(""));
            data.setTitle(type.path("title").asString(""));
            data.setDossierName(type.path("dossierName").asString(""));
            data.setIcons(VortexBattleTypeIcons.parse(type.path("icons")));
            list.add(data);
        }
        return list;
    }
}
