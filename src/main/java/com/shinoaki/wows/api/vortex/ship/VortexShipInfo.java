package com.shinoaki.wows.api.vortex.ship;

import com.fasterxml.jackson.databind.JsonNode;
import com.shinoaki.wows.api.error.BasicException;
import com.shinoaki.wows.api.utils.WowsJsonUtils;

/**
 * 用户战绩信息
 * {@code https://vortex.worldofwarships.asia/api/accounts/2022515210/ships/pvp/}
 *
 * @param battles_count                 战斗场次
 * @param battles_count_512             战斗场次(意义不明)
 * @param battles_count_510             战斗场次(意义不明)
 * @param battles_count_078
 * @param battles_count_0910            战斗场次(意义不明)
 *                                      命中率信息
 * @param hits_by_skip
 * @param shots_by_skip
 * @param hits_by_atba
 * @param shots_by_atba
 * @param hits_by_rocket
 * @param shots_by_rocket
 * @param hits_by_bomb
 * @param shots_by_bomb
 * @param hits_by_tbomb
 * @param shots_by_tbomb
 * @param hits_by_tpd                   鱼雷命中次数
 * @param shots_by_tpd                  鱼雷发射次数
 * @param hits_by_main                  主武器命中次数
 * @param shots_by_main                 主武器发射次数
 *                                      最高记录
 * @param max_frags                     单场最多击杀
 * @param max_frags_by_main             主炮
 * @param max_frags_by_tpd              鱼雷
 * @param max_frags_by_dbomb            深水炸弹
 * @param max_frags_by_ram              撞击
 * @param max_frags_by_atba
 * @param max_frags_by_planes           最高使用舰载机击杀
 * @param max_damage_dealt              最高伤害
 * @param max_damage_dealt_to_buildings
 * @param max_scouting_damage           最大侦查伤害
 * @param max_planes_killed             最高击落飞机
 * @param max_ships_spotted             最高发现战舰
 * @param max_total_agro                最高潜在伤害
 * @param max_suppressions_count
 * @param max_premium_exp               最高会员经验
 * @param max_exp
 * @param exp                           获得的总经验
 * @param premium_exp                   会员经验
 * @param ships_spotted                 发现战舰数
 * @param planes_killed                 击落飞机数
 * @param damage_dealt_to_buildings
 * @param frags_by_tpd                  鱼雷击杀
 * @param art_agro                      主武器潜在?
 * @param battles_count_0711            战斗场次(意义不明)
 * @param original_exp                  基础经验
 * @param frags                         击杀数
 * @param frags_by_planes               舰载机击杀
 * @param capture_points
 * @param frags_by_ram                  撞击击沉
 * @param frags_by_dbomb                深水炸弹击杀
 * @param suppressions_count
 * @param survived                      存活场次
 * @param frags_by_atba                 副炮击沉
 * @param scouting_damage               侦查输出
 * @param tpd_agro                      鱼雷潜在?
 * @param wins                          胜利场次
 * @param losses                        失败次数
 * @param win_and_survived              胜利并且存活的次数
 * @param damage_dealt                  总伤害
 * @param frags_by_main                 主武器击杀
 * @param dropped_capture_points        掉落的占领点(机翻) 基地防御点
 * @param control_captured_points       控制捕获点(机翻)   基地占领点
 * @param control_dropped_points        控制掉点(机翻)    占领时被打掉的点数
 * @param team_control_captured_points  团队控制占领点(机翻) 团队防御点
 * @param team_control_dropped_points   团队控制丢分(机翻)  团队占领点
 * @author Xun
 * @date 2023/3/18 11:39 星期六
 */
