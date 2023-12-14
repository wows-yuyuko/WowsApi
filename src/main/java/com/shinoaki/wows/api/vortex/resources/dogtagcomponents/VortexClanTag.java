package com.shinoaki.wows.api.vortex.resources.dogtagcomponents;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

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
            tag.setX(node.get("x").asText());
            tag.setY(node.get("y").asText());
            tag.setFontColor(node.get("fontColor").asText());
        }
        return tag;
    }
}
