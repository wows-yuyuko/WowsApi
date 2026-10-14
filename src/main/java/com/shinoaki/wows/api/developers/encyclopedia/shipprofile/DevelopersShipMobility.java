package com.shinoaki.wows.api.developers.encyclopedia.shipprofile;

/**
 * 机动性：航速、转舵时间、旋转半径。
 *
 * @author Xun
 */
public record DevelopersShipMobility(double max_speed, double rudder_time, int total, int turning_radius) {
}
