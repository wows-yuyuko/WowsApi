package com.shinoaki.wows.api.vortex.resources.glossary;

import lombok.Data;
import tools.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;

/**
 * 舰种(驱逐/巡洋/战列/航母/潜艇)，对应 getGlossData 的 {@code vehicleTypes[]}。
 *
 * @author Xun
 */
@Data
public class VortexVehicleType {
    private String name;
    private String title;
    private VortexVehicleTypeIcons icons;

    public static List<VortexVehicleType> parseList(JsonNode node) {
        List<VortexVehicleType> list = new ArrayList<>();
        for (JsonNode type : node) {
            VortexVehicleType data = new VortexVehicleType();
            data.setName(type.path("name").asString(""));
            data.setTitle(type.path("title").asString(""));
            data.setIcons(VortexVehicleTypeIcons.parse(type.path("icons")));
            list.add(data);
        }
        return list;
    }
}
