package com.shinoaki.wows.api.developers.encyclopedia.modules;

/**
 * 鱼雷模块属性（shot_speed 官方口径即装填时间）。
 *
 * @author Xun
 */
public record DevelopersModuleTorpedoes(double distance, int max_damage, double shot_speed, int torpedo_speed) {
}
