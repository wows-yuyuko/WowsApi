package com.shinoaki.wows.api.developers.account;

import com.shinoaki.wows.api.developers.warships.DevelopersMeta;
import com.shinoaki.wows.api.error.BasicException;
import com.shinoaki.wows.api.error.HttpThrowableStatus;
import com.shinoaki.wows.api.utils.JsonUtils;
import tools.jackson.databind.JsonNode;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * /wows/account/achievements/ 玩家成就：成就ID -&gt; 次数/进度值。
 * <p>
 * 隐藏战绩的账号会被服务器排除（meta.hidden 列出），此时两表为空。
 *
 * @param meta      响应 meta
 * @param accountId 账号ID
 * @param battle    已获得成就（成就ID -&gt; 获得次数）
 * @param progress  成就进度（成就ID -&gt; 进度值）
 * @author Xun
 */
public record DevelopersUserAchievements(DevelopersMeta meta, long accountId,
                                         Map<String, Long> battle, Map<String, Long> progress) {

    public static DevelopersUserAchievements parse(JsonNode node) throws BasicException {
        BasicException.status(node);
        DevelopersMeta meta = JsonUtils.json().parse(node.path("meta"), DevelopersMeta.class);
        for (var entry : node.path("data").properties()) {
            if (entry.getValue() == null || entry.getValue().isNull()) {
                continue;
            }
            return new DevelopersUserAchievements(meta, Long.parseLong(entry.getKey()),
                    counts(entry.getValue().path("battle")), counts(entry.getValue().path("progress")));
        }
        if (meta.hidden() != null && meta.hidden().length > 0) {
            return new DevelopersUserAchievements(meta, meta.hidden()[0], Map.of(), Map.of());
        }
        throw new BasicException(HttpThrowableStatus.DATA_STATUS, "账号成就响应为空");
    }

    private static Map<String, Long> counts(JsonNode node) {
        Map<String, Long> map = new LinkedHashMap<>();
        if (node == null || !node.isObject()) {
            return map;
        }
        for (var entry : node.properties()) {
            map.put(entry.getKey(), entry.getValue().asLong(0));
        }
        return map;
    }
}
