package com.shinoaki.wows.api.developers.encyclopedia.shipprofile;

/**
 * 整数区间（min/max；字段可能为 null，解析为 0）。
 *
 * @author Xun
 */
public record DevelopersShipIntRange(int min, int max) {
}
