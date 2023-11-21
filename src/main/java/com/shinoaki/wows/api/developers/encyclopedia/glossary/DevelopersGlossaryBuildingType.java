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
public record DevelopersGlossaryBuildingType(
        long building_type_id,
        String name
) {

    public static List<DevelopersGlossaryBuildingType> parse(WowsJsonUtils json, JsonNode node) throws BasicException {
        List<DevelopersGlossaryBuildingType> list = new ArrayList<>();
        var iterator = node.get("building_types").fields();
        while (iterator.hasNext()) {
            Map.Entry<String, JsonNode> map = iterator.next();
            list.add(json.parse(map.getValue().toString(), new TypeReference<DevelopersGlossaryBuildingType>() {
                @Override
                public Type getType() {
                    return super.getType();
                }
            }));
        }
        return list;
    }
}
