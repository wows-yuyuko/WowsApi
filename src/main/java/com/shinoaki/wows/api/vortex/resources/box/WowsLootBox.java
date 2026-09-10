package com.shinoaki.wows.api.vortex.resources.box;

import com.shinoaki.wows.api.codec.HttpCodec;
import com.shinoaki.wows.api.codec.VortexHttp;
import com.shinoaki.wows.api.error.BasicException;
import com.shinoaki.wows.api.type.WowsServer;
import com.shinoaki.wows.api.utils.JsonUtils;
import com.shinoaki.wows.api.vortex.VortexResourcesLanguage;
import tools.jackson.databind.JsonNode;

import java.net.URI;
import java.net.http.HttpClient;
import java.util.ArrayList;
import java.util.List;

/**
 * 箱子详情，对应接口 GET {vortex}/api/get_lootbox/{language}/{lootboxId}/
 * <p>
 * 示例：<a href="https://vortex.worldofwarships.asia/api/get_lootbox/zh-sg/4288861104/">https://vortex.worldofwarships.asia/api/get_lootbox/zh-sg/4288861104/</a>
 * <p>
 * 与 {@link WowsBox}(箱子列表)不同，本接口返回单个箱子的完整掉落配置：
 * 包含保底奖励(filler)、槽位(slots)，槽位内分为普通奖励(commonRewards)与
 * 高价值奖励(valuableRewards)，每一项按 list0/list1... 分组并带有概率、权重等信息。
 *
 * @param status     接口状态，正常为 ok
 * @param id         箱子ID
 * @param name       箱子内部名称，例如 PCL005_Epic
 * @param tags       标签
 * @param filler     保底奖励(未抽中时填充)
 * @param isPremium  是否为高级箱子
 * @param savePoint  保底点数
 * @param icons      箱子图标
 * @param title      箱子标题
 * @param shortTitle 箱子短标题
 * @param slots      槽位，通常仅一个
 * @author Xun
 */
public record WowsLootBox(
        String status,
        long id,
        String name,
        List<String> tags,
        WowsLootBoxFiller filler,
        boolean isPremium,
        int savePoint,
        WowsLootBoxIcon icons,
        String title,
        String shortTitle,
        List<WowsLootBoxSlot> slots
) {
    private static final String PATH = "/api/get_lootbox/%s/%s/";

    /**
     * 请求箱子详情，默认取 SG(zh-sg) 语言。
     */
    public static WowsLootBox request(WowsServer server, long lootboxId) throws BasicException {
        return request(false, server, VortexResourcesLanguage.SG, lootboxId);
    }

    /**
     * 请求箱子详情。
     */
    public static WowsLootBox request(WowsServer server, VortexResourcesLanguage language, long lootboxId) throws BasicException {
        if (server == WowsServer.RU) {
            return request(false, server, VortexResourcesLanguage.RU, lootboxId);
        }
        return request(false, server, language, lootboxId);
    }

    public static WowsLootBox request(boolean isPtServer, WowsServer server, VortexResourcesLanguage language, long lootboxId) throws BasicException {
        return request(isPtServer, HttpCodec.client(), server, language, lootboxId);
    }

    public static WowsLootBox request(boolean isPtServer, HttpClient client, WowsServer server, VortexResourcesLanguage language, long lootboxId) throws BasicException {
        var url = isPtServer ? server.ptVortexServer() : server.vortex();
        var uri = URI.create(url + String.format(PATH, language.getLanguage(), lootboxId));
        var resp = VortexHttp.send(client, VortexHttp.request(uri));
        return parse(JsonUtils.json().parse(VortexHttp.response(resp)));
    }

    public static WowsLootBox parse(JsonNode node) throws BasicException {
        BasicException.status(node);
        var data = node.path("data");
        List<String> tags = new ArrayList<>();
        for (var tag : data.path("tags")) {
            tags.add(tag.asString());
        }
        return new WowsLootBox(
                node.path("status").asString(""),
                data.path("id").asLong(0),
                data.path("name").asString(""),
                tags,
                WowsLootBoxFiller.parse(data.path("filler")),
                data.path("isPremium").asBoolean(false),
                data.path("savePoint").asInt(0),
                WowsLootBoxIcon.parse(data.path("icons")),
                data.path("title").asString(""),
                data.path("shortTitle").asString(""),
                WowsLootBoxSlot.parseList(data.path("slots"))
        );
    }
}
