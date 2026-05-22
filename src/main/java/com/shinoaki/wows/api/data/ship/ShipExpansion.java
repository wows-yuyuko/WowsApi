package com.shinoaki.wows.api.data.ship;

import com.shinoaki.wows.api.error.BasicException;
import com.shinoaki.wows.api.vortex.ship.VortexShipStatistics;
import tools.jackson.databind.JsonNode;

import java.util.HashMap;
import java.util.Map;

public record ShipExpansion(int topGrade) {

    public static ShipExpansion of(VortexShipStatistics shipStatistics) {
        return new ShipExpansion(topGradeOf(shipStatistics.masterySign()));
    }

    public static Map<Long, ShipExpansion> parseDevelopers(long accountId, JsonNode node) throws BasicException {
        BasicException.status(node);
        Map<Long, ShipExpansion> map = new HashMap<>();
        for (var data : node.get("data").get(String.valueOf(accountId))) {
            map.put(data.get("ship_id").asLong(0L), new ShipExpansion(data.get("top_grade_class").asInt(5)));
        }
        return map;
    }

    public static ShipExpansion empty() {
        return new ShipExpansion(5);
    }

    private static int topGradeOf(String s) {
        return switch (s) {
            case "Sign_M" -> 1;
            case "Sign_1" -> 2;
            case "Sign_2" -> 3;
            case "Sign_3" -> 4;
            default -> 5;
        };
    }
}
