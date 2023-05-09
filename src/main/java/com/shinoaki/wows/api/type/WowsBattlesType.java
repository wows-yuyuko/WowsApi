package com.shinoaki.wows.api.type;

import java.util.Locale;

/**
 * 战斗类型
 *
 * @author Xun
 * @date 2023/3/18 11:26 星期六
 */
public enum WowsBattlesType {
    /**
     * PVP对局
     */
    PVP,
    /**
     * PVP单人
     */
    PVP_SOLO,
    /**
     * PVP2人
     */
    PVP_DIV2,
    /**
     * PVP3人
     */
    PVP_DIV3,
    /**
     * 排位
     */
    RANK_SOLO;

    @Override
    public String toString() {
        return super.toString().toLowerCase(Locale.ROOT);
    }

    public static WowsBattlesType findCode(String code) {
        for (var v : WowsBattlesType.values()) {
            if (v.name().equalsIgnoreCase(code)) {
                return v;
            }
        }
        throw new NullPointerException(code + " 匹配不到对应战斗类型");
    }
}
