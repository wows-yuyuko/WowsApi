package com.shinoaki.wows.api.error;

/**
 * @author Xun
 * @date 2023/4/10 16:14 星期一
 */
public class HttpStatusException extends Exception {
    public HttpStatusException(String message) {
        super(message);
    }

    public HttpStatusException(int httpCode) {
        super("response code is not 200 value=" + httpCode);
    }
}
