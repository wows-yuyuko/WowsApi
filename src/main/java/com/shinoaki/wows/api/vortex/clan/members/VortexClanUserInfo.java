package com.shinoaki.wows.api.vortex.clan.members;


import com.fasterxml.jackson.databind.JsonNode;
import com.shinoaki.wows.api.type.WowsServer;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author Xun
 * @date 2022/07/21 星期四
 */
@Data
public class VortexClanUserInfo {
    private long accountId;
    private String nickName;
    private WowsServer wowsServer;
    private long lastBattleTime;
    private String roleName;
    private int roleCode;
    private int daysInClan;
    private Boolean isBonusActivated;
    private double battlesPerDay;
    private double damagePerBattle;
    private int rank;
    private double expPerBattle;
    private Boolean onlineStatus;
    private int battlesCount;
    private Boolean isPress;
    private int seasonId;
    private Boolean isHiddenStatistics;
    private double winsPercentage;
    private Boolean abnormalResults;
    private double fragsPerBattle;
    private Boolean isBanned;
    private String profileLink;

    public static VortexClanStatisticsInfo to(WowsServer server, JsonNode body) {
        if (body.get("status").asText("error").contentEquals("ok")) {
            List<VortexClanUserInfo> list = new ArrayList<>();
            JsonNode items = body.get("items");
            for (JsonNode js : items) {
                VortexClanUserInfo info = new VortexClanUserInfo();
                info.setAccountId(js.get("id").asLong());
                info.setWowsServer(server);
                info.setNickName(js.get("name").asText());
                info.setDaysInClan(js.get("days_in_clan").asInt());
                JsonNode role = js.get("role");
                info.setRoleName(role.get("name").asText());
                info.setRoleCode(role.get("order").asInt());
                info.setProfileLink(js.get("profile_link").asText());
                info.setIsHiddenStatistics(js.get("is_hidden_statistics").asBoolean());
                //隐藏战绩以下为null
                if (!info.getIsHiddenStatistics()) {
                    info.setLastBattleTime(js.get("last_battle_time").asInt());
                    info.setIsBonusActivated(Optional.ofNullable(js.get("is_bonus_activated")).map(JsonNode::asBoolean).orElse(Boolean.FALSE));
                    info.setBattlesPerDay(js.get("battles_per_day").asInt());
                    info.setDamagePerBattle(js.get("damage_per_battle").asInt());
                    info.setRank(Optional.ofNullable(js.get("rank")).map(JsonNode::asInt).orElse(0));
                    info.setExpPerBattle(js.get("exp_per_battle").asInt());
                    info.setOnlineStatus(js.get("online_status").asBoolean());
                    info.setBattlesCount(js.get("battles_count").asInt());
                    info.setIsPress(js.get("is_press").asBoolean());
                    info.setSeasonId(Optional.ofNullable(js.get("season_id")).map(JsonNode::asInt).orElse(0));
                    info.setWinsPercentage(js.get("wins_percentage").asInt());
                    info.setAbnormalResults(js.get("abnormal_results").asBoolean());
                    info.setFragsPerBattle(Optional.ofNullable(js.get("frags_per_battle")).map(JsonNode::asInt).orElse(0));
                    info.setIsBanned(js.get("is_banned").asBoolean());
                }
                list.add(info);
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
