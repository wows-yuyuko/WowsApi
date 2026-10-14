package com.shinoaki.wows.api.developers.encyclopedia.shipprofile;

/**
 * 防空炮座：口径、射程、数量、每秒均伤。
 *
 * @author Xun
 */
public record DevelopersShipAASlot(String name, int caliber, double distance, int guns, int avg_damage) {
}
