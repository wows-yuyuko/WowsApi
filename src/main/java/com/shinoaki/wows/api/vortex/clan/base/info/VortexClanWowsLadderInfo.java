package com.shinoaki.wows.api.vortex.clan.base.info;

import com.shinoaki.wows.api.utils.WowsUtils;
import tools.jackson.databind.JsonNode;

import java.util.List;

public record VortexClanWowsLadderInfo(
        int total_battles_count,
        int league,
        int members_count,
        String prime_time,
        String last_battle_at,
        int division_rating,
        int season_number,
        int initial_public_rating,
        String rating_realm,
        int team_number,
        int planned_prime_time,
        String last_win_at,
        int battles_count,
        int division_rating_max,
        List<VortexClanLeagueInfo> ratings,
        String realm,
        long id,
        int current_winning_streak,
        int division,
        boolean is_banned,
        int public_rating,
        int color,
        boolean is_qualified,
        int longest_winning_streak,
        int max_public_rating,
        String status,
        int wins_count,
        boolean is_disbanded,
        boolean is_best_season_rating,
        JsonNode max_position,
        int leading_team_number
) {

    public static VortexClanWowsLadderInfo parse(long clanId, final JsonNode node) {
        return new VortexClanWowsLadderInfo(
                WowsUtils.json(node.path("total_battles_count"), 0),
                WowsUtils.json(node.path("league"), 0),
                WowsUtils.json(node.path("members_count"), 0),
                WowsUtils.json(node.path("prime_time"), null),
                WowsUtils.json(node.path("last_battle_at"), null),
                WowsUtils.json(node.path("division_rating"), 0),
                WowsUtils.json(node.path("season_number"), 0),
                WowsUtils.json(node.path("initial_public_rating"), 0),
                WowsUtils.json(node.path("rating_realm"), null),
                WowsUtils.json(node.path("team_number"), 0),
                WowsUtils.json(node.path("planned_prime_time"), 0),
                WowsUtils.json(node.path("last_win_at"), null),
                WowsUtils.json(node.path("battles_count"), 0),
                WowsUtils.json(node.path("division_rating_max"), 0),
                VortexClanLeagueInfo.clan(clanId, node.path("ratings")),
                WowsUtils.json(node.path("realm"), null),
                WowsUtils.jsonLong(node.path("id"), 0),
                WowsUtils.json(node.path("current_winning_streak"), 0),
                WowsUtils.json(node.path("division"), 0),
                WowsUtils.json(node.path("is_banned"), false),
                WowsUtils.json(node.path("public_rating"), 0),
                WowsUtils.json(node.path("color"), 0),
                WowsUtils.json(node.path("is_qualified"), false),
                WowsUtils.json(node.path("longest_winning_streak"), 0),
                WowsUtils.json(node.path("max_public_rating"), 0),
                WowsUtils.json(node.path("status"), null),
                WowsUtils.json(node.path("wins_count"), 0),
                WowsUtils.json(node.path("is_disbanded"), false),
                WowsUtils.json(node.path("is_best_season_rating"), false),
                node.path("max_position"),
                WowsUtils.json(node.path("leading_team_number"), 0)
        );
    }
}
