package com.shinoaki.wows.api.vortex.resources.box;

import tools.jackson.databind.JsonNode;

/**
 * 箱子奖励项，对应 commonRewards/valuableRewards 分组内的 rewards[]。
 * <p>
 * 普通奖励(commonRewards)一般不含 crewLevel/addTop/additionalData，
 * 高价值奖励(valuableRewards，例如战舰)则携带这些字段，此处统一兼容。
 *
 * @param probability          命中概率(字符串，如 "1.65")
 * @param weight               权重
 * @param probabilityDisplayed 命中概率(数值，如 1.65)
 * @param amount               数量
 * @param type                 奖励类型，例如 ship/steel/coal/gold/signal/camoboost...
 * @param id                   奖励ID，可能为 null
 * @param crewLevel            舰长等级，可能为 null（莱服不返回该字段，恒为 null）
 * @param defaultCrew          默认舰长ID，可能为 null（两条通道都有；舰长详情在
 *                             {@link WowsLootBoxRewardData#defaultCrew()} 里，是个对象）
 * @param addTop               是否附加顶级
 * @param additionalData       附加数据(名称/标签/国籍/图标等)，可能为 null
 * @author Xun
 */
public record WowsLootBoxReward(
        String probability,
        int weight,
        double probabilityDisplayed,
        int amount,
        String type,
        Long id,
        Integer crewLevel,
        Long defaultCrew,
        boolean addTop,
        WowsLootBoxRewardData additionalData
) {
    public static WowsLootBoxReward parse(JsonNode node) {
        JsonNode id = node.path("id");
        JsonNode crewLevel = node.path("crewLevel");
        JsonNode defaultCrew = node.path("defaultCrew");
        JsonNode additionalData = node.path("additionalData");
        return new WowsLootBoxReward(
                node.path("probability").asString(""),
                node.path("weight").asInt(0),
                node.path("probabilityDisplayed").asDouble(0),
                node.path("amount").asInt(0),
                node.path("type").asString(""),
                id.isNumber() ? id.asLong() : null,
                crewLevel.isNumber() ? crewLevel.asInt() : null,
                defaultCrew.isNumber() ? defaultCrew.asLong() : null,
                node.path("addTop").asBoolean(false),
                (additionalData.isMissingNode() || additionalData.isNull()) ? null : WowsLootBoxRewardData.parse(additionalData)
        );
    }
}
