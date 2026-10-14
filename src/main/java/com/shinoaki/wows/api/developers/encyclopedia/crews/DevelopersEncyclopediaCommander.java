package com.shinoaki.wows.api.developers.encyclopedia.crews;

import com.shinoaki.wows.api.error.BasicException;
import com.shinoaki.wows.api.utils.JsonUtils;
import tools.jackson.databind.JsonNode;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * /wows/encyclopedia/crews/ 指挥官字典：指挥官ID -&gt; 信息。
 *
 * @param icons      立绘列表（每项为「技能点数区间 -&gt; URL」的映射，如 {"1": "https://..."}）
 * @param first_names 名字候选
 * @param last_names  姓氏候选（可能为空表）
 * @author Xun
 */
public record DevelopersEncyclopediaCommander(int base_training_level, long base_training_hire_price, int gold_training_level,
                                              long gold_training_hire_price, long gold_retraining_price, int money_training_level,
                                              long money_training_hire_price, Long money_retraining_price, boolean is_retrainable,
                                              String nation, int subnation_index, List<String> first_names,
                                              List<String> last_names, List<Map<String, String>> icons) {

    public static Map<Long, DevelopersEncyclopediaCommander> parse(JsonNode node) throws BasicException {
        BasicException.status(node);
        Map<Long, DevelopersEncyclopediaCommander> map = new LinkedHashMap<>();
        for (var entry : node.path("data").properties()) {
            map.put(Long.parseLong(entry.getKey()), JsonUtils.json().parse(entry.getValue(), DevelopersEncyclopediaCommander.class));
        }
        return map;
    }
}
