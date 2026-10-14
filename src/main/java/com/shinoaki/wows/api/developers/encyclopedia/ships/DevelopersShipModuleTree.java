package com.shinoaki.wows.api.developers.encyclopedia.ships;

import java.util.List;

/**
 * 模块研发树节点（next_modules / next_ships 可能为 null，表示无后续）。
 *
 * @author Xun
 */
public record DevelopersShipModuleTree(long module_id, String module_id_str, String name, String type,
                                       long price_credit, long price_xp, List<Long> next_modules,
                                       List<Long> next_ships, boolean is_default) {
}
