package com.shinoaki.wows.api.developers.clan.seasion;


import com.shinoaki.wows.api.error.BasicException;
import com.shinoaki.wows.api.utils.JsonUtils;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;

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
    public static List<DevelopersSeasonInfo> parse(String jsonData) throws BasicException {
        return parse(JsonUtils.json().parse(jsonData));
    }

    public static List<DevelopersSeasonInfo> parse(JsonNode node) throws BasicException {
        BasicException.status(node);
        List<DevelopersSeasonInfo> infos = new ArrayList<>();
        for (var data : node.path("data")) {
            infos.add(JsonUtils.json().parse(data, new TypeReference<DevelopersSeasonInfo>() {
            }));
        }
        return infos;
    }
}
