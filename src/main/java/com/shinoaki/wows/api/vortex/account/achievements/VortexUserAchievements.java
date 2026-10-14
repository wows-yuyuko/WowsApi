package com.shinoaki.wows.api.vortex.account.achievements;

import com.shinoaki.wows.api.error.BasicException;
import com.shinoaki.wows.api.error.HiddenProfileException;
import com.shinoaki.wows.api.error.HttpThrowableStatus;
import tools.jackson.databind.JsonNode;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 账号成就，对应 {@code /api/accounts/{id}/achievements/}。
 * <p>
 * 响应字段：{@code data.<accountId>} 下的 name / created_at / activated_at 与
 * {@code statistics.achievements}（成就ID -&gt; 获得情况，见 {@link VortexUserAchievement}）；
 * 隐藏战绩（hidden_profile=true）时抛 {@link HiddenProfileException}，配置 share token（?ac=）可解锁。
 *
 * @param accountId    账号ID
 * @param name         昵称
 * @param created_at   账号创建时间
 * @param activated_at 账号激活时间
 * @param achievements 成就ID -&gt; 获得情况（无成就时为空表）
 * @author Xun
 */
public record VortexUserAchievements(long accountId, String name, double created_at, double activated_at,
                                     Map<Long, VortexUserAchievement> achievements) {

    public static VortexUserAchievements parse(JsonNode node, long accountId) throws BasicException {
        BasicException.status(node);
        JsonNode data = node.path("data").get(String.valueOf(accountId));
        if (data == null || data.isMissingNode()) {
            throw new BasicException(HttpThrowableStatus.DATA_STATUS, accountId + "成就数据状态异常");
        }
        var hidden = data.path("hidden_profile");
        if (!hidden.isMissingNode() && hidden.asBoolean()) {
            throw new HiddenProfileException(accountId);
        }
        Map<Long, VortexUserAchievement> achievements = new LinkedHashMap<>();
        for (var entry : data.path("statistics").path("achievements").properties()) {
            achievements.put(Long.parseLong(entry.getKey()), VortexUserAchievement.parse(entry.getValue()));
        }
        return new VortexUserAchievements(accountId, data.path("name").asString(""),
                data.path("created_at").asDouble(0), data.path("activated_at").asDouble(0), achievements);
    }
}
