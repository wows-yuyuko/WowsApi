package com.shinoaki.wows.api.developers.encyclopedia.modules;

import com.shinoaki.wows.api.developers.warships.DevelopersMeta;
import com.shinoaki.wows.api.error.BasicException;
import com.shinoaki.wows.api.utils.JsonUtils;
import tools.jackson.databind.JsonNode;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * /wows/encyclopedia/modules/ 模块字典（分页）：模块ID -&gt; 信息。
 *
 * @author Xun
 */
public record DevelopersEncyclopediaModules(DevelopersMeta meta, Map<Long, DevelopersEncyclopediaModule> modules) {

    public static DevelopersEncyclopediaModules parse(JsonNode node) throws BasicException {
        BasicException.status(node);
        DevelopersMeta meta = JsonUtils.json().parse(node.path("meta"), DevelopersMeta.class);
        Map<Long, DevelopersEncyclopediaModule> modules = new LinkedHashMap<>();
        for (var entry : node.path("data").properties()) {
            modules.put(Long.parseLong(entry.getKey()), JsonUtils.json().parse(entry.getValue(), DevelopersEncyclopediaModule.class));
        }
        return new DevelopersEncyclopediaModules(meta, modules);
    }
}
