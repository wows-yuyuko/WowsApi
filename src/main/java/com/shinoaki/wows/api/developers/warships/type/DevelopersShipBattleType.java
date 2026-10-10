package com.shinoaki.wows.api.developers.warships.type;

import com.shinoaki.wows.api.developers.warships.DevelopersDataBattery;
import com.shinoaki.wows.api.developers.warships.DevelopersDataBatteryV2;
import com.shinoaki.wows.api.error.BasicException;
import com.shinoaki.wows.api.utils.JsonUtils;
import tools.jackson.databind.JsonNode;


/**
 * @author Xun
 * @date 2023/4/24 20:56 星期一
 */
public record DevelopersShipBattleType(int max_xp, long damage_to_buildings, DevelopersDataBattery main_battery, long suppressions_count, long max_damage_scouting, long art_agro,
        long ships_spotted, DevelopersDataBattery second_battery, long xp, long survived_battles, long dropped_capture_points,
        long max_damage_dealt_to_buildings, long torpedo_agro, long draws, long battles_since_510, long planes_killed, long battles, long max_ships_spotted,
        long team_capture_points, long frags, long damage_scouting, long max_total_agro, long max_frags_battle, long capture_points,
        DevelopersDataBatteryV2 ramming, DevelopersDataBattery torpedoes, DevelopersDataBatteryV2 aircraft, long survived_wins, long max_damage_dealt,
        long wins, long losses, long damage_dealt, long max_planes_killed, long max_suppressions_count, long team_dropped_capture_points,
        long battles_since_512, long max_frags_ship_id, long max_damage_dealt_ship_id, long max_damage_dealt_to_buildings_ship_id, long max_xp_ship_id,
        long max_planes_killed_ship_id, long max_ships_spotted_ship_id, long max_scouting_damage_ship_id, long max_suppressions_ship_id,
        long max_total_agro_ship_id, long control_captured_points, long control_dropped_points,
        // ---- 以下 14 个为莱服(korabli.su)官方API 独有，其它服缺失、恒为 0 ----
        // 莱服同时还少了 damage_scouting/max_damage_scouting/max_scouting_damage_ship_id（不统计侦查伤害）
        long assist_damage, long assist_damage_kills, long assist_kills, long assist_spot_kills,
        long max_assist_damage, long max_assist_damage_ship_id, long max_assist_kills, long max_assist_kills_ship_id,
        long frags_by_secondary, long hits_by_secondary, long shots_by_secondary,
        long max_frags_by_secondary, long max_frags_by_secondary_ship_id, long battles_since_2608) {

    public static DevelopersShipBattleType parse(JsonNode node) throws BasicException {
        return JsonUtils.json().parse(node, DevelopersShipBattleType.class);
    }

    public static DevelopersShipBattleType empty() {
        return new DevelopersShipBattleType(0, 0, new DevelopersDataBattery(0, 0, 0, 0, 0), 0, 0, 0, 0, new DevelopersDataBattery(0, 0, 0, 0, 0), 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                new DevelopersDataBatteryV2(0, 0, 0), new DevelopersDataBattery(0, 0, 0, 0, 0), new DevelopersDataBatteryV2(0, 0, 0), 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
    }
}
