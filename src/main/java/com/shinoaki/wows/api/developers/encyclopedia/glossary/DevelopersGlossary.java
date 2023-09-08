package com.shinoaki.wows.api.developers.encyclopedia.glossary;

import com.shinoaki.wows.api.error.BasicException;
import com.shinoaki.wows.api.utils.WowsJsonUtils;

import java.util.List;

/**
 * @author Xun
 */
public record DevelopersGlossary(
        List<DevelopersGlossaryBuildingType> building_types,
        List<DevelopersGlossaryBuilding> buildings
) {

    public static DevelopersGlossary parse(WowsJsonUtils json, String jsonData) throws BasicException {
        var node = json.parse(jsonData);
        BasicException.status(node);
        var data = node.get("data");
        return new DevelopersGlossary(DevelopersGlossaryBuildingType.parse(json, data),
                DevelopersGlossaryBuilding.parse(json, data));
    }
}
