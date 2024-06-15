package com.shinoaki.wows.api.error;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

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

    public BasicException(JsonProcessingException e) {
        super(e);
        this.code = HttpThrowableStatus.DATA_PARSE;
    }

    public BasicException(InterruptedException e) {
        this.code = HttpThrowableStatus.THREAD;
    }

    public BasicException(ExecutionException e) {
        this.code = HttpThrowableStatus.EXECUTION;
    }

    private BasicException(JsonNode node) {
        super("获取status节点异常 value=" + node);
        this.code = HttpThrowableStatus.DATA_STATUS;
    }

    public static void status(JsonNode node) throws BasicException {
        JsonNode status = node.get("status");
        if (!status.isNull() && status.asText().equalsIgnoreCase("ok")) {
            return;
        }
        //抛出解析status 异常的问题
        throw new BasicException(node);
    }
}
