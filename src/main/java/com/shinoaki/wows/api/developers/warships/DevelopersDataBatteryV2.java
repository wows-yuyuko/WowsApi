package com.shinoaki.wows.api.developers.warships;

/**
 * 武器数据块（aircraft / ramming，没有命中与射击数）。
 * <p>
 * 例：{@code {"frags": 1519, "max_frags_battle": 5, "max_frags_ship_id": 4179604944}}
 *
 * @param max_frags_battle  单场最多击杀
 * @param frags             击杀总数
 * @param max_frags_ship_id 单场最多击杀所用战舰ID
 * @author Xun
 * @date 2023/4/24 20:59 星期一
 */
public record DevelopersDataBatteryV2(int max_frags_battle, int frags, long max_frags_ship_id) {
}
