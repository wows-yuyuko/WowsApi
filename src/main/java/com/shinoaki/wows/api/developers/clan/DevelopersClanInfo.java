package com.shinoaki.wows.api.developers.clan;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.shinoaki.wows.api.error.StatusException;
import com.shinoaki.wows.api.utils.JsonUtils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author Xun
 * @date 2023/5/27 2:24 星期六
 */
public record DevelopersClanInfo(int members_count,
                                 String name,
                                 String creator_name,
                                 long created_at,
                                 String tag,
                                 long updated_at,
                                 String leader_name,
                                 List<Long> members_ids,
                                 long creator_id,
                                 long clan_id,
                                 List<DevelopersClanUserInfo> members,
                                 String old_name,
                                 Boolean is_clan_disbanded,
                                 String renamed_at,
                                 String old_tag,
                                 String leader_id,
                                 String description) {

    public static DevelopersClanInfo parse(JsonUtils utils, long clanId, String response) throws JsonProcessingException, StatusException {
        JsonNode node = utils.parse(response);
        StatusException.status(node);
        JsonNode data = node.get("data").get(String.valueOf(clanId));
        return new DevelopersClanInfo(
                data.get("members_count").asInt(),
                data.get("name").asText(),
                data.get("creator_name").asText(),
                data.get("created_at").asLong(),
                data.get("tag").asText(),
                data.get("updated_at").asLong(),
                data.get("leader_name").asText(),
                utils.parse(data.get("members_ids").toString(), new TypeReference<List<Long>>() {
                    @Override
                    public Type getType() {
                        return super.getType();
                    }
                }),
                data.get("creator_id").asInt(),
                data.get("clan_id").asLong(),
                DevelopersClanUserInfo.parse(data.get("members")),
                data.get("old_name").asText(),
                data.get("is_clan_disbanded").asBoolean(false),
                data.get("renamed_at").asText(),
                data.get("old_tag").asText(),
                data.get("leader_id").asText(),
                data.get("description").asText()
        );
    }

    public record DevelopersClanUserInfo(String role, long joined_at, long account_id, String account_name) {
        public static List<DevelopersClanUserInfo> parse(JsonNode node) {
            if (node == null || node.isNull() || node.isEmpty()) {
                return List.of();
            }
            List<DevelopersClanUserInfo> info = new ArrayList<>();
            Iterator<Map.Entry<String, JsonNode>> iterator = node.fields();
            while (iterator.hasNext()) {
                Map.Entry<String, JsonNode> next = iterator.next();
                info.add(new DevelopersClanUserInfo(next.getValue().get("role").asText(),
                        next.getValue().get("joined_at").asLong(),
                        next.getValue().get("account_id").asLong(),
                        next.getValue().get("account_name").asText()));
            }
            return info;
        }
    }
}
