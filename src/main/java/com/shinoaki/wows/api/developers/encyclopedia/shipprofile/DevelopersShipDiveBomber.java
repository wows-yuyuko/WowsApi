package com.shinoaki.wows.api.developers.encyclopedia.shipprofile;

/**
 * 轰炸机中队（bomb_* 为炸弹数据，accuracy 为投弹精度区间；若干字段可能为 null）。
 *
 * @author Xun
 */
public record DevelopersShipDiveBomber(long dive_bomber_id, String dive_bomber_id_str, String name, int squadrons,
                                       double prepare_time, DevelopersShipIntRange count_in_squadron, int cruise_speed,
                                       int max_health, int plane_level, int gunner_damage,
                                       DevelopersShipDoubleRange accuracy, String bomb_name, int bomb_bullet_mass,
                                       double bomb_burn_probability, int bomb_damage) {
}
