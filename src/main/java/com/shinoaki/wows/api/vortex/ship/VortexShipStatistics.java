package com.shinoaki.wows.api.vortex.ship;

/**
 * 单条战舰的战绩：战斗数据 + 精通签名 + 优秀标记。
 * <p>
 * 对应 {@code /api/accounts/{id}/ships/{type}/} 中 {@code data.<accountId>.statistics.<shipId>}。
 *
 * @param shipInfo        战斗数据(该 type 对应的数据块)
 * @param masterySign     精通签名
 * @param markOfExcellence 优秀标记(personal / division)
 * @author Xun
 */
public record VortexShipStatistics(VortexShipInfo shipInfo, String masterySign, VortexShipMarkOfExcellence markOfExcellence) {
}
