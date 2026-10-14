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
 * 资源/箱子视图：1 调用 = 1 请求；由 {@code WowsApi.resources()} 创建。
 * 箱子详情走 GET，三个 graphql 端点走 POST（请求体来自模型类的 {@code requestBody(...)}）；PT 用 {@code WowsApi.pt()}。
 *
 * @author Xun
 */
public final class ResourcesClient {

    private final HttpPipeline pipeline;

    public ResourcesClient(HttpPipeline pipeline) {
        this.pipeline = Objects.requireNonNull(pipeline, "pipeline");
    }

    /**
     * 箱子详情；默认语言：莱服固定 ru，其余服 zh-sg。
     */
    public WowsLootBox lootbox(long lootboxId) throws BasicException {
        return lootbox(lootboxId, VortexResourcesLanguage.SG);
    }

    /**
     * 箱子详情；莱服语言固定为 {@code ru}（传入不生效，旧行为）。
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
     * 箱子列表（graphql Lootbox，POST）。
     */
    public List<WowsBox> lootboxes(VortexResourcesLanguage language) throws BasicException {
        return pipeline.execute(ResourcesEndpoints.LOOTBOX_LIST,
                EndpointRequest.of(Map.of("language", language.getLanguage())));
    }

    /**
     * 资源列表（graphql Items，POST）。
     */
    public List<WowsResources> items(VortexResourcesLanguage language) throws BasicException {
        return pipeline.execute(ResourcesEndpoints.ITEMS,
                EndpointRequest.of(Map.of("language", language.getLanguage())));
    }

    /**
     * 资源索引/词典（graphql getGlossData，POST）：词典 + 船 + 成就一次拉取。
     */
    public RequestVortexResourcesInfo glossary(VortexResourcesLanguage language) throws BasicException {
        return pipeline.execute(ResourcesEndpoints.GLOSSARY,
                EndpointRequest.of(Map.of("language", language.getLanguage())));
    }
}
