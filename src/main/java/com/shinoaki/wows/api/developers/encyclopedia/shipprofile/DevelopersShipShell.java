package com.shinoaki.wows.api.developers.encyclopedia.shipprofile;

/**
 * 炮弹数据（burn_probability 对 AP 弹为 null，解析为 0）。
 *
 * @author Xun
 */
public record DevelopersShipShell(String name, String type, int damage, double burn_probability, int bullet_mass,
                                  int bullet_speed) {
}
