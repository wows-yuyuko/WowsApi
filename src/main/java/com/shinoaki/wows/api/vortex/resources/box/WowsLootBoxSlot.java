package com.shinoaki.wows.api.vortex.resources.box;

import tools.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 箱子槽位，对应 data.slots[]。
 * <p>
 * 一个槽位内包含普通奖励(commonRewards)与高价值奖励(valuableRewards)，
 * 二者都是 {@code list0/list1...} 形式的奖励分组。
 *
 * @param title              槽位标题
 * @param continuousRewards  是否连续奖励
 * @param commonRewards      普通奖励分组，key 为 list0/list1...
 * @param valuableRewards    高价值奖励分组，key 为 list0/list1...
 * @author Xun
 */
public record WowsLootBoxSlot(
        String title,
        boolean continuousRewards,
        Map<String, WowsLootBoxRewardGroup> commonRewards,
        Map<String, WowsLootBoxRewardGroup> valuableRewards
) {
    public static List<WowsLootBoxSlot> parseList(JsonNode node) {
        List<WowsLootBoxSlot> list = new ArrayList<>();
        for (var slot : node) {
            list.add(new WowsLootBoxSlot(
                    slot.path("title").asString(""),
                    slot.path("continuousRewards").asBoolean(false),
                    parseGroups(slot.path("commonRewards")),
                    parseGroups(slot.path("valuableRewards"))
            ));
        }
        return list;
    }

    private static Map<String, WowsLootBoxRewardGroup> parseGroups(JsonNode node) {
        Map<String, WowsLootBoxRewardGroup> map = new LinkedHashMap<>();
        for (var entry : node.properties()) {
            map.put(entry.getKey(), WowsLootBoxRewardGroup.parse(entry.getValue()));
        }
        return map;
    }
}
