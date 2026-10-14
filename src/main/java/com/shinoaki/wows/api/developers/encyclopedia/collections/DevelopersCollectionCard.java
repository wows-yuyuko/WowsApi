package com.shinoaki.wows.api.developers.encyclopedia.collections;

import com.shinoaki.wows.api.error.BasicException;
import com.shinoaki.wows.api.utils.JsonUtils;
import tools.jackson.databind.JsonNode;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * /wows/encyclopedia/collectioncards/ 收藏卡牌字典：卡牌ID -&gt; 信息。
 *
 * @param card_id       卡牌ID
 * @param collection_id 所属收藏ID
 * @param name          卡牌名
 * @param description   描述（可能为 null）
 * @param tag           标签
 * @param images        三种尺寸图（large 可能为 null）
 * @author Xun
 */
public record DevelopersCollectionCard(long card_id, long collection_id, String name, String description, String tag,
                                       DevelopersCollectionCardImages images) {

    public static Map<Long, DevelopersCollectionCard> parse(JsonNode node) throws BasicException {
        BasicException.status(node);
        Map<Long, DevelopersCollectionCard> map = new LinkedHashMap<>();
        for (var entry : node.path("data").properties()) {
            map.put(Long.parseLong(entry.getKey()), JsonUtils.json().parse(entry.getValue(), DevelopersCollectionCard.class));
        }
        return map;
    }
}
