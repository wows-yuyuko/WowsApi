package com.shinoaki.wows.api.vortex.resources.box;

import tools.jackson.databind.JsonNode;

/**
 * 箱子保底奖励(未抽中时填充的资源)，对应 data.filler。
 *
 * @param amount 数量
 * @param type   资源类型，例如 gold/credits
 * @param id     资源ID，可能为 null
 * @author Xun
 */
public record WowsLootBoxFiller(
        int amount,
        String type,
        Long id
) {
    public static WowsLootBoxFiller parse(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return null;
        }
        JsonNode id = node.path("id");
        return new WowsLootBoxFiller(
                node.path("amount").asInt(0),
                node.path("type").asString(""),
                id.isNumber() ? id.asLong() : null
        );
    }
}
