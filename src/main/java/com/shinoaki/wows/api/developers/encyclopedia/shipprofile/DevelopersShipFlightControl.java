package com.shinoaki.wows.api.developers.encyclopedia.shipprofile;

/**
 * 飞行控制：各中队数量。
 *
 * @author Xun
 */
public record DevelopersShipFlightControl(long flight_control_id, String flight_control_id_str, int fighter_squadrons,
                                          int bomber_squadrons, int torpedo_squadrons) {
}
