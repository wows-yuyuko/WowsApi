package com.shinoaki.wows.api.client;

import com.shinoaki.wows.api.auth.AccessToken;
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
import com.shinoaki.wows.api.endpoint.DevelopersEndpoints;
import com.shinoaki.wows.api.endpoint.EndpointRequest;
import com.shinoaki.wows.api.error.BasicException;
import com.shinoaki.wows.api.transport.HttpPipeline;
import com.shinoaki.wows.api.type.WowsBattlesType;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 官方开发者API 视图（3.0 重写）：<b>1 调用 = 1 请求</b>，方法名不带通道后缀。
 * <p>
 * 实例由 {@code WowsApi.developers()} 创建；application_id 来自实例凭证表（必填），
 * access_token 可选（按账号匹配）。CN 不支持该通道（调用即抛 {@code UnsupportedOnServerException}）。
 *
 * @author Xun
 */
public final class DevelopersClient {

    private final HttpPipeline pipeline;

    public DevelopersClient(HttpPipeline pipeline) {
        this.pipeline = Objects.requireNonNull(pipeline, "pipeline");
    }

    /**
     * 玩家搜索（1 请求）。
     */
    public List<DevelopersSearchUser> search(String text) throws BasicException {
        return pipeline.execute(DevelopersEndpoints.ACCOUNT_SEARCH, EndpointRequest.of(Map.of("search", text)));
    }

    /**
     * 玩家详情（1 请求）；实例配置了该账号的 access_token 时返回 private.* 私有数据。
     */
    public DevelopersUserInfo user(long accountId) throws BasicException {
        return user(accountId, null);
    }

    /**
     * 玩家详情，调用级覆盖 access_token（逃生门：优先于实例配置；账号须与凭证绑定账号一致）；
     * 传 null 表示使用实例配置。
     */
    public DevelopersUserInfo user(long accountId, AccessToken accessToken) throws BasicException {
        EndpointRequest request = EndpointRequest.account(accountId,
                Map.of("extra", pipeline.profile().accountInfoExtra()));
        if (accessToken != null) {
            request = request.credentials(accessToken);
        }
        return pipeline.execute(DevelopersEndpoints.ACCOUNT_INFO, request);
    }

    /**
     * 玩家所属军团（1 请求）。
     */
    public DevelopersSearchUserClan accountClan(long accountId) throws BasicException {
        return pipeline.execute(DevelopersEndpoints.CLAN_ACCOUNT, EndpointRequest.account(accountId));
    }

    /**
     * 军团详情（1 请求，含 members 全量成员列表）。
     */
    public DevelopersClanInfo clanInfo(long clanId) throws BasicException {
        return pipeline.execute(DevelopersEndpoints.CLAN_INFO,
                EndpointRequest.of(Map.of("clanId", String.valueOf(clanId))));
    }

    /**
     * 军团搜索（1 请求）。
     */
    public List<DevelopersSearchClan> clanSearch(String clanTag) throws BasicException {
        return pipeline.execute(DevelopersEndpoints.CLAN_SEARCH, EndpointRequest.of(Map.of("search", clanTag)));
    }

    /**
     * 军团赛季/联赛（1 请求）；语言默认 {@code zh-cn}（与旧实现一致）。
     */
    public List<DevelopersSeasonInfo> clanSeason() throws BasicException {
        return clanSeason("zh-cn");
    }

    /**
     * 军团赛季/联赛（1 请求）。
     */
    public List<DevelopersSeasonInfo> clanSeason(String language) throws BasicException {
        return pipeline.execute(DevelopersEndpoints.CLAN_SEASON, EndpointRequest.of(Map.of("language", language)));
    }

    /**
     * 军团词典（1 请求）。
     */
    public DevelopersGlossary glossary() throws BasicException {
        return pipeline.execute(DevelopersEndpoints.CLAN_GLOSSARY, EndpointRequest.of(Map.of()));
    }

    /**
     * 战舰统计（1 请求；extra 为历史集合）。
     */
    public DevelopersUserShip ships(long accountId) throws BasicException {
        return ships(accountId, null);
    }

    /**
     * 战舰统计（1 请求）；带 access_token 时附带 {@code in_garage=1}（与旧实现一致）。
     */
    public DevelopersUserShip ships(long accountId, AccessToken accessToken) throws BasicException {
        return pipeline.execute(DevelopersEndpoints.SHIP_STATS,
                shipRequest(accountId, accessToken, DevelopersEndpoints.statsExtra()));
    }

    /**
     * 战舰全量战绩（1 请求；extra 为全量 14 类，含 oper 块）。
     */
    public DevelopersUserShipOper shipsOper(long accountId) throws BasicException {
        return shipsOper(accountId, null);
    }

    /**
     * 战舰全量战绩（1 请求）；带 access_token 时附带 {@code in_garage=1}。
     */
    public DevelopersUserShipOper shipsOper(long accountId, AccessToken accessToken) throws BasicException {
        return pipeline.execute(DevelopersEndpoints.SHIP_OPER,
                shipRequest(accountId, accessToken, WowsBattlesType.DEVELOPERS_ALL_STATS_EXTRA));
    }

    /**
     * 战舰徽章（1 请求）。莱服不提供该接口：调用抛 {@code UnsupportedOnServerException}（发送前，零网络请求）。
     */
    public Map<Long, ShipExpansion> shipBadges(long accountId) throws BasicException {
        return pipeline.execute(DevelopersEndpoints.SHIP_BADGES, EndpointRequest.account(accountId));
    }

    private EndpointRequest shipRequest(long accountId, AccessToken accessToken, String extra) {
        EndpointRequest request = EndpointRequest.account(accountId, Map.of("extra", extra));
        return accessToken == null ? request : request.credentials(accessToken);
    }
}
