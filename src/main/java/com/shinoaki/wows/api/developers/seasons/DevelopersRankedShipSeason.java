package com.shinoaki.wows.api.developers.seasons;

import java.util.Map;

/**
 * 单艘舰船的排位战绩：赛季ID -&gt; 分组键（如 -1 / 0）-&gt; 战斗数据。
 *
 * @author Xun
 */
public record DevelopersRankedShipSeason(long ship_id, long account_id,
                                         Map<Long, Map<Long, DevelopersRankedShipSeasonBlock>> seasons) {
}
