package com.shinoaki.wows.api.endpoint;

import com.shinoaki.wows.api.auth.AuthKind;
import com.shinoaki.wows.api.auth.AuthSlot;
import com.shinoaki.wows.api.data.ship.ShipExpansion;
import com.shinoaki.wows.api.developers.DevelopersUserShip;
import com.shinoaki.wows.api.developers.DevelopersUserShipOper;
import com.shinoaki.wows.api.developers.account.DevelopersSearchUser;
import com.shinoaki.wows.api.developers.account.DevelopersUserInfo;
import com.shinoaki.wows.api.developers.clan.DevelopersClanInfo;
import com.shinoaki.wows.api.developers.clan.DevelopersSearchClan;
import com.shinoaki.wows.api.developers.clan.DevelopersSearchUserClan;
import com.shinoaki.wows.api.developers.clan.seasion.DevelopersSeasonInfo;
import com.shinoaki.wows.api.developers.encyclopedia.glossary.DevelopersGlossary;
import com.shinoaki.wows.api.server.DevNamespace;
import com.shinoaki.wows.api.type.WowsBattlesType;
import com.shinoaki.wows.api.type.WowsServer;

import java.util.List;
import java.util.Map;

/**
 * 官方开发者API 端点表：主机由 ServerProfile 注入，路径为命名空间内路径（前缀 /wows/ 或 /mk/ 按服务器）；
 * CN 无该通道（发送前拒绝）。
 *
 * @author Xun
 */
public final class DevelopersEndpoints {

    private DevelopersEndpoints() {
    }

    /**
     * 玩家搜索（application_id 必填）。
     */
    public static final Endpoint<List<DevelopersSearchUser>> ACCOUNT_SEARCH = Endpoint.dev(
            "dev.account.search",
            DevNamespace.ACCOUNT,
            "/account/list/",
            List.of(AuthSlot.required(AuthKind.APPLICATION_ID)),
            List.of(new ValueParam("search", "search", true)),
            (node, request, profile) -> DevelopersSearchUser.parse(node));

    /**
     * 玩家详情（extra 按服由 ServerProfile 提供；access_token 可选）。
     */
    public static final Endpoint<DevelopersUserInfo> ACCOUNT_INFO = Endpoint.dev(
            "dev.account.info",
            DevNamespace.ACCOUNT,
            "/account/info/",
            List.of(AuthSlot.required(AuthKind.APPLICATION_ID), AuthSlot.optional(AuthKind.ACCESS_TOKEN)),
            List.of(new ValueParam("account_id", "accountId", false), new ValueParam("extra", "extra", false)),
            (node, request, profile) -> DevelopersUserInfo.parse(request.accountId(), node));

    /**
     * 玩家所属军团（clan 命名空间：莱服仍在 /wows/ 下）。
     */
    public static final Endpoint<DevelopersSearchUserClan> CLAN_ACCOUNT = Endpoint.dev(
            "dev.clan.account",
            DevNamespace.CLAN,
            "/clans/accountinfo/",
            List.of(AuthSlot.required(AuthKind.APPLICATION_ID)),
            List.of(new ValueParam("account_id", "accountId", false), new LiteralParam("extra", "clan")),
            (node, request, profile) -> DevelopersSearchUserClan.parse(request.accountId(), node));

    /**
     * 军团详情（members 全量成员列表）。
     */
    public static final Endpoint<DevelopersClanInfo> CLAN_INFO = Endpoint.dev(
            "dev.clan.info",
            DevNamespace.CLAN,
            "/clans/info/",
            List.of(AuthSlot.required(AuthKind.APPLICATION_ID)),
            List.of(new ValueParam("clan_id", "clanId", false), new LiteralParam("extra", "members")),
            (node, request, profile) -> DevelopersClanInfo.parse(request.longParam("clanId"), node));

