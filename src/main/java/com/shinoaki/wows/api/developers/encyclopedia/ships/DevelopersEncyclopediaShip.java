package com.shinoaki.wows.api.developers.encyclopedia.ships;

import com.shinoaki.wows.api.developers.encyclopedia.shipprofile.DevelopersShipProfile;

import java.util.List;
import java.util.Map;

/**
 * 单艘舰船信息（含默认配置参数 default_profile、模块树、可用改装件）。
 *
 * @param upgrades     兼容的改装件ID列表
 * @param modules      各系统模块ID列表（值为 null 表示该舰没有此系统，如战列舰无鱼雷）
 * @param next_ships   可研发的后续舰船（舰船ID -&gt; 数值；金币舰为空表）
 * @param modules_tree 模块研发树（模块ID -&gt; 节点）
 * @author Xun
 */
public record DevelopersEncyclopediaShip(long ship_id, String ship_id_str, String name, String nation, String type,
                                         int tier, boolean is_premium, boolean is_special, boolean has_demo_profile,
                                         int mod_slots, long price_credit, long price_gold, String description,
                                         List<Long> upgrades, DevelopersShipModules modules,
                                         Map<Long, Long> next_ships, Map<Long, DevelopersShipModuleTree> modules_tree,
                                         DevelopersShipImages images, DevelopersShipProfile default_profile) {
}
