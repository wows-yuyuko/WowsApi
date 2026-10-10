package com.shinoaki.wows.api.vortex.resources.glossary;

import lombok.Data;
import tools.jackson.databind.JsonNode;

/**
 * 国家旗帜图标，对应 getGlossData 的 {@code nations[].icons}。
 *
 * @author Xun
 */
@Data
public class VortexNationIcons {
    private String smallUrl;
    private String tinyUrl;

    public static VortexNationIcons parse(JsonNode node) {
        VortexNationIcons icons = new VortexNationIcons();
        icons.setSmallUrl(node.path("small").asString(""));
        icons.setTinyUrl(node.path("tiny").asString(""));
        return icons;
    }
}
