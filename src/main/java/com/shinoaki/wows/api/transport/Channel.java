package com.shinoaki.wows.api.transport;

/**
 * 请求通道：通道与主机由 {@code ServerProfile.host(Channel)} 对应解析；端点只声明通道，不感知域名。
 *
 * @author Xun
 */
public enum Channel {

    /**
     * vortex 业务接口（https://vortex.*）。
     */
    VORTEX,

    /**
     * clans 军团接口（https://clans.*）。
     */
    CLANS,

    /**
     * 官方开发者API（https://api.*，前缀 /wows/ 或 /mk/）；CN 不提供。
     */
    DEV
}
