package com.shinoaki.wows.api.pr;

import lombok.Data;

/**
 * @author Xun
 */
@Data
public class PrCalculationDetails {
    private  int pr;
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
}
