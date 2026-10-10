package com.shinoaki.wows.api.vortex.resources.glossary;

import lombok.Data;
import tools.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;

/**
 * 成就子分组，对应 getGlossData 的 {@code achievementsGroups[].subgroups[]}。
 *
 * @author Xun
 */
@Data
public class VortexAchievementsSubgroup {
    private String id;
    private String title;
    private int sortOrder;

    public static List<VortexAchievementsSubgroup> parseList(JsonNode node) {
        List<VortexAchievementsSubgroup> list = new ArrayList<>();
        for (JsonNode subgroup : node) {
            VortexAchievementsSubgroup data = new VortexAchievementsSubgroup();
            data.setId(subgroup.path("id").asString(""));
            data.setTitle(subgroup.path("title").asString(""));
            data.setSortOrder(subgroup.path("sortOrder").asInt(0));
            list.add(data);
        }
        return list;
    }
}
