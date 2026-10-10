package com.shinoaki.wows.api.vortex.resources.glossary;

import lombok.Data;
import tools.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;

/**
 * 成就分组，对应 getGlossData 的 {@code achievementsGroups[]}。
 *
 * @author Xun
 */
@Data
public class VortexAchievementsGroup {
    private String id;
    private String title;
    private int sortOrder;
    private List<VortexAchievementsSubgroup> subgroups;

    public static List<VortexAchievementsGroup> parseList(JsonNode node) {
        List<VortexAchievementsGroup> list = new ArrayList<>();
        for (JsonNode group : node) {
            VortexAchievementsGroup data = new VortexAchievementsGroup();
            data.setId(group.path("id").asString(""));
            data.setTitle(group.path("title").asString(""));
            data.setSortOrder(group.path("sortOrder").asInt(0));
            data.setSubgroups(VortexAchievementsSubgroup.parseList(group.path("subgroups")));
            list.add(data);
        }
        return list;
    }
}
