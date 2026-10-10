package com.shinoaki.wows.api.vortex.clan.base.info;

import tools.jackson.databind.JsonNode;

/**
 * 军团招募条件，对应 {@code clanview.clan.recruiting_restrictions}。
 * <p>
 * 例：{@code {"battles_count": 300, "win_rate": 48}}；未设置条件的军团返回空对象。
 *
 * @param battles_count 要求场次
 * @param win_rate      要求胜率
 * @author Xun
 */
public record VortexClanRecruitingRestrictions(Integer battles_count, Integer win_rate) {

    public static VortexClanRecruitingRestrictions parse(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull() || node.isEmpty()) {
            return empty();
        }
        return new VortexClanRecruitingRestrictions(
                node.path("battles_count").isNumber() ? node.path("battles_count").asInt(0) : null,
                node.path("win_rate").isNumber() ? node.path("win_rate").asInt(0) : null);
    }

    public static VortexClanRecruitingRestrictions empty() {
        return new VortexClanRecruitingRestrictions(null, null);
    }
}
