package com.shinoaki.wows.api.developers.warships.type;

import com.fasterxml.jackson.databind.JsonNode;
import com.shinoaki.wows.api.developers.warships.DevelopersDataBattery;
import com.shinoaki.wows.api.developers.warships.DevelopersDataBatteryV2;
import com.shinoaki.wows.api.error.BasicException;
import com.shinoaki.wows.api.utils.WowsJsonUtils;

/**
 * @author Xun
 * @date 2023/4/24 20:56 星期一
 */
public record DevelopersShipBattleType(
        int max_xp,
        long damage_to_buildings,
        DevelopersDataBattery main_battery,
        long suppressions_count,
        long max_damage_scouting,
        long art_agro,
        long ships_spotted,
        DevelopersDataBattery second_battery,
        long xp,
        long survived_battles,
        long dropped_capture_points,
        long max_damage_dealt_to_buildings,
        long torpedo_agro,
        long draws,
        long battles_since_510,
        long planes_killed,
        long battles,
        long max_ships_spotted,
        long team_capture_points,
        long frags,
        long damage_scouting,
        long max_total_agro,
        long max_frags_battle,
        long capture_points,
        DevelopersDataBatteryV2 ramming,
        DevelopersDataBattery torpedoes,
        DevelopersDataBatteryV2 aircraft,
        long survived_wins,
        long max_damage_dealt,
        long wins,
        long losses,
        long damage_dealt,
        long max_planes_killed,
        long max_suppressions_count,
        long team_dropped_capture_points,
        long battles_since_512
) {

    public static DevelopersShipBattleType parse(JsonNode node) throws BasicException {
        return new WowsJsonUtils().parse(node, DevelopersShipBattleType.class);
    }
}
