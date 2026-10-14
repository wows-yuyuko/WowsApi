package com.shinoaki.wows.api.developers.encyclopedia.battletypes;

import com.shinoaki.wows.api.error.BasicException;
import com.shinoaki.wows.api.utils.JsonUtils;
import tools.jackson.databind.JsonNode;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * /wows/encyclopedia/battletypes/ 战斗类型字典：tag -&gt; 信息。
 *
 * @param tag         战斗类型标签（PVP / RANKED / CLAN / PVE / COOPERATIVE 等）
 * @param name        显示名
 * @param description 描述
 * @param image       图片
 * @author Xun
 */
public record DevelopersBattleType(String tag, String name, String description, String image) {

    public static Map<String, DevelopersBattleType> parse(JsonNode node) throws BasicException {
        BasicException.status(node);
        Map<String, DevelopersBattleType> map = new LinkedHashMap<>();
        for (var entry : node.path("data").properties()) {
            map.put(entry.getKey(), JsonUtils.json().parse(entry.getValue(), DevelopersBattleType.class));
        }
        return map;
    }
}
