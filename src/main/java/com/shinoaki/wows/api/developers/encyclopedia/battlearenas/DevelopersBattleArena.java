package com.shinoaki.wows.api.developers.encyclopedia.battlearenas;

import com.shinoaki.wows.api.error.BasicException;
import com.shinoaki.wows.api.utils.JsonUtils;
import tools.jackson.databind.JsonNode;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * /wows/encyclopedia/battlearenas/ 地图字典：地图ID -&gt; 信息。
 *
 * @param battle_arena_id 地图ID
 * @param name            地图名
 * @param description     描述
 * @param icon            图标
 * @author Xun
 */
public record DevelopersBattleArena(long battle_arena_id, String name, String description, String icon) {

    public static Map<Long, DevelopersBattleArena> parse(JsonNode node) throws BasicException {
        BasicException.status(node);
        Map<Long, DevelopersBattleArena> map = new LinkedHashMap<>();
        for (var entry : node.path("data").properties()) {
            map.put(Long.parseLong(entry.getKey()), JsonUtils.json().parse(entry.getValue(), DevelopersBattleArena.class));
        }
        return map;
    }
}
