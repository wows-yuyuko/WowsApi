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
        return new PrData(gameDamage(battle, damage + history.damage),
                gameFrags(battle, frags + history.frags),
                gameWins(battle, wins + history.wins));
    }

    private static double gameDamage(int battle, double damage) {
        return battle <= 0 ? damage : damage / battle;
    }

    private static double gameWins(int battle, double wins) {
        if (battle <= 0 || wins <= 0.0) {
            return 0.0;
        }
        return 100.0 * (wins / battle);
    }

    private static double gameFrags(int battle, double frags) {
        if (frags <= 0 || battle <= 0) {
            return 0.0;
        }
        return frags / battle;
    }

}
