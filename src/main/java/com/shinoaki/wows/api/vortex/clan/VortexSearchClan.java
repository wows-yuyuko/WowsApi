package com.shinoaki.wows.api.vortex.clan;

import com.fasterxml.jackson.core.type.TypeReference;
import com.shinoaki.wows.api.error.BasicException;
import com.shinoaki.wows.api.utils.WowsJsonUtils;

import java.lang.reflect.Type;
import java.util.List;

/**
 * @author Xun
 * @date 2023/5/27 22:23 星期六
 */
public record VortexSearchClan(long id, String tag, String hex_color, String name) {

    public static List<VortexSearchClan> parse(WowsJsonUtils utils, String response) throws BasicException {
        return utils.parse(utils.parse(response).get("search_autocomplete_result").toString(), new TypeReference<List<VortexSearchClan>>() {
            @Override
            public Type getType() {
                return super.getType();
            }
        });
    }
}
