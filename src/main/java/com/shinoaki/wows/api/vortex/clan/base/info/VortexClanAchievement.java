package com.shinoaki.wows.api.vortex.clan.base.info;

import tools.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;

/**
 * 军团成就，对应 {@code /api/clanbase/{id}/claninfo/} 中 {@code clanview.achievements[]}。
 * <p>
 * 例：{@code {"count": 5, "cd": 4042449840}}
 *
 * @param cd    成就ID
 * @param count 完成次数
 * @author Xun
 */
public record VortexClanAchievement(long cd, int count) {

    public static List<VortexClanAchievement> parse(JsonNode node) {
        List<VortexClanAchievement> list = new ArrayList<>();
        if (node == null || node.isMissingNode() || node.isNull()) {
            return list;
        }
        for (JsonNode achievement : node) {
            list.add(new VortexClanAchievement(achievement.path("cd").asLong(0), achievement.path("count").asInt(0)));
        }
        return list;
    }
}
