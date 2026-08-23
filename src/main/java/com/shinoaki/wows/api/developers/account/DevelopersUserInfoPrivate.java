package com.shinoaki.wows.api.developers.account;


import com.shinoaki.wows.api.error.BasicException;
import com.shinoaki.wows.api.utils.JsonUtils;
import com.shinoaki.wows.api.utils.WowsUtils;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;

import java.util.List;

/**
 *
 * @param wows_premium_expires_at   高账到期时间
 * @param gold  金币
 * @param free_xp   全局
 * @param port  战舰列表
 * @param credits   银币
 * @param premium_expires_at
 * @param empty_slots   空余船位
 * @param slots 战舰数量
 * @param battle_life_time  游戏时间
 */
public record DevelopersUserInfoPrivate(
        long wows_premium_expires_at,
        int gold,
        int free_xp,
        List<Long> port,
        long credits,
        long premium_expires_at,
        int empty_slots,
        int slots,
        long battle_life_time
) {

    public static DevelopersUserInfoPrivate parse(JsonNode node) throws BasicException {
        if (node == null || node.isNull() || node.isMissingNode()) {
            return null;
        }
        JsonNode port = node.path("port");
        var portList = port.isMissingNode() || port.isNull() ? List.<Long>of() : JsonUtils.json().parse(port.toString(), new TypeReference<List<Long>>() {
        });
        return new DevelopersUserInfoPrivate(
                WowsUtils.json(node.path("wows_premium_expires_at"), 0),
                WowsUtils.json(node.path("gold"), 0),
                WowsUtils.json(node.path("free_xp"), 0),
                portList,
                WowsUtils.json(node.path("credits"), 0),
                WowsUtils.json(node.path("premium_expires_at"), 0),
                WowsUtils.json(node.path("empty_slots"), 0),
                WowsUtils.json(node.path("slots"), 0),
                WowsUtils.json(node.path("battle_life_time"), 0)
        );
    }
}
