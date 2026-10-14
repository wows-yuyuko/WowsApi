package com.shinoaki.wows.api.developers.seasons;

import com.shinoaki.wows.api.developers.warships.type.DevelopersShipBattleType;

/**
 * 排位战斗数据块（rank_div2 / rank_div3 通常为 null，单人排位只填 rank_solo）。
 * <p>
 * 数据块与随机战共用 {@link DevelopersShipBattleType}（排位侧只出现其子集，缺失字段恒为 0）。
 *
 * @author Xun
 */
public record DevelopersRankedShipSeasonBlock(DevelopersShipBattleType rank_solo, DevelopersShipBattleType rank_div2,
                                              DevelopersShipBattleType rank_div3) {
}
