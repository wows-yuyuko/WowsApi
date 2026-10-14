package com.shinoaki.wows.api.developers.encyclopedia.ships;

import com.shinoaki.wows.api.developers.warships.DevelopersMeta;
import com.shinoaki.wows.api.error.BasicException;
import com.shinoaki.wows.api.utils.JsonUtils;
import tools.jackson.databind.JsonNode;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * /wows/encyclopedia/ships/ 舰船字典（分页）：舰船ID -&gt; 信息。
 * <p>
 * 单船查询（ship_id 参数）时 map 只有一条；分页信息见 {@link DevelopersMeta}（limit/page/page_total/total）。
 *
 * @author Xun
 */
public record DevelopersEncyclopediaShips(DevelopersMeta meta, Map<Long, DevelopersEncyclopediaShip> ships) {

    public static DevelopersEncyclopediaShips parse(JsonNode node) throws BasicException {
        BasicException.status(node);
        DevelopersMeta meta = JsonUtils.json().parse(node.path("meta"), DevelopersMeta.class);
        Map<Long, DevelopersEncyclopediaShip> ships = new LinkedHashMap<>();
        for (var entry : node.path("data").properties()) {
            ships.put(Long.parseLong(entry.getKey()), JsonUtils.json().parse(entry.getValue(), DevelopersEncyclopediaShip.class));
        }
        return new DevelopersEncyclopediaShips(meta, ships);
    }
}
