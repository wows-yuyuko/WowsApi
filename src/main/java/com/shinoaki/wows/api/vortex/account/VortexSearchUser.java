package com.shinoaki.wows.api.vortex.account;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.shinoaki.wows.api.utils.JsonUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * https://vortex.wowsgame.cn/api/accounts/search/西行寺雨季/
 *
 * @author Xun
 * @date 2023/5/19 23:31 星期五
 */
public record VortexSearchUser(long spa_id, String name, boolean hidden) {

    public static List<VortexSearchUser> parse(JsonUtils utils, JsonNode node) throws JsonProcessingException {
        String status = node.get("status").asText();
        if ("ok".equalsIgnoreCase(status)) {
            List<VortexSearchUser> list = new ArrayList<>();
            for (var data : node.get("data")) {
                list.add(utils.parse(data, VortexSearchUser.class));
            }
            return list;
        } else {
            //表示用户没找到
            return List.of();
        }
    }
}
