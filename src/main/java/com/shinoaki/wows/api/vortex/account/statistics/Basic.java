package com.shinoaki.wows.api.vortex.account.statistics;

import tools.jackson.databind.JsonNode;

/**
 * 账号基础信息，对应 {@code statistics.basic}。
 *
 * @param leveling_tier   账号等级
 * @param created_at      创建时间(Unix秒)
 * @param leveling_points 升级点数
 * @param karma            karma值
 * @param last_battle_time 最后战斗时间(Unix秒)
 * @param mastery_rank    精通点(莱服独有，替代其它服的 {@code statistics.mastery_sign})
 */
public record Basic(
        int leveling_tier,
        long created_at,
        int leveling_points,
        int karma,
        long last_battle_time,
        long mastery_rank
) {

    public static Basic parse(JsonNode node) {
        return new Basic(node.path("leveling_tier").asInt(),
                node.path("created_at").asLong(),
                node.path("leveling_points").asInt(),
                node.path("karma").asInt(),
                node.path("last_battle_time").asLong(),
                node.path("mastery_rank").asLong(0));
    }

    public static Basic empty() {
        return new Basic(0, 0, 0, 0, 0, 0);
    }
}
