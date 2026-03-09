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
            tag.setX(node.get("x").asString());
            tag.setY(node.get("y").asString());
            tag.setFontColor(node.get("fontColor").asString());
        }
        return tag;
    }
}
