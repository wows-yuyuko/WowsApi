package com.shinoaki.wows.api.developers.seasons;

/**
 * 排位段位进度（某赛季/联赛/冲刺下的段位信息）。
 *
 * @author Xun
 */
public record DevelopersRankedRankInfo(int rank, int rank_best, int stars, int stage, int best_rank_in_sprint,
                                       int victories, int battles, int max_stars_total, int qualification_status,
                                       int sprint_number) {
}
