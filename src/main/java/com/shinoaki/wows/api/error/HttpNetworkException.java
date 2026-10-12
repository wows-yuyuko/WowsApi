package com.shinoaki.wows.api.error;

/**
 * 网络层异常：传输失败——连接不上、超时、请求中断、响应体解压/读取失败等。
 * <p>
 * code 为 {@link HttpThrowableStatus#HTTP_IO}；需要把"网络问题"与其它失败分开处理时，
 * 直接捕获本类型即可（如重试逻辑）。注意与 {@link HttpStatusException} 的区别：
 * 本类表示"没拿到正常响应"，那边表示"拿到了响应但状态码不对"。
 *
 * @author Xun
 */
public class HttpNetworkException extends BasicException {

    public HttpNetworkException(String message) {
        super(HttpThrowableStatus.HTTP_IO, message);
    }

    public HttpNetworkException(String message, Throwable cause) {
        super(HttpThrowableStatus.HTTP_IO, message, cause);
    }

    /**
     * 以原因异常构造（消息取原因异常自身描述）。
     */
    public HttpNetworkException(Exception cause) {
        super(HttpThrowableStatus.HTTP_IO, cause);
    }
}
