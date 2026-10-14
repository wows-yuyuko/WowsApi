package com.shinoaki.wows.api.developers.seasons;

import java.util.Map;

/**
 * 排位联赛：段位区间、起始段位与各段位定义。
 *
 * @author Xun
 */
public record DevelopersRankedLeague(int min_ship_tier, int max_ship_tier, int ship_total_limit, int start_rank,
                                     String league_name, DevelopersRankedLeagueImages images,
                                     Map<Long, DevelopersRankedRank> ranks) {
}
