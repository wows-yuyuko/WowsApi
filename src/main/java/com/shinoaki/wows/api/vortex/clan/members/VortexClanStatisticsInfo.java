package com.shinoaki.wows.api.vortex.clan.members;

import java.util.List;

/**
 * 军团成员列表接口的汇总信息，对应 {@code /api/members/{id}/} 的根节点。
 *
 * @param battles_count      场均场次(服务端字段)
 * @param wins_percentage    胜率
 * @param exp_per_battle     场均经验
 * @param damage_per_battle  场均伤害
 * @param infos              成员列表
 * @param status             状态
 * @param is_hidden_statistics 是否隐藏了统计
 * @param last_updated_at    数据更新时间
 * @param league_best        历史最高联赛名(莱服独有，没打过为 null；其它服的响应里没有该字段)
 * @param league_best_rating 历史最高联赛时的评分(莱服独有，没打过为 null)
 * @author Xun
 */
public record VortexClanStatisticsInfo(
        double battles_count,
        double wins_percentage,
        double exp_per_battle,
        double damage_per_battle,
        List<VortexClanUserInfo> infos,
        String status,
        boolean is_hidden_statistics,
        String last_updated_at,
        String league_best,
        Integer league_best_rating
) {

    public static VortexClanStatisticsInfo empty() {
        return new VortexClanStatisticsInfo(0, 0, 0, 0, List.of(), "", false, "", null, null);
    }
}
