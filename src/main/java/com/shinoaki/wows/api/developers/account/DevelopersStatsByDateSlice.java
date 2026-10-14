package com.shinoaki.wows.api.developers.account;

/**
 * /wows/account/statsbydate/ 的单日统计切片。
 *
 * @author Xun
 */
public record DevelopersStatsByDateSlice(long account_id, String battle_type, String date, long battles, long wins,
                                         long damage_dealt, long frags, long xp, long max_xp, long capture_points,
                                         long dropped_capture_points, long survived_battles, long planes_killed) {
}
