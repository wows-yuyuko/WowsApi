package com.shinoaki.wows.api.client;

import com.shinoaki.wows.api.endpoint.ClansEndpoints;
import com.shinoaki.wows.api.endpoint.EndpointRequest;
import com.shinoaki.wows.api.error.BasicException;
import com.shinoaki.wows.api.transport.HttpPipeline;
import com.shinoaki.wows.api.vortex.clan.VortexSearchClan;
import com.shinoaki.wows.api.vortex.clan.account.VortexSearchClanUser;
import com.shinoaki.wows.api.vortex.clan.base.VortexClanInfo;
import com.shinoaki.wows.api.vortex.clan.members.VortexClanStatisticsInfo;
import com.shinoaki.wows.api.vortex.clan.rank.ClanRankInfo;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * clans 军团视图：1 调用 = 1 请求（{@link #ladder(int)} 除外，内部分页）；由 {@code WowsApi.clans()} 创建。
 *
 * @author Xun
 */
public final class ClansClient {

    /**
     * 排行分页上限（防死循环）。
     */
    private static final int MAX_RANK_PAGE = 10_000;

    private final HttpPipeline pipeline;

    public ClansClient(HttpPipeline pipeline) {
        this.pipeline = Objects.requireNonNull(pipeline, "pipeline");
    }

    /**
     * 玩家所属军团；非 API 服务器（CN）查无公会返回 404 → 空对象（旧语义）。
     */
    public VortexSearchClanUser accountClan(long accountId) throws BasicException {
        return pipeline.execute(ClansEndpoints.ACCOUNT_CLAN, EndpointRequest.account(accountId));
    }

    /**
     * 军团详情（莱服 ladder 字段自动按服务器区分）。
     */
    public VortexClanInfo clanInfo(long clanId) throws BasicException {
        return pipeline.execute(ClansEndpoints.INFO, EndpointRequest.of(Map.of("clanId", String.valueOf(clanId))));
    }

    /**
     * 军团成员战绩（默认 pvp）。
     */
    public VortexClanStatisticsInfo members(long clanId) throws BasicException {
        return members(clanId, "pvp");
    }

    /**
     * 军团成员战绩（指定战斗类型，如 cvc）。
     */
    public VortexClanStatisticsInfo members(long clanId, String battleType) throws BasicException {
        return members(clanId, battleType, null);
    }

    /**
     * 军团成员战绩（{@code cvc} 支持 season）。
     */
    public VortexClanStatisticsInfo members(long clanId, String battleType, Integer season) throws BasicException {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("clanId", String.valueOf(clanId));
        params.put("battleType", battleType == null ? "pvp" : battleType);
        if (season != null) {
            params.put("season", String.valueOf(season));
        }
        return pipeline.execute(ClansEndpoints.MEMBERS, EndpointRequest.of(params));
    }

    /**
     * 军团搜索。
     */
    public List<VortexSearchClan> search(String clanTag) throws BasicException {
        return pipeline.execute(ClansEndpoints.SEARCH, EndpointRequest.of(Map.of("search", clanTag)));
    }

    /**
     * 军团排行完整拉取（clan_id 游标分页，内部多个请求）；任一页失败直接抛异常（不吞）。
     *
     * @param season 赛季；{@code <=0} 表示最新赛季
     */
    public List<ClanRankInfo> ladder(int season) throws BasicException {
        Map<Long, ClanRankInfo> rankMaps = new HashMap<>();
        List<ClanRankInfo> index = ladderPage(season, null);
        if (index.isEmpty()) {
            return List.of();
        }
        index.forEach(x -> rankMaps.put(x.id(), x));
        var last = index.getLast();
        for (int page = 0; page < MAX_RANK_PAGE; page++) {
            List<ClanRankInfo> run = ladderPage(season, last.id());
            if (run.isEmpty()) {
                break;
            }
            last = run.getLast();
            if (rankMaps.containsKey(last.id())) {
                break;
            }
            run.forEach(x -> rankMaps.put(x.id(), x));
        }
        return rankMaps.values().stream().toList();
    }

    /**
     * 军团排行单页。{@code season<=0} 不传 season 参数（服务端对空值返回 422 → 省略 = 最新赛季）。
     *
     * @param season 赛季；{@code <=0} 表示最新赛季
     * @param clanId 游标（上一页最后一条的 id）；null = 第一页
     */
    public List<ClanRankInfo> ladderPage(int season, Long clanId) throws BasicException {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("realm", pipeline.profile().ladderRealm());
        if (season > 0) {
            params.put("season", String.valueOf(season));
        }
        if (clanId != null) {
            params.put("clanId", String.valueOf(clanId));
        }
        return pipeline.execute(ClansEndpoints.LADDER, EndpointRequest.of(params));
    }
}
