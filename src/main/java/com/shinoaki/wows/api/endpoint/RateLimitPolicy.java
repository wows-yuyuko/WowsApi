package com.shinoaki.wows.api.endpoint;

/**
 * 限流容错策略（3.0 重写，非异常）：把旧实现的"隐藏行为"（国服搜索 503 静默吞掉）显式化。
 * <ul>
 *     <li>{@link #STRICT}（默认）：命中限流状态（429/503）抛出 {@code HttpStatusException}；</li>
 *     <li>{@link #TOLERANT}：命中限流状态且端点声明了容错空值时，返回空值并记日志（"空结果"只表示服务器明确返回了空）。</li>
 * </ul>
 * 通过 {@code EndpointRequest.policy(...)} 按调用指定，如
 * {@code api.vortex().search(text, RateLimitPolicy.TOLERANT)}。
 *
 * @author Xun
 */
public enum RateLimitPolicy {

    STRICT,

    TOLERANT
}
