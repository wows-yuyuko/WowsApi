package com.shinoaki.wows.api.developers.encyclopedia.shipprofile;

import com.shinoaki.wows.api.error.BasicException;
import com.shinoaki.wows.api.error.HttpThrowableStatus;
import com.shinoaki.wows.api.utils.JsonUtils;
import tools.jackson.databind.JsonNode;

/**
 * 舰船参数明细（encyclopedia/shipprofile 的响应体，也是 ships 字典里 default_profile 的结构）。
 * <p>
 * 各武器/系统块按舰种「有则出现」：缺失或不适用的块解析为 null（如战列舰的 torpedoes、航母的 artillery）。
 * 被 ships 字典嵌入时不含 ship_id（解析为 0）。
 *
 * @author Xun
 */
public record DevelopersShipProfile(long ship_id, int battle_level_range_min, int battle_level_range_max,
                                    DevelopersShipHullProfile hull, DevelopersShipEngineProfile engine,
                                    DevelopersShipArtilleryProfile artillery, DevelopersShipTorpedoProfile torpedoes,
                                    DevelopersShipFireControl fire_control, DevelopersShipFlightControl flight_control,
                                    DevelopersShipFighters fighters, DevelopersShipTorpedoBomber torpedo_bomber,
                                    DevelopersShipDiveBomber dive_bomber, DevelopersShipSonarProfile submarine_sonar,
                                    DevelopersShipAtbaProfile atbas, DevelopersShipAntiAircraftProfile anti_aircraft,
                                    DevelopersShipArmourProfile armour, DevelopersShipMobility mobility,
                                    DevelopersShipConcealment concealment, DevelopersShipWeaponry weaponry,
                                    DevelopersShipSubmarineBattery submarine_battery,
                                    DevelopersShipSubmarineMobility submarine_mobility,
                                    DevelopersShipDepthCharge depth_charge) {

    public static DevelopersShipProfile parse(JsonNode node) throws BasicException {
        BasicException.status(node);
        for (var entry : node.path("data").properties()) {
            return JsonUtils.json().parse(entry.getValue(), DevelopersShipProfile.class);
        }
        throw new BasicException(HttpThrowableStatus.DATA_STATUS, "舰船参数响应为空");
    }
}
