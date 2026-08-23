package com.shinoaki.wows.api.developers.clan;


import com.shinoaki.wows.api.error.BasicException;
import com.shinoaki.wows.api.utils.JsonUtils;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;

import java.util.List;

/**
 * @author Xun
 * @date 2023/5/27 2:15 星期六
 */
public record DevelopersSearchClan(int members_count, long created_at, long clan_id, String tag, String name) {
    public static List<DevelopersSearchClan> parse(String response) throws BasicException {
        JsonNode node = JsonUtils.json().parse(response);
        BasicException.status(node);
        return JsonUtils.json().parse(node.path("data").toString(), new TypeReference<List<DevelopersSearchClan>>() {
        });
    }
}
