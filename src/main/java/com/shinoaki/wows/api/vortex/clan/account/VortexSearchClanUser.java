package com.shinoaki.wows.api.vortex.clan.account;

import com.fasterxml.jackson.databind.JsonNode;
import com.shinoaki.wows.api.codec.HttpSend;
import com.shinoaki.wows.api.error.StatusException;

/**
 * https://vortex.worldofwarships.asia/api/accounts/2022515210/clans/
 *
 * @author Xun
 * @date 2023/5/19 23:34 星期五
 */
public record VortexSearchClanUser(String role, VortexSearchClanInfo clan, String joined_at, long clan_id) {

    public static VortexSearchClanUser to(JsonNode node) throws StatusException {
        StatusException.status(node);
        JsonNode data = node.get("data");
        JsonNode clanId = data.get("clan_id");
        return new VortexSearchClanUser(data.get("role").asText(),
                VortexSearchClanInfo.to(data.get("clan")),
                data.get("joined_at").asText(),
                clanId.isNull() ? 0 : clanId.asLong());
    }
}
