package com.shinoaki.wows.api.developers.account;

import com.shinoaki.wows.api.developers.account.statistics.DevelopersUserInfoStatistics;
import com.shinoaki.wows.api.error.BasicException;
import com.shinoaki.wows.api.error.HiddenProfileException;
import com.shinoaki.wows.api.error.HttpThrowableStatus;
import com.shinoaki.wows.api.utils.JsonUtils;
import tools.jackson.databind.JsonNode;


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
        DevelopersUserInfoPrivate userInfoPrivate,
        String nickname,
        Boolean hidden_profile,
        long created_at
) {

    public static DevelopersUserInfo parse(long accountId, String json) throws BasicException {
        JsonNode node = JsonUtils.json().parse(json);
        BasicException.status(node);
        JsonNode data = node.path("data").get(String.valueOf(accountId));
        if (data == null || data.isNull()) {
            throw new BasicException(HttpThrowableStatus.DATA_STATUS, accountId + "用户数据状态异常");
        }
        //hidden_profile在data下的账号节点中，不在响应根节点
        var hidden = data.path("hidden_profile");
        if (!hidden.isMissingNode() && hidden.asBoolean()) {
            throw new HiddenProfileException(accountId);
        }
        var statistics = DevelopersUserInfoStatistics.parse(data.path("statistics"));
        var infoPrivate = DevelopersUserInfoPrivate.parse(data.path("private"));
        return new DevelopersUserInfo(data.path("account_id").asLong(), statistics, infoPrivate,
                data.path("nickname").asString(), false,
                data.path("created_at").asLong());
    }
}
