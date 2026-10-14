package com.shinoaki.wows.api.developers.account;

import com.shinoaki.wows.api.developers.warships.DevelopersMeta;
import com.shinoaki.wows.api.error.BasicException;
import com.shinoaki.wows.api.error.HttpThrowableStatus;
import com.shinoaki.wows.api.utils.JsonUtils;
import tools.jackson.databind.JsonNode;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * /wows/account/statsbydate/ 按日期切片统计：战斗类型 -&gt; 日期(YYYYMMDD) -&gt; 切片。
 * <p>
 * 注意：WG 直营服（亚/欧/美）该接口已下线（实测 GET/POST 均 METHOD_NOT_FOUND），仅莱服提供，
 * 返回的是「date=0」的汇总存根；日期段参数见 {@code dates}（最多当前日期前 28 天）。
 *
 * @param meta       响应 meta
 * @param accountId  账号ID
 * @param battleTypes 战斗类型 -&gt; 日期 -&gt; 统计切片
 * @author Xun
 */
public record DevelopersStatsByDate(DevelopersMeta meta, long accountId,
                                    Map<String, Map<String, DevelopersStatsByDateSlice>> battleTypes) {

    public static DevelopersStatsByDate parse(JsonNode node) throws BasicException {
        BasicException.status(node);
        DevelopersMeta meta = JsonUtils.json().parse(node.path("meta"), DevelopersMeta.class);
        for (var entry : node.path("data").properties()) {
            if (entry.getValue() == null || entry.getValue().isNull()) {
                continue;
            }
            Map<String, Map<String, DevelopersStatsByDateSlice>> map = new LinkedHashMap<>();
            for (var typeEntry : entry.getValue().properties()) {
                Map<String, DevelopersStatsByDateSlice> slices = new LinkedHashMap<>();
                for (var sliceEntry : typeEntry.getValue().properties()) {
                    slices.put(sliceEntry.getKey(),
                            JsonUtils.json().parse(sliceEntry.getValue(), DevelopersStatsByDateSlice.class));
                }
                map.put(typeEntry.getKey(), slices);
            }
            return new DevelopersStatsByDate(meta, Long.parseLong(entry.getKey()), map);
        }
        if (meta.hidden() != null && meta.hidden().length > 0) {
            return new DevelopersStatsByDate(meta, meta.hidden()[0], Map.of());
        }
        throw new BasicException(HttpThrowableStatus.DATA_STATUS, "按日期统计响应为空");
    }
}
