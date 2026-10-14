package com.shinoaki.wows.api.developers.encyclopedia.shipprofile;

/**
 * 战斗机中队（count_in_squadron / prepare_time / max_ammo / avg_damage / gunner_damage 可能为 null）。
 *
 * @author Xun
 */
public record DevelopersShipFighters(long fighters_id, String fighters_id_str, String name, int squadrons,
                                     double prepare_time, DevelopersShipIntRange count_in_squadron, int cruise_speed,
                                     int max_ammo, int avg_damage, int max_health, int plane_level, int gunner_damage) {
}
