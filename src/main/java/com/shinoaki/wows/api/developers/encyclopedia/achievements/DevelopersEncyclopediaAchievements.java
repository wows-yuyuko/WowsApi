package com.shinoaki.wows.api.developers.encyclopedia.achievements;

import com.shinoaki.wows.api.error.BasicException;
import com.shinoaki.wows.api.utils.JsonUtils;
import tools.jackson.databind.JsonNode;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * /wows/encyclopedia/achievements/ 成就字典：成就ID -&gt; 信息。
 * <p>
 * battle = 战斗成就，progress = 进度类成就（响应可能不返回，解析为空表）。
 *
 * @author Xun
 */
public record DevelopersEncyclopediaAchievements(Map<String, DevelopersEncyclopediaAchievement> battle,
                                                 Map<String, DevelopersEncyclopediaAchievement> progress) {

    public static DevelopersEncyclopediaAchievements parse(JsonNode node) throws BasicException {
        BasicException.status(node);
        JsonNode data = node.path("data");
        return new DevelopersEncyclopediaAchievements(achievements(data.path("battle")), achievements(data.path("progress")));
    }

    private static Map<String, DevelopersEncyclopediaAchievement> achievements(JsonNode node) {
        Map<String, DevelopersEncyclopediaAchievement> map = new LinkedHashMap<>();
        if (node == null || !node.isObject()) {
            return map;
        }
        for (var entry : node.properties()) {
            map.put(entry.getKey(), JsonUtils.json().parse(entry.getValue(), DevelopersEncyclopediaAchievement.class));
        }
        return map;
    }
}
