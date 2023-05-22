package com.shinoaki.wows.api.data.account;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.shinoaki.wows.api.error.StatusException;
import com.shinoaki.wows.api.utils.JsonUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Xun
 * @date 2023/5/22 22:45 星期一
 */
public record DevelopersSearchUser(String nickname, long account_id) {
    public static List<DevelopersSearchUser> parse(JsonUtils utils, JsonNode node) throws StatusException, JsonProcessingException {
        if ("ok".equalsIgnoreCase(node.get("status").asText())) {
            List<DevelopersSearchUser> list = new ArrayList<>();
            for (var data : node.get("data")) {
                list.add(utils.parse(data, DevelopersSearchUser.class));
            }
            return list;
        }
        throw new StatusException(node.get("error"));
    }
}
