package com.shinoaki.wows.api.developers.encyclopedia.consumables;

import com.shinoaki.wows.api.developers.warships.DevelopersMeta;
import com.shinoaki.wows.api.error.BasicException;
import com.shinoaki.wows.api.utils.JsonUtils;
import tools.jackson.databind.JsonNode;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * /wows/encyclopedia/consumables/ 消耗品字典（分页）：消耗品ID -&gt; 信息。
 * <p>
 * 覆盖迷彩/永久涂装/皮肤/升级品/信号旗等（type 字段区分）。
 *
 * @author Xun
 */
public record DevelopersEncyclopediaConsumables(DevelopersMeta meta,
                                                Map<Long, DevelopersEncyclopediaConsumable> consumables) {

    public static DevelopersEncyclopediaConsumables parse(JsonNode node) throws BasicException {
        BasicException.status(node);
        DevelopersMeta meta = JsonUtils.json().parse(node.path("meta"), DevelopersMeta.class);
        Map<Long, DevelopersEncyclopediaConsumable> consumables = new LinkedHashMap<>();
        for (var entry : node.path("data").properties()) {
            consumables.put(Long.parseLong(entry.getKey()),
                    JsonUtils.json().parse(entry.getValue(), DevelopersEncyclopediaConsumable.class));
        }
        return new DevelopersEncyclopediaConsumables(meta, consumables);
    }
}
