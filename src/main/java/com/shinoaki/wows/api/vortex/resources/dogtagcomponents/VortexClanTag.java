package com.shinoaki.wows.api.vortex.resources.dogtagcomponents;

import lombok.Data;
import tools.jackson.databind.JsonNode;

/**
 * @author Xun
 * @date 2023/12/14 星期四
 */
@Data
public class VortexClanTag {
    private String x;
    private String y;
    private String fontColor;

    public static VortexClanTag parse(JsonNode node) {
        VortexClanTag tag = new VortexClanTag();
        if (!node.isNull()) {
            tag.setX(node.path("x").asString());
            tag.setY(node.path("y").asString());
            tag.setFontColor(node.path("fontColor").asString());
        }
        return tag;
    }
}
