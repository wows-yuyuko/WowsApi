package com.shinoaki.wows.api.developers.account;

import com.shinoaki.wows.api.error.BasicException;
import com.shinoaki.wows.api.utils.JsonUtils;
import tools.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Xun
 * @date 2023/5/22 22:45 星期一
 */
public record DevelopersSearchUser(String nickname, long account_id) {
    public static List<DevelopersSearchUser> parse(String json) throws BasicException {
        JsonNode node = JsonUtils.json().parse(json);
        BasicException.status(node);
        List<DevelopersSearchUser> list = new ArrayList<>();
        for (var data : node.path("data")) {
            list.add(JsonUtils.json().parse(data, DevelopersSearchUser.class));
        }
        return list;
    }
}
