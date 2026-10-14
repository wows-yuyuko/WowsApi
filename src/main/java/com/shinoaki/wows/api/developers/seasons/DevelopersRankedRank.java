package com.shinoaki.wows.api.developers.seasons;

/**
 * 排位段位定义（stars_to_next / star_loss_place 对保星段位不返回，解析为 0）。
 *
 * @author Xun
 */
public record DevelopersRankedRank(boolean is_save_point, int stars_to_next, int star_loss_place,
                                   DevelopersRankedRankImages images) {
}
