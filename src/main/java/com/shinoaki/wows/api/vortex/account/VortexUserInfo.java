package com.shinoaki.wows.api.vortex.account;

import com.fasterxml.jackson.databind.JsonNode;
import com.shinoaki.wows.api.error.BasicException;
import com.shinoaki.wows.api.error.HttpThrowableStatus;

/**
 * @author Xun
 */
public record VortexUserInfo(
        String name,
        double created_at,
        double activated_at,
        long dog_tag_texture_id,
        long dog_tag_symbol_id,
        long dog_tag_border_color_id,
        long dog_tag_background_color_id,
        long dog_tag_background_id
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
                if (dogTag == null || dogTag.isEmpty()) {
                    return new VortexUserInfo(name, createAt, activatedAt, -1, -1, -1, -1, -1);
                } else {
                    return new VortexUserInfo(name, createAt, activatedAt,
                            dogTag.get("texture_id").asLong(),
                            dogTag.get("symbol_id").asLong(),
                            dogTag.get("border_color_id").asLong(),
                            dogTag.get("background_color_id").asLong(),
                            dogTag.get("background_id").asLong());
                }
            }
        }
        return null;
    }
}
