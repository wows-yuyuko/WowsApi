package com.shinoaki.wows.api.developers.warships.type;

import tools.jackson.databind.JsonNode;

import java.util.TreeMap;

public record DevelopersOperBattleType(
        long xp, int battles, int survived_wins, int survived_battles, int wins, int losses,
        TreeMap<Integer, Integer> wins_by_tasks
) {

    public static DevelopersOperBattleType empty() {
        return new DevelopersOperBattleType(0, 0, 0, 0, 0, 0, new TreeMap<>());
    }

    public static DevelopersOperBattleType parse(JsonNode node) {
        TreeMap<Integer, Integer> tasksMap = new TreeMap<>();
        for (var tasks : node.get("wins_by_tasks").properties()) {
            tasksMap.put(Integer.parseInt(tasks.getKey()), tasks.getValue().asInt(0));
        }
        return new DevelopersOperBattleType(node.get("xp").asLong(0L),
                node.get("battles").asInt(0),
                node.get("survived_wins").asInt(0),
                node.get("survived_battles").asInt(0),
                node.get("wins").asInt(0),
                node.get("losses").asInt(0),
                tasksMap);
    }
}
