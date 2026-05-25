package com.shinoaki.wows.api.vortex.clan.base;

import com.shinoaki.wows.api.type.WowsServer;
import com.shinoaki.wows.api.utils.DateUtils;
import com.shinoaki.wows.api.vortex.clan.base.info.VortexClanBuildingsInfo;
import com.shinoaki.wows.api.vortex.clan.base.info.VortexClanWowsLadderInfo;
import tools.jackson.databind.JsonNode;

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
        JsonNode clanview = body.path("clanview");
        if (clanview != null) {
            JsonNode clan = clanview.path("clan");
            return new VortexClanInfo(
                    clanId,
                    server,
                    clan.path("tag").asString(),
                    clan.path("name").asString(),
                    clan.path("description").asString(),
                    clan.path("members_count").asInt(),
                    clan.path("max_members_count").asInt(),
                    clan.path("color").asString(),
                    DateUtils.toTimeMillis(LocalDateTime.parse(clan.path("created_at").asString(), DateTimeFormatter.ISO_DATE_TIME)),
                    VortexClanWowsLadderInfo.parse(clanId, server == WowsServer.RU ? clanview.path("mk_ladder") : clanview.path("wows_ladder")),
                    VortexClanBuildingsInfo.clan(clanId, clanview.path("buildings"))
            );
        }
        return null;
    }
}
