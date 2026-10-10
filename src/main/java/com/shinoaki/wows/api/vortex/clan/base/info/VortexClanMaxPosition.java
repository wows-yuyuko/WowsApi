package com.shinoaki.wows.api.vortex.clan.base.info;

import tools.jackson.databind.JsonNode;

/**
 * 军团最高段位，对应 {@code wows_ladder.max_position} / {@code wows_ladder.ratings[].max_position}。
 * <p>
 * 例：{@code {"league": 4, "public_rating": 1100, "division": 2, "division_rating": 0}}
 *
 * @param league          联盟等级(0 表示紫会)
 * @param public_rating   公开评级
 * @param division        段位
 * @param division_rating 段位分数
 * @author Xun
 */
public record VortexClanMaxPosition(int league, int public_rating, int division, int division_rating) {

    public static VortexClanMaxPosition parse(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull() || !node.isObject()) {
            return empty();
        }
        return new VortexClanMaxPosition(node.path("league").asInt(0),
                node.path("public_rating").asInt(0),
                node.path("division").asInt(0),
                node.path("division_rating").asInt(0));
    }

    public static VortexClanMaxPosition empty() {
        return new VortexClanMaxPosition(0, 0, 0, 0);
    }
}
