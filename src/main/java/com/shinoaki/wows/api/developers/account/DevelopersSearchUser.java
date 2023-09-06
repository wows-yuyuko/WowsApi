package com.shinoaki.wows.api.developers.account;

import com.fasterxml.jackson.databind.JsonNode;
import com.shinoaki.wows.api.error.BasicException;
import com.shinoaki.wows.api.utils.WowsJsonUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Xun
 * @date 2023/5/22 22:45 星期一
 */
public record DevelopersSearchUser(String nickname, long account_id) {
    public static List<DevelopersSearchUser> parse(WowsJsonUtils utils, String json) throws BasicException {
        JsonNode node = utils.parse(json);
        BasicException.status(node);
        List<DevelopersSearchUser> list = new ArrayList<>();
        for (var data : node.get("data")) {
            list.add(utils.parse(data, DevelopersSearchUser.class));
        }
        return list;
    }
}
