package com.shinoaki.wows.api.developers.clan;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.shinoaki.wows.api.error.BasicException;
import com.shinoaki.wows.api.utils.WowsJsonUtils;

import java.lang.reflect.Type;
import java.util.List;

/**
 * @author Xun
 * @date 2023/5/27 2:15 星期六
 */
public record DevelopersSearchClan(int members_count, long created_at, long clan_id, String tag, String name) {
    public static List<DevelopersSearchClan> parse(WowsJsonUtils utils, String response) throws BasicException {
        JsonNode node = utils.parse(response);
        BasicException.status(node);
        return utils.parse(node.get("data").toString(), new TypeReference<List<DevelopersSearchClan>>() {
            @Override
            public Type getType() {
                return super.getType();
            }
        });
    }
}
