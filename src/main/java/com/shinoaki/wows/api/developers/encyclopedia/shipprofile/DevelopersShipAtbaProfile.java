package com.shinoaki.wows.api.developers.encyclopedia.shipprofile;

import java.util.Map;

/**
 * 副炮组：射程 + 各炮座数据。
 *
 * @author Xun
 */
public record DevelopersShipAtbaProfile(double distance, Map<String, DevelopersShipAtbaSlot> slots) {
}
