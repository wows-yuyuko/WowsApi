package com.shinoaki.wows.api.vortex.ship;

import tools.jackson.databind.JsonNode;

/**
 * 单船「优秀标记」的达成情况（personal / division 各一份）。
 * <p>
 * 例：{@code {"ship_id": "4178491344", "grade": {"name": "Mark_2", "grade_class": 2},
 * "percentile": 94, "battles_left": 0, "achieved_at": "2026-06-15T11:21:44.918201"}}
 *
 * @param ship_id     战舰ID(接口返回的是字符串)
 * @param grade       当前标记等级
 * @param percentile  百分位
 * @param battles_left 还差多少场
 * @param achieved_at 达成时间
 * @author Xun
 */
public record VortexShipMarkInfo(long ship_id, VortexShipMarkGrade grade, int percentile, int battles_left, String achieved_at) {

    public static VortexShipMarkInfo parse(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull() || node.isEmpty()) {
            return empty();
        }
        return new VortexShipMarkInfo(node.path("ship_id").asLong(0),
                VortexShipMarkGrade.parse(node.path("grade")),
                node.path("percentile").asInt(0),
                node.path("battles_left").asInt(0),
                node.path("achieved_at").asString(""));
    }

    public static VortexShipMarkInfo empty() {
        return new VortexShipMarkInfo(0, VortexShipMarkGrade.empty(), 0, 0, "");
    }

    public boolean isEmpty() {
        return ship_id == 0 && achieved_at.isEmpty() && grade.isEmpty();
    }
}
