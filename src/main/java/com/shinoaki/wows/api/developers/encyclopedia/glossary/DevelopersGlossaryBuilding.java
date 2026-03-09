package com.shinoaki.wows.api.developers.encyclopedia.glossary;


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
public record DevelopersGlossaryBuilding(
        String name,
        int bonus_value,
        String ship_tier,
        String bonus_type,
        long cost,
        String ship_type,
        long building_type_id,
        long building_id,
        String ship_nation
) {

    public static List<DevelopersGlossaryBuilding> parse(JsonNode node) throws BasicException {
        List<DevelopersGlossaryBuilding> list = new ArrayList<>();
        for (var map : node.get("buildings").properties()) {
            list.add(JsonUtils.json().parse(map.getValue().toString(), new TypeReference<DevelopersGlossaryBuilding>() {
                @Override
                public Type getType() {
                    return super.getType();
                }
            }));
        }
        return list;
    }
}
