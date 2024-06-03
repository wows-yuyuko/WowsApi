package com.shinoaki.wows.api.vortex.resources;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author Xun
 * @date 2023/12/7 星期四
 */
@Data
@AllArgsConstructor
public class WowsIcons {
    private String defaultUrl;

    public WowsIcons() {
    }

    public static WowsIcons parse(JsonNode node) {
        return new WowsIcons(node.get("default").asText());
    }
}
