package com.shinoaki.wows.api.developers.encyclopedia.glossary;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.shinoaki.wows.api.error.BasicException;
import com.shinoaki.wows.api.utils.WowsJsonUtils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    public static List<DevelopersGlossaryBuilding> parse(WowsJsonUtils json, JsonNode node) throws BasicException {
        List<DevelopersGlossaryBuilding> list = new ArrayList<>();
        var iterator = node.get("buildings").fields();
        while (iterator.hasNext()) {
            Map.Entry<String, JsonNode> map = iterator.next();
            list.add(json.parse(map.getValue().toString(), new TypeReference<DevelopersGlossaryBuilding>() {
                @Override
                public Type getType() {
                    return super.getType();
                }
            }));
        }
        return list;
    }
}
