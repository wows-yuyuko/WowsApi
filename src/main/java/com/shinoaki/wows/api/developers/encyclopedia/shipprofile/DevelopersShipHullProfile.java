package com.shinoaki.wows.api.developers.encyclopedia.shipprofile;

/**
 * 船体：血量、炮塔/鱼雷管数量、射程区间、机库容量（planes_amount 可能为 null）。
 *
 * @author Xun
 */
public record DevelopersShipHullProfile(long hull_id, String hull_id_str, int health, DevelopersShipIntRange range,
                                        int artillery_barrels, int atba_barrels, int anti_aircraft_barrels,
                                        int torpedoes_barrels, int planes_amount) {
}
