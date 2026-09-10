package com.shinoaki.wows.api.vortex.resources.box;

import tools.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;

/**
 * 奖励附加数据，对应奖励项的 additionalData。
 * <p>
 * 普通资源(钢铁/煤炭等)只会有 title/tags；战舰则会带 level/nation/icons/isPremium 等字段。
 *
 * @param title       名称，例如 "India Delta" 或战舰名
 * @param tags        标签
 * @param level       等级，可能为 null
 * @param type        类型
 * @param isPremium   是否为金币/高级
 * @param isSpecial   是否为特殊
 * @param defaultCrew 默认舰长原始数据，可能为 null
 * @param nation      国籍信息，可能为 null
 * @param icons       战舰图标，可能为 null
 * @author Xun
 */
public record WowsLootBoxRewardData(
        String title,
        List<String> tags,
        Integer level,
        String type,
        boolean isPremium,
        boolean isSpecial,
        JsonNode defaultCrew,
        WowsLootBoxNation nation,
        WowsLootBoxShipIcons icons
) {
    public static WowsLootBoxRewardData parse(JsonNode node) {
        List<String> tags = new ArrayList<>();
        for (var tag : node.path("tags")) {
            tags.add(tag.asString());
        }
        JsonNode level = node.path("level");
        JsonNode defaultCrew = node.path("defaultCrew");
        JsonNode nation = node.path("nation");
        JsonNode icons = node.path("icons");
        return new WowsLootBoxRewardData(
                node.path("title").asString(""),
                tags,
                level.isNumber() ? level.asInt() : null,
                node.path("type").asString(""),
                node.path("isPremium").asBoolean(false),
                node.path("isSpecial").asBoolean(false),
                (defaultCrew.isMissingNode() || defaultCrew.isNull()) ? null : defaultCrew,
                (nation.isMissingNode() || nation.isNull()) ? null : WowsLootBoxNation.parse(nation),
                (icons.isMissingNode() || icons.isNull()) ? null : WowsLootBoxShipIcons.parse(icons)
        );
    }
}
