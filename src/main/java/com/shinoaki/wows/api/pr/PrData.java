package com.shinoaki.wows.api.pr;

import com.shinoaki.wows.api.data.ShipInfo;

/**
 * @author Xun
 * @date 2023/5/10 23:21 星期三
 */
public record PrData(double damage, double frags, double wins) {

    public static PrData server(double damage, double frags, double wins) {
        return new PrData(damage, frags, wins);
    }

    public static PrData user(ShipInfo shipInfo) {
        return new PrData(
                shipInfo.gameDamage(),
                shipInfo.gameFrags(),
                shipInfo.gameWins());
    }

    public static PrData empty() {
        return new PrData(0.0, 0.0, 0.0);
    }

    /**
     * 相加
     */
    public PrData addition(int battle, PrData history) {
        return new PrData(damage + gameDamage(battle, history.damage),
                frags + gameFrags(battle, history.frags),
                wins + gameWins(battle, history.wins));
    }

    /**
     * 是否存在0
     *
     * @return 值
     */
    public boolean checkZero() {
        return damage <= 0.0 || frags <= 0.0 || wins <= 0.0;
    }

    private static double gameDamage(int battle, double damage) {
        return battle <= 0 ? damage : battle * damage;
    }

    private static double gameWins(int battle, double wins) {
        return battle <= 0 ? wins : battle * wins / 100;
    }

    private static double gameFrags(int battle, double frags) {
        return battle <= 0 ? frags : battle * frags;
    }

}
