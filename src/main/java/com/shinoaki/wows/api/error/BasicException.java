package com.shinoaki.wows.api.error;


import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import tools.jackson.databind.JsonNode;

import java.util.concurrent.ExecutionException;

/**
 * @author Xun
 */
@Getter
@ToString
@EqualsAndHashCode(callSuper = false)
public class BasicException extends Exception {
    private final HttpThrowableStatus code;

    public BasicException(HttpThrowableStatus code, Exception message) {
        super(message);
        this.code = code;
    }

    public BasicException(HttpThrowableStatus code, String message) {
        super(message);
        this.code = code;
    }



    public BasicException(InterruptedException e) {
        super("线程被中断");
        this.code = HttpThrowableStatus.THREAD;
    }

    public BasicException(ExecutionException e) {
        super("多线程任务终止");
        this.code = HttpThrowableStatus.EXECUTION;
    }

    private BasicException(JsonNode node) {
        super("获取status节点异常 value=" + node);
        this.code = HttpThrowableStatus.DATA_STATUS;
    }

    public static void status(JsonNode node) throws BasicException {
        JsonNode status = node.get("status");
        if (!status.isNull() && status.asString().equalsIgnoreCase("ok")) {
            return;
        }
        //抛出解析status 异常的问题
        throw new BasicException(node);
    }
}
