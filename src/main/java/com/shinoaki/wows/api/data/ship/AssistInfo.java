package com.shinoaki.wows.api.data.ship;

import com.shinoaki.wows.api.developers.warships.type.DevelopersShipBattleType;
import com.shinoaki.wows.api.vortex.ship.VortexShipInfo;

import java.io.Serializable;

/**
 * 助攻数据（分家后莱服新增，其它服恒为 0）。
 * <p>
 * 映射：vortex {@code assist_*} / {@code max_assist_*}；官方开发者API {@code assist_*}。
 *
 * @param assistDamage       助攻伤害
 * @param assistKills        助攻击杀
 * @param assistSpotKills    助攻点亮击杀
 * @param assistDamageKills  助攻伤害击杀
 * @author Xun
 */
public record AssistInfo(long assistDamage, long assistKills, long assistSpotKills, long assistDamageKills) implements Serializable {

    public static AssistInfo empty() {
        return new AssistInfo(0, 0, 0, 0);
    }

    public static AssistInfo to(VortexShipInfo info) {
        return new AssistInfo(info.assist_damage(), info.assist_kills(), info.assist_spot_kills(), info.assist_damage_kills());
    }

    public static AssistInfo to(DevelopersShipBattleType info) {
        return new AssistInfo(info.assist_damage(), info.assist_kills(), info.assist_spot_kills(), info.assist_damage_kills());
    }

    /**
     * 相加
     *
     * @param history 旧的历史数据
     * @return 相加后的结果
     */
    public AssistInfo addition(AssistInfo history) {
        return new AssistInfo(this.assistDamage() + history.assistDamage(),
                this.assistKills() + history.assistKills(),
                this.assistSpotKills() + history.assistSpotKills(),
                this.assistDamageKills() + history.assistDamageKills());
    }

    /**
     * 相减
     *
     * @param history 旧的历史数据
     * @return 相减后的结果
     */
    public AssistInfo subtraction(AssistInfo history) {
        return new AssistInfo(this.assistDamage() - history.assistDamage(),
                this.assistKills() - history.assistKills(),
                this.assistSpotKills() - history.assistSpotKills(),
                this.assistDamageKills() - history.assistDamageKills());
    }
}
