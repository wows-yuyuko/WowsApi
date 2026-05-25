package com.shinoaki.wows.api.pr;

import tools.jackson.databind.JsonNode;

/**
 * @param originalServer 原始服务器pr数据 在多船计算时该数据和userServer一致
 * @param user           用户平均数据
 * @param userServer     用户战斗场次*服务器数据后的结果
 * @param two            第二步的计算
 * @param three          第三步的计算
 * @author Xun
 */

public record PrCalculationDetails(int pr, PrCalculation originalServer, PrCalculation user, PrCalculation userServer, PrCalculation two, PrCalculation three) {

    public static PrCalculationDetails json(JsonNode node) {
        return new PrCalculationDetails(node.path("pr").asInt(), prCalculation(node.path("originalServer")),
                prCalculation(node.path("user")), prCalculation(node.path("userServer")), prCalculation(node.path("two")), prCalculation(node.path("three")));
    }

    public static PrCalculation prCalculation(JsonNode node) {
        return new PrCalculation(node.path("shipId").asLong(), node.path("damage").asDouble(), node.path("frags").asDouble(), node.path("wins").asDouble());
    }
}
