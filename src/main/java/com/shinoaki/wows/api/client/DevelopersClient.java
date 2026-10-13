package com.shinoaki.wows.api.client;

import com.shinoaki.wows.api.auth.AccessToken;
import com.shinoaki.wows.api.developers.account.DevelopersSearchUser;
import com.shinoaki.wows.api.developers.account.DevelopersUserInfo;
import com.shinoaki.wows.api.developers.clan.DevelopersClanInfo;
import com.shinoaki.wows.api.developers.clan.DevelopersSearchUserClan;
import com.shinoaki.wows.api.endpoint.DevelopersEndpoints;
import com.shinoaki.wows.api.endpoint.EndpointRequest;
import com.shinoaki.wows.api.error.BasicException;
import com.shinoaki.wows.api.transport.HttpPipeline;

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
}
