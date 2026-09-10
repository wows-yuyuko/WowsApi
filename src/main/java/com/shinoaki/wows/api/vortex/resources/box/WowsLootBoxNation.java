package com.shinoaki.wows.api.vortex.resources.box;

import tools.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;

/**
 * 奖励所属国籍信息，对应 additionalData.nation。
 *
 * @param id           国籍ID
 * @param name         国籍代号，例如 uk/germany/pan_asia
 * @param color        颜色值
 * @param tags         标签
 * @param localization 多语言文本原始数据
 * @param icons        国籍旗帜图标
 * @author Xun
 */
public record WowsLootBoxNation(
        long id,
        String name,
        int color,
        List<String> tags,
        JsonNode localization,
        WowsLootBoxNationIcon icons
) {
    public static WowsLootBoxNation parse(JsonNode node) {
        List<String> tags = new ArrayList<>();
        for (var tag : node.path("tags")) {
            tags.add(tag.asString());
        }
        JsonNode localization = node.path("localization");
        JsonNode icons = node.path("icons");
        return new WowsLootBoxNation(
                node.path("id").asLong(0),
                node.path("name").asString(""),
                node.path("color").asInt(0),
                tags,
                (localization.isMissingNode() || localization.isNull()) ? null : localization,
                (icons.isMissingNode() || icons.isNull()) ? null : WowsLootBoxNationIcon.parse(icons)
        );
    }
}
