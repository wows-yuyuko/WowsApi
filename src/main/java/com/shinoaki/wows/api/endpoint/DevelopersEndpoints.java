package com.shinoaki.wows.api.endpoint;

import com.shinoaki.wows.api.auth.AuthKind;
import com.shinoaki.wows.api.auth.AuthSlot;
import com.shinoaki.wows.api.developers.account.DevelopersSearchUser;
import com.shinoaki.wows.api.developers.account.DevelopersUserInfo;
import com.shinoaki.wows.api.developers.clan.DevelopersClanInfo;
import com.shinoaki.wows.api.developers.clan.DevelopersSearchUserClan;
import com.shinoaki.wows.api.server.DevNamespace;

import java.util.List;

/**
 * 官方开发者API 端点表（3.0 重写）。
 * <p>
 * 主机为 {@code https://api.*}（由 ServerProfile 注入），路径为命名空间内路径
 * （前缀 {@code /wows/} 或 {@code /mk/} 按服务器注入）；CN 不提供该通道（发送前拒绝）。
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
            (node, request) -> DevelopersSearchUser.parse(node));

    /**
     * 玩家详情（extra 串由 ServerProfile 按服提供；access_token 可选，解锁 private.*）。
     */
    public static final Endpoint<DevelopersUserInfo> ACCOUNT_INFO = Endpoint.dev(
            "dev.account.info",
            DevNamespace.ACCOUNT,
            "/account/info/",
            List.of(AuthSlot.required(AuthKind.APPLICATION_ID), AuthSlot.optional(AuthKind.ACCESS_TOKEN)),
            List.of(new ValueParam("account_id", "accountId", false), new ValueParam("extra", "extra", false)),
            (node, request) -> DevelopersUserInfo.parse(request.accountId(), node));

    /**
     * 玩家所属军团（clan 命名空间：莱服本接口仍在 /wows/ 下，见 {@link DevNamespace#CLAN}）。
     */
    public static final Endpoint<DevelopersSearchUserClan> CLAN_ACCOUNT = Endpoint.dev(
            "dev.clan.account",
            DevNamespace.CLAN,
            "/clans/accountinfo/",
            List.of(AuthSlot.required(AuthKind.APPLICATION_ID)),
            List.of(new ValueParam("account_id", "accountId", false), new LiteralParam("extra", "clan")),
            (node, request) -> DevelopersSearchUserClan.parse(request.accountId(), node));

    /**
     * 军团详情（members 全量成员列表）。
     */
    public static final Endpoint<DevelopersClanInfo> CLAN_INFO = Endpoint.dev(
            "dev.clan.info",
            DevNamespace.CLAN,
            "/clans/info/",
            List.of(AuthSlot.required(AuthKind.APPLICATION_ID)),
            List.of(new ValueParam("clan_id", "clanId", false), new LiteralParam("extra", "members")),
            (node, request) -> DevelopersClanInfo.parse(request.longParam("clanId"), node));
}
