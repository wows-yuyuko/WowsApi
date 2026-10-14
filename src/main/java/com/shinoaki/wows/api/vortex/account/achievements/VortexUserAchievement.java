package com.shinoaki.wows.api.vortex.account.achievements;

import tools.jackson.databind.JsonNode;

/**
 * 单条成就的获得情况。
 * <p>
 * {@code counter_d} 仅莱服(korabli.su)出现（实测全 0），其它服缺失、按 0 兜底。
 *
 * @param count     获得次数
 * @param ship_id   关联战舰ID（0 = 无）
 * @param last_time 最近获得时间（Unix 秒）
 * @param counter_a 成就计数器A（部分成就非 0）
 * @param counter_d 成就计数器D（莱服）
 * @author Xun
 */
public record VortexUserAchievement(long count, long ship_id, long last_time, long counter_a, long counter_d) {

    public static VortexUserAchievement parse(JsonNode node) {
        return new VortexUserAchievement(
                node.path("count").asLong(0),
                node.path("ship_id").asLong(0),
                node.path("last_time").asLong(0),
                node.path("counter_a").asLong(0),
                node.path("counter_d").asLong(0));
    }
}
