package com.shinoaki.wows.api.data.ship;

import com.shinoaki.wows.api.developers.warships.type.DevelopersShipBattleType;
import com.shinoaki.wows.api.vortex.ship.VortexShipInfo;

/**
 * @param frags         总击杀
 * @param fragsByMain   主武器击杀
 * @param fragsByAtba   副炮击杀
 * @param fragsByPlanes 舰载机击杀
 * @param fragsByTpd    鱼雷击杀
 * @param fragsByRam    撞击击沉
 * @param fragsByDbomb  深水炸弹击杀-注意目前深弹不算起火和进水
 * @author Xun
 * @date 2023/4/9 11:10 星期日
 */
public record FragsInfo(
        int frags,
        int fragsByMain,
        int fragsByAtba,
        int fragsByPlanes,
        int fragsByTpd,
        int fragsByRam,
        int fragsByDbomb

) {
    public static FragsInfo to(VortexShipInfo info) {
        return new FragsInfo((int) info.frags(),
                (int) info.frags_by_main(),
                (int) info.frags_by_atba(),
                (int) info.frags_by_planes(),
                (int) info.frags_by_tpd(),
                (int) info.frags_by_ram(),
                (int) info.frags_by_dbomb());
    }

    public static FragsInfo to(DevelopersShipBattleType info) {
        return new FragsInfo((int) info.frags(),
                info.main_battery().frags(),
                info.second_battery().frags(),
                info.aircraft().frags(),
                info.torpedoes().frags(),
                info.ramming().frags(),
                0);
    }

    /**
     * 相加
     *
     * @param history 旧的历史数据
     * @return 相加后的结果
     */
    public FragsInfo addition(FragsInfo history) {
        return new FragsInfo(this.frags() + history.frags(),
                this.fragsByMain() + history.fragsByMain(),
                this.fragsByAtba() + history.fragsByAtba(),
                this.fragsByPlanes() + history.fragsByPlanes(),
                this.fragsByTpd() + history.fragsByTpd(),
                this.fragsByRam() + history.fragsByRam(),
                this.fragsByDbomb() + history.fragsByDbomb());
    }

    /**
     * 相减
     *
     * @param history 旧的历史数据
     * @return 相减后的结果
     */
    public FragsInfo subtraction(FragsInfo history) {
        return new FragsInfo(this.frags() - history.frags(),
                this.fragsByMain() - history.fragsByMain(),
                this.fragsByAtba() - history.fragsByAtba(),
                this.fragsByPlanes() - history.fragsByPlanes(),
                this.fragsByTpd() - history.fragsByTpd(),
                this.fragsByRam() - history.fragsByRam(),
                this.fragsByDbomb() - history.fragsByDbomb());
    }


    /**
     * 场均击杀
     *
     * @return 结果
     */
    public double gameFrags(Battle battle) {
        if (this.frags() <= 0 || battle.battle() <= 0) {
            return 0.0;
        }
        return (double) this.frags() / battle.battle();
    }
}
