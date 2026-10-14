package com.shinoaki.wows.api.developers.seasons;

import com.shinoaki.wows.api.developers.warships.DevelopersMeta;
import com.shinoaki.wows.api.error.BasicException;
import com.shinoaki.wows.api.error.HttpThrowableStatus;
import com.shinoaki.wows.api.utils.JsonUtils;
import tools.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;

/**
 * /wows/seasons/shipstats/ 排位单船战绩：账号 + 各舰船按赛季的统计。
 * <p>
 * 不带 season_id 时返回该账号全部有数据的赛季（每艘船各自一块）。
 *
 * @param meta      响应 meta（hidden 为隐藏战绩的账号）
 * @param accountId 账号ID
 * @param ships     各舰船的排位战绩
 * @author Xun
 */
public record DevelopersRankedShipStats(DevelopersMeta meta, long accountId, List<DevelopersRankedShipSeason> ships) {

    public static DevelopersRankedShipStats parse(JsonNode node) throws BasicException {
        BasicException.status(node);
        DevelopersMeta meta = JsonUtils.json().parse(node.path("meta"), DevelopersMeta.class);
        for (var entry : node.path("data").properties()) {
            long accountId = Long.parseLong(entry.getKey());
            JsonNode value = entry.getValue();
            if (value == null || value.isNull()) {
                return new DevelopersRankedShipStats(meta, accountId, List.of());
            }
            List<DevelopersRankedShipSeason> ships = new ArrayList<>();
            for (JsonNode item : value) {
                ships.add(JsonUtils.json().parse(item, DevelopersRankedShipSeason.class));
            }
            return new DevelopersRankedShipStats(meta, accountId, ships);
        }
        if (meta.hidden() != null && meta.hidden().length > 0) {
            return new DevelopersRankedShipStats(meta, meta.hidden()[0], List.of());
        }
        throw new BasicException(HttpThrowableStatus.DATA_STATUS, "排位单船战绩响应为空");
    }
}
