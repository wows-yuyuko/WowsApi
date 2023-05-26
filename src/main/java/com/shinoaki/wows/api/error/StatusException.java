package com.shinoaki.wows.api.error;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * @author Xun
 * @date 2023/4/9 18:52 星期日
 */
public class StatusException extends Exception {

    public StatusException(JsonNode node) {
        super("获取status节点异常 value=" + node);
    }

    public static void status(JsonNode node) throws StatusException {
        JsonNode status = node.get("status");
        if (!status.isNull() && status.asText().equalsIgnoreCase("ok")) {
            return;
        }
        //抛出解析status 异常的问题
        throw new StatusException(node);
    }
}
