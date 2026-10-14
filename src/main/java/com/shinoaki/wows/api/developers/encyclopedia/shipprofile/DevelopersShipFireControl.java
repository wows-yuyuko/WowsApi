package com.shinoaki.wows.api.developers.encyclopedia.shipprofile;

/**
 * 射控系统：射程与射程加成（distance_increase 可能为 null）。
 *
 * @author Xun
 */
public record DevelopersShipFireControl(long fire_control_id, String fire_control_id_str, double distance,
                                        int distance_increase) {
}
