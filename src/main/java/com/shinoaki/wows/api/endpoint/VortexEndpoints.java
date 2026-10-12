package com.shinoaki.wows.api.endpoint;

import com.shinoaki.wows.api.auth.AuthKind;
import com.shinoaki.wows.api.auth.AuthSlot;
import com.shinoaki.wows.api.transport.Channel;
import com.shinoaki.wows.api.vortex.account.VortexSearchUser;
import com.shinoaki.wows.api.vortex.account.VortexUserInfo;

import java.util.List;

/**
 * vortex 通道端点表（3.0 重写）。
 * <p>
 * 主机为 {@code https://vortex.*}（由 ServerProfile 注入），端点定义不含域名。
 *
 * @author Xun
 */
public final class VortexEndpoints {

    private VortexEndpoints() {
    }

    /**
     * 玩家搜索。CN 对高频请求返回 503，调用方可选 {@link RateLimitPolicy#TOLERANT} 容错（返回空表）。
     */
    public static final Endpoint<List<VortexSearchUser>> ACCOUNT_SEARCH = Endpoint.of(
            "vortex.account.search",
            Channel.VORTEX,
            "/api/accounts/search/autocomplete/{search}/",
            List.of(),
            List.of(),
            List::of,
            (node, request) -> VortexSearchUser.parse(node));

    /**
     * 玩家详情。配置 share token（?ac=）时返回含私有数据的完整档案。
     */
    public static final Endpoint<VortexUserInfo> ACCOUNT_INFO = Endpoint.of(
            "vortex.account.info",
            Channel.VORTEX,
            "/api/accounts/{accountId}/",
            List.of(AuthSlot.optional(AuthKind.SHARE_TOKEN)),
            (node, request) -> VortexUserInfo.parse(node, request.accountId()));
}
