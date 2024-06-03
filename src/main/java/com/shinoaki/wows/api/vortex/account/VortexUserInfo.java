package com.shinoaki.wows.api.vortex.account;

import com.fasterxml.jackson.databind.JsonNode;
import com.shinoaki.wows.api.data.DogTag;
import com.shinoaki.wows.api.error.BasicException;
import com.shinoaki.wows.api.error.HttpThrowableStatus;
import com.shinoaki.wows.api.vortex.account.statistics.VortexUserInfoStatistics;

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
        String status = node.get("status").asText();
        if ("ok".equalsIgnoreCase(status)) {
            JsonNode data = node.get("data").get(String.valueOf(accountId));
            if (data != null) {
                var hidden = data.get("hidden_profile");
                if (hidden != null && hidden.asBoolean()) {
                    throw new BasicException(HttpThrowableStatus.HIDDEN, accountId + "用户隐藏了战绩!");
                }
                String name = data.get("name").asText();
                double createAt = data.get("created_at").asDouble();
                double activatedAt = data.get("activated_at").asDouble();
                JsonNode dogTag = data.get("dog_tag");
                var statistics = VortexUserInfoStatistics.parse(data.get("statistics"));
                if (dogTag == null || dogTag.isEmpty()) {
                    return new VortexUserInfo(statistics, name, createAt, activatedAt, DogTag.empty());
                } else {
                    return new VortexUserInfo(statistics, name, createAt, activatedAt,
                            new DogTag(dogTag.get("texture_id").asLong(),
                                    dogTag.get("symbol_id").asLong(),
                                    dogTag.get("border_color_id").asLong(),
                                    dogTag.get("background_color_id").asLong(),
                                    dogTag.get("background_id").asLong()));
                }
            }
        }
        throw new BasicException(HttpThrowableStatus.DATA_STATUS, accountId + "用户数据状态异常code=" + status);
    }
}
