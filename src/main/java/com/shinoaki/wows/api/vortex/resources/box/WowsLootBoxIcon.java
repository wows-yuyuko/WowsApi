package com.shinoaki.wows.api.vortex.resources.box;

import tools.jackson.databind.JsonNode;

/**
 * 箱子图标，对应 data.icons。
 *
 * @param small      小图标(网络)
 * @param large      大图标(网络)
 * @param localSmall 小图标(本地路径)
 * @param localLarge 大图标(本地路径)
 * @param defaultUrl 默认图标(网络)
 * @author Xun
 */
public record WowsLootBoxIcon(
        String small,
        String large,
        String localSmall,
        String localLarge,
        String defaultUrl
) {
    public static WowsLootBoxIcon parse(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return null;
        }
        return new WowsLootBoxIcon(
                node.path("small").asString(""),
                node.path("large").asString(""),
                node.path("localSmall").asString(""),
                node.path("localLarge").asString(""),
                node.path("default").asString("")
        );
    }
}
