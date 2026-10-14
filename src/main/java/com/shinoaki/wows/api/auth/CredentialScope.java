package com.shinoaki.wows.api.auth;

/**
 * 凭证作用域：SERVER = 服务器级（实例即边界，整实例通用）；ACCOUNT = 账号级（仅本次请求账号与绑定账号一致才注入）。
 *
 * @author Xun
 */
public enum CredentialScope {

    SERVER,

    ACCOUNT
}
