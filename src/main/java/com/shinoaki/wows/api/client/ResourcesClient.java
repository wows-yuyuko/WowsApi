package com.shinoaki.wows.api.client;

import com.shinoaki.wows.api.endpoint.EndpointRequest;
import com.shinoaki.wows.api.endpoint.ResourcesEndpoints;
import com.shinoaki.wows.api.endpoint.VortexEndpoints;
import com.shinoaki.wows.api.error.BasicException;
import com.shinoaki.wows.api.transport.HttpPipeline;
import com.shinoaki.wows.api.type.WowsServer;
import com.shinoaki.wows.api.vortex.VortexResourcesLanguage;
import com.shinoaki.wows.api.vortex.resources.RequestVortexResourcesInfo;
import com.shinoaki.wows.api.vortex.resources.WowsResources;
import com.shinoaki.wows.api.vortex.resources.box.WowsBox;
import com.shinoaki.wows.api.vortex.resources.box.WowsLootBox;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 资源/箱子视图（3.0 重写）：<b>1 调用 = 1 请求</b>。
 * <p>
 * 实例由 {@code WowsApi.resources()} 创建；箱子详情走 GET，三个 graphql 端点走 POST
 * （请求体由模型类的 {@code requestBody(...)} 提供，与旧实现逐字节一致）。
 * PT 测试服：{@code WowsApi.pt().resources()}（vortex 主机切到 PT）。
 *
 * @author Xun
 */
public final class ResourcesClient {

    private final HttpPipeline pipeline;

    public ResourcesClient(HttpPipeline pipeline) {
        this.pipeline = Objects.requireNonNull(pipeline, "pipeline");
    }

    /**
     * 箱子详情（1 请求）；默认语言：莱服固定 ru，其余服 zh-sg（与旧实现一致）。
     */
    public WowsLootBox lootbox(long lootboxId) throws BasicException {
        return lootbox(lootboxId, VortexResourcesLanguage.SG);
    }

    /**
     * 箱子详情（1 请求）。莱服内容只有 RU 版本：语言在莱服被固定为 {@code ru}（沿用旧行为，传入的语言不生效）。
     */
    public WowsLootBox lootbox(long lootboxId, VortexResourcesLanguage language) throws BasicException {
        VortexResourcesLanguage use = pipeline.profile().server() == WowsServer.RU
                ? VortexResourcesLanguage.RU
                : language;
        return pipeline.execute(VortexEndpoints.LOOTBOX_DETAIL, EndpointRequest.of(Map.of(
                "language", use.getLanguage(),
                "lootboxId", String.valueOf(lootboxId))));
    }

    /**
     * 箱子列表（graphql Lootbox schema，POST；1 请求）。
     */
    public List<WowsBox> lootboxes(VortexResourcesLanguage language) throws BasicException {
        return pipeline.execute(ResourcesEndpoints.LOOTBOX_LIST,
                EndpointRequest.of(Map.of("language", language.getLanguage())));
    }

    /**
     * 资源列表（graphql Items schema，POST；1 请求）。
     */
    public List<WowsResources> items(VortexResourcesLanguage language) throws BasicException {
        return pipeline.execute(ResourcesEndpoints.ITEMS,
                EndpointRequest.of(Map.of("language", language.getLanguage())));
    }

    /**
     * 资源索引/词典（graphql getGlossData schema，POST；1 请求）：词典 + 船 + 成就一次拉取。
     */
    public RequestVortexResourcesInfo glossary(VortexResourcesLanguage language) throws BasicException {
        return pipeline.execute(ResourcesEndpoints.GLOSSARY,
                EndpointRequest.of(Map.of("language", language.getLanguage())));
    }
}
