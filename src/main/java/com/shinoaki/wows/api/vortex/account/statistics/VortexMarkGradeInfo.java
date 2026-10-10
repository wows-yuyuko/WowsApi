package com.shinoaki.wows.api.vortex.account.statistics;

import tools.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;

/**
 * 账号「优秀标记」的一段统计（personal / division 的当前值与历史分段）。
 * <p>
 * 例：{@code {"name": "Mark_1", "grade_class": 1, "ships_count": 4, "percentile": 98,
 * "grades": [{"name": "Mark_1", "grade_class": 1, "ships_count": 4, "percentile": 98}, ...]}}
 *
 * @param name        标记名称
 * @param grade_class 标记等级
 * @param ships_count 该等级的战舰数
 * @param percentile  百分位
 * @param grades      各等级明细(元素不含嵌套 grades)
 * @author Xun
 */
public record VortexMarkGradeInfo(String name, int grade_class, int ships_count, int percentile, List<VortexMarkGradeInfo> grades) {

    public static VortexMarkGradeInfo parse(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull() || node.isEmpty()) {
            return empty();
        }
        List<VortexMarkGradeInfo> grades = new ArrayList<>();
        for (JsonNode grade : node.path("grades")) {
            grades.add(new VortexMarkGradeInfo(grade.path("name").asString(""), grade.path("grade_class").asInt(0),
                    grade.path("ships_count").asInt(0), grade.path("percentile").asInt(0), List.of()));
        }
        return new VortexMarkGradeInfo(node.path("name").asString(""), node.path("grade_class").asInt(0),
                node.path("ships_count").asInt(0), node.path("percentile").asInt(0), List.copyOf(grades));
    }

    public static VortexMarkGradeInfo empty() {
        return new VortexMarkGradeInfo("", 0, 0, 0, List.of());
    }
}
