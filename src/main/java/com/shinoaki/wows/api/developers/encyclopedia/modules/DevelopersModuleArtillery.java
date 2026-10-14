package com.shinoaki.wows.api.developers.encyclopedia.modules;

/**
 * 主炮模块属性（max_damage_HE 可能为 null）。
 *
 * @author Xun
 */
public record DevelopersModuleArtillery(double gun_rate, int max_damage_AP, int max_damage_HE, double rotation_time) {
}
