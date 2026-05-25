package com.shinoaki.wows.api.developers.encyclopedia.glossary;

import com.shinoaki.wows.api.error.BasicException;
import com.shinoaki.wows.api.utils.JsonUtils;

import java.util.List;

/**
 * @author Xun
 */
public record DevelopersGlossary(
        List<DevelopersGlossaryBuildingType> building_types,
        List<DevelopersGlossaryBuilding> buildings
) {

    public static DevelopersGlossary parse(String jsonData) throws BasicException {
        var node = JsonUtils.json().parse(jsonData);
        BasicException.status(node);
        var data = node.path("data");
        return new DevelopersGlossary(DevelopersGlossaryBuildingType.parse(data),
                DevelopersGlossaryBuilding.parse(data));
    }
}
