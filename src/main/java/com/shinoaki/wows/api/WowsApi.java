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
 * WowsApi 入口（3.0 重写）：实例即服务器边界，凭证表随实例走（跨服串用结构上不可能）。
 * <p>
 * <b>视图按通道分区</b>，1 调用 = 1 请求；多请求编排显式走 aggregate 层。用法：
 * <pre>{@code
 * WowsApi api = WowsApi.of(WowsServer.RU)
 *         .credentials(c -> c
 *                 .applicationId("...")          // dev · 服务器级
 *                 .accessToken(accountId, "...") // dev · 账号级（可选）
 *                 .shareToken(accountId, "..."));// vortex · 账号级（可选）
 *
 * api.vortex().user(accountId);                 // 1 请求
 * api.developers().user(accountId);             // 1 请求
 * AccountAssembler.of(api).info(accountId);     // 3 请求（显式编排）
 * }</pre>
 * 不可变性：{@link #credentials(Consumer)} 返回<b>新实例</b>（原实例不受影响）；
 * {@link #timeout(int)} / {@link #proxy(ProxySelector)} 作用于进程级的共享传输配置
 * （见 {@link HttpClientProvider} 的说明），对全部实例生效。
 * <p>
 * 同步 API：需要异步时由调用方用线程池或 JDK21 虚拟线程包装。
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
     * 按服务器创建（内置服务器档案）。
     */
    public static WowsApi of(WowsServer server) {
        Objects.requireNonNull(server, "server");
        return new WowsApi(ServerProfile.of(server), CredentialStore.EMPTY);
    }

    /**
     * 按自定义服务器档案创建（测试假上游 / 自建镜像等高级用法；常规使用 {@link #of(WowsServer)}）。
     */
    public static WowsApi of(ServerProfile profile) {
        Objects.requireNonNull(profile, "profile");
        return new WowsApi(profile, CredentialStore.EMPTY);
    }

    /**
     * 配置凭证（不可变：返回新实例，原实例不受影响；重复配置同 kind + 同账号 = 覆盖）。
     */
    public WowsApi credentials(Consumer<CredentialsBuilder> config) {
        CredentialsBuilder builder = CredentialsBuilder.from(credentials);
        config.accept(builder);
        return new WowsApi(profile, builder.build());
    }

    /**
     * 设置请求超时（秒，进程级共享——影响共享 HttpClient 与后续全部请求）。
     */
    public WowsApi timeout(int seconds) {
        HttpClientProvider.updateTimeout(seconds);
        return this;
    }

    /**
     * 设置全局代理选择器（进程级共享；null = 直连）。变更后共享客户端在下次取用时自动重建。
     */
    public WowsApi proxy(ProxySelector selector) {
        HttpClientProvider.updateProxySelector(selector);
        return this;
    }

    /**
     * 测试服（PT）变体：vortex 主机替换为 PT 主机（其余服务器事实不变），返回新实例。
     * <p>
     * 与旧 {@code isPtServer} 参数语义一致——仅影响走 vortex 主机的端点（箱子/资源类）；clans / dev 不变。
     */
    public WowsApi pt() {
        return new WowsApi(profile.withPtServer(), credentials);
    }

    public WowsServer server() {
        return profile.server();
    }

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
     * 资源/箱子视图（箱子详情/列表、资源列表、资源索引；1 调用 = 1 请求）。
     */
    public ResourcesClient resources() {
        return resources;
    }
}
