package com.shinoaki.wows.api.developers.encyclopedia.shipprofile;

/**
 * 雷击机中队（torpedo_* 为鱼雷数据；若干字段可能为 null）。
 *
 * @author Xun
 */
public record DevelopersShipTorpedoBomber(long torpedo_bomber_id, String torpedo_bomber_id_str, String name,
                                          int squadrons, double prepare_time, DevelopersShipIntRange count_in_squadron,
                                          int cruise_speed, int max_health, int plane_level, int gunner_damage,
                                          String torpedo_name, int torpedo_damage, double torpedo_distance,
                                          int torpedo_max_speed) {
}
