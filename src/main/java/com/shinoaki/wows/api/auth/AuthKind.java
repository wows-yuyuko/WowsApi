package com.shinoaki.wows.api.auth;

/**
 * 凭证种类（3.0 重写）：<b>kind 决定通道绑定与端点槽位匹配</b>——
 * vortex 凭证永远不会被注入 dev 端点（反之亦然），因为端点只声明自己通道的槽位。
 *
 * @author Xun
 */
public enum AuthKind {

    /**
     * 官方开发者API 的服务器级令牌（查询参数 {@code application_id}）。
     * 莱服只认莱服自己签发的 key（WG key 无效）。
     */
    APPLICATION_ID,

    /**
     * 官方开发者API 的账号级用户态令牌（查询参数 {@code access_token}），解锁 {@code private.*} 私有数据。
     */
    ACCESS_TOKEN,

    /**
     * vortex 的账号级分享凭证（查询参数 {@code ac}），与账号绑定、只对该账号有效。
     */
    SHARE_TOKEN,

    /**
     * 预留：cookie 类认证（请求头 {@code Cookie}），按账号绑定。
     * 与 WAF cookie（api.korabli.su 的 bp_chl 307 握手）分层——后者属于传输层，
     * 由 {@code HostPolicy} 管理；两者并存时的合并发送待首个用例接入。
     */
    COOKIE
}
