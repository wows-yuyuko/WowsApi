package com.shinoaki.wows.api.client;

import com.shinoaki.wows.api.auth.ShareToken;
import com.shinoaki.wows.api.endpoint.EndpointRequest;
import com.shinoaki.wows.api.endpoint.RateLimitPolicy;
import com.shinoaki.wows.api.endpoint.VortexEndpoints;
import com.shinoaki.wows.api.error.BasicException;
import com.shinoaki.wows.api.error.UnsupportedOnServerException;
import com.shinoaki.wows.api.transport.HttpPipeline;
import com.shinoaki.wows.api.type.WowsBattlesType;
import com.shinoaki.wows.api.vortex.VortexUserShip;
import com.shinoaki.wows.api.vortex.account.VortexSearchUser;
import com.shinoaki.wows.api.vortex.account.VortexUserInfo;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * vortex 通道视图（3.0 重写）：<b>1 调用 = 1 请求</b>，方法名不带通道后缀。
 * <p>
 * 实例由 {@code WowsApi.vortex()} 创建，持有服务器档案与凭证表（不可变）。
 *
 * @author Xun
 */
public final class VortexClient {

    private final HttpPipeline pipeline;

    public VortexClient(HttpPipeline pipeline) {
        this.pipeline = Objects.requireNonNull(pipeline, "pipeline");
    }

    /**
     * 玩家搜索（严格模式：限流状态抛异常）。
     */
    public List<VortexSearchUser> search(String text) throws BasicException {
        return search(text, RateLimitPolicy.STRICT);
    }

    /**
     * 玩家搜索。CN 对高频请求返回 503，可传 {@link RateLimitPolicy#TOLERANT} 容错（返回空表并记日志）。
     */
    public List<VortexSearchUser> search(String text, RateLimitPolicy policy) throws BasicException {
        return pipeline.execute(VortexEndpoints.ACCOUNT_SEARCH,
                EndpointRequest.of(Map.of("search", text)).policy(policy));
    }

    /**
     * 玩家详情；配置了该账号的 share token 时返回含私有数据的完整档案。
     */
    public VortexUserInfo user(long accountId) throws BasicException {
        return pipeline.execute(VortexEndpoints.ACCOUNT_INFO, EndpointRequest.account(accountId));
    }

    /**
     * 玩家详情，调用级覆盖 share token（逃生门：优先于实例配置；账号须与凭证绑定账号一致）。
     */
    public VortexUserInfo user(long accountId, ShareToken shareToken) throws BasicException {
        return pipeline.execute(VortexEndpoints.ACCOUNT_INFO,
                EndpointRequest.account(accountId).credentials(shareToken));
    }

    /**
     * 玩家战舰战绩列表（1 请求）。
     * <p>
     * 战斗类型受服务器限制：莱服专属类型（brawl / clan / rating* 等）仅 RU 可用，
     * 其余服务器调用抛 {@link UnsupportedOnServerException}（发送前，零网络请求）。
     */
    public VortexUserShip ships(long accountId, WowsBattlesType type) throws BasicException {
        return ships(accountId, type, null);
    }

    /**
     * 玩家战舰战绩列表，调用级覆盖 share token（null = 用实例配置）。
     */
    public VortexUserShip ships(long accountId, WowsBattlesType type, ShareToken shareToken) throws BasicException {
        if (!type.supportedOn(pipeline.profile().server())) {
            throw new UnsupportedOnServerException("战斗类型 " + type, pipeline.profile().server(),
                    "该类型仅支持: " + type.servers());
        }
        EndpointRequest request = EndpointRequest.account(accountId, Map.of("battleType", type.code()));
        if (shareToken != null) {
            request = request.credentials(shareToken);
        }
        return pipeline.execute(VortexEndpoints.SHIP_LIST, request);
    }
}
