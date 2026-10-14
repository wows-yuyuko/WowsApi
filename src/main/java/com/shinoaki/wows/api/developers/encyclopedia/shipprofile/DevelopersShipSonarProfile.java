package com.shinoaki.wows.api.developers.encyclopedia.shipprofile;

/**
 * 水听（潜艇声呐）：脉冲参数。
 *
 * @author Xun
 */
public record DevelopersShipSonarProfile(long submarine_sonar_id, String submarine_sonar_id_str, int total,
                                         int wave_duration_0, int wave_duration_1, double wave_max_dist,
                                         double wave_shot_delay, int wave_speed_max, int wave_width) {
}
