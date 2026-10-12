package com.shinoaki.wows.api.error;

import lombok.Getter;

/**
 * HTTP 状态码异常：响应已拿到，但状态码不是 200（或重定向超限等协议层问题）。
 * <p>
 * code 为 {@link HttpThrowableStatus#HTTP_STATUS}；{@link #statusCode()} 为最近一次响应的状态码
 * （拿不到具体状态码时为 -1），可直接用于结构化判断——如 503 限流的特殊处理，
 * 不再依赖消息里的字符串匹配。
 *
 * @author Xun
 */
@Getter
public class HttpStatusException extends BasicException {
    /**
     * 最近一次响应的 HTTP 状态码；未知时为 -1
     */
    private final int statusCode;

    public HttpStatusException(int statusCode, String message) {
        super(HttpThrowableStatus.HTTP_STATUS, message);
        this.statusCode = statusCode;
    }

    public HttpStatusException(int statusCode, String message, Throwable cause) {
        super(HttpThrowableStatus.HTTP_STATUS, message, cause);
        this.statusCode = statusCode;
    }
}
