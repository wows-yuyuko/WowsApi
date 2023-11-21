package com.shinoaki.wows.api.pr;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

/**
 * @author Xun
 */
@Data
public class PrCalculationDetails {
    private int pr;
    /**
     * 原始服务器pr数据 在多船计算时该数据和userServer一致
     */
    private PrCalculation originalServer;
    /**
     * 用户平均数据
     */
    private PrCalculation user;
    /**
     * 用户战斗场次*服务器数据后的结果
     */
    private PrCalculation userServer;
    /**
     * 第二步的计算
     */
    private PrCalculation two;
    /**
     * 第三步的计算
     */
    private PrCalculation three;

    public static PrCalculationDetails json(JsonNode node) {
        PrCalculationDetails details = new PrCalculationDetails();
        details.setPr(node.get("pr").asInt());
        details.setOriginalServer(prCalculation(node.get("originalServer")));
        details.setUser(prCalculation(node.get("user")));
        details.setUserServer(prCalculation(node.get("userServer")));
        details.setTwo(prCalculation(node.get("two")));
        details.setThree(prCalculation(node.get("three")));
        return details;
    }

    public static PrCalculation prCalculation(JsonNode node) {
        return new PrCalculation(node.get("shipId").asLong(), node.get("damage").asDouble(), node.get("frags").asDouble(), node.get("wins").asDouble());
    }
}
