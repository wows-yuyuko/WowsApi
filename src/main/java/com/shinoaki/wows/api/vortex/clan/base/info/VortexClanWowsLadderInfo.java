package com.shinoaki.wows.api.vortex.clan.base.info;

import com.fasterxml.jackson.databind.JsonNode;
import com.shinoaki.wows.api.utils.WowsUtils;

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
                WowsUtils.json(node.get("total_battles_count"), 0),
                WowsUtils.json(node.get("league"), 0),
                WowsUtils.json(node.get("members_count"), 0),
                WowsUtils.json(node.get("prime_time"), null),
                WowsUtils.json(node.get("last_battle_at"), null),
                WowsUtils.json(node.get("division_rating"), 0),
                WowsUtils.json(node.get("season_number"), 0),
                WowsUtils.json(node.get("initial_public_rating"), 0),
                WowsUtils.json(node.get("rating_realm"), null),
                WowsUtils.json(node.get("team_number"), 0),
                WowsUtils.json(node.get("planned_prime_time"), 0),
                WowsUtils.json(node.get("last_win_at"), null),
                WowsUtils.json(node.get("battles_count"), 0),
                WowsUtils.json(node.get("division_rating_max"), 0),
                VortexClanLeagueInfo.clan(clanId, node.get("ratings")),
                WowsUtils.json(node.get("realm"), null),
                WowsUtils.json(node.get("id"), 0),
                WowsUtils.json(node.get("current_winning_streak"), 0),
                WowsUtils.json(node.get("division"), 0),
                WowsUtils.json(node.get("is_banned"), false),
                WowsUtils.json(node.get("public_rating"), 0),
                WowsUtils.json(node.get("color"), 0),
                WowsUtils.json(node.get("is_qualified"), false),
                WowsUtils.json(node.get("longest_winning_streak"), 0),
                WowsUtils.json(node.get("max_public_rating"), 0),
                WowsUtils.json(node.get("status"), null),
                WowsUtils.json(node.get("wins_count"), 0),
                WowsUtils.json(node.get("is_disbanded"), false),
                WowsUtils.json(node.get("is_best_season_rating"), false),
                node.get("max_position"),
                WowsUtils.json(node.get("leading_team_number"), 0)
        );
    }
}
