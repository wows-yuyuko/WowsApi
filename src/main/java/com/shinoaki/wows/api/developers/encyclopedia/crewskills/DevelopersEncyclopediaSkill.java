package com.shinoaki.wows.api.developers.encyclopedia.crewskills;

import com.shinoaki.wows.api.error.BasicException;
import com.shinoaki.wows.api.utils.JsonUtils;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * /wows/encyclopedia/crewskills/ 指挥官技能字典：技能ID -&gt; 信息。
 *
 * @param customization 舰种（AirCarrier/Battleship/...）-&gt; 位置与强化
 * @author Xun
 */
public record DevelopersEncyclopediaSkill(String icon, String name, int type_id, String type_name,
                                          Map<String, DevelopersSkillCustomization> customization) {

    public static Map<Long, DevelopersEncyclopediaSkill> parse(JsonNode node) throws BasicException {
        BasicException.status(node);
        Map<Long, DevelopersEncyclopediaSkill> map = new LinkedHashMap<>();
        for (var entry : node.path("data").properties()) {
            map.put(Long.parseLong(entry.getKey()), JsonUtils.json().parse(entry.getValue(), DevelopersEncyclopediaSkill.class));
        }
        return map;
    }
}
