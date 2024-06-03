package com.shinoaki.wows.api.pr;

import com.shinoaki.wows.api.data.ShipInfo;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Xun
 */
public record PrCalculation(long shipId, double damage, double frags, double wins) {

    private PrCalculation addition(PrCalculation data) {
        return new PrCalculation(this.shipId(), this.damage() + data.damage(), this.frags() + data.frags(), this.wins() + data.wins());
    }

    /**
     * 是否存在0
     *
     * @return 值
     */
    public boolean checkZero() {
        return damage == 0.0 || frags == 0.0 || wins == 0.0;
    }

    public static PrCalculationDetails ship(ShipInfo info, PrCalculation server) {
        var user = user(info);
        var userServer = userServer(info, server);
        return pr(server, user, userServer);
    }

    public static PrCalculationDetails shipList(List<ShipInfo> info, List<PrCalculation> server) {
        return shipList(info, server.stream().collect(Collectors.toMap(PrCalculation::shipId, v -> v, (v1, v2) -> v1)));
    }

    public static PrCalculationDetails shipList(List<ShipInfo> info, Map<Long, PrCalculation> server) {
        //计算用户全部
        var infoShip = info.stream().filter(f -> !server.getOrDefault(f.shipId(), PrCalculation.empty(f.shipId())).checkZero()).toList();
        PrCalculation userSum = empty(0);
        PrCalculation serverSum = empty(0);
        for (ShipInfo shipInfo : infoShip) {
            userSum = userSum.addition(user(shipInfo));
        }
        for (ShipInfo m : infoShip) {
            serverSum = serverSum.addition(userServer(m, server.getOrDefault(m.shipId(), empty(m.shipId()))));
        }
        return pr(serverSum, userSum, serverSum);
    }

    public static PrCalculationDetails pr(final PrCalculation originalServer, final PrCalculation user, final PrCalculation userServer) {
        if (userServer.checkZero()) {
            return new PrCalculationDetails(0, originalServer, user, userServer, PrCalculation.empty(user.shipId()), PrCalculation.empty(user.shipId()));
        }
        //2
        double nd = user.damage() / userServer.damage();
        double nf = user.frags() / userServer.frags();
        double nw = user.wins() / userServer.wins();
        var two = new PrCalculation(user.shipId(), nd, nf, nw);
        //3
        double maxNd = Math.max(0, (nd - 0.4) / (1 - 0.4));
        double maxNf = Math.max(0, (nf - 0.1) / (1 - 0.1));
        double maxNw = Math.max(0, (nw - 0.7) / (1 - 0.7));
        var three = new PrCalculation(user.shipId(), maxNd, maxNf, maxNw);
        //最终计算pr
        var pr = (int) Math.round(700 * maxNd + 300 * maxNf + 150 * maxNw);
        return new PrCalculationDetails(pr, originalServer, user, userServer, two, three);
    }


    private static PrCalculation user(ShipInfo info) {
        return new PrCalculation(info.shipId(), info.damageDealt(), info.fragsInfo().frags(), info.battle().wins());
    }

    private static PrCalculation userServer(ShipInfo info, PrCalculation server) {
        return new PrCalculation(
                info.shipId(),
                gameDamage(info.battle().battle(), server.damage),
                gameFrags(info.battle().battle(), server.frags),
                gameWins(info.battle().battle(), server.wins)
        );
    }

    public static PrCalculation empty(long shipId) {
        return new PrCalculation(shipId, 0, 0, 0);
    }

    private static double gameDamage(int battle, double damage) {
        return battle <= 0 ? 0 : battle * damage;
    }

    private static double gameWins(int battle, double wins) {
        return battle <= 0 ? 0 : battle * wins / 100;
    }

    private static double gameFrags(int battle, double frags) {
        return battle <= 0 ? 0 : battle * frags;
    }
}
