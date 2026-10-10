package com.shinoaki.wows.api.vortex.resources.glossary;

import lombok.Data;
import tools.jackson.databind.JsonNode;

/**
 * 战斗类型图标，对应 getGlossData 的 {@code battleTypes[].icons}。
 *
 * @author Xun
 */
@Data
public class VortexBattleTypeIcons {
    private String defaultUrl;

    public static VortexBattleTypeIcons parse(JsonNode node) {
        VortexBattleTypeIcons icons = new VortexBattleTypeIcons();
        icons.setDefaultUrl(node.path("default").asString(""));
        return icons;
    }
}
