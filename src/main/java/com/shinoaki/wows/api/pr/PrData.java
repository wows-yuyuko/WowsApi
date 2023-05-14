package com.shinoaki.wows.api.pr;

import com.shinoaki.wows.api.data.ShipInfo;

/**
 * @author Xun
 * @date 2023/5/10 23:21 星期三
 */
public record PrData(int battle, double damage, double frags, double wins) {

    public static PrData server(double damage, double frags, double wins) {
        return new PrData(0, damage, frags, wins);
    }

    public static PrData user(ShipInfo shipInfo) {
        return new PrData(shipInfo.battle().battle(),
                shipInfo.gameDamage(),
                shipInfo.gameFrags(),
                shipInfo.gameWins());
    }

    public static PrData empty() {
        return new PrData(0, 0.0, 0.0, 0.0);
    }

    /**
     * 相加
     */
    public PrData addition(PrData history) {
        return new PrData(history.battle, damage + history.damage, frags + history.frags, wins + history.wins);
    }

    /**
     * 减去
     */
    public PrData subtraction(PrData history) {
        return new PrData(history.battle, damage - history.damage, frags - history.frags, wins - history.wins);
    }
}
