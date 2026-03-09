package com.shinoaki.wows.api.vortex.account;

import com.shinoaki.wows.api.error.BasicException;
import com.shinoaki.wows.api.utils.JsonUtils;
import tools.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;

/**
 * https://vortex.wowsgame.cn/api/accounts/search/西行寺雨季/
 *
 * @author Xun
 * @date 2023/5/19 23:31 星期五
 */
public record VortexSearchUser(long spa_id, String name, boolean hidden) {

    public static List<VortexSearchUser> parse(  String json) throws BasicException {
        JsonNode node =  JsonUtils.json().parse(json);
        String status = node.get("status").asString();
        if ("ok".equalsIgnoreCase(status)) {
            List<VortexSearchUser> list = new ArrayList<>();
            for (var data : node.get("data")) {
                list.add( JsonUtils.json().parse(data, VortexSearchUser.class));
            }
            return list;
        } else {
            //表示用户没找到
            return List.of();
        }
    }
}
