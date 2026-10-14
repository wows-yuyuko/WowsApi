package com.shinoaki.wows.api.developers.encyclopedia.achievements;

import java.util.List;

/**
 * 单条成就定义。
 *
 * @param battle_types 可获得该成就的战斗类型（见 battletypes 字典）
 * @author Xun
 */
public record DevelopersEncyclopediaAchievement(String achievement_id, String name, String description, String type,
                                                String sub_type, boolean reward, String count_per_battle, String image,
                                                String image_inactive, int is_progress, int max_progress, int hidden,
                                                int max_ship_level, int min_ship_level, int multiple,
                                                List<String> battle_types) {
}
