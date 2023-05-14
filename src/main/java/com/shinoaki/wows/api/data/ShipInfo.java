package com.shinoaki.wows.api.data;

import com.shinoaki.wows.api.data.ship.*;
import com.shinoaki.wows.api.developers.warships.type.DevelopersShipBattleType;
import com.shinoaki.wows.api.vortex.ship.VortexShipInfo;

/**
 * @param shipId                          船ID
 * @param battle                          场次
 * @param xp                              经验-会员加成
 * @param basicXp                         经验-基础
 * @param damageDealt                     场均
 * @param scoutingDamage                  潜在
 * @param fragsInfo                       击杀信息
 * @param shipsSpotted                    发现战舰数
 * @param planesKilled                    飞机击落数
 * @param artAgro                         主武器潜在
 * @param tpdAgro                         鱼雷潜在
 * @param maxInfo                         最高历史记录
 * @param controlCapturedAndDroppedPoints 占领和防御贡献
 * @param ratioMain                       主武器命中数据
 * @param ratioAtba                       副武器命中数据
 * @param ratioTpd                        鱼雷武器命中数据
 * @param ratioTbomb                      深弹武器命中数据
 * @param ratioBomb                       未知-武器命中数据
 * @param ratioRocket                     未知-武器命中数据
 * @param ratioSkip                       未知-武器命中数据
 * @param lastBattleTime                  最后的战斗时间
 * @param recordTime                      记录时间
 * @author Xun
 * @date 2023/4/8 16:21 星期六
 */
public record ShipInfo(long shipId, Battle battle, long xp, long basicXp, long damageDealt, long scoutingDamage, FragsInfo fragsInfo, int shipsSpotted,
                       int planesKilled, long artAgro, long tpdAgro,

                       MaxInfo maxInfo, ControlCapturedAndDroppedPoints controlCapturedAndDroppedPoints, HitRatio ratioMain, HitRatio ratioAtba,
                       HitRatio ratioTpd, HitRatio ratioTbomb, HitRatio ratioBomb, HitRatio ratioRocket, HitRatio ratioSkip, long lastBattleTime,
                       long recordTime) {
    public static ShipInfo to(long shipId, VortexShipInfo info, long recordTime) {
        return new ShipInfo(shipId, Battle.to(info), info.premium_exp(), info.original_exp(), info.damage_dealt(), info.scouting_damage(), FragsInfo.to(info),
                (int) info.ships_spotted(), (int) info.planes_killed(), info.art_agro(), info.tpd_agro(), MaxInfo.to(shipId, info),
                ControlCapturedAndDroppedPoints.to(info), new HitRatio(info.shots_by_main(), info.hits_by_main()), new HitRatio(info.shots_by_atba(),
                info.hits_by_atba()), new HitRatio(info.shots_by_tpd(), info.shots_by_tpd()), new HitRatio(info.shots_by_tbomb(), info.hits_by_tbomb()),
                new HitRatio(info.shots_by_bomb(), info.hits_by_bomb()), new HitRatio(info.shots_by_rocket(), info.hits_by_rocket()),
                new HitRatio(info.shots_by_skip(), info.hits_by_skip()), 0L, recordTime);
    }

    public static ShipInfo to(long shipId, DevelopersShipBattleType info, long lastBattleTime, long recordTime) {
        return new ShipInfo(shipId,
                Battle.to(info),
                info.xp(),
                -1,
                info.damage_dealt(),
                info.damage_scouting(),
                FragsInfo.to(info),
                (int) info.ships_spotted(),
                (int) info.planes_killed(),
                info.art_agro(),
                info.torpedo_agro(),
                MaxInfo.to(shipId, info),
                ControlCapturedAndDroppedPoints.to(info),
                new HitRatio(info.main_battery().shots(), info.main_battery().hits()),
                new HitRatio(info.second_battery().shots(), info.second_battery().hits()),
                new HitRatio(info.torpedoes().shots(), info.torpedoes().hits()),
                new HitRatio(-1, -1),
                new HitRatio(-1, -1),
                new HitRatio(-1, -1),
                new HitRatio(-1, -1),
                lastBattleTime, recordTime);
    }

    /**
     * 相加
     *
     * @param history 旧的历史数据
     * @return 相加后的结果
     */
    public ShipInfo addition(ShipInfo history) {
        return new ShipInfo(this.shipId(), this.battle().addition(history.battle()), this.xp() + history.xp(), this.basicXp() + history.basicXp(),
                this.damageDealt() + history.damageDealt(), this.scoutingDamage() + history.scoutingDamage(), this.fragsInfo().addition(history.fragsInfo()),
                this.shipsSpotted() + history.shipsSpotted(), this.planesKilled() + history.planesKilled(), this.artAgro() + history.artAgro(),
                this.tpdAgro() + history.tpdAgro(), this.maxInfo().sort(history.maxInfo),
                this.controlCapturedAndDroppedPoints().addition(history.controlCapturedAndDroppedPoints()), this.ratioMain(), this.ratioAtba(),
                this.ratioTpd(), this.ratioTbomb(), this.ratioBomb(), this.ratioRocket(), this.ratioSkip(), this.lastBattleTime(), history.recordTime());
    }

    /**
     * 相减
     *
     * @param history 旧的历史数据
     * @return 相减后的结果
     */
    public ShipInfo subtraction(ShipInfo history) {
        return new ShipInfo(this.shipId(), this.battle().subtraction(history.battle()), this.xp() - history.xp(), this.basicXp() - history.basicXp(),
                this.damageDealt() - history.damageDealt(), this.scoutingDamage() - history.scoutingDamage(),
                this.fragsInfo().subtraction(history.fragsInfo()), this.shipsSpotted() - history.shipsSpotted(), this.planesKilled() - history.planesKilled()
                , this.artAgro() - history.artAgro(), this.tpdAgro() - history.tpdAgro(), this.maxInfo().sort(history.maxInfo),
                this.controlCapturedAndDroppedPoints().subtraction(history.controlCapturedAndDroppedPoints()), this.ratioMain(), this.ratioAtba(),
                this.ratioTpd(), this.ratioTbomb(), this.ratioBomb(), this.ratioRocket(), this.ratioSkip(), this.lastBattleTime(), history.recordTime());
    }

    /**
     * 场均数据
     *
     * @return 用户每场平均输出
     */
    public double gameDamage() {
        return battle.battle() <= 0 ? damageDealt : damageDealt / (double) battle.battle();
    }

    /**
     * 平均胜率
     *
     * @return
     */
    public double gameWins() {
        return battle.gameWins();
    }

    /**
     * 平均击杀
     *
     * @return
     */
    public double gameFrags() {
        return fragsInfo.gameFrags(battle);
    }
}
