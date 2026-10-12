package com.shinoaki.wows.api.endpoint;

import com.shinoaki.wows.api.error.BasicException;
import tools.jackson.databind.JsonNode;

/**
 * 端点响应解析器（3.0 重写）：吃已解析的 {@link JsonNode} 与本次请求（携带参数），产出业务模型。
 * <p>
 * 统一以 JsonNode 交接（而不是响应字符串），使字段漂移监视的"精确模式"可以直接包裹被解析的树。
 *
 * @param <T> 解析结果类型
 * @author Xun
 */
@FunctionalInterface
public interface Parser<T> {

    T parse(JsonNode node, EndpointRequest request) throws BasicException;
}
