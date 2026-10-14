package com.shinoaki.wows.api.developers.encyclopedia.accountlevels;

import com.shinoaki.wows.api.error.BasicException;
import com.shinoaki.wows.api.utils.JsonUtils;
import tools.jackson.databind.JsonNode;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * /wows/encyclopedia/accountlevels/ 服役记录等级：等级 -&gt; 信息。
 *
 * @param tier   等级
 * @param points 达到该等级所需点数
 * @param image  等级图标
 * @author Xun
 */
public record DevelopersAccountLevel(int tier, int points, String image) {

    public static Map<Long, DevelopersAccountLevel> parse(JsonNode node) throws BasicException {
        BasicException.status(node);
        Map<Long, DevelopersAccountLevel> map = new LinkedHashMap<>();
        for (var entry : node.path("data").properties()) {
            map.put(Long.parseLong(entry.getKey()), JsonUtils.json().parse(entry.getValue(), DevelopersAccountLevel.class));
        }
        return map;
    }
}
