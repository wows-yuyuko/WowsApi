package com.shinoaki.wows.api.developers.encyclopedia.shipprofile;

import java.util.Map;

/**
 * 鱼雷发射管：射程/伤害/装填/被发现距离 + 各发射器数据。
 *
 * @author Xun
 */
public record DevelopersShipTorpedoProfile(long torpedoes_id, String torpedoes_id_str, String torpedo_name,
                                           double distance, int max_damage, double reload_time, double rotation_time,
                                           int torpedo_speed, double visibility_dist,
                                           Map<String, DevelopersShipTorpedoSlot> slots) {
}
