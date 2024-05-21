package com.shinoaki.wows.api.pr;

import com.fasterxml.jackson.databind.JsonNode;

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
        return new PrCalculationDetails(node.get("pr").asInt(), prCalculation(node.get("originalServer")),
                prCalculation(node.get("user")), prCalculation(node.get("userServer")), prCalculation(node.get("two")), prCalculation(node.get("three")));
    }

    public static PrCalculation prCalculation(JsonNode node) {
        return new PrCalculation(node.get("shipId").asLong(), node.get("damage").asDouble(), node.get("frags").asDouble(), node.get("wins").asDouble());
    }
}
