package com.shinoaki.wows.api.vortex.account.statistics;

import tools.jackson.databind.JsonNode;

/**
 * 账号「优秀标记」汇总，对应 {@code /api/accounts/{id}/} 中 {@code statistics.mark_of_excellence}。
 *
 * @param personal 个人标记
 * @param division 同型舰标记
 * @author Xun
 */
public record VortexMarkOfExcellence(VortexMarkGradeInfo personal, VortexMarkGradeInfo division) {

    public static VortexMarkOfExcellence parse(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull() || node.isEmpty()) {
            return empty();
        }
        return new VortexMarkOfExcellence(VortexMarkGradeInfo.parse(node.path("personal")),
                VortexMarkGradeInfo.parse(node.path("division")));
    }

    public static VortexMarkOfExcellence empty() {
        return new VortexMarkOfExcellence(VortexMarkGradeInfo.empty(), VortexMarkGradeInfo.empty());
    }
}
