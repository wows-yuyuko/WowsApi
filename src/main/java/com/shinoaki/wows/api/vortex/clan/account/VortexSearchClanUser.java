package com.shinoaki.wows.api.vortex.clan.account;

import com.shinoaki.wows.api.error.BasicException;
import tools.jackson.databind.JsonNode;

/**
 * https://vortex.worldofwarships.asia/api/accounts/2022515210/clans/
 *
 * @author Xun
 * @date 2023/5/19 23:34 星期五
 */
public record VortexSearchClanUser(String role, VortexSearchClanInfo clan, String joined_at, long clan_id) {

    public static VortexSearchClanUser to(JsonNode node) throws BasicException {
        BasicException.status(node);
        JsonNode data = node.path("data");
        JsonNode clanId = data.path("clan_id");
        return new VortexSearchClanUser(data.path("role").asString(),
                VortexSearchClanInfo.to(data.path("clan")),
                data.path("joined_at").asString(),
                clanId.isNull() ? 0 : clanId.asLong());
    }


    public record VortexSearchClanInfo(String tag, String color, int members_count, String name) {

        public static VortexSearchClanInfo to(JsonNode node) {
            if (!node.isEmpty()) {
                return new VortexSearchClanInfo(node.path("tag").asString(), "#" + Long.toHexString(node.path("color").asLong()), node.path("members_count").asInt(),
                        node.path("name").asString());
            }
            return empty();
        }

        public static VortexSearchClanInfo empty() {
            return new VortexSearchClanInfo(null, null, 0, null);
        }
    }
}
