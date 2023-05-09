package com.shinoaki.wows.api.data.ship;

import com.shinoaki.wows.api.developers.warships.type.DevelopersShipBattleType;
import com.shinoaki.wows.api.vortex.ship.VortexShipInfo;

/**
 * @param battle         战斗场次
 * @param wins           胜利场次
 * @param losses         失败场次
 * @param survived       存活场次
 * @param winAndSurvived 胜利且存活场次
 * @author Xun
 * @date 2023/4/8 16:47 星期六
 */
public record Battle(
        int battle,
        int wins,
        int losses,
        int survived,
        int winAndSurvived
) {
    public static Battle to(VortexShipInfo info) {
        return new Battle((int) info.battles_count(),
                (int) info.wins(),
                (int) info.losses(),
                (int) info.survived(),
                (int) info.win_and_survived());
    }

    public static Battle to(DevelopersShipBattleType info) {
        return new Battle((int) info.battles(),
                (int) info.wins(),
                (int) info.losses(),
                (int) info.survived_battles(),
                (int) info.survived_wins());
    }

    /**
     * 相加
     *
     * @param history 旧的历史数据
     * @return 相加后的结果
     */
    public Battle addition(Battle history) {
        return new Battle(this.battle() + history.battle(),
                this.wins() + history.wins(),
                this.losses() + history.losses(),
                this.survived() + history.survived(),
                this.winAndSurvived() + history.survived());
    }

    /**
     * 相减
     *
     * @param history 旧的历史数据
     * @return 相减后的结果
     */
    public Battle subtraction(Battle history) {
        return new Battle(this.battle() - history.battle(),
                this.wins() - history.wins(),
                this.losses() - history.losses(),
                this.survived() - history.survived(),
                this.winAndSurvived() - history.survived());
    }

    /**
     * 胜率
     *
     * @return 胜率
     */
    public double gameWins() {
        if (this.battle() <= 0 || this.wins() <= 0) {
            return 0.0;
        }
        return 100.0 * ((double) this.wins() / this.battle());
    }

    /**
     * KD
     *
     * @return kd
     */
    public double gameKd(FragsInfo info) {
        int v = this.battle() - this.survived();
        if (v <= 0 || info.frags() <= 0) {
            return 0.0;
        } else {
            return (double) info.frags() / v;
        }
    }
}