public record VortexShipInfo(long max_frags_by_main, long battles_count_512, long battles_count_510, long hits_by_skip, long hits_by_atba, long shots_by_main,
                             long hits_by_rocket, long ships_spotted, long hits_by_bomb, long max_scouting_damage, long premium_exp, long dropped_capture_points,
                             long max_frags_by_dbomb, long control_captured_points, long max_planes_killed, long battles_count, long planes_killed,
                             long damage_dealt_to_buildings, long frags_by_tpd, long max_ships_spotted, long art_agro, long max_frags_by_ram, long battles_count_0711,
                             long shots_by_tbomb, long original_exp, long frags, long max_frags_by_planes, long frags_by_planes, long max_total_agro,
                             long shots_by_tpd, long capture_points, long frags_by_ram, long shots_by_bomb, long frags_by_dbomb, long max_suppressions_count,
                             long suppressions_count, long max_damage_dealt_to_buildings, long shots_by_rocket, long survived, long scouting_damage,
                             long frags_by_atba, long shots_by_skip, long control_dropped_points, long max_damage_dealt, long max_frags_by_tpd, long max_premium_exp,
                             long hits_by_tpd, long max_frags_by_atba, long tpd_agro, long team_control_captured_points, long wins, long losses, long hits_by_main,
                             long win_and_survived, long damage_dealt, long battles_count_078, long frags_by_main, long team_control_dropped_points, long max_frags,
                             long exp, long max_exp, long battles_count_0910, long hits_by_tbomb, long shots_by_atba) {

    public static VortexShipInfo parse(JsonNode node) throws BasicException {
        if (node.isEmpty()) {
            return VortexShipInfo.defaultValue();
        }
        return new WowsJsonUtils().parse(node, VortexShipInfo.class);
    }

    public static VortexShipInfo defaultValue() {
        return new VortexShipInfo(0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L,
                0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L);
    }


    /**
     * 场均击杀
     *
     * @return 结果
     */
    public double avgFrags() {
        if (this.frags() <= 0 || this.battles_count() <= 0) {
            return 0.0;
        }
        return (double) this.frags() / this.battles_count();

    }


    /**
     * 场均
     *
     * @return 场均
     */
    public double avgDamage() {
        if (this.battles_count() <= 0 || this.damage_dealt() <= 0) {
            return 0.0;
        }
        return (double) this.damage_dealt() / this.battles_count();
    }

    /**
     * 胜率
     *
     * @return 胜率
     */
    public double avgWins() {
        if (this.battles_count() <= 0 || this.wins() <= 0) {
            return 0.0;
        }
        return 100.0 * ((double) this.wins() / this.battles_count());
    }

    /**
     * 经验
     *
     * @return 经验
     */
    public double avgXp() {
        if (this.battles_count() <= 0 || this.original_exp() <= 0) {
            return 0.0;
        }
        return (double) this.original_exp() / this.battles_count();
    }

    /**
     * KD
     *
     * @return kd
     */
    public double avgKd() {
        long v = this.battles_count() - this.survived();
        if (v <= 0 || this.frags() <= 0) {
            return 0.0;
        } else {
            return (double) this.frags() / v;
        }
    }

    /**
     * 主武器命中率
     *
     * @return 命中率
     */
    public double avgMainHitRatio() {
        if (this.hits_by_main() <= 0 || this.shots_by_main() <= 0) {
            return 0.0;
        }
        return 100.0 * ((double) this.hits_by_main() / this.shots_by_main());
    }

    /**
     * 鱼雷命中率
     *
     * @return 鱼雷命中
     */
    public double avgTorpedoesHitRatio() {
        if (this.hits_by_tpd() <= 0 || this.shots_by_tpd() <= 0) {
            return 0;
        }
        return 100.0 * ((double) this.hits_by_tpd() / this.shots_by_tpd());
    }

    /**
     * 占领贡献
     *
     * @return 进攻贡献率
     */
    public double avgContributionToCapture() {
        return 100.0 * ((double) this.team_control_captured_points() / this.control_captured_points());
    }

    /**
     * 防御贡献
     *
     * @return 防御贡献率
     */
    public double avgContributionToDefense() {
        return 100.0 * ((double) this.team_control_dropped_points() / this.control_dropped_points());
    }
}
