package com.shinoaki.wows.api.pr;

/**
 * https://wows-numbers.com/personal/rating
 *
 * @author Xun
 * @date 2023/5/10 23:21 星期三
 */
public class PrUtils {
    private PrUtils() {
    }

    public static int pr(PrData user, PrData server) {
        double nd = user.damage() / server.damage();
        double nf = user.frags() / server.frags();
        double nw = user.wins() / server.wins();
        return result(nd, nf, nw);
    }

    /**
     * 计算结果
     *
     * @param nd 场均
     * @param nf 平均击杀
     * @param nw 胜率
     * @return 结果
     */
    private static int result(double nd, double nf, double nw) {
        double maxNd = Math.max(0, (nd - 0.4) / (1 - 0.4));
        double maxNf = Math.max(0, (nf - 0.1) / (1 - 0.1));
        double maxNw = Math.max(0, (nw - 0.7) / (1 - 0.7));
        return (int) Math.ceil(700 * maxNd + 300 * maxNf + 150 * maxNw);
    }
}
