package com.shinoaki.wows.api.auth;

/**
 * 凭证种类：端点只声明自己通道的槽位，vortex / dev 凭证不会串。
 *
 * @author Xun
 */
public enum AuthKind {

    /**
     * 官方开发者API 服务器级令牌（query {@code application_id}）；莱服只认莱服 key。
     */
    APPLICATION_ID,

    /**
     * 官方开发者API 账号级用户态令牌（query {@code access_token}），解锁 {@code private.*}。
     */
    ACCESS_TOKEN,

    /**
     * vortex 账号级分享凭证（query {@code ac}），与账号绑定。
     */
    SHARE_TOKEN,

    /**
     * 预留：cookie 类认证（请求头 {@code Cookie}），与 WAF cookie（传输层 HostPolicy）分层。
     */
    COOKIE
}
