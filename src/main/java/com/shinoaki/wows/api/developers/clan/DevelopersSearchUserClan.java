package com.shinoaki.wows.api.developers.clan;

import com.shinoaki.wows.api.error.BasicException;
import com.shinoaki.wows.api.utils.JsonUtils;
import tools.jackson.databind.JsonNode;
 

/**
 * @author Xun
 * @date 2023/5/27 2:43 星期六
 */
public record DevelopersSearchUserClan(Clan clan, long account_id, long joined_at, long clan_id, String role, String account_name) {

    public static DevelopersSearchUserClan parse(  long accountId, String response) throws BasicException {
        JsonNode node =  JsonUtils.json().parse(response);
        BasicException.status(node);
        JsonNode data = node.path("data").get(String.valueOf(accountId));
        if (data.isNull()) {
            return new DevelopersSearchUserClan(Clan.empty(), accountId, 0L, 0L, "", "");
        }
        return new DevelopersSearchUserClan(
                Clan.parse(data.path("clan")),
                data.path("account_id").asLong(),
                data.path("joined_at").asLong(),
                data.path("clan_id").asLong(),
                data.path("role").asString(),
                data.path("account_name").asString());
    }

    public record Clan(int members_count, long created_at, long clan_id, String tag, String name) {
        public static Clan parse(JsonNode node) {
            if (node == null || node.isNull() || node.isEmpty()) {
                return Clan.empty();
            }
            return new Clan(node.path("members_count").asInt(), node.path("created_at").asLong(), node.path("clan_id").asLong(), node.path("tag").asString(),
                    node.path("name").asString());
        }

        public static Clan empty() {
            return new Clan(0, 0, 0, null, null);
        }
    }
}
