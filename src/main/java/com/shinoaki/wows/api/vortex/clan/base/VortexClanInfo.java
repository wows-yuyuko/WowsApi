package com.shinoaki.wows.api.vortex.clan.base;

import com.shinoaki.wows.api.type.WowsServer;
import com.shinoaki.wows.api.utils.DateUtils;
import com.shinoaki.wows.api.vortex.clan.base.info.VortexClanAchievement;
import com.shinoaki.wows.api.vortex.clan.base.info.VortexClanBuildingsInfo;
import com.shinoaki.wows.api.vortex.clan.base.info.VortexClanRecruitingRestrictions;
import com.shinoaki.wows.api.vortex.clan.base.info.VortexClanWowsLadderInfo;
import tools.jackson.databind.JsonNode;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 军团详情，对应 {@code /api/clanbase/{id}/claninfo/} 的 {@code clanview}。
 * <p>
 * 响应字段：clan(含下面除 achievements/buildings/wowsLadder 外的全部字段)、achievements、buildings、wows_ladder。
 * 响应里的 {@code clan.id} 与入参 clanId 相同，故不再重复暴露。
 *
 * @param clanId                 军团ID(入参)
 * @param wowsServer             服务器
 * @param tag                    显示tag
 * @param name                   公会名称
 * @param description            介绍
 * @param membersCount           公会成员数量
 * @param maxMembersCount        最大成员数
 * @param colorRgb               公会标签颜色
 * @param createdAt              公会创建时间
 * @param wowsLadder             军团排位数据
 * @param clanBuildingsInfoList  军团建筑
 * @param achievements           军团成就
 * @param motto                  军团格言
 * @param rawDescription         未处理过的介绍(含换行与转义)
 * @param recruitingPolicy       招募策略
 * @param isDisbanded            是否已解散
 * @param isApplicationSent      是否已发送入团申请
 * @param isInviteReceived      是否收到邀请
 * @param isSuitedForAutorecruiting 是否符合自动招募
 * @param leveling               军团等级
 * @param accumulativeResource   累计军团资源
 * @param personalResource       个人军团资源
 * @param communityUrls          社区链接
 * @param preModeration          预审核名单(结构随服务端变化,保留原始节点)
 * @param stats                  各战斗类型场次
 * @param recruitingRestrictions 招募条件
 * @author Xun
 * @date 2023/4/10 16:25 星期一
 */
public record VortexClanInfo(long clanId, WowsServer wowsServer, String tag, String name, String description, int membersCount, int maxMembersCount,
                             String colorRgb, long createdAt, VortexClanWowsLadderInfo wowsLadder,
                             List<VortexClanBuildingsInfo> clanBuildingsInfoList,
                             List<VortexClanAchievement> achievements,
                             String motto, String rawDescription, String recruitingPolicy,
                             boolean isDisbanded, boolean isApplicationSent, boolean isInviteReceived, boolean isSuitedForAutorecruiting,
                             Integer leveling, Long accumulativeResource, Long personalResource,
                             Map<String, String> communityUrls, JsonNode preModeration, Map<String, Long> stats,
                             VortexClanRecruitingRestrictions recruitingRestrictions) {

    public static VortexClanInfo to(WowsServer server, long clanId, JsonNode body) {
        JsonNode clanview = body.path("clanview");
        JsonNode clan = clanview.path("clan");
        if (clan.isMissingNode() || clan.isNull()) {
            return null;
        }
        return new VortexClanInfo(
                clanId,
                server,
                clan.path("tag").asString(),
                clan.path("name").asString(),
                clan.path("description").asString(),
                clan.path("members_count").asInt(),
                clan.path("max_members_count").asInt(),
                clan.path("color").asString(),
                parseCreatedAt(clan.path("created_at")),
                VortexClanWowsLadderInfo.parse(clanId, server == WowsServer.RU ? clanview.path("mk_ladder") : clanview.path("wows_ladder")),
                VortexClanBuildingsInfo.clan(clanId, clanview.path("buildings")),
                VortexClanAchievement.parse(clanview.path("achievements")),
                clan.path("motto").asString(""),
                clan.path("raw_description").asString(""),
                clan.path("recruiting_policy").asString(""),
                clan.path("is_disbanded").asBoolean(false),
                clan.path("is_application_sent").asBoolean(false),
                clan.path("is_invite_received").asBoolean(false),
                clan.path("is_suited_for_autorecruiting").asBoolean(false),
                numberOrNull(clan.path("leveling")),
                longOrNull(clan.path("accumulative_resource")),
                longOrNull(clan.path("personal_resource")),
                stringMap(clan.path("community_urls")),
                clan.path("pre_moderation"),
                longMap(clan.path("stats")),
                VortexClanRecruitingRestrictions.parse(clan.path("recruiting_restrictions"))
        );
    }

    private static Integer numberOrNull(JsonNode node) {
        return node != null && node.isNumber() ? node.asInt() : null;
    }

    private static Long longOrNull(JsonNode node) {
        return node != null && node.isNumber() ? node.asLong() : null;
    }

    private static Map<String, String> stringMap(JsonNode node) {
        Map<String, String> map = new LinkedHashMap<>();
        if (node == null || !node.isObject()) {
            return map;
        }
        for (Map.Entry<String, JsonNode> entry : node.properties()) {
            map.put(entry.getKey(), entry.getValue().asString(""));
        }
        return map;
    }

    /**
     * 各战斗类型场次，例：{@code {"cvc": 0, "pvp": 1030}}。
     */
    private static Map<String, Long> longMap(JsonNode node) {
        Map<String, Long> map = new LinkedHashMap<>();
        if (node == null || !node.isObject()) {
            return map;
        }
        for (Map.Entry<String, JsonNode> entry : node.properties()) {
            map.put(entry.getKey(), entry.getValue().isNumber() ? entry.getValue().asLong() : 0L);
        }
        return map;
    }

    private static long parseCreatedAt(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return 0;
        }
        String value = node.asString("");
        if (value.isEmpty()) {
            return 0;
        }
        try {
            return DateUtils.toTimeMillis(LocalDateTime.parse(value, DateTimeFormatter.ISO_DATE_TIME));
        } catch (DateTimeParseException ignore) {
            try {
                return DateUtils.toTimeMillis(OffsetDateTime.parse(value, DateTimeFormatter.ISO_DATE_TIME).toLocalDateTime());
            } catch (DateTimeParseException e) {
                return 0;
            }
        }
    }
}
