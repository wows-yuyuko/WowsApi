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
 * clans 通道视图（3.0 重写）：<b>1 调用 = 1 请求</b>（{@link #ladder(int)} 除外，见方法说明）。
 * <p>
 * 实例由 {@code WowsApi.clans()} 创建；主机为 {@code https://clans.*}（ServerProfile 注入）。
 *
 * @author Xun
 */
public final class ClansClient {

    /**
     * 排行分页拉取的最大页数上限（防服务端数据异常导致死循环）。
     */
    private static final int MAX_RANK_PAGE = 10_000;

    private final HttpPipeline pipeline;

    public ClansClient(HttpPipeline pipeline) {
        this.pipeline = Objects.requireNonNull(pipeline, "pipeline");
    }

    /**
     * 玩家所属军团（1 请求）。非官方API服务器（CN）查无公会返回 404 → 空对象（旧语义保留）。
     */
    public VortexSearchClanUser accountClan(long accountId) throws BasicException {
        return pipeline.execute(ClansEndpoints.ACCOUNT_CLAN, EndpointRequest.account(accountId));
    }

    /**
     * 军团详情（1 请求；莱服 ladder 字段自动按服务器区分）。
     */
    public VortexClanInfo clanInfo(long clanId) throws BasicException {
        return pipeline.execute(ClansEndpoints.INFO, EndpointRequest.of(Map.of("clanId", String.valueOf(clanId))));
    }

    /**
     * 军团成员战绩（1 请求；默认 pvp）。
     */
    public VortexClanStatisticsInfo members(long clanId) throws BasicException {
        return members(clanId, "pvp");
    }

    /**
     * 军团成员战绩（1 请求；指定战斗类型，如 {@code cvc}）。
     */
    public VortexClanStatisticsInfo members(long clanId, String battleType) throws BasicException {
        return members(clanId, battleType, null);
    }

    /**
     * 军团成员战绩（1 请求；{@code cvc} 支持 season）。
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
     * 军团搜索（1 请求）。
     */
    public List<VortexSearchClan> search(String clanTag) throws BasicException {
        return pipeline.execute(ClansEndpoints.SEARCH, EndpointRequest.of(Map.of("search", clanTag)));
    }

    /**
     * 军团排行完整拉取（clan_id 游标分页，<b>内部多个请求</b>——等价旧 {@code ClanRankUtils.getRanks}）。
     * <p>
     * 分页上限 {@value #MAX_RANK_PAGE} 页；失败语义与 2.x 不同：不再静默吞异常返回空表，
     * 任一页失败直接抛 {@code BasicException}。
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
     * 军团排行单页（1 请求）。
     * <p>
     * 实测（2026-10）：{@code season<=0} 时<b>不传 season 参数</b>（服务端对空值返回 422，
     * 旧实现 {@code &season=} 因此静默失败——3.0 修正为省略参数 = 最新赛季）。
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
