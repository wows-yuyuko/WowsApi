package com.shinoaki.wows.api.error;


import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import tools.jackson.databind.JsonNode;

import java.util.concurrent.ExecutionException;

/**
 * 基础受检异常：所有请求/解析失败的公共父类，携带 {@link HttpThrowableStatus} 错误码与消息。
 * <p>
 * 按失败层次区分子类（便于 catch 与排障）：
 * <ul>
 *     <li>{@link HttpNetworkException}：网络传输失败（连接/超时/解压）——code {@code HTTP_IO}</li>
 *     <li>{@link HttpStatusException}：HTTP 状态码异常（非200/重定向超限）——code {@code HTTP_STATUS}</li>
 *     <li>{@link ResponseParseException}：响应解析失败（JSON 非法/字段解析）——code {@code DATA_PARSE}</li>
 *     <li>{@link HiddenProfileException}：账号隐藏战绩——code {@code HIDDEN}</li>
 * </ul>
 * 其余场景（业务状态异常 {@code DATA_STATUS}、线程中断 {@code THREAD} 等）仍直接使用本类。
 * 现有 {@code catch (BasicException)} 对全部子类依然生效。
 *
 * @author Xun
 */
@Getter
@ToString
@EqualsAndHashCode(callSuper = false)
public class BasicException extends Exception {
    private final String detailMessage;
    private final HttpThrowableStatus code;

    public BasicException(HttpThrowableStatus code, Exception message) {
        super(message);
        this.detailMessage = message.getMessage();
        this.code = code;
    }

    public BasicException(HttpThrowableStatus code, String message) {
        super(message);
        this.detailMessage = message;
        this.code = code;
    }

    /**
     * 携带原因链的构造器（保留原始异常堆栈，便于定位）。
     */
    public BasicException(HttpThrowableStatus code, String message, Throwable cause) {
        super(message, cause);
        this.detailMessage = message;
        this.code = code;
    }


    public BasicException(InterruptedException e) {
        super("线程被中断");
        this.detailMessage = "线程被中断";
        this.code = HttpThrowableStatus.THREAD;
    }

    public BasicException(ExecutionException e) {
        super("多线程任务终止");
        this.detailMessage = "多线程任务终止";
        this.code = HttpThrowableStatus.EXECUTION;
    }

    private BasicException(JsonNode node) {
        super("获取status节点异常 value=" + node);
        this.detailMessage = "获取status节点异常 value=" + node;
        this.code = HttpThrowableStatus.DATA_STATUS;
    }

    public static void status(JsonNode node) throws BasicException {
        JsonNode status = node.path("status");
        if (!status.isNull() && status.asString("").equalsIgnoreCase("ok")) {
            return;
        }
        //抛出解析status 异常的问题
        throw new BasicException(node);
    }
}
