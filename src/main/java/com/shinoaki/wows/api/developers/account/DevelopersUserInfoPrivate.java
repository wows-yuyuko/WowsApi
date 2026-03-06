package com.shinoaki.wows.api.developers.account;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.shinoaki.wows.api.error.BasicException;
import com.shinoaki.wows.api.utils.JsonUtils;
import com.shinoaki.wows.api.utils.WowsJsonUtils;
import com.shinoaki.wows.api.utils.WowsUtils;

import java.lang.reflect.Type;
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
        if (node == null || node.isNull()) {
            return null;
        }
        return new DevelopersUserInfoPrivate(
                WowsUtils.json(node.get("wows_premium_expires_at"), 0),
                WowsUtils.json(node.get("gold"), 0),
                WowsUtils.json(node.get("free_xp"), 0),
                new WowsJsonUtils().parse(node.get("port").toString(), new TypeReference<List<Long>>() {
                    @Override
                    public Type getType() {
                        return super.getType();
                    }
                }),
                WowsUtils.json(node.get("credits"), 0),
                WowsUtils.json(node.get("premium_expires_at"), 0),
                WowsUtils.json(node.get("empty_slots"), 0),
                WowsUtils.json(node.get("slots"), 0),
                WowsUtils.json(node.get("battle_life_time"), 0)
                );
    }
}
