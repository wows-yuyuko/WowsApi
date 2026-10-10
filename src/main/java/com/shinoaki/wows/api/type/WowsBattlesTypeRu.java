package com.shinoaki.wows.api.type;

import java.io.Serializable;
import java.util.Locale;

/**
 * 莱服(korabli.su)战斗类型——{@link WowsBattlesType} 的莱服专属扩展。
 * <p>
 * 两家分家后莱服多出来一批战斗类型（乱斗 {@code brawl}、军团战 {@code clan}、评级 {@code rating*}、
 * 行动 {@code oper_mm}、新排位 {@code rank} 等）。这些值<b>不放进</b> {@link WowsBattlesType}：
 * 那边是各服通用的稳定集合（不少调用方拿它做 {@code EnumMap}/switch），加值会连带影响它们。
 * 这里单独开一套莱服专属枚举，并保持双向兼容：
 * <ul>
 *     <li>{@link #toWowsBattlesType()}：莱服类型 → 通用类型；莱服独有的返回 {@code null}；</li>
 *     <li>{@link #of(WowsBattlesType)}：通用类型 → 莱服类型（恒非 null）。</li>
 * </ul>
 * {@link #code()} 是 URL/JSON 里的段名（小写）。部分类型只在其中一条通道有：
 * {@code pve_solo}/{@code pve_div2}/{@code pve_div3} 见官方开发者API，
 * {@code oper_mm}/{@code brawl}/{@code clan}/{@code rating*} 见 vortex 官网接口。
 *
 * @author Xun
 */
public enum WowsBattlesTypeRu implements Serializable {
    /**
     * PVP对局
     */
    PVP("pvp", WowsBattlesType.PVP),
    /**
     * PVP单人
     */
    PVP_SOLO("pvp_solo", WowsBattlesType.PVP_SOLO),
    /**
     * PVP2人
     */
    PVP_DIV2("pvp_div2", WowsBattlesType.PVP_DIV2),
    /**
     * PVP3人
     */
    PVP_DIV3("pvp_div3", WowsBattlesType.PVP_DIV3),
    /**
     * 排位
     */
    RANK_SOLO("rank_solo", WowsBattlesType.RANK_SOLO),
    /**
     * 剧情(合作)模式
     */
    PVE("pve", null),
    /**
     * 剧情单人
     */
    PVE_SOLO("pve_solo", null),
    /**
     * 剧情2人
     */
    PVE_DIV2("pve_div2", null),
    /**
     * 剧情3人
     */
    PVE_DIV3("pve_div3", null),
    /**
     * 行动模式(匹配)
     */
    OPER_MM("oper_mm", null),
    /**
     * 乱斗
     */
    BRAWL("brawl", null),
    /**
     * 军团战(账号汇总里有该数据块，但没有对应的单船接口)
     */
    CLAN("clan", null),
    /**
     * 新排位(莱服)
     */
    RANK("rank", null),
    /**
     * 新排位2人
     */
    RANK_DIV2("rank_div2", null),
    /**
     * 新排位3人
     */
    RANK_DIV3("rank_div3", null),
    /**
     * 旧排位(莱服)
     */
    RANK_OLD("rank_old", null),
    /**
     * 旧排位单人
     */
    RANK_OLD_SOLO("rank_old_solo", null),
    /**
     * 旧排位2人
     */
    RANK_OLD_DIV2("rank_old_div2", null),
    /**
     * 旧排位3人
     */
    RANK_OLD_DIV3("rank_old_div3", null),
    /**
     * 评级战(莱服)
     */
    RATING("rating", null),
    /**
     * 评级战单人
     */
    RATING_SOLO("rating_solo", null),
    /**
     * 评级战组队
     */
    RATING_DIV("rating_div", null);

    /**
     * JSON/URL 里的段名（小写）
     */
    private final String code;
    /**
     * 通用战斗类型；莱服独有的为 null
     */
    private final WowsBattlesType base;

    WowsBattlesTypeRu(String code, WowsBattlesType base) {
        this.code = code;
        this.base = base;
    }

    public String code() {
        return code;
    }

    /**
     * 转成通用的 {@link WowsBattlesType}。
     *
     * @return 通用战斗类型；莱服独有的类型返回 {@code null}
     */
    public WowsBattlesType toWowsBattlesType() {
        return base;
    }

    /**
     * 是否莱服独有（{@link WowsBattlesType} 里没有对应值）。
     */
    public boolean isRuOnly() {
        return base == null;
    }

    /**
     * 通用战斗类型 → 莱服战斗类型。
     */
    public static WowsBattlesTypeRu of(WowsBattlesType type) {
        for (var v : WowsBattlesTypeRu.values()) {
            if (v.base == type) {
                return v;
            }
        }
        throw new IllegalArgumentException(type + " 在莱服没有对应战斗类型");
    }

    /**
     * 按 {@link #code()} 匹配（忽略大小写）。
     */
    public static WowsBattlesTypeRu findCode(String code) {
        for (var v : WowsBattlesTypeRu.values()) {
            if (v.code.equalsIgnoreCase(code)) {
                return v;
            }
        }
        throw new IllegalArgumentException(code + " 匹配不到对应莱服战斗类型");
    }

    @Override
    public String toString() {
        return super.toString().toUpperCase(Locale.ROOT);
    }
}
