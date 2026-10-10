package com.shinoaki.wows.api.vortex.resources.glossary;

import lombok.Data;
import tools.jackson.databind.JsonNode;

/**
 * 成就分组信息，对应 getGlossData 的 {@code achievements[].grouping}。
 *
 * @author Xun
 */
@Data
public class VortexAchievementGrouping {
    private int sortOrder;
    private int sortOrderInGroup;
    private String group;
    private String subgroup;

    public static VortexAchievementGrouping parse(JsonNode node) {
        VortexAchievementGrouping grouping = new VortexAchievementGrouping();
        grouping.setSortOrder(node.path("sortOrder").asInt(0));
        grouping.setSortOrderInGroup(node.path("sortOrderInGroup").asInt(0));
        grouping.setGroup(node.path("group").asString(""));
        grouping.setSubgroup(node.path("subgroup").asString(""));
        return grouping;
    }
}
