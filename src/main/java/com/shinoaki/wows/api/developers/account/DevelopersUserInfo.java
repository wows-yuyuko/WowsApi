package com.shinoaki.wows.api.developers.account;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.shinoaki.wows.api.error.StatusException;
import com.shinoaki.wows.api.utils.JsonUtils;

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
        String nickname,
        Boolean hidden_profile,
        long created_at

) {

    public static DevelopersUserInfo parse(JsonUtils utils, long accountId, JsonNode node) throws StatusException, JsonProcessingException {
        StatusException.status(node);
        JsonNode data = node.get("data").get(String.valueOf(accountId));
        if (data == null || data.isNull()) {
            return new DevelopersUserInfo(-1, "", false, 0);
        }
        return utils.parse(data.toString(), DevelopersUserInfo.class);
    }
}
