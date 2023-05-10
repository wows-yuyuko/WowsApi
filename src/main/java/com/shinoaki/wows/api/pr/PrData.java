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
                shipInfo.fragsInfo().gameFrags(shipInfo.battle()),
                shipInfo.battle().battle() * shipInfo.gameWins() / 100.0);
    }
}
