package com.shinoaki.wows.api.utils;

import com.fasterxml.jackson.databind.JsonNode;

public class WowsUtils {
    private WowsUtils(){

    }

    public static int json(JsonNode node, int defaultValue) {
        if (node == null) {
            return defaultValue;
        }
        return node.asInt(defaultValue);
    }

    public static long jsonLong(JsonNode node, long defaultValue) {
        if (node == null) {
            return defaultValue;
        }
        return node.asLong(defaultValue);
    }

    public static boolean json(JsonNode node, boolean defaultValue) {
        if (node == null) {
            return defaultValue;
        }
        return node.asBoolean(defaultValue);
    }

    public static String json(JsonNode node, String defaultValue) {
        if (node == null) {
            return defaultValue;
        }
        return node.asText(defaultValue);
    }
}
