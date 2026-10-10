package com.shinoaki.wows.api.data;

import com.shinoaki.wows.api.data.ship.*;
import com.shinoaki.wows.api.developers.warships.type.DevelopersShipBattleType;
import com.shinoaki.wows.api.vortex.ship.VortexShipInfo;
import com.shinoaki.wows.api.vortex.ship.VortexShipStatistics;

import java.io.Serializable;

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
 * @param expansion 扩展字段
 * @param lastBattleTime                  最后的战斗时间
 * @param recordTime                      记录时间
 * @param source                          数据来源(vortex / 官方开发者API)，用于判断哪些字段在这份数据里有效
 * @author Xun
 * @date 2023/4/8 16:21 星期六
 */
public record ShipInfo(long shipId, Battle battle, long xp, long basicXp, long damageDealt, long scoutingDamage, FragsInfo fragsInfo, int shipsSpotted,
                       int planesKilled, long artAgro, long tpdAgro,

                       MaxInfo maxInfo, ControlCapturedAndDroppedPoints controlCapturedAndDroppedPoints, HitRatio ratioMain, HitRatio ratioAtba,
                       HitRatio ratioTpd, HitRatio ratioTbomb, HitRatio ratioBomb, HitRatio ratioRocket, HitRatio ratioSkip,
                       ShipExpansion expansion,
                       long lastBattleTime,
                       long recordTime,
                       ShipInfoSource source) implements Serializable {

    public static ShipInfo empty(long shipId) {
        return prInfo(shipId, 0, 0, 0, 0);
    }

    public static ShipInfo prInfo(long shipId, int battle, long damageDealt, int wins, int frags) {
        return new ShipInfo(shipId, new Battle(battle, wins, 0, 0, 0), 0, 0, damageDealt, 0,
                new FragsInfo(frags, 0, 0, 0, 0, 0, 0),
                0, 0, 0, 0, MaxInfo.empty(),
                ControlCapturedAndDroppedPoints.empty()
                , HitRatio.empty(), HitRatio.empty(), HitRatio.empty(), HitRatio.empty(), HitRatio.empty(), HitRatio.empty(), HitRatio.empty(),
                ShipExpansion.empty(), 0, 0, ShipInfoSource.UNKNOWN);
    }

    public static ShipInfo to(long shipId, VortexShipStatistics shipStatistics, long recordTime) {
        var info = shipStatistics.shipInfo();
        return new ShipInfo(shipId, Battle.to(info), info.premium_exp(), info.original_exp(), info.damage_dealt(), info.scouting_damage(), FragsInfo.to(info)
                , (int) info.ships_spotted(), (int) info.planes_killed(), info.art_agro(), info.tpd_agro(), MaxInfo.to(shipId, info),
                ControlCapturedAndDroppedPoints.to(info), new HitRatio(info.shots_by_main(), info.hits_by_main()), new HitRatio(info.shots_by_atba(),
                info.hits_by_atba()), new HitRatio(info.shots_by_tpd(), info.hits_by_tpd()), new HitRatio(info.shots_by_tbomb(), info.hits_by_tbomb()),
                new HitRatio(info.shots_by_bomb(), info.hits_by_bomb()), new HitRatio(info.shots_by_rocket(), info.hits_by_rocket()),
                new HitRatio(info.shots_by_skip(), info.hits_by_skip()), ShipExpansion.of(shipStatistics), 0L, recordTime, ShipInfoSource.VORTEX);
    }

    public static ShipInfo to(long shipId, DevelopersShipBattleType info, long lastBattleTime, long recordTime) {
        return new ShipInfo(shipId, Battle.to(info), info.xp(), 0, info.damage_dealt(), info.damage_scouting(), FragsInfo.to(info),
                (int) info.ships_spotted(), (int) info.planes_killed(), info.art_agro(), info.torpedo_agro(), MaxInfo.to(shipId, info),
                ControlCapturedAndDroppedPoints.to(info), new HitRatio(info.main_battery().shots(), info.main_battery().hits()),
                new HitRatio(info.second_battery().shots(), info.second_battery().hits()), new HitRatio(info.torpedoes().shots(), info.torpedoes().hits()),
                new HitRatio(0, 0), new HitRatio(0, 0), new HitRatio(0, 0), new HitRatio(0, 0),
                ShipExpansion.empty(),
                lastBattleTime, recordTime, ShipInfoSource.DEVELOPERS);
    }

    /**
     * 相加
     *
     * @param history 旧的历史数据
     * @return 相加后的结果
     */
    public ShipInfo addition(ShipInfo history) {
        return new ShipInfo(this.shipId(),
                this.battle().addition(history.battle()),
                this.xp() + history.xp(),
                this.basicXp() + history.basicXp(),
                this.damageDealt() + history.damageDealt(),
                this.scoutingDamage() + history.scoutingDamage(),
                this.fragsInfo().addition(history.fragsInfo()),
                this.shipsSpotted() + history.shipsSpotted(),
                this.planesKilled() + history.planesKilled(),
                this.artAgro() + history.artAgro(),
                this.tpdAgro() + history.tpdAgro(),
                this.maxInfo().sort(history.maxInfo),
                this.controlCapturedAndDroppedPoints().addition(history.controlCapturedAndDroppedPoints()),
                this.ratioMain().addition(history.ratioMain),
                this.ratioAtba().addition(history.ratioAtba),
                this.ratioTpd().addition(history.ratioTpd),
                this.ratioTbomb().addition(history.ratioTbomb),
                this.ratioBomb().addition(history.ratioBomb),
                this.ratioRocket().addition(history.ratioRocket),
                this.ratioSkip().addition(history.ratioSkip),
                //expansion为最新状态字段（如精通徽章等级），聚合时保留新值(this)，不取历史值
                this.expansion,
                this.lastBattleTime(), history.recordTime(), ShipInfoSource.merge(this.source, history.source));
    }

    /**
     * 相减
     *
     * @param history 旧的历史数据
     * @return 相减后的结果
     */
    public ShipInfo subtraction(ShipInfo history) {
        return new ShipInfo(this.shipId(),
                this.battle().subtraction(history.battle()),
                this.xp() - history.xp(),
                this.basicXp() - history.basicXp(),
                this.damageDealt() - history.damageDealt(),
                this.scoutingDamage() - history.scoutingDamage(),
                this.fragsInfo().subtraction(history.fragsInfo()),
                this.shipsSpotted() - history.shipsSpotted(),
                this.planesKilled() - history.planesKilled()
                , this.artAgro() - history.artAgro(),
                this.tpdAgro() - history.tpdAgro(),
                this.maxInfo().sort(history.maxInfo),
                this.controlCapturedAndDroppedPoints().subtraction(history.controlCapturedAndDroppedPoints()),
                this.ratioMain().subtraction(history.ratioMain),
                this.ratioAtba().subtraction(history.ratioAtba),
                this.ratioTpd().subtraction(history.ratioTpd),
                this.ratioTbomb().subtraction(history.ratioTbomb),
                this.ratioBomb().subtraction(history.ratioBomb),
                this.ratioRocket().subtraction(history.ratioRocket),
                this.ratioSkip().subtraction(history.ratioSkip),
                //expansion为最新状态字段（如精通徽章等级），相减时保留新值(this)，不取历史值
                this.expansion,
                this.lastBattleTime(), history.recordTime(), ShipInfoSource.merge(this.source, history.source));
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
     * 潜在
     *
     * @return
     */
    public double gameScoutingDamage() {
        return battle.battle() <= 0 ? scoutingDamage : scoutingDamage / (double) battle.battle();
    }

    /**
     * 发现战舰
     *
     * @return
     */
    public double gameShipsSpotted() {
        return battle.battle() <= 0 ? shipsSpotted : shipsSpotted / (double) battle.battle();
    }

    /**
     * 飞机击落
     *
     * @return
     */
    public double gamePlanesKilled() {
        return battle.battle() <= 0 ? planesKilled : planesKilled / (double) battle.battle();
    }

    /**
     * 主武器潜在
     *
     * @return
     */
    public double gameArtAgro() {
        return battle.battle() <= 0 ? artAgro : artAgro / (double) battle.battle();
    }

    /**
     * 鱼雷潜在
     *
     * @return
     */
    public double gameTpdAgro() {
        return battle.battle() <= 0 ? tpdAgro : tpdAgro / (double) battle.battle();
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

    /**
     * 战损(k/d)
     *
     * @return
     */
    public double gameKd() {
        return battle.gameKd(fragsInfo);
    }

    /**
     * 经验-会员加成
     *
     * @return
     */
    public double gameXp() {
        return battle.battle() <= 0 ? xp : xp / (double) battle.battle();
    }

    /**
     * 基础经验
     * <p>
     * 官方开发者API 没有基础经验字段（{@link #hasBasicXp()} 为 false 时本方法恒为 0）。
     *
     * @return 基础经验
     */
    public double gameBasicXp() {
        return battle.battle() <= 0 ? basicXp : basicXp / (double) battle.battle();
    }

    //----------------------------------------------------------------- 字段可用性(跨来源聚合前先看这里)

    /**
     * 基础经验（{@code basicXp} / {@code maxInfo.maxBasicXp}）在这份数据里是否有意义。
     * <p>
     * 官方开发者API 没有该字段，值为 0 只代表"没有这个数据"，与 vortex 的数据相加减会失真。
     */
    public boolean hasBasicXp() {
        return source.providesBasicXp();
    }

    /**
     * 深水炸弹数据（{@code fragsInfo.fragsByDbomb} / {@code maxInfo.maxFragsByDbomb}）是否有意义。
     */
    public boolean hasDbomb() {
        return source.providesDbomb();
    }

    /**
     * 深弹/炸弹/火箭/跳弹命中（{@code ratioTbomb/ratioBomb/ratioRocket/ratioSkip}）是否有意义。
     */
    public boolean hasOtherHitRatio() {
        return source.providesOtherHitRatio();
    }

    /**
     * {@code lastBattleTime} 是否有意义（vortex 单船接口不返回该船的最后战斗时间）。
     */
    public boolean hasLastBattleTime() {
        return source.providesLastBattleTime();
    }

    /**
     * {@code expansion}（精通徽章等级）是否有意义。
     * <p>
     * 官方开发者API 的舰船统计里没有徽章，需要另外调用 {@code /wows/ships/badges/}
     * （{@code WowsHttpShipTools.Developers#shipBadges()}）后自行合并；莱服不支持该接口。
     */
    public boolean hasExpansion() {
        return source.providesExpansion();
    }
}
