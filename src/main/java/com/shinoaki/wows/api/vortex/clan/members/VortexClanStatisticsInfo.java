package com.shinoaki.wows.api.vortex.clan.members;

import java.util.List;

/**
 * @author Xun
 */
public record VortexClanStatisticsInfo(
        double battles_count,
        double wins_percentage,
        double exp_per_battle,
        double damage_per_battle,
        List<VortexClanUserInfo> infos
) {

    public static VortexClanStatisticsInfo empty() {
        return new VortexClanStatisticsInfo(0, 0, 0, 0, List.of());
    }
}