    /**
     * 军团搜索（clan 命名空间：莱服仍走 /wows/）。
     */
    public static final Endpoint<List<DevelopersSearchClan>> CLAN_SEARCH = Endpoint.dev(
            "dev.clan.search",
            DevNamespace.CLAN,
            "/clans/list/",
            List.of(AuthSlot.required(AuthKind.APPLICATION_ID)),
            List.of(new ValueParam("search", "search", true)),
            (node, request, profile) -> DevelopersSearchClan.parse(node));

    /**
     * 军团赛季/联赛（language 参数，默认 zh-cn）。
     */
    public static final Endpoint<List<DevelopersSeasonInfo>> CLAN_SEASON = Endpoint.dev(
            "dev.clan.season",
            DevNamespace.CLAN,
            "/clans/season/",
            List.of(AuthSlot.required(AuthKind.APPLICATION_ID)),
            List.of(new ValueParam("language", "language", false)),
            (node, request, profile) -> DevelopersSeasonInfo.parse(node));

    /**
     * 军团词典（莱服前缀 /mk/）。
     */
    public static final Endpoint<DevelopersGlossary> CLAN_GLOSSARY = Endpoint.dev(
            "dev.clan.glossary",
            DevNamespace.GLOSSARY,
            "/clans/glossary/",
            List.of(AuthSlot.required(AuthKind.APPLICATION_ID)),
            List.of(),
            (node, request, profile) -> DevelopersGlossary.parse(node));

    /**
     * 战舰统计（extra 见 {@link #statsExtra()}；带 access_token 时附带 in_garage=1）。
     */
    public static final Endpoint<DevelopersUserShip> SHIP_STATS = Endpoint.dev(
            "dev.ship.stats",
            DevNamespace.SHIP,
            "/ships/stats/",
            List.of(AuthSlot.required(AuthKind.APPLICATION_ID), AuthSlot.optional(AuthKind.ACCESS_TOKEN)),
            List.of(new ValueParam("account_id", "accountId", false), new ValueParam("extra", "extra", false),
                    new CredentialParam("in_garage", "1", AuthKind.ACCESS_TOKEN)),
            (node, request, profile) -> DevelopersUserShip.parse(node));

    /**
     * 战舰全量战绩（与 stats 同路径，extra 为全量 14 类；端点 id 区分监控基线）。
     */
    public static final Endpoint<DevelopersUserShipOper> SHIP_OPER = Endpoint.dev(
            "dev.ship.oper",
            DevNamespace.SHIP,
            "/ships/stats/",
            List.of(AuthSlot.required(AuthKind.APPLICATION_ID), AuthSlot.optional(AuthKind.ACCESS_TOKEN)),
            List.of(new ValueParam("account_id", "accountId", false), new ValueParam("extra", "extra", false),
                    new CredentialParam("in_garage", "1", AuthKind.ACCESS_TOKEN)),
            (node, request, profile) -> DevelopersUserShipOper.parse(node));

    /**
     * 战舰徽章（莱服不提供——端点声明支持面，调用即拒绝）。
     */
    public static final Endpoint<Map<Long, ShipExpansion>> SHIP_BADGES = Endpoint.dev(
                    "dev.ship.badges",
                    DevNamespace.SHIP,
                    "/ships/badges/",
                    List.of(AuthSlot.required(AuthKind.APPLICATION_ID)),
                    List.of(new ValueParam("account_id", "accountId", false)),
                    (node, request, profile) -> ShipExpansion.parseDevelopers(request.accountId(), node))
            .withServers(WowsServer.ASIA, WowsServer.EU, WowsServer.NA);

    /**
     * {@code dev.ship.stats} 的 extra 串（历史集合，pvp 除外；勿扩充，与监控基线契约绑定）。
     */
    private static final String STATS_EXTRA = String.join(",", WowsBattlesType.DEVELOPERS_STATS_TYPES.stream()
            .filter(type -> type != WowsBattlesType.PVP)
            .map(WowsBattlesType::code)
            .toList());

    public static String statsExtra() {
        return STATS_EXTRA;
    }
}
