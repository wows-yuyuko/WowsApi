package com.shinoaki.wows.api.data;

/**
 * @author Xun
 * create or update time = 2023/8/13 18:13 星期日
 */
public enum HttpThrowableStatus {
    SUCCESS,
    /**
     * 网络层次异常
     */
    HTTP_IO,
    /**
     * 网络状态码异常
     */
    HTTP_STATUS,
    /**
     * 业务状态异常/接口返回的数据异常
     */
    DATA_STATUS,
    /**
     * 解析数据出现异常
     */
    DATA_PARSE,
    /**
     * 未知异常
     */
    ERROR,
    NULL
}
