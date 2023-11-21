package com.shinoaki.wows.api.developers.clan.seasion;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.shinoaki.wows.api.error.BasicException;
import com.shinoaki.wows.api.utils.WowsJsonUtils;

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
    public static List<DevelopersSeasonInfo> parse(WowsJsonUtils json, String jsonData) throws BasicException {
        JsonNode node = json.parse(jsonData);
        BasicException.status(node);
        List<DevelopersSeasonInfo> infos = new ArrayList<>();
        for (var data : node.get("data")) {
            infos.add(json.parse(data.toString(), new TypeReference<DevelopersSeasonInfo>() {
                @Override
                public Type getType() {
                    return super.getType();
                }
            }));
        }
        return infos;
    }
}
