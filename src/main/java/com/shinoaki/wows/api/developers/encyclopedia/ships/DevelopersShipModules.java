package com.shinoaki.wows.api.developers.encyclopedia.ships;

import java.util.List;

/**
 * 舰船各系统的模块ID列表（系统缺失时值为 null）。
 *
 * @author Xun
 */
public record DevelopersShipModules(List<Long> artillery, List<Long> fire_control, List<Long> torpedoes,
                                    List<Long> flight_control, List<Long> fighter, List<Long> torpedo_bomber,
                                    List<Long> dive_bomber, List<Long> hull, List<Long> engine, List<Long> sonar) {
}
