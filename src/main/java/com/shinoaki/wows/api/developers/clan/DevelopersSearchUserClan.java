package com.shinoaki.wows.api.developers.clan;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.shinoaki.wows.api.error.StatusException;
import com.shinoaki.wows.api.utils.JsonUtils;

/**
 * @author Xun
 * @date 2023/5/27 2:43 星期六
 */
public record DevelopersSearchUserClan(Clan clan, long account_id, long joined_at, long clan_id, String role, String account_name) {

    public static DevelopersSearchUserClan parse(JsonUtils utils, long accountId, String response) throws JsonProcessingException, StatusException {
        JsonNode node = utils.parse(response);
        StatusException.status(node);
        JsonNode data = node.get("data").get(String.valueOf(accountId));
        if (data.isNull()) {
            return new DevelopersSearchUserClan(Clan.empty(), accountId, 0L, 0L, "", "");
        }
        return new DevelopersSearchUserClan(
                Clan.parse(data.get("clan")),
                data.get("account_id").asLong(),
                data.get("joined_at").asLong(),
                data.get("clan_id").asLong(),
                data.get("role").asText(),
                data.get("account_name").asText());
    }

    public record Clan(int members_count, long created_at, long clan_id, String tag, String name) {
        public static Clan parse(JsonNode node) {
            if (node == null || node.isNull() || node.isEmpty()) {
                return Clan.empty();
            }
            return new Clan(node.get("members_count").asInt(), node.get("created_at").asLong(), node.get("clan_id").asLong(), node.get("tag").asText(),
                    node.get("name").asText());
        }

        public static Clan empty() {
            return new Clan(0, 0, 0, null, null);
        }
    }
}
