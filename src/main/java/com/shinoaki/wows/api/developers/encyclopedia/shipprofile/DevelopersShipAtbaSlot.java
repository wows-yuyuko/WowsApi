package com.shinoaki.wows.api.developers.encyclopedia.shipprofile;

/**
 * 副炮炮座：弹种、伤害、点火率、存速、射速与装填。
 *
 * @author Xun
 */
public record DevelopersShipAtbaSlot(String name, String type, int damage, double burn_probability, int bullet_mass,
                                     int bullet_speed, double gun_rate, double shot_delay) {
}
