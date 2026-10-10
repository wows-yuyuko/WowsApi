package com.shinoaki.wows.api.vortex.ship;

import tools.jackson.databind.JsonNode;

/**
 * 单船「优秀标记」等级：{@code mark_of_excellence.personal.grade}。
 * <p>
 * 例：{@code {"name": "Mark_2", "grade_class": 2}}
 *
 * @param name        等级名称(Mark_1/Mark_2/Mark_3)
 * @param grade_class 等级序号
 * @author Xun
 */
public record VortexShipMarkGrade(String name, int grade_class) {

    public static VortexShipMarkGrade parse(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull() || node.isEmpty()) {
            return empty();
        }
        return new VortexShipMarkGrade(node.path("name").asString(""), node.path("grade_class").asInt(0));
    }

    public static VortexShipMarkGrade empty() {
        return new VortexShipMarkGrade("", 0);
    }

    public boolean isEmpty() {
        return name.isEmpty() && grade_class == 0;
    }
}
