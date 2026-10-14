package com.shinoaki.wows.api.developers.encyclopedia.shipprofile;

/**
 * 潜艇机动性：下潜/上浮速度与操舵。
 *
 * @author Xun
 */
public record DevelopersShipSubmarineMobility(double buoyancy_rudder_time, double max_buoyancy_speed,
                                              int max_speed_under_water, int total) {
}
