package com.shinoaki.wows.api.vortex.resources.glossary;

import lombok.Data;
import tools.jackson.databind.JsonNode;

/**
 * 舰种图标，对应 getGlossData 的 {@code vehicleTypes[].icons}。
 *
 * @author Xun
 */
@Data
public class VortexVehicleTypeIcons {
    private String defaultUrl;
    private String specialUrl;
    private String normalUrl;
    private String eliteUrl;
    private String premiumUrl;

    public static VortexVehicleTypeIcons parse(JsonNode node) {
        VortexVehicleTypeIcons icons = new VortexVehicleTypeIcons();
        icons.setDefaultUrl(node.path("default").asString(""));
        icons.setSpecialUrl(node.path("special").asString(""));
        icons.setNormalUrl(node.path("normal").asString(""));
        icons.setEliteUrl(node.path("elite").asString(""));
        icons.setPremiumUrl(node.path("premium").asString(""));
        return icons;
    }
}
