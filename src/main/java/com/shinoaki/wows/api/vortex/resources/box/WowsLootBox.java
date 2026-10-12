package com.shinoaki.wows.api.vortex.resources.box;

import com.shinoaki.wows.api.codec.HttpCodec;
import com.shinoaki.wows.api.codec.VortexHttp;
import com.shinoaki.wows.api.error.BasicException;
import com.shinoaki.wows.api.error.ResponseParseException;
import com.shinoaki.wows.api.type.WowsServer;
import com.shinoaki.wows.api.utils.JsonUtils;
import com.shinoaki.wows.api.vortex.VortexResourcesLanguage;
import tools.jackson.core.JacksonException;
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
 * @param cancharge  是否可充值获得
 * @param cardprob   卡牌掉落概率
 * @param clanresource 军团资源数量
 * @param dogtagprob 狗牌掉落概率
 * @param epicprob   史诗掉落概率
 * @param nestinglevel 嵌套等级
 * @param price      价格(金币)
 * @param quotesTitle 带引号的展示名(莱服独有，例如 «Суперконтейнер»)
 * @param rarity     稀有度(莱服独有，例如 elite；其它服在奖励的 additionalData.rarity 上)
 * @param sortOrder  排序权重(莱服独有)
 * <p>
 * <b>莱服(korabli.su)差异</b>：多 {@code quotesTitle}/{@code rarity}/{@code sortOrder}；
 * 不返回 {@code tags}/{@code filler}/{@code price}/{@code savePoint}/{@code cancharge}/{@code cardprob}/
 * {@code clanresource}/{@code dogtagprob}/{@code epicprob}/{@code nestinglevel}——这些字段在莱服解析后是默认值
 * (0/false/空列表)，<b>不要当成真实数据</b>：莱服的保底奖励与保底点数在 {@link WowsLootBoxSlot} 上
 * ({@code filler}/{@code savePoint})，价格/充值/概率加成莱服整个不提供。
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
        List<WowsLootBoxSlot> slots,
        boolean cancharge,
        double cardprob,
        long clanresource,
        double dogtagprob,
        double epicprob,
        int nestinglevel,
        long price,
        //---- 以下为莱服(korabli.su)独有，其它服缺失、解析为默认值 ----
        String quotesTitle,
        String rarity,
        int sortOrder
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
        try {
            return parse(JsonUtils.json().parseResponse(VortexHttp.response(resp)));
        } catch (JacksonException e) {
            throw new ResponseParseException("响应解析失败: " + e.getMessage(), e);
        }
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
                WowsLootBoxSlot.parseList(data.path("slots")),
                data.path("cancharge").asBoolean(false),
                data.path("cardprob").asDouble(0),
                data.path("clanresource").asLong(0),
                data.path("dogtagprob").asDouble(0),
                data.path("epicprob").asDouble(0),
                data.path("nestinglevel").asInt(0),
                data.path("price").asLong(0),
                data.path("quotesTitle").asString(""),
                data.path("rarity").asString(""),
                data.path("sortOrder").asInt(0)
        );
    }
}
