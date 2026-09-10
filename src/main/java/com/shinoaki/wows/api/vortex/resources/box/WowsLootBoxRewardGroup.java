package com.shinoaki.wows.api.vortex.resources.box;

import tools.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;

/**
 * 箱子奖励分组，对应 commonRewards/valuableRewards 下的 list0/list1... 节点。
 * <p>
 * 该分组自身的 probability/weight/probabilityDisplayed 表示本组(例如某等级段战舰)整体被命中的概率。
 *
 * @param title                分组标题，例如 "V–VII级战舰"
 * @param shortTitle           分组短标题，例如 "V–VII级"
 * @param rewards              分组内可掉落的奖励列表
 * @param probability          命中本组的概率(字符串，如 "0.82")
 * @param weight               权重
 * @param probabilityDisplayed 命中本组的概率(数值，如 0.82)
 * @param savePoint            保底点数，可能为 null
 * @param rerollNonUniqueCrews 是否重roll非唯一舰长
 * @param hasUniqueRewards     是否包含唯一奖励
 * @author Xun
 */
public record WowsLootBoxRewardGroup(
        String title,
        String shortTitle,
        List<WowsLootBoxReward> rewards,
        String probability,
        int weight,
        double probabilityDisplayed,
        long savePoint,
        boolean rerollNonUniqueCrews,
        boolean hasUniqueRewards
) {
    public static WowsLootBoxRewardGroup parse(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return null;
        }
        List<WowsLootBoxReward> rewards = new ArrayList<>();
        for (var reward : node.path("rewards")) {
            rewards.add(WowsLootBoxReward.parse(reward));
        }
        JsonNode savePoint = node.path("savePoint");
        return new WowsLootBoxRewardGroup(
                node.path("title").asString(""),
                node.path("shortTitle").asString(""),
                rewards,
                node.path("probability").asString(""),
                node.path("weight").asInt(0),
                node.path("probabilityDisplayed").asDouble(0),
                savePoint.isNumber() ? savePoint.asLong() : 0,
                node.path("rerollNonUniqueCrews").asBoolean(false),
                node.path("hasUniqueRewards").asBoolean(false)
        );
    }
}
