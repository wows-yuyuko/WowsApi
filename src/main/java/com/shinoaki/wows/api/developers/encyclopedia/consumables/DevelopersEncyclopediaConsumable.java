package com.shinoaki.wows.api.developers.encyclopedia.consumables;

import java.util.Map;

/**
 * 单个消耗品（description 可能为 null；profile 为特性代码 -&gt; 数值+说明，无特性时为空表）。
 *
 * @author Xun
 */
public record DevelopersEncyclopediaConsumable(long consumable_id, String type, String name, String description,
                                               long price_credit, long price_gold, String image,
                                               Map<String, DevelopersConsumableEffect> profile) {

    public DevelopersEncyclopediaConsumable {
        profile = profile == null ? Map.of() : profile;
    }
}
