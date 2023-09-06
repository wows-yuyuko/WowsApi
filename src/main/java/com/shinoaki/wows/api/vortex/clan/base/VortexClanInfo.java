package com.shinoaki.wows.api.vortex.clan.base;

import com.fasterxml.jackson.databind.JsonNode;
import com.shinoaki.wows.api.type.WowsServer;
import com.shinoaki.wows.api.utils.DateUtils;
import com.shinoaki.wows.api.vortex.clan.base.info.VortexClanBuildingsInfo;
import com.shinoaki.wows.api.vortex.clan.base.info.VortexClanLeagueInfo;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * @author Xun
 * @date 2023/4/10 16:25 星期一
 */
@Data
public class VortexClanInfo {
    private long clanId;
    /**
     * 服务器
     */
    private WowsServer wowsServer;
    /**
     * 显示tag
     */
    private String tag;
    /**
     * 公会名称
     */
    private String name;
    /**
     * 介绍
     */
    private String description;
    /**
     * 公会成员数量
     */
    private int membersCount;
    /**
     * 最大成员数
     */
    private int maxMembersCount;
    /**
     * 公会标签颜色
     */
    private String colorRgb;
    /**
     * 公会创建时间
     */
    private long createdAt;
    /**
     * 资料更新时间
     */
    private long updatedAt;

    private List<VortexClanLeagueInfo> clanLeagueInfoList;
    private List<VortexClanBuildingsInfo> clanBuildingsInfoList;

    public static VortexClanInfo to(WowsServer server, long clanId, JsonNode body) {
        JsonNode clanview = body.get("clanview");
        if (clanview != null) {
            JsonNode clan = clanview.get("clan");
            VortexClanInfo info = new VortexClanInfo();
            info.setClanId(clanId);
            info.setWowsServer(server);
            info.setClanBuildingsInfoList(VortexClanBuildingsInfo.clan(clanId, clanview.get("buildings")));
            info.setClanLeagueInfoList(VortexClanLeagueInfo.clan(clanId, clanview.get("wows_ladder").get("ratings")));
            info.setTag(clan.get("tag").asText());
            info.setName(clan.get("name").asText());
            info.setDescription(clan.get("description").asText());
            info.setMembersCount(clan.get("members_count").asInt());
            info.setMaxMembersCount(clan.get("max_members_count").asInt());
            info.setColorRgb(clan.get("color").asText());
            info.setCreatedAt(DateUtils.toTimeMillis(LocalDateTime.parse(clan.get("created_at").asText(), DateTimeFormatter.ISO_DATE_TIME)));
            return info;
        }
        return null;
    }
}
