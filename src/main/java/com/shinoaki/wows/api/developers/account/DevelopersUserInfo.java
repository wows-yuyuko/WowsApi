package com.shinoaki.wows.api.developers.account;

import com.fasterxml.jackson.databind.JsonNode;
import com.shinoaki.wows.api.developers.account.statistics.DevelopersUserInfoStatistics;
import com.shinoaki.wows.api.error.BasicException;
import com.shinoaki.wows.api.utils.WowsJsonUtils;

/**
 * @param account_id     账号id -1表示没有这个用户
 * @param nickname       用户名称
 * @param hidden_profile 是否隐藏战绩
 * @param created_at     创建时间
 * @author Xun
 * @date 2023/6/7 16:17 星期三
 */
public record DevelopersUserInfo(
        long account_id,
        DevelopersUserInfoStatistics statistics,
        String nickname,
        Boolean hidden_profile,
        long created_at

) {

    public static DevelopersUserInfo parse(WowsJsonUtils utils, long accountId, String json) throws BasicException {
        JsonNode node = utils.parse(json);
        BasicException.status(node);
        JsonNode data = node.get("data").get(String.valueOf(accountId));
        if (data == null || data.isNull()) {
            return new DevelopersUserInfo(-1, null, "", true, 0);
        }
        var statistics = DevelopersUserInfoStatistics.parse(data.get("statistics"));
        return new DevelopersUserInfo(data.get("account_id").asLong(), statistics, data.get("nickname").asText(), data.get("hidden_profile").asBoolean(),
                data.get("created_at").asLong());
    }
}
