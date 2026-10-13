package com.shinoaki.wows.api.vortex.clan;


import com.shinoaki.wows.api.error.BasicException;
import com.shinoaki.wows.api.utils.JsonUtils;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;

import java.util.List;

/**
 * @author Xun
 * @date 2023/5/27 22:23 星期六
 */
public record VortexSearchClan(long id, String tag, String hex_color, String name) {

    public static List<VortexSearchClan> parse(String response) throws BasicException {
        return parse(JsonUtils.json().parse(response));
    }

    public static List<VortexSearchClan> parse(JsonNode node) throws BasicException {
        return JsonUtils.json().parse(node.path("search_autocomplete_result"), new TypeReference<List<VortexSearchClan>>() {
        });
    }
}
