package com.shinoaki.wows.api.vortex.clan.members;


import com.shinoaki.wows.api.type.WowsServer;
import tools.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Xun
 * @date 2022/07/21 星期四
 */
public record VortexClanUserInfo(long accountId, String nickName, WowsServer wowsServer, long lastBattleTime, String roleName, int roleCode, int daysInClan,
                                 Boolean isBonusActivated, double battlesPerDay, double damagePerBattle, int rank, double expPerBattle, Boolean onlineStatus,
                                 int battlesCount, Boolean isPress, int seasonId, Boolean isHiddenStatistics, double winsPercentage, Boolean abnormalResults,
                                 double fragsPerBattle, Boolean isBanned, String profileLink) {
    public static VortexClanStatisticsInfo to(WowsServer server, JsonNode body) {
        if (body.path("status").asString("error").contentEquals("ok")) {
            List<VortexClanUserInfo> list = new ArrayList<>();
            JsonNode items = body.path("items");
            for (JsonNode js : items) {
                JsonNode role = js.path("role");
                if (js.path("is_hidden_statistics").asBoolean()) {
                    list.add(new VortexClanUserInfo(
                            js.path("id").asLong(),
                            js.path("name").asString(),
                            server,
                            0,
                            role.path("name").asString(),
                            role.path("order").asInt(),
                            js.path("days_in_clan").asInt(),
                            null,
                            0.0,
                            0.0,
                            0,
                            0.0,
                            null,
                            0,
                            null,
                            0,
                            js.path("is_hidden_statistics").asBoolean(),
                            0.0,
                            null,
                            0.0,
                            null,
                            js.path("profile_link").asString()
                    ));
                } else {
                    list.add(new VortexClanUserInfo(
                            js.path("id").asLong(),
                            js.path("name").asString(),
                            server,
                            js.path("last_battle_time").asInt(),
                            role.path("name").asString(),
                            role.path("order").asInt(),
                            js.path("days_in_clan").asInt(),
                            js.path("is_bonus_activated").asBoolean(),
                            js.path("battles_per_day").asDouble(),
                            js.path("damage_per_battle").asDouble(),
                            js.path("rank").asInt(),
                            js.path("exp_per_battle").asDouble(),
                            js.path("online_status").asBoolean(),
                            js.path("battles_count").asInt(),
                            js.path("is_press").asBoolean(),
                            js.path("season_id").asInt(),
                            js.path("is_hidden_statistics").asBoolean(),
                            js.path("wins_percentage").asDouble(),
                            js.path("abnormal_results").asBoolean(),
                            js.path("frags_per_battle").asDouble(),
                            js.path("is_banned").asBoolean(),
                            js.path("profile_link").asString()
                    ));
                }
            }
            var statistics = body.path("clan_statistics");
            if (!statistics.isEmpty()) {
                return new VortexClanStatisticsInfo(statistics.path("battles_count").asDouble(),
                        statistics.path("wins_percentage").asDouble(),
                        statistics.path("exp_per_battle").asDouble(),
                        statistics.path("damage_per_battle").asDouble(),
                        list);
            }
        }
        return VortexClanStatisticsInfo.empty();
    }
}
