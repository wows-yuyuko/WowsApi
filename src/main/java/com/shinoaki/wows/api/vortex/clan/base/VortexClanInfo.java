package com.shinoaki.wows.api.vortex.clan.base;

import com.fasterxml.jackson.databind.JsonNode;
import com.shinoaki.wows.api.type.WowsServer;
import com.shinoaki.wows.api.utils.DateUtils;
import com.shinoaki.wows.api.vortex.clan.base.info.VortexClanBuildingsInfo;
import com.shinoaki.wows.api.vortex.clan.base.info.VortexClanWowsLadderInfo;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * @author Xun
 * @date 2023/4/10 16:25 星期一
 * @param wowsServer
服务器
 * @param tag
显示tag
 * @param name
公会名称
 * @param description
介绍
 * @param membersCount
公会成员数量
 * @param maxMembersCount
最大成员数
 * @param colorRgb
公会标签颜色
 * @param createdAt
公会创建时间
 */
public record VortexClanInfo(long clanId, WowsServer wowsServer, String tag, String name, String description, int membersCount, int maxMembersCount,
                             String colorRgb, long createdAt, VortexClanWowsLadderInfo wowsLadder,
                             List<VortexClanBuildingsInfo> clanBuildingsInfoList) {
    public static VortexClanInfo to(WowsServer server, long clanId, JsonNode body) {
        JsonNode clanview = body.get("clanview");
        if (clanview != null) {
            JsonNode clan = clanview.get("clan");
            return new VortexClanInfo(
                    clanId,
                    server,
                    clan.get("tag").asText(),
                    clan.get("name").asText(),
                    clan.get("description").asText(),
                    clan.get("members_count").asInt(),
                    clan.get("max_members_count").asInt(),
                    clan.get("color").asText(),
                    DateUtils.toTimeMillis(LocalDateTime.parse(clan.get("created_at").asText(), DateTimeFormatter.ISO_DATE_TIME)),
                    VortexClanWowsLadderInfo.parse(clanId, clanview.get("wows_ladder")),
                    VortexClanBuildingsInfo.clan(clanId, clanview.get("buildings"))
            );
        }
        return null;
    }
}
