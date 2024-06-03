package com.shinoaki.wows.api.vortex.account.statistics;

import com.fasterxml.jackson.databind.JsonNode;

public record Basic(
        int leveling_tier,
        long created_at,
        int leveling_points,
        int karma,
        long last_battle_time
) {

    public static Basic parse(JsonNode node) {
        return new Basic(node.get("leveling_tier").asInt(),
                node.get("created_at").asLong(),
                node.get("leveling_points").asInt(),
                node.get("karma").asInt(),
                node.get("last_battle_time").asLong());
    }

    public static Basic empty() {
        return new Basic(0,0,0,0,0);
    }
}
