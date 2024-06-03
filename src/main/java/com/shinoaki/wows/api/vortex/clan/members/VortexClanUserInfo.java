package com.shinoaki.wows.api.vortex.clan.members;


import com.fasterxml.jackson.databind.JsonNode;
import com.shinoaki.wows.api.type.WowsServer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author Xun
 * @date 2022/07/21 星期四
 */
public record VortexClanUserInfo(long accountId, String nickName, WowsServer wowsServer, long lastBattleTime, String roleName, int roleCode, int daysInClan,
                                 Boolean isBonusActivated, double battlesPerDay, double damagePerBattle, int rank, double expPerBattle, Boolean onlineStatus,
                                 int battlesCount, Boolean isPress, int seasonId, Boolean isHiddenStatistics, double winsPercentage, Boolean abnormalResults,
                                 double fragsPerBattle, Boolean isBanned, String profileLink) {
    public static VortexClanStatisticsInfo to(WowsServer server, JsonNode body) {
        if (body.get("status").asText("error").contentEquals("ok")) {
            List<VortexClanUserInfo> list = new ArrayList<>();
            JsonNode items = body.get("items");
            for (JsonNode js : items) {
                JsonNode role = js.get("role");
                if (js.get("is_hidden_statistics").asBoolean()) {
                    list.add(new VortexClanUserInfo(
                            js.get("id").asLong(),
                            js.get("name").asText(),
                            server,
                            0,
                            role.get("name").asText(),
                            role.get("order").asInt(),
                            js.get("days_in_clan").asInt(),
                            null,
                            0.0,
                            0.0,
                            0,
                            0.0,
                            null,
                            0,
                            null,
                            0,
                            js.get("is_hidden_statistics").asBoolean(),
                            0.0,
                            null,
                            0.0,
                            null,
                            js.get("profile_link").asText()
                    ));
                } else {
                    list.add(new VortexClanUserInfo(
                            js.get("id").asLong(),
                            js.get("name").asText(),
                            server,
                            js.get("last_battle_time").asInt(),
                            role.get("name").asText(),
                            role.get("order").asInt(),
                            js.get("days_in_clan").asInt(),
                            Optional.ofNullable(js.get("is_bonus_activated")).map(JsonNode::asBoolean).orElse(Boolean.FALSE),
                            js.get("battles_per_day").asDouble(),
                            js.get("damage_per_battle").asDouble(),
                            Optional.ofNullable(js.get("rank")).map(JsonNode::asInt).orElse(0),
                            js.get("exp_per_battle").asDouble(),
                            js.get("online_status").asBoolean(),
                            js.get("battles_count").asInt(),
                            js.get("is_press").asBoolean(),
                            Optional.ofNullable(js.get("season_id")).map(JsonNode::asInt).orElse(0),
                            js.get("is_hidden_statistics").asBoolean(),
                            js.get("wins_percentage").asDouble(),
                            js.get("abnormal_results").asBoolean(),
                            Optional.ofNullable(js.get("frags_per_battle")).map(JsonNode::asDouble).orElse(0.0),
                            js.get("is_banned").asBoolean(),
                            js.get("profile_link").asText()
                    ));
                }
            }
            var statistics = body.get("clan_statistics");
            if (!statistics.isEmpty()) {
                return new VortexClanStatisticsInfo(statistics.get("battles_count").asDouble(),
                        statistics.get("wins_percentage").asDouble(),
                        statistics.get("exp_per_battle").asDouble(),
                        statistics.get("damage_per_battle").asDouble(),
                        list);
            }
        }
        return VortexClanStatisticsInfo.empty();
    }
}
