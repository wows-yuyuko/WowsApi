package com.shinoaki.wows.api.developers.encyclopedia.shipprofile;

import java.util.Map;

/**
 * 主炮组：口径配置（slots）与弹种数据（shells，按弹种代码 HE/AP 索引）。
 *
 * @author Xun
 */
public record DevelopersShipArtilleryProfile(long artillery_id, String artillery_id_str, double distance,
                                             double gun_rate, int max_dispersion, double rotation_time, double shot_delay,
                                             Map<String, DevelopersShipArtillerySlot> slots,
                                             Map<String, DevelopersShipShell> shells) {
}
