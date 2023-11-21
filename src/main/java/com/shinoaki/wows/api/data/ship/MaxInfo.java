package com.shinoaki.wows.api.data.ship;

import com.shinoaki.wows.api.developers.warships.type.DevelopersShipBattleType;
import com.shinoaki.wows.api.vortex.ship.VortexShipInfo;

import java.io.Serializable;

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
public record MaxInfo(MaxShipInfo maxFrags, MaxShipInfo maxFragsByMain, MaxShipInfo maxFragsByTpd, MaxShipInfo maxFragsByDbomb, MaxShipInfo maxFragsByRam,
                      MaxShipInfo maxFragsByAtba,
                      MaxShipInfo maxDamageDealtToBuildings, MaxShipInfo maxFragsByPlanes, MaxShipInfo maxDamageDealt, MaxShipInfo maxScoutingDamage,
                      MaxShipInfo maxPlanesKilled,
                      MaxShipInfo maxShipsSpotted, MaxShipInfo maxTotalAgro, MaxShipInfo maxSuppressionsCount, MaxShipInfo maxXp, MaxShipInfo maxBasicXp) implements Serializable {

    public static MaxInfo empty() {
        return new MaxInfo(
                MaxShipInfo.empty(), MaxShipInfo.empty(), MaxShipInfo.empty(), MaxShipInfo.empty(), MaxShipInfo.empty(), MaxShipInfo.empty(),
                MaxShipInfo.empty(), MaxShipInfo.empty(), MaxShipInfo.empty(), MaxShipInfo.empty(), MaxShipInfo.empty(), MaxShipInfo.empty(),
                MaxShipInfo.empty(),
                MaxShipInfo.empty(), MaxShipInfo.empty(), MaxShipInfo.empty()
        );
    }

    public static MaxInfo to(long shipId, VortexShipInfo info) {
        return new MaxInfo(new MaxShipInfo(shipId, (int) info.max_frags()),
                new MaxShipInfo(shipId, (int) info.max_frags_by_main()),
                new MaxShipInfo(shipId, (int) info.max_frags_by_tpd()),
                new MaxShipInfo(shipId, (int) info.max_frags_by_dbomb()),
                new MaxShipInfo(shipId, (int) info.max_frags_by_ram()),
                new MaxShipInfo(shipId, (int) info.max_frags_by_atba()),
                new MaxShipInfo(shipId, (int) info.max_damage_dealt_to_buildings()),
                new MaxShipInfo(shipId, (int) info.max_frags_by_planes()),
                new MaxShipInfo(shipId, (int) info.max_damage_dealt()),
                new MaxShipInfo(shipId, (int) info.max_scouting_damage()),
                new MaxShipInfo(shipId, (int) info.max_planes_killed()),
                new MaxShipInfo(shipId, (int) info.max_ships_spotted()),
                new MaxShipInfo(shipId, (int) info.max_total_agro()),
                new MaxShipInfo(shipId, (int) info.max_suppressions_count()),
                new MaxShipInfo(shipId, (int) info.max_premium_exp()),
                new MaxShipInfo(shipId, (int) info.max_exp()));
    }

    public static MaxInfo to(long shipId, DevelopersShipBattleType info) {
        return new MaxInfo(new MaxShipInfo(shipId, (int) info.max_frags_battle()),
                new MaxShipInfo(shipId, info.main_battery().max_frags_battle()),
                new MaxShipInfo(shipId, info.torpedoes().max_frags_battle()),
                new MaxShipInfo(shipId, 0),
                new MaxShipInfo(shipId, info.ramming().max_frags_battle()),
                new MaxShipInfo(shipId, info.second_battery().max_frags_battle()),
                new MaxShipInfo(shipId, (int) info.max_damage_dealt_to_buildings()),
                new MaxShipInfo(shipId, info.aircraft().max_frags_battle()),
                new MaxShipInfo(shipId, (int) info.max_damage_dealt()),
                new MaxShipInfo(shipId, (int) info.max_damage_scouting()),
                new MaxShipInfo(shipId, (int) info.max_planes_killed()),
                new MaxShipInfo(shipId, (int) info.max_ships_spotted()),
                new MaxShipInfo(shipId, (int) info.max_total_agro()),
                new MaxShipInfo(shipId, (int) info.max_suppressions_count()),
                new MaxShipInfo(shipId, info.max_xp()),
                new MaxShipInfo(shipId, 0));
    }

    /**
     * 返回最大值
     *
     * @return
     */
    public MaxInfo sort(MaxInfo history) {
        return new MaxInfo(
                this.maxFrags().max(history.maxFrags),
                this.maxFragsByMain().max(history.maxFragsByMain),
                this.maxFragsByTpd().max(history.maxFragsByTpd),
                this.maxFragsByDbomb().max(history.maxFragsByDbomb),
                this.maxFragsByRam().max(history.maxFragsByRam),
                this.maxFragsByAtba().max(history.maxFragsByAtba),
                this.maxDamageDealtToBuildings().max(history.maxDamageDealtToBuildings),
                this.maxFragsByPlanes().max(history.maxFragsByPlanes),
                this.maxDamageDealt().max(history.maxDamageDealt),
                this.maxScoutingDamage().max(history.maxScoutingDamage),
                this.maxPlanesKilled().max(history.maxPlanesKilled),
                this.maxShipsSpotted().max(history.maxShipsSpotted),
                this.maxTotalAgro().max(history.maxTotalAgro),
                this.maxSuppressionsCount().max(history.maxSuppressionsCount),
                this.maxXp().max(history.maxXp),
                this.maxBasicXp().max(history.maxBasicXp)
        );
    }

    public record MaxShipInfo(long shipId, int value)  implements Serializable {
        public MaxShipInfo max(MaxShipInfo info) {
            if (info.value > value) {
                return info;
            }
            return this;
        }

        public static MaxShipInfo empty() {
            return new MaxShipInfo(0, 0);
        }
    }
}
