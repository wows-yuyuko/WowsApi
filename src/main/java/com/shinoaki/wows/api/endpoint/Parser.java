package com.shinoaki.wows.api.endpoint;

import com.shinoaki.wows.api.error.BasicException;
import com.shinoaki.wows.api.server.ServerProfile;
import tools.jackson.databind.JsonNode;

/**
 * 响应解析器：吃 JsonNode + 本次请求 + 服务器档案（部分模型解析需要服务器事实，如军团 mk_ladder）。
 *
 * @param <T> 解析结果类型
 * @author Xun
 */
@FunctionalInterface
public interface Parser<T> {

    T parse(JsonNode node, EndpointRequest request, ServerProfile profile) throws BasicException;
}
