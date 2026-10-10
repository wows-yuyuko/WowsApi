package com.shinoaki.wows.api.vortex.ship;

import tools.jackson.databind.JsonNode;

/**
 * 单船「优秀标记」信息，对应 {@code /api/accounts/{id}/ships/{type}/} 中
 * {@code data.<accountId>.statistics.<shipId>.mark_of_excellence}。
 *
 * @param personal 个人(该船自己的最好成绩)
 * @param division 同型舰(同舰种分支内的排名)
 * @author Xun
 */
public record VortexShipMarkOfExcellence(VortexShipMarkInfo personal, VortexShipMarkInfo division) {

    public static VortexShipMarkOfExcellence parse(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull() || node.isEmpty()) {
            return empty();
        }
        return new VortexShipMarkOfExcellence(VortexShipMarkInfo.parse(node.path("personal")),
                VortexShipMarkInfo.parse(node.path("division")));
    }

    public static VortexShipMarkOfExcellence empty() {
        return new VortexShipMarkOfExcellence(VortexShipMarkInfo.empty(), VortexShipMarkInfo.empty());
    }

    public boolean isEmpty() {
        return personal.isEmpty() && division.isEmpty();
    }
}
