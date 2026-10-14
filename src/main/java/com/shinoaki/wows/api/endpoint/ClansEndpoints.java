package com.shinoaki.wows.api.endpoint;

import com.shinoaki.wows.api.transport.Channel;
import com.shinoaki.wows.api.utils.JsonUtils;
import com.shinoaki.wows.api.vortex.clan.VortexSearchClan;
import com.shinoaki.wows.api.vortex.clan.account.VortexSearchClanUser;
import com.shinoaki.wows.api.vortex.clan.base.VortexClanInfo;
import com.shinoaki.wows.api.vortex.clan.members.VortexClanStatisticsInfo;
import com.shinoaki.wows.api.vortex.clan.members.VortexClanUserInfo;
import com.shinoaki.wows.api.vortex.clan.rank.ClanRankInfo;
import tools.jackson.core.type.TypeReference;

import java.util.List;

/**
 * clans 通道端点表：主机由 ServerProfile 注入，端点定义不含域名。
 *
 * @author Xun
 */
public final class ClansEndpoints {

    private ClansEndpoints() {
    }

    /**
     * 玩家所属军团（在 vortex 主机上，不是 clans 主机）；非 API 服务器（CN）404 → 空对象（旧语义）。
     */
    public static final Endpoint<VortexSearchClanUser> ACCOUNT_CLAN = Endpoint.of(
                    "clans.account.clan",
                    Channel.VORTEX,
                    "/api/accounts/{accountId}/clans/",
                    List.of(),
                    (node, request, profile) -> VortexSearchClanUser.to(node))
            .withNotFoundEmpty(() -> new VortexSearchClanUser("", VortexSearchClanUser.VortexSearchClanInfo.empty(), "", 0));

    /**
     * 军团详情（莱服 ladder 字段是 mk_ladder，解析层按服务器区分）。
     */
    public static final Endpoint<VortexClanInfo> INFO = Endpoint.of(
            "clans.info",
            Channel.CLANS,
            "/api/clanbase/{clanId}/claninfo/",
            List.of(),
            (node, request, profile) -> VortexClanInfo.to(profile.server(), request.longParam("clanId"), node));

    /**
     * 军团成员战绩（battle_type 默认 pvp；cvc 支持 season）。
     */
    public static final Endpoint<VortexClanStatisticsInfo> MEMBERS = Endpoint.of(
            "clans.members",
            Channel.CLANS,
            "/api/members/{clanId}/",
            List.of(),
            List.of(new ValueParam("battle_type", "battleType", false), new ValueParam("season", "season", false)),
            (node, request, profile) -> VortexClanUserInfo.to(profile.server(), node));

    /**
     * 军团搜索。
     */
    public static final Endpoint<List<VortexSearchClan>> SEARCH = Endpoint.of(
            "clans.search",
            Channel.CLANS,
            "/api/search/autocomplete/",
            List.of(),
            List.of(new ValueParam("search", "search", true), new LiteralParam("type", "clans")),
            (node, request, profile) -> VortexSearchClan.parse(node));

    /**
     * 军团排行（clan_id 游标分页；realm 由 ServerProfile 提供）。响应体是裸数组，无 status/data 包裹。
     */
    public static final Endpoint<List<ClanRankInfo>> LADDER = Endpoint.of(
            "clans.ladder",
            Channel.CLANS,
            "/api/ladder/structure/",
            List.of(),
            List.of(new ValueParam("realm", "realm", false), new ValueParam("season", "season", false),
                    new ValueParam("clan_id", "clanId", false)),
            (node, request, profile) -> JsonUtils.json().parse(node, new TypeReference<List<ClanRankInfo>>() {
            }));
}
