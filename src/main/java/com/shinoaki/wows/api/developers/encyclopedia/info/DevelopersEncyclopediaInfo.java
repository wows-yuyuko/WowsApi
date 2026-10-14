package com.shinoaki.wows.api.developers.encyclopedia.info;

import com.shinoaki.wows.api.error.BasicException;
import com.shinoaki.wows.api.utils.JsonUtils;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * /wows/encyclopedia/info/ 百科基础信息：游戏版本、可用语言、模块/舰种/国家等字典。
 *
 * @param game_version       游戏客户端版本
 * @param ships_updated_at   舰船数据更新时间戳
 * @param languages          语言代码 -&gt; 显示名
 * @param ship_modifications 改装件代码 -&gt; 显示名
 * @param ship_modules       模块类型代码 -&gt; 显示名
 * @param ship_nations       国家代码 -&gt; 显示名
 * @param ship_types         舰种代码 -&gt; 显示名
 * @param ship_type_images   舰种 -&gt; 图标
 * @author Xun
 */
public record DevelopersEncyclopediaInfo(String game_version, long ships_updated_at, Map<String, String> languages,
                                          Map<String, String> ship_modifications, Map<String, String> ship_modules,
                                          Map<String, String> ship_nations, Map<String, String> ship_types,
                                          Map<String, DevelopersShipTypeImages> ship_type_images) {

    public static DevelopersEncyclopediaInfo parse(JsonNode node) throws BasicException {
        BasicException.status(node);
        JsonNode data = node.path("data");
        return new DevelopersEncyclopediaInfo(
                data.path("game_version").asString(""),
                data.path("ships_updated_at").asLong(0),
                stringMap(data.path("languages")),
                stringMap(data.path("ship_modifications")),
                stringMap(data.path("ship_modules")),
                stringMap(data.path("ship_nations")),
                stringMap(data.path("ship_types")),
                JsonUtils.json().parse(data.path("ship_type_images"), new TypeReference<Map<String, DevelopersShipTypeImages>>() {
                }));
    }

    private static Map<String, String> stringMap(JsonNode node) {
        Map<String, String> map = new LinkedHashMap<>();
        if (node == null || !node.isObject()) {
            return map;
        }
        for (var entry : node.properties()) {
            map.put(entry.getKey(), entry.getValue().asString(""));
        }
        return map;
    }
}
