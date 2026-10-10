package com.shinoaki.wows.api.developers.encyclopedia.glossary;

import tools.jackson.databind.JsonNode;

/**
 * 军团设置，对应 glossary 的 {@code data.settings}。
 *
 * @param max_members_count 军团人数上限
 * @author Xun
 */
public record DevelopersGlossarySettings(int max_members_count) {

    public static DevelopersGlossarySettings parse(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return empty();
        }
        return new DevelopersGlossarySettings(node.path("max_members_count").asInt(0));
    }

    public static DevelopersGlossarySettings empty() {
        return new DevelopersGlossarySettings(0);
    }
}
