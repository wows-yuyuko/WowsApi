package com.shinoaki.wows.api.endpoint;

/**
 * 限流容错策略：STRICT（默认）命中 429/503 抛异常；TOLERANT 且端点声明了空值时返回空值并记日志。
 *
 * @author Xun
 */
public enum RateLimitPolicy {

    STRICT,

    TOLERANT
}
