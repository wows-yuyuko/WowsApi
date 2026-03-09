package com.shinoaki.wows.api.developers.clan.seasion;


import com.shinoaki.wows.api.error.BasicException;
import com.shinoaki.wows.api.utils.JsonUtils;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Xun
 */
public record DevelopersSeasonInfo(
        List<DevelopersSeasonLeaguesInfo> leagues,
        String name,
        int ship_tier_max,
        long finish_time,
        int ship_tier_min,
        long start_time,
        int division_points,
        int season_id
) {
    public static List<DevelopersSeasonInfo> parse(  String jsonData) throws BasicException {
        JsonNode node = JsonUtils.json().parse(jsonData);
        BasicException.status(node);
        List<DevelopersSeasonInfo> infos = new ArrayList<>();
        for (var data : node.get("data")) {
            infos.add(JsonUtils.json().parse(data.toString(), new TypeReference<DevelopersSeasonInfo>() {
                @Override
                public Type getType() {
                    return super.getType();
                }
            }));
        }
        return infos;
    }
}
