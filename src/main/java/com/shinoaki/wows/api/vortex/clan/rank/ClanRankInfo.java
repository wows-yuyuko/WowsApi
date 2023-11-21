package com.shinoaki.wows.api.vortex.clan.rank;

/**
 * @author Xun
 */
public record ClanRankInfo(
        boolean disbanded,
        int rank,
        String realm,
        int members_count,
        String name,
        int division,
        int public_rating,
        int division_rating,
        int league,
        int battles_count,
        String hex_color,
        String last_win_at,
        String rating_realm,
        String last_battle_at,
        int season_number,
        long id,
        int leading_team_number,
        String color,
        String tag
) {
}
