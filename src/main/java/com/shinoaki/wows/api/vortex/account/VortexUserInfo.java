package com.shinoaki.wows.api.vortex.account;

import com.shinoaki.wows.api.data.DogTag;
import com.shinoaki.wows.api.error.BasicException;
import com.shinoaki.wows.api.error.HiddenProfileException;
import com.shinoaki.wows.api.error.HttpThrowableStatus;
import com.shinoaki.wows.api.vortex.account.statistics.VortexUserInfoStatistics;
import tools.jackson.databind.JsonNode;

/**
 * 账号详情，对应 {@code /api/accounts/{id}/}。
 * <p>
 * 响应字段：{@code data.<accountId>} 下的 name / created_at / activated_at / dog_tag /
 * visibility_settings / statistics(见 {@link VortexUserInfoStatistics}) / hidden_profile(隐藏战绩时出现)。
 *
 * @param statistics          战绩汇总
 * @param name                昵称
 * @param created_at          账号创建时间
 * @param activated_at        账号激活时间
 * @param dogTag              狗牌
 * @param visibility_settings 战绩可见性设置
 * @author Xun
 */
public record VortexUserInfo(
        VortexUserInfoStatistics statistics,
        String name,
        double created_at,
        double activated_at,
        DogTag dogTag,
        boolean visibility_settings
) {

    public static VortexUserInfo parse(JsonNode node, long accountId) throws BasicException {
        String status = node.path("status").asString();
        if ("ok".equalsIgnoreCase(status)) {
            JsonNode data = node.path("data").get(String.valueOf(accountId));
            if (data != null) {
                var hidden = data.path("hidden_profile");
                if (!hidden.isMissingNode() && hidden.asBoolean()) {
                    throw new HiddenProfileException(accountId);
                }
                String name = data.path("name").asString();
                double createAt = data.path("created_at").asDouble();
                double activatedAt = data.path("activated_at").asDouble();
                boolean visibilitySettings = data.path("visibility_settings").asBoolean(false);
                JsonNode dogTag = data.path("dog_tag");
                var statistics = VortexUserInfoStatistics.parse(data.path("statistics"));
                if (dogTag == null || dogTag.isEmpty()) {
                    return new VortexUserInfo(statistics, name, createAt, activatedAt, DogTag.empty(), visibilitySettings);
                } else {
                    return new VortexUserInfo(statistics, name, createAt, activatedAt,
                            new DogTag(dogTag.path("texture_id").asLong(),
                                    dogTag.path("symbol_id").asLong(),
                                    dogTag.path("border_color_id").asLong(),
                                    dogTag.path("background_color_id").asLong(),
                                    dogTag.path("background_id").asLong()),
                            visibilitySettings);
                }
            }
        }
        throw new BasicException(HttpThrowableStatus.DATA_STATUS, accountId + "用户数据状态异常code=" + status);
    }
}
