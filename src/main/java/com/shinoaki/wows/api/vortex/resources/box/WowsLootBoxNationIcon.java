package com.shinoaki.wows.api.vortex.resources.box;

import tools.jackson.databind.JsonNode;

/**
 * 国籍旗帜图标，对应 nation.icons。
 *
 * @param large      大图标(网络)
 * @param defaultUrl 默认图标(网络)
 * @param localLarge 大图标(本地路径)
 * @param localTiny  极小图标(本地路径)
 * @param small      小图标(网络)
 * @param localSmall 小图标(本地路径)
 * @param tiny       极小图标(网络)
 * @author Xun
 */
public record WowsLootBoxNationIcon(
        String large,
        String defaultUrl,
        String localLarge,
        String localTiny,
        String small,
        String localSmall,
        String tiny
) {
    public static WowsLootBoxNationIcon parse(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return null;
        }
        return new WowsLootBoxNationIcon(
                node.path("large").asString(""),
                node.path("default").asString(""),
                node.path("localLarge").asString(""),
                node.path("localTiny").asString(""),
                node.path("small").asString(""),
                node.path("localSmall").asString(""),
                node.path("tiny").asString("")
        );
    }
}
