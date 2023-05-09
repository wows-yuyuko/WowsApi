package com.shinoaki.wows.api.data.ship;

import com.shinoaki.wows.api.developers.warships.type.DevelopersShipBattleType;
import com.shinoaki.wows.api.vortex.ship.VortexShipInfo;

/**
 * 最高记录
 *
 * @param maxFrags                  最多击杀
 * @param maxFragsByMain            最多主炮击杀
 * @param maxFragsByTpd             最多鱼雷击杀
 * @param maxFragsByDbomb           最多深水炸弹击杀
 * @param maxFragsByRam             最多撞击击杀
 * @param maxFragsByAtba            副炮击杀
 * @param maxDamageDealtToBuildings 对建筑造成最大的伤害
 * @param maxFragsByPlanes          最高使用舰载机击杀
 * @param maxDamageDealt            最高伤害
 * @param maxScoutingDamage         最大侦查伤害
 * @param maxPlanesKilled           最高击落飞机
 * @param maxShipsSpotted           最高发现战舰
 * @param maxTotalAgro              最高潜在伤害
 * @param maxSuppressionsCount      最大抑制计数
 * @param maxXp                     最高经验
 * @param maxBasicXp                最高基础经验
 * @author Xun
 * @date 2023/4/8 16:28 星期六
 */
public record MaxInfo(int maxFrags, int maxFragsByMain, int maxFragsByTpd, int maxFragsByDbomb, int maxFragsByRam, int maxFragsByAtba,
                      int maxDamageDealtToBuildings, int maxFragsByPlanes, int maxDamageDealt, int maxScoutingDamage, int maxPlanesKilled,
                      int maxShipsSpotted, int maxTotalAgro, int maxSuppressionsCount, int maxXp, int maxBasicXp) {
    public static MaxInfo to(VortexShipInfo info) {
        return new MaxInfo((int) info.max_frags(), (int) info.max_frags_by_main(), (int) info.max_frags_by_tpd(), (int) info.max_frags_by_dbomb(),
                (int) info.max_frags_by_ram(), (int) info.max_frags_by_atba(), (int) info.max_damage_dealt_to_buildings(), (int) info.max_frags_by_planes(),
                (int) info.max_damage_dealt(), (int) info.max_scouting_damage(), (int) info.max_planes_killed(), (int) info.max_ships_spotted(),
                (int) info.max_total_agro(), (int) info.max_suppressions_count(), (int) info.max_premium_exp(), (int) info.max_exp());
    }

    public static MaxInfo to(DevelopersShipBattleType info) {
        return new MaxInfo((int) info.max_frags_battle(),
                info.main_battery().max_frags_battle(),
                info.torpedoes().max_frags_battle(),
                -1,
                info.ramming().max_frags_battle(),
                info.second_battery().max_frags_battle(),
                (int) info.max_damage_dealt_to_buildings(),
                info.aircraft().max_frags_battle(),
                (int) info.max_damage_dealt(),
                (int) info.max_damage_scouting(),
                (int) info.max_planes_killed(),
                (int) info.max_ships_spotted(),
                (int) info.max_total_agro(),
                (int) info.max_suppressions_count(),
                info.max_xp(),
                -1);
    }
}
