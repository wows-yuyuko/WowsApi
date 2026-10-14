package com.shinoaki.wows.api.developers.encyclopedia.shipprofile;

/**
 * 隐蔽性：对空/对海/对潜发现距离。
 *
 * @author Xun
 */
public record DevelopersShipConcealment(double detect_distance_by_plane, double detect_distance_by_ship,
                                        double detect_distance_by_submarine, int total) {
}
