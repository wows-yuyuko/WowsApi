package com.shinoaki.wows.api.error;

/**
 * @author Xun
 * create or update time = 2023/8/13 18:13 星期日
 */
public enum HttpThrowableStatus {

    /**
     * 网络层次异常（传输失败：连接/超时/解压）——对应 {@link HttpNetworkException}
     */
    HTTP_IO,
    /**
     * 网络状态码异常（响应已拿到但非200/重定向超限）——对应 {@link HttpStatusException}
     */
    HTTP_STATUS,
    /**
     * 业务状态异常/接口返回的数据异常（status≠ok、找不到数据等）
     */
    DATA_STATUS,
    /**
     * 解析数据出现异常（JSON 非法/字段解析失败）——对应 {@link ResponseParseException}
     */
    DATA_PARSE,
    /**
     * 线程中断异常
     */
    THREAD,
    EXECUTION,
    /**
     * 隐藏战绩
     */
    HIDDEN
}
