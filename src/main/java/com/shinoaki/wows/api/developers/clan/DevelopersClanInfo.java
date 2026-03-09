package com.shinoaki.wows.api.developers.clan;


import com.shinoaki.wows.api.error.BasicException;
import com.shinoaki.wows.api.utils.JsonUtils;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

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

    public static DevelopersClanInfo parse(long clanId, String response) throws BasicException {
        JsonNode node = JsonUtils.json().parse(response);
        BasicException.status(node);
        JsonNode data = node.get("data").get(String.valueOf(clanId));
        var membersIds =JsonUtils.json().parse(data.get("members_ids").toString(), new TypeReference<List<Long>>() {
            @Override
            public Type getType() {
                return super.getType();
            }
        });
        return new DevelopersClanInfo(
                data.get("members_count").asInt(),
                data.get("name").asString(),
                data.get("creator_name").asString(),
                data.get("created_at").asLong(),
                data.get("tag").asString(),
                data.get("updated_at").asLong(),
                data.get("leader_name").asString(),
                membersIds,
                data.get("creator_id").asInt(),
                data.get("clan_id").asLong(),
                DevelopersClanUserInfo.parse(data.get("members")),
                data.get("old_name").asString(),
                data.get("is_clan_disbanded").asBoolean(false),
                data.get("renamed_at").asString(),
                data.get("old_tag").asString(),
                data.get("leader_id").asString(),
                data.get("description").asString()
        );
    }

    public record DevelopersClanUserInfo(String role, long joined_at, long account_id, String account_name) {
        public static List<DevelopersClanUserInfo> parse(JsonNode node) {
            if (node == null || node.isNull() || node.isEmpty()) {
                return List.of();
            }
            List<DevelopersClanUserInfo> info = new ArrayList<>();
            for (var next : node.properties()) {
                info.add(new DevelopersClanUserInfo(next.getValue().get("role").asString(),
                        next.getValue().get("joined_at").asLong(),
                        next.getValue().get("account_id").asLong(),
                        next.getValue().get("account_name").asString()));
            }
            return info;
        }
    }
}
