package com.shinoaki.wows.api.vortex.resources.glossary;

import lombok.Data;
import tools.jackson.databind.JsonNode;

/**
 * 成就图标，对应 getGlossData 的 {@code achievements[].icons}。
 *
 * @author Xun
 */
@Data
public class VortexAchievementIcons {
    private String defaultUrl;
    private String inactiveUrl;
    private String normalUrl;

    public static VortexAchievementIcons parse(JsonNode node) {
        VortexAchievementIcons icons = new VortexAchievementIcons();
        icons.setDefaultUrl(node.path("default").asString(""));
        icons.setInactiveUrl(node.path("inactive").asString(""));
        icons.setNormalUrl(node.path("normal").asString(""));
        return icons;
    }
}
