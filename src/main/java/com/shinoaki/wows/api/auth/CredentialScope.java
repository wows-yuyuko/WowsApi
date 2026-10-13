package com.shinoaki.wows.api.auth;

/**
 * 凭证作用域（3.0 重写）：决定凭证的匹配方式。
 * <ul>
 *     <li>{@link #SERVER}：服务器级——实例即服务器边界，整实例通用（跨服串用结构上不可能）；</li>
 *     <li>{@link #ACCOUNT}：账号级——持有绑定的 accountId，仅当<b>本次请求账号与凭证绑定账号一致</b>才注入，
 *     不一致视同未配置；对不带账号的端点（搜索、军团列表等）不注入。</li>
 * </ul>
 *
 * @author Xun
 */
public enum CredentialScope {

    SERVER,

    ACCOUNT
}
