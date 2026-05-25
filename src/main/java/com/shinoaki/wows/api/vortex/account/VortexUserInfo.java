package com.shinoaki.wows.api.vortex.account;

import com.shinoaki.wows.api.data.DogTag;
import com.shinoaki.wows.api.error.BasicException;
import com.shinoaki.wows.api.error.HttpThrowableStatus;
import com.shinoaki.wows.api.vortex.account.statistics.VortexUserInfoStatistics;
import tools.jackson.databind.JsonNode;

/**
 * @author Xun
 */
public record VortexUserInfo(
        VortexUserInfoStatistics statistics,
        String name,
        double created_at,
        double activated_at,
        DogTag dogTag
) {

    public static VortexUserInfo parse(JsonNode node, long accountId) throws BasicException {
        String status = node.path("status").asString();
        if ("ok".equalsIgnoreCase(status)) {
            JsonNode data = node.path("data").get(String.valueOf(accountId));
            if (data != null) {
                var hidden = data.path("hidden_profile");
                if (!hidden.isMissingNode() && hidden.asBoolean()) {
                    throw new BasicException(HttpThrowableStatus.HIDDEN, accountId + "用户隐藏了战绩!");
                }
                String name = data.path("name").asString();
                double createAt = data.path("created_at").asDouble();
                double activatedAt = data.path("activated_at").asDouble();
                JsonNode dogTag = data.path("dog_tag");
                var statistics = VortexUserInfoStatistics.parse(data.path("statistics"));
                if (dogTag == null || dogTag.isEmpty()) {
                    return new VortexUserInfo(statistics, name, createAt, activatedAt, DogTag.empty());
                } else {
                    return new VortexUserInfo(statistics, name, createAt, activatedAt,
                            new DogTag(dogTag.path("texture_id").asLong(),
                                    dogTag.path("symbol_id").asLong(),
                                    dogTag.path("border_color_id").asLong(),
                                    dogTag.path("background_color_id").asLong(),
                                    dogTag.path("background_id").asLong()));
                }
            }
        }
        throw new BasicException(HttpThrowableStatus.DATA_STATUS, accountId + "用户数据状态异常code=" + status);
    }
}
