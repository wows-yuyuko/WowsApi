package com.shinoaki.wows.api.developers.encyclopedia.collections;

import com.shinoaki.wows.api.error.BasicException;
import com.shinoaki.wows.api.utils.JsonUtils;
import tools.jackson.databind.JsonNode;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * /wows/encyclopedia/collections/ 收藏字典：收藏ID -&gt; 信息。
 *
 * @param collection_id 收藏ID
 * @param name          收藏名
 * @param description   描述
 * @param image         图片
 * @param tag           标签
 * @param card_cost     重复卡牌兑换所需数量
 * @author Xun
 */
public record DevelopersCollection(long collection_id, String name, String description, String image, String tag,
                                   int card_cost) {

    public static Map<Long, DevelopersCollection> parse(JsonNode node) throws BasicException {
        BasicException.status(node);
        Map<Long, DevelopersCollection> map = new LinkedHashMap<>();
        for (var entry : node.path("data").properties()) {
            map.put(Long.parseLong(entry.getKey()), JsonUtils.json().parse(entry.getValue(), DevelopersCollection.class));
        }
        return map;
    }
}
