package com.shinoaki.wows.api.endpoint;

import com.shinoaki.wows.api.auth.Credential;

import java.util.List;
import java.util.Map;

/**
 * 端点查询参数声明（3.0 重写）：端点定义中查询参数的唯一来源。
 * <p>
 * 认证参数（application_id / access_token / ac）不在此声明——由凭证解析按槽位注入；
 * 本接口只描述端点自身的参数：字面量、来自请求参数、或"凭证触发的字面量"（见 {@link CredentialParam}）。
 *
 * @author Xun
 */
public sealed interface QueryParam permits LiteralParam, ValueParam, CredentialParam {

    /**
     * 查询参数名。
     */
    String key();

    /**
     * 渲染参数值；返回 {@code null} 表示本次请求省略该参数（可选参数未提供 / 触发凭证未解析出）。
     *
     * @param params      本次请求参数
     * @param credentials 本次请求已解析的凭证（按槽位顺序）
     */
    String render(Map<String, String> params, List<Credential> credentials);
}
