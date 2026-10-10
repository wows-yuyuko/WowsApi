package com.shinoaki.wows.api.vortex.resources.glossary;

import lombok.Data;
import tools.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;

/**
 * 国家，对应 getGlossData 的 {@code nations[]}。
 *
 * @author Xun
 */
@Data
public class VortexNation {
    private String name;
    private String title;
    private String color;
    private VortexNationIcons icons;

    public static List<VortexNation> parseList(JsonNode node) {
        List<VortexNation> list = new ArrayList<>();
        for (JsonNode nation : node) {
            VortexNation data = new VortexNation();
            data.setName(nation.path("name").asString(""));
            data.setTitle(nation.path("title").asString(""));
            data.setColor(nation.path("color").asString(""));
            data.setIcons(VortexNationIcons.parse(nation.path("icons")));
            list.add(data);
        }
        return list;
    }
}
