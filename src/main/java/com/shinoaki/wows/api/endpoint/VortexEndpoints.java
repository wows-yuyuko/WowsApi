package com.shinoaki.wows.api.endpoint;

import com.shinoaki.wows.api.auth.AuthKind;
import com.shinoaki.wows.api.auth.AuthSlot;
import com.shinoaki.wows.api.transport.Channel;
import com.shinoaki.wows.api.type.WowsBattlesType;
import com.shinoaki.wows.api.vortex.VortexUserShip;
import com.shinoaki.wows.api.vortex.account.VortexSearchUser;
import com.shinoaki.wows.api.vortex.account.VortexUserInfo;
import com.shinoaki.wows.api.vortex.resources.box.WowsLootBox;

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
                    (node, request, profile) -> VortexSearchUser.parse(node))
            .withThrottleEmpty(List::of);

    /**
     * 玩家详情。配置 share token（?ac=）时返回含私有数据的完整档案。
     */
    public static final Endpoint<VortexUserInfo> ACCOUNT_INFO = Endpoint.of(
            "vortex.account.info",
            Channel.VORTEX,
            "/api/accounts/{accountId}/",
            List.of(AuthSlot.optional(AuthKind.SHARE_TOKEN)),
            (node, request, profile) -> VortexUserInfo.parse(node, request.accountId()));

    /**
     * 玩家战舰战绩列表。战斗类型受服务器限制（莱服专属类型仅 RU，由调用方在发送前检查）；
     * 配置 share token（?ac=）时返回含私有数据的完整列表。
     */
    public static final Endpoint<VortexUserShip> SHIP_LIST = Endpoint.of(
            "vortex.ship.list",
            Channel.VORTEX,
            "/api/accounts/{accountId}/ships/{battleType}/",
            List.of(AuthSlot.optional(AuthKind.SHARE_TOKEN)),
            (node, request, profile) -> VortexUserShip.parse(WowsBattlesType.findCode(request.param("battleType")), node));

    /**
     * 箱子详情：单个箱子的完整掉落配置；语言与箱子 ID 都在路径里（语言由调用方选择，莱服固定 ru）。
     */
    public static final Endpoint<WowsLootBox> LOOTBOX_DETAIL = Endpoint.of(
            "vortex.lootbox.detail",
            Channel.VORTEX,
            "/api/get_lootbox/{language}/{lootboxId}/",
            List.of(),
            (node, request, profile) -> WowsLootBox.parse(node));
}
