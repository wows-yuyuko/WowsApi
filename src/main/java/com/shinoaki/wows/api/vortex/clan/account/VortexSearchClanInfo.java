package com.shinoaki.wows.api.vortex.clan.account;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * @author Xun
 * @date 2023/5/19 23:34 星期五
 */
public record VortexSearchClanInfo(String tag, String color, int members_count, String name) {

    public static VortexSearchClanInfo to(JsonNode node) {
        if (!node.isEmpty()) {
            return new VortexSearchClanInfo(node.get("tag").asText(), node.get("color").asText(), node.get("members_count").asInt(), node.get("name").asText());
        }
        return empty();
    }

    public static VortexSearchClanInfo empty() {
        return new VortexSearchClanInfo(null, null, 0, null);
    }
}
