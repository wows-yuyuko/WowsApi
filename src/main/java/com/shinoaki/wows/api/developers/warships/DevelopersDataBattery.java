package com.shinoaki.wows.api.developers.warships;

/**
 * 武器数据块（main_battery / second_battery / torpedoes）。
 * <p>
 * 例：{@code {"frags": 7694, "max_frags_battle": 8, "max_frags_ship_id": 4290689008, "shots": 2360070, "hits": 813786}}
 *
 * @param max_frags_battle  单场最多击杀
 * @param frags             击杀总数
 * @param hits              命中数
 * @param shots             射击数
 * @param max_frags_ship_id 单场最多击杀所用战舰ID
 * @author Xun
 * @date 2023/4/24 20:53 星期一
 */
public record DevelopersDataBattery(int max_frags_battle, int frags, int hits, int shots, long max_frags_ship_id) {
}
