package com.shinoaki.wows.api.developers.encyclopedia.shipprofile;

import java.util.Map;

/**
 * 防空：总评分 + 各防空炮座数据。
 *
 * @author Xun
 */
public record DevelopersShipAntiAircraftProfile(int defense, Map<String, DevelopersShipAASlot> slots) {
}
