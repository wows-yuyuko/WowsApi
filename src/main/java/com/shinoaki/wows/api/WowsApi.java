package com.shinoaki.wows.api;

import com.shinoaki.wows.api.auth.CredentialStore;
import com.shinoaki.wows.api.auth.CredentialsBuilder;
import com.shinoaki.wows.api.client.ClansClient;
import com.shinoaki.wows.api.client.DevelopersClient;
import com.shinoaki.wows.api.client.ResourcesClient;
import com.shinoaki.wows.api.client.VortexClient;
import com.shinoaki.wows.api.server.ServerProfile;
import com.shinoaki.wows.api.transport.HttpClientProvider;
import com.shinoaki.wows.api.transport.HttpPipeline;
import com.shinoaki.wows.api.type.WowsServer;

import java.net.ProxySelector;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * WowsApi 入口：实例即服务器边界，凭证表随实例走。视图按通道分区，1 调用 = 1 请求；多请求编排走 aggregate 层。
 * <pre>{@code
 * WowsApi api = WowsApi.of(WowsServer.RU)
 *         .credentials(c -> c.applicationId("...").shareToken(accountId, "..."));
 * api.vortex().user(accountId);
 * AccountAssembler.of(api).info(accountId); // 3 请求（显式编排）
 * }</pre>
 * {@link #credentials(Consumer)} 返回新实例；{@link #timeout(int)}/{@link #proxy(ProxySelector)} 为进程级共享配置。同步 API。
 *
 * @author Xun
 */
public final class WowsApi {

    private final ServerProfile profile;
    private final CredentialStore credentials;
    private final VortexClient vortex;
    private final DevelopersClient developers;
    private final ClansClient clans;
    private final ResourcesClient resources;

    private WowsApi(ServerProfile profile, CredentialStore credentials) {
        this.profile = profile;
        this.credentials = credentials;
        HttpPipeline pipeline = new HttpPipeline(profile, credentials);
        this.vortex = new VortexClient(pipeline);
        this.developers = new DevelopersClient(pipeline);
        this.clans = new ClansClient(pipeline);
        this.resources = new ResourcesClient(pipeline);
    }

    /**
     * 按内置服务器档案创建。
     */
    public static WowsApi of(WowsServer server) {
        Objects.requireNonNull(server, "server");
        return new WowsApi(ServerProfile.of(server), CredentialStore.EMPTY);
    }

    /**
     * 自定义服务器档案（测试 / 自建镜像等高级用法；常规使用 {@link #of(WowsServer)}）。
     */
    public static WowsApi of(ServerProfile profile) {
        Objects.requireNonNull(profile, "profile");
        return new WowsApi(profile, CredentialStore.EMPTY);
    }

    /**
     * 配置凭证（返回新实例；同 kind 同账号 = 覆盖）。
     */
    public WowsApi credentials(Consumer<CredentialsBuilder> config) {
        CredentialsBuilder builder = CredentialsBuilder.from(credentials);
        config.accept(builder);
        return new WowsApi(profile, builder.build());
    }

    /**
     * 设置请求超时（秒，进程级共享）。
     */
    public WowsApi timeout(int seconds) {
        HttpClientProvider.updateTimeout(seconds);
        return this;
    }

    /**
     * 设置全局代理选择器（进程级共享；null = 直连）。
     */
    public WowsApi proxy(ProxySelector selector) {
        HttpClientProvider.updateProxySelector(selector);
        return this;
    }

    /**
     * 测试服（PT）变体：仅 vortex 主机切换，返回新实例（clans / dev 不变）。
     */
    public WowsApi pt() {
        return new WowsApi(profile.withPtServer(), credentials);
    }

    /**
     * 当前服务器。
     */
    public WowsServer server() {
        return profile.server();
    }

    /**
     * 当前服务器档案。
     */
    public ServerProfile profile() {
        return profile;
    }

    /**
     * vortex 通道视图（玩家、战舰列表等；1 调用 = 1 请求）。
     */
    public VortexClient vortex() {
        return vortex;
    }

    /**
     * 官方开发者API 视图（账号、战舰统计、军团、词典等；1 调用 = 1 请求）。
     */
    public DevelopersClient developers() {
        return developers;
    }

    /**
     * clans 军团视图（详情、成员、搜索、排行；1 调用 = 1 请求）。
     */
    public ClansClient clans() {
        return clans;
    }

    /**
     * 资源/箱子视图（箱子、资源列表、索引；1 调用 = 1 请求）。
     */
    public ResourcesClient resources() {
        return resources;
    }
}
