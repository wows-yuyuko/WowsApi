package com.shinoaki.wows.api.data;

import com.shinoaki.wows.api.error.BasicException;
import com.shinoaki.wows.api.utils.JsonUtils;
import tools.jackson.databind.JsonNode;
 

/**
 * 游戏账号基础信息
 *
 * @param accountId 账号id
 * @param nickname  账号名称
 * @param createdAt 创建时间
 * @param hiddenProfile 是否隐藏战绩
 * @param lastBattleTime    最后战斗时间
 * @param statsUpdatedAt    账号状态更新时间
 * @param logoutAt  登出时间
 * @param clan 公会信息
 */
public record AccountInfo(
        long accountId,
        String nickname,
        long createdAt,
        boolean hiddenProfile,
        long lastBattleTime,
        long statsUpdatedAt,
        long logoutAt,
        AccountClanInfo clan
) {

    public static AccountInfo parse(  long accountId, String json, AccountClanInfo clan) throws BasicException {
        JsonNode node =  JsonUtils.json().parse(json);
        BasicException.status(node);
        JsonNode data = node.path("data").get(String.valueOf(accountId));
        if (data == null || data.isNull()) {
            return new AccountInfo(-1, null, 0, false, 0, 0, 0, clan);
        }
        return new AccountInfo(data.path("account_id").asLong(),
                data.path("nickname").asString(),
                data.path("created_at").asLong(),
                data.path("hidden_profile").asBoolean(),
                data.path("last_battle_time").asLong(),
                data.path("stats_updated_at").asLong(),
                data.path("logout_at").asLong(),
                clan);
    }
}
