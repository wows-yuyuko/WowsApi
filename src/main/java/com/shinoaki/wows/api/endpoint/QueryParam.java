package com.shinoaki.wows.api.endpoint;

import com.shinoaki.wows.api.auth.Credential;

import java.util.List;
import java.util.Map;

/**
 * 端点查询参数声明：认证参数（application_id / access_token / ac）不在此声明，由凭证解析按槽位注入。
 *
 * @author Xun
 */
public sealed interface QueryParam permits LiteralParam, ValueParam, CredentialParam {

    /**
     * 查询参数名。
     */
    String key();

    /**
     * 渲染参数值；null = 本次请求省略该参数。
     *
     * @param params      本次请求参数
     * @param credentials 本次请求已解析的凭证
     */
    String render(Map<String, String> params, List<Credential> credentials);
}
