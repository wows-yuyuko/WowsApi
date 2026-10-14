package com.shinoaki.wows.api.developers.encyclopedia.consumables;

/**
 * 消耗品特性项：系数值与本地化说明（如「战舰生命值: +5%」）。
 *
 * @author Xun
 */
public record DevelopersConsumableEffect(double value, String description) {
}
