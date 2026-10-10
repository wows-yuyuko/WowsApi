package com.shinoaki.wows.api.vortex.clan.members;


import com.shinoaki.wows.api.type.WowsServer;
import tools.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;

/**
 * 军团成员信息，对应 {@code /api/members/{id}/} 中 {@code items[]} 的单个成员。
 *
 * @param accountId                 账号ID
 * @param nickName                  昵称
 * @param wowsServer                服务器
 * @param lastBattleTime            最后战斗时间
 * @param roleName                  职位名称
 * @param roleCode                  职位顺序(order)
 * @param roleRank                  职位等级(rank)
 * @param daysInClan                入团天数
 * @param isBonusActivated          是否激活加成
 * @param battlesPerDay             日均场次
 * @param damagePerBattle           场均伤害
 * @param rank                      军衔
 * @param expPerBattle              场均经验
 * @param onlineStatus              在线状态
 * @param battlesCount              场次
 * @param isPress                   是否新闻官
 * @param seasonId                  赛季ID
 * @param seasonRank                赛季军衔
 * @param isHiddenStatistics        是否隐藏统计
 * @param winsPercentage            胜率
 * @param abnormalResults           异常战绩
 * @param fragsPerBattle            场均击杀
 * @param isBanned                  是否被封禁
 * @param bans                      封禁记录(结构随服务端变化,保留原始节点)
 * @param accumulativeClanResource  累计军团资源
 * @param leveling                  军团等级相关
 * @param profileLink               个人资料链接
 * @author Xun
 * @date 2022/07/21 星期四
 */
public record VortexClanUserInfo(long accountId, String nickName, WowsServer wowsServer, long lastBattleTime, String roleName, int roleCode, int roleRank,
                                 int daysInClan, Boolean isBonusActivated, double battlesPerDay, double damagePerBattle, int rank, double expPerBattle,
                                 Boolean onlineStatus, int battlesCount, Boolean isPress, int seasonId, Integer seasonRank, Boolean isHiddenStatistics,
                                 double winsPercentage, Boolean abnormalResults, double fragsPerBattle, Boolean isBanned, JsonNode bans,
                                 Long accumulativeClanResource, Integer leveling, String profileLink) {

    public static VortexClanStatisticsInfo to(WowsServer server, JsonNode body) {
        String status = body.path("status").asString("error");
        if (status.contentEquals("ok")) {
            List<VortexClanUserInfo> list = new ArrayList<>();
            JsonNode items = body.path("items");
            for (JsonNode js : items) {
                JsonNode role = js.path("role");
                boolean hidden = js.path("is_hidden_statistics").asBoolean(false);
                list.add(new VortexClanUserInfo(
                        js.path("id").asLong(),
                        js.path("name").asString(),
                        server,
                        hidden ? 0 : js.path("last_battle_time").asLong(),
                        role.path("name").asString(),
                        role.path("order").asInt(),
                        role.path("rank").asInt(),
                        js.path("days_in_clan").asInt(),
                        hidden ? null : booleanOrNull(js.path("is_bonus_activated")),
                        hidden ? 0.0 : js.path("battles_per_day").asDouble(),
                        hidden ? 0.0 : js.path("damage_per_battle").asDouble(),
                        hidden ? 0 : js.path("rank").asInt(),
                        hidden ? 0.0 : js.path("exp_per_battle").asDouble(),
                        hidden ? null : booleanOrNull(js.path("online_status")),
                        hidden ? 0 : js.path("battles_count").asInt(),
                        hidden ? null : booleanOrNull(js.path("is_press")),
                        js.path("season_id").asInt(),
                        integerOrNull(js.path("season_rank")),
                        hidden,
                        hidden ? 0.0 : js.path("wins_percentage").asDouble(),
                        hidden ? null : booleanOrNull(js.path("abnormal_results")),
                        hidden ? 0.0 : js.path("frags_per_battle").asDouble(),
                        booleanOrNull(js.path("is_banned")),
                        js.path("bans"),
                        longOrNull(js.path("accumulative_clan_resource")),
                        integerOrNull(js.path("leveling")),
                        js.path("profile_link").asString()
                ));
            }
            var statistics = body.path("clan_statistics");
            if (!statistics.isEmpty()) {
                return new VortexClanStatisticsInfo(statistics.path("battles_count").asDouble(),
                        statistics.path("wins_percentage").asDouble(),
                        statistics.path("exp_per_battle").asDouble(),
                        statistics.path("damage_per_battle").asDouble(),
                        list,
                        status,
                        body.path("is_hidden_statistics").asBoolean(false),
                        body.path("last_updated_at").asString(""));
            }
        }
        return VortexClanStatisticsInfo.empty();
    }

    private static Boolean booleanOrNull(JsonNode node) {
        return node != null && node.isBoolean() ? node.asBoolean() : null;
    }

    private static Integer integerOrNull(JsonNode node) {
        return node != null && node.isNumber() ? node.asInt() : null;
    }

    private static Long longOrNull(JsonNode node) {
        return node != null && node.isNumber() ? node.asLong() : null;
    }
}
