package com.shinoaki.wows.api.endpoint;

import com.shinoaki.wows.api.error.BasicException;
import com.shinoaki.wows.api.server.ServerProfile;
import tools.jackson.databind.JsonNode;

/**
 * 端点响应解析器（3.0 重写）：吃已解析的 {@link JsonNode}、本次请求与服务器档案，产出业务模型。
 * <p>
 * 统一以 JsonNode 交接（而不是响应字符串），使字段漂移监视的"精确模式"可以直接包裹被解析的树；
 * 部分模型的解析需要服务器事实（如军团详情区分莱服 {@code mk_ladder}），故携带 {@link ServerProfile}。
 *
 * @param <T> 解析结果类型
 * @author Xun
 */
@FunctionalInterface
public interface Parser<T> {

    T parse(JsonNode node, EndpointRequest request, ServerProfile profile) throws BasicException;
}
