package com.shinoaki.wows.api.developers.encyclopedia.glossary;

import com.shinoaki.wows.api.error.BasicException;
import com.shinoaki.wows.api.utils.JsonUtils;
import tools.jackson.databind.JsonNode;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 军团词典，对应官方API {@code /wows/clans/glossary/}（莱服 {@code /mk/clans/glossary/}）。
 * <p>
 * 响应字段：data 下的 building_types、buildings、clans_roles、settings。
 *
 * @param building_types 建筑类型
 * @param buildings      建筑
 * @param clans_roles    军团职位名称(职位key -> 显示名)
 * @param settings       军团设置
 * @author Xun
 */
public record DevelopersGlossary(
        List<DevelopersGlossaryBuildingType> building_types,
        List<DevelopersGlossaryBuilding> buildings,
        Map<String, String> clans_roles,
        DevelopersGlossarySettings settings
) {

    public static DevelopersGlossary parse(String jsonData) throws BasicException {
        var node = JsonUtils.json().parse(jsonData);
        BasicException.status(node);
        var data = node.path("data");
        return new DevelopersGlossary(DevelopersGlossaryBuildingType.parse(data),
                DevelopersGlossaryBuilding.parse(data),
                clanRoles(data.path("clans_roles")),
                DevelopersGlossarySettings.parse(data.path("settings")));
    }

    private static Map<String, String> clanRoles(JsonNode node) {
        Map<String, String> roles = new LinkedHashMap<>();
        if (node == null || !node.isObject()) {
            return roles;
        }
        for (Map.Entry<String, JsonNode> role : node.properties()) {
            roles.put(role.getKey(), role.getValue().asString(""));
        }
        return roles;
    }
}
