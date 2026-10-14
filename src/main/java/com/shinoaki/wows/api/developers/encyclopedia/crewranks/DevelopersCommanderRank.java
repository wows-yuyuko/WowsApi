package com.shinoaki.wows.api.developers.encyclopedia.crewranks;

import com.shinoaki.wows.api.error.BasicException;
import com.shinoaki.wows.api.utils.JsonUtils;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * /wows/encyclopedia/crewranks/ 指挥官军衔：国家代码 -&gt; 军衔列表。
 *
 * @param rank       军衔等级
 * @param experience 所需经验
 * @param name       军衔名（多国共用时为 null，取 names）
 * @param names      子国家下标 -&gt; 军衔名
 * @author Xun
 */
public record DevelopersCommanderRank(int rank, long experience, String name, Map<String, String> names) {

    public static Map<String, List<DevelopersCommanderRank>> parse(JsonNode node) throws BasicException {
        BasicException.status(node);
        Map<String, List<DevelopersCommanderRank>> map = new LinkedHashMap<>();
        for (var entry : node.path("data").properties()) {
            map.put(entry.getKey(), JsonUtils.json().parse(entry.getValue(), new TypeReference<List<DevelopersCommanderRank>>() {
            }));
        }
        return map;
    }
}
