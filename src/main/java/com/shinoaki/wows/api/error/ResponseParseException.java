package com.shinoaki.wows.api.error;

/**
 * 响应解析异常：网络已通、响应拿到了，但内容解析不了——响应体不是合法 JSON、
 * 或字段解析失败（字段缺失/类型不符）。
 * <p>
 * code 为 {@link HttpThrowableStatus#DATA_PARSE}；与 {@link HttpNetworkException}（传输失败）
 * 明确区分：排障时一眼可辨"是没连上"还是"拿回来的东西不对"。
 * 消息通常附带响应开头摘要，便于识别"网关 HTML 错误页"这类情况。
 *
 * @author Xun
 */
public class ResponseParseException extends BasicException {

    public ResponseParseException(String message) {
        super(HttpThrowableStatus.DATA_PARSE, message);
    }

    public ResponseParseException(String message, Throwable cause) {
        super(HttpThrowableStatus.DATA_PARSE, message, cause);
    }
}
