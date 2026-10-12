package com.shinoaki.wows.api.transport;

/**
 * 请求通道（3.0 重写）：目标主机族 + 后续差异化策略（请求头、异常策略等）的挂点。
 * <p>
 * 通道与主机由 {@code ServerProfile.host(Channel)} 对应解析；
 * 端点只声明通道，不感知具体域名。
 *
 * @author Xun
 */
public enum Channel {

    /**
     * vortex 业务接口（https://vortex.*）：玩家、战舰列表等。
     */
    VORTEX,

    /**
     * clans 军团接口（https://clans.*）：军团详情、成员、排行等。
     */
    CLANS,

    /**
     * 官方开发者API（https://api.*，前缀 /wows/ 或 /mk/）：CN 不提供。
     */
    DEV
}
