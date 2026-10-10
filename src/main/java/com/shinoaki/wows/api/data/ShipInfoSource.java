package com.shinoaki.wows.api.data;

/**
 * {@link ShipInfo} 的数据来源。
 * <p>
 * 两条通道（vortex 官网接口 / 官方开发者API）能提供的字段并不完全一致：
 * <ul>
 *     <li><b>官方开发者API 缺少</b>：基础经验（vortex 的 {original_exp}/{max_exp}）、深水炸弹击杀、
 *     深弹/炸弹/火箭/跳弹命中数、精通徽章（需另查 {@code /wows/ships/badges/}，且莱服不支持）；</li>
 *     <li><b>vortex 单船接口缺少</b>：该船的最后战斗时间（官方API 的单船数据里有）。</li>
 * </ul>
 * 因此 {@link ShipInfo} 带上来源标记，供调用方判断"这个字段在这份数据里是不是有意义的"：
 * 官方API 侧 {@code basicXp} 永远是 0，如果直接把它和 vortex 的数据相加，就会得到"一半是 0 的合计"。
 * <p>
 * 聚合（{@link ShipInfo#addition}/{@link ShipInfo#subtraction}）时只有**同源**才保留来源，
 * 混合来源退化为 {@link #MIXED}，相关可用性判断一律返回 false（最保守）。
 *
 * @author Xun
 */
public enum ShipInfoSource {
    /**
     * vortex 官网接口（{@code /api/accounts/{id}/ships/{type}/}）
     */
    VORTEX,
    /**
     * 官方开发者API（{@code /wows/ships/stats/}）
     */
    DEVELOPERS,
    /**
     * 两条通道混合聚合：字段集不一定一致，缺字段按最保守处理
     */
    MIXED,
    /**
     * 未知来源（例如 PR 计算构造的占位数据）
     */
    UNKNOWN;

    /**
     * 是否提供基础经验（vortex 的 original_exp / max_exp）。
     */
    public boolean providesBasicXp() {
        return this == VORTEX;
    }

    /**
     * 是否提供深水炸弹相关数据（击杀与最高击杀）。
     */
    public boolean providesDbomb() {
        return this == VORTEX;
    }

    /**
     * 是否提供深弹/炸弹/火箭/跳弹的命中数据。
     */
    public boolean providesOtherHitRatio() {
        return this == VORTEX;
    }

    /**
     * 是否提供该船的最后战斗时间。
     */
    public boolean providesLastBattleTime() {
        return this == DEVELOPERS;
    }

    /**
     * 是否提供精通徽章等级（官方API 需要另外查 {@code /wows/ships/badges/}）。
     */
    public boolean providesExpansion() {
        return this == VORTEX;
    }

    /**
     * 聚合后的来源：同源保留，异源退化为 {@link #MIXED}。
     */
    public static ShipInfoSource merge(ShipInfoSource a, ShipInfoSource b) {
        if (a == b) {
            return a;
        }
        return MIXED;
    }
}
