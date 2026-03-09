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
public record DevelopersGlossaryBuildingType(
        long building_type_id,
        String name
) {

    public static List<DevelopersGlossaryBuildingType> parse(JsonNode node) throws BasicException {
        List<DevelopersGlossaryBuildingType> list = new ArrayList<>();
        for (var map : node.get("building_types").properties()) {
            list.add(JsonUtils.json().parse(map.getValue().toString(), new TypeReference<DevelopersGlossaryBuildingType>() {
                @Override
                public Type getType() {
                    return super.getType();
                }
            }));
        }
        return list;
    }
}
