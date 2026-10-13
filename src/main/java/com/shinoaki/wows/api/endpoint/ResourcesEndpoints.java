package com.shinoaki.wows.api.endpoint;

import com.shinoaki.wows.api.transport.Channel;
import com.shinoaki.wows.api.vortex.resources.RequestVortexResourcesInfo;
import com.shinoaki.wows.api.vortex.resources.WowsResources;
import com.shinoaki.wows.api.vortex.resources.box.WowsBox;

import java.util.List;

/**
 * 资源/箱子端点表（3.0 重写）——vortex 主机上的 graphql 接口（POST）。
 * <p>
 * 三个端点共用 {@code /api/graphql/glossary/}，以 endpoint id 区分 schema 与监控基线
 * （不再需要手写 {@code SchemaRecorder.key}）。
 *
 * @author Xun
 */
public final class ResourcesEndpoints {

    private ResourcesEndpoints() {
    }

    /**
     * 箱子列表（Lootbox schema）。语言经请求体变量传入。
     */
    public static final Endpoint<List<WowsBox>> LOOTBOX_LIST = Endpoint.of(
                    "vortex.lootbox.list",
                    Channel.VORTEX,
                    "/api/graphql/glossary/",
                    List.of(),
                    (node, request, profile) -> WowsBox.parse(node))
            .post(params -> WowsBox.requestBody(params.get("language")));

    /**
     * 资源列表（Items schema）。语言经请求体变量传入。
     */
    public static final Endpoint<List<WowsResources>> ITEMS = Endpoint.of(
                    "vortex.resources.items",
                    Channel.VORTEX,
                    "/api/graphql/glossary/",
                    List.of(),
                    (node, request, profile) -> WowsResources.parse(node))
            .post(params -> WowsResources.requestBody(params.get("language")));

    /**
     * 资源索引/词典（getGlossData schema）：词典 + 船 + 成就一次拉取；语言同时进入请求体与解析（vehicles.language）。
     */
    public static final Endpoint<RequestVortexResourcesInfo> GLOSSARY = Endpoint.of(
                    "vortex.resources.glossary",
                    Channel.VORTEX,
                    "/api/graphql/glossary/",
                    List.of(),
                    (node, request, profile) -> RequestVortexResourcesInfo.parse(node, request.param("language")))
            .post(params -> RequestVortexResourcesInfo.requestBody(params.get("language")));
}
