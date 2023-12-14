package com.shinoaki.wows.api.pr;

import com.shinoaki.wows.api.data.ShipInfo;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Xun
 */
@Getter
@ToString
@EqualsAndHashCode
public class PrCalculation {
    private final long shipId;
    private double damage;
    private double frags;
    private double wins;

    public PrCalculation(long shipId, double damage, double frags, double wins) {
        this.shipId = shipId;
        this.damage = damage;
        this.frags = frags;
        this.wins = wins;
    }

    private void addition(PrCalculation data) {
        this.damage += data.damage;
        this.frags += data.frags;
        this.wins += data.wins;
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
        return shipList(info, server.stream().collect(Collectors.toMap(PrCalculation::getShipId, v -> v, (v1, v2) -> v1)));
    }

    public static PrCalculationDetails shipList(List<ShipInfo> info, Map<Long, PrCalculation> server) {
        //计算用户全部
        var infoShip = info.stream().filter(f -> !server.getOrDefault(f.shipId(), PrCalculation.empty(f.shipId())).checkZero()).toList();
        PrCalculation userSum = empty(0);
        PrCalculation serverSum = empty(0);
        infoShip.stream().map(PrCalculation::user).forEach(userSum::addition);
        infoShip.stream().map(m ->
                        userServer(m, server.getOrDefault(m.shipId(), empty(m.shipId()))))
                .forEach(serverSum::addition);
        return pr(serverSum, userSum, serverSum);
    }

    public static PrCalculationDetails pr(final PrCalculation originalServer, final PrCalculation user, final PrCalculation userServer) {
        //1
        PrCalculationDetails pr = new PrCalculationDetails();
        pr.setOriginalServer(originalServer);
        pr.setUser(user);
        pr.setUserServer(userServer);
        if (userServer.checkZero()) {
            return pr;
        }
        //2
        double nd = user.getDamage() / userServer.getDamage();
        double nf = user.getFrags() / userServer.getFrags();
        double nw = user.getWins() / userServer.getWins();
        pr.setTwo(new PrCalculation(user.getShipId(), nd, nf, nw));
        //3
        double maxNd = Math.max(0, (nd - 0.4) / (1 - 0.4));
        double maxNf = Math.max(0, (nf - 0.1) / (1 - 0.1));
        double maxNw = Math.max(0, (nw - 0.7) / (1 - 0.7));
        pr.setThree(new PrCalculation(user.getShipId(), maxNd, maxNf, maxNw));
        //最终计算pr
        pr.setPr((int) Math.round(700 * maxNd + 300 * maxNf + 150 * maxNw));
        return pr;
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
