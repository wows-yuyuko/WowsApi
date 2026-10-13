package com.shinoaki.wows.api.data;

import com.shinoaki.wows.api.developers.clan.DevelopersClanInfo;
import com.shinoaki.wows.api.developers.clan.DevelopersSearchUserClan;
import com.shinoaki.wows.api.error.BasicException;
import com.shinoaki.wows.api.utils.JsonUtils;
import tools.jackson.databind.JsonNode;
 

/**
 * 用户公会信息
 * @param clanId     公会id
 * @param name  公会名称
 * @param tag   tag
 * @param description   介绍
 * @param membersCount  成员数量
 * @param userRole 用户在公会的角色
 * @param joinedAt 用户加入公会时间
 * @param createdAt 公会创建时间
 * @param updatedAt 公会更新时间
 */
public record AccountClanInfo(
        long clanId,
        String name,
        String tag,
        String description,
        int membersCount,
        String userRole,
        long joinedAt,
        long createdAt,
        long updatedAt
) {

    public static AccountClanInfo accountClan(long accountId, String json) throws BasicException {
        return accountClan(accountId, JsonUtils.json().parse(json));
    }

    public static AccountClanInfo accountClan(long accountId, JsonNode node) throws BasicException {
        BasicException.status(node);
        JsonNode data = node.path("data").get(String.valueOf(accountId));
        if (data == null || data.isNull()) {
            return empty();
        }
        return new AccountClanInfo(
                data.path("clan_id").asLong(),
                "", "", "", 0,
                data.path("role").asString(),
                data.path("joined_at").asLong(),
                0, 0);
    }

    /**
     * 由 3.0 新管线的「玩家所属军团」端点结果（{@code dev.clan.account}）构造，
     * 字段与 {@link #accountClan(long, JsonNode)} 同一来源、同形（未补全公会详情）。
     */
    public static AccountClanInfo of(DevelopersSearchUserClan ref) {
        return new AccountClanInfo(ref.clan_id(), "", "", "", 0, ref.role(), ref.joined_at(), 0, 0);
    }

    public static AccountClanInfo of(AccountClanInfo info, DevelopersClanInfo developersInfo) {
        return new AccountClanInfo(
                info.clanId(),
                developersInfo.name(),
                developersInfo.tag(),
                developersInfo.description(),
                developersInfo.members_count(),
                info.userRole(),
                info.joinedAt(),
                developersInfo.created_at(),
                developersInfo.updated_at());
    }

    public static AccountClanInfo empty() {
        return new AccountClanInfo(0, "", "", "", 0, "", 0, 0, 0);
    }
}
