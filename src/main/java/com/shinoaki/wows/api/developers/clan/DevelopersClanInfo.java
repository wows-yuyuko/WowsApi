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
        JsonNode data = node.path("data").get(String.valueOf(clanId));
        var membersIds =JsonUtils.json().parse(data.path("members_ids").toString(), new TypeReference<List<Long>>() {
            @Override
            public Type getType() {
                return super.getType();
            }
        });
        return new DevelopersClanInfo(
                data.path("members_count").asInt(),
                data.path("name").asString(),
                data.path("creator_name").asString(),
                data.path("created_at").asLong(),
                data.path("tag").asString(),
                data.path("updated_at").asLong(),
                data.path("leader_name").asString(),
                membersIds,
                data.path("creator_id").asInt(),
                data.path("clan_id").asLong(),
                DevelopersClanUserInfo.parse(data.path("members")),
                data.path("old_name").asString(),
                data.path("is_clan_disbanded").asBoolean(false),
                data.path("renamed_at").asString(),
                data.path("old_tag").asString(),
                data.path("leader_id").asString(),
                data.path("description").asString()
        );
    }

    public record DevelopersClanUserInfo(String role, long joined_at, long account_id, String account_name) {
        public static List<DevelopersClanUserInfo> parse(JsonNode node) {
            if (node == null || node.isNull() || node.isEmpty()) {
                return List.of();
            }
            List<DevelopersClanUserInfo> info = new ArrayList<>();
            for (var next : node.properties()) {
                info.add(new DevelopersClanUserInfo(next.getValue().path("role").asString(),
                        next.getValue().path("joined_at").asLong(),
                        next.getValue().path("account_id").asLong(),
                        next.getValue().path("account_name").asString()));
            }
            return info;
        }
    }
}
