package com.shinoaki.wows.api.vortex.resources;

import lombok.Data;
import tools.jackson.databind.JsonNode;

/**
 * @author Xun
 * @date 2023/12/7 星期四
 */
@Data
public class WowsIcons {
    private String defaultUrl;
    /**
     * 自己额外新增的扩展
     */
    private String originUrl = "";

    public WowsIcons() {
    }

    public WowsIcons(String defaultUrl) {
        this.defaultUrl = defaultUrl;
        this.originUrl = defaultUrl;
    }

    public static WowsIcons parse(JsonNode node) {
        return new WowsIcons(node.path("default").asString());
    }
}
