package com.shinoaki.wows.testkit;

import com.shinoaki.wows.api.codec.HttpCodec;
import com.shinoaki.wows.api.type.WowsServer;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 测试集中配置：账号 / 军团 / 搜索词 / 令牌（官方API + vortex）/ 按服务器代理。
 * <p>
 * <b>账号与军团</b>：各在线测试统一从这里取，不再散落在测试类里。
 * <p>
 * <b>令牌读取顺序</b>：环境变量 &gt; 系统属性（IDEA VM options） &gt; 内置默认值（与 SchemaMonitorTest 既有约定一致）。
 * <ul>
 *     <li>官方开发者API（直营）：{@code -Dwows.appId=} / {@code WOWS_APP_ID}</li>
 *     <li>官方开发者API（莱服）：{@code -Dwows.appIdRu=} / {@code WOWS_APP_ID_RU}（莱服只认自己的 key）</li>
 *     <li>vortex access token（按账号，私有分享凭证）：{@code -Dwows.vortexToken.<accountId>=} / {@code WOWS_VORTEX_TOKEN_<accountId>}</li>
 * </ul>
 * <b>代理（按服务器选中走 / 不走）</b>：本类加载时自动把规则挂到 {@link HttpCodec}，
 * 因此测试里一律用 {@code HttpCodec.client()} 即可生效（不要再自建 HttpClient）。
 * <ul>
 *     <li>{@code -Dwows.proxy=host:port}：全局默认（所有服务器）</li>
 *     <li>{@code -Dwows.proxy.<code>=host:port}：按服务器覆盖，{@code code}=cn/asia/eu/na/ru</li>
 *     <li>{@code -Dwows.proxy.<code>=direct}：该服务器强制直连（覆盖全局默认）</li>
 * </ul>
 * 例：{@code -Dwows.proxy.ru=127.0.0.1:7890} 让莱服走本地代理，其余直连。
 *
 * @author Xun
 */
public final class TestConfig {

    private TestConfig() {
    }

    // ------------------------------------------------------------ 账号与军团

    /** 亚服测试账号（vortex 与官方API 通用） */
    public static final long ACCOUNT_ASIA = 2022515210L;
    /** 国服私有测试账号（其专属 vortex token 见 {@link #VORTEX_TOKEN_CN}，两者必须成对使用） */
    public static final long ACCOUNT_CN = 7047921442L;
    /** 莱服测试账号（vortex + 官方 /mk/ API） */
    public static final long ACCOUNT_RU = 212112137L;
    /** 国服离线 dump 数据对应账号（target/cn-dump，勿改：与采集文件绑定） */
    public static final long ACCOUNT_CN_DUMP = 7060948811L;

    /** 亚服测试军团（官方API 有数据） */
    public static final long CLAN_ASIA = 2000016057L;
    /** 国服测试军团（vortex） */
    public static final long CLAN_CN = 7000009259L;
    /** 莱服测试军团（vortex + clans.korabli.su） */
    public static final long CLAN_RU = 451731L;
    /** 亚服军团成员接口用（SchemaMonitorTest） */
    public static final long CLAN_MEMBERS_ASIA = 2000025691L;

    /** 亚服测试箱子 ID */
    public static final long LOOTBOX_ASIA = 4288861104L;
    /** 莱服测试箱子 ID（莱服专属字段多） */
    public static final long LOOTBOX_RU = 4134720432L;

    /** 玩家搜索词（按服） */
    public static final String SEARCH_USER_ASIA = "JustOn";
    public static final String SEARCH_USER_CN = "西行寺";
    public static final String SEARCH_USER_RU = "yu_yu_ko";
    /** 军团搜索词（亚服） */
    public static final String SEARCH_CLAN = "YU_RI";

    /**
     * 未配置测试账号的服务器返回 {@code null}（如 EU/NA，冒烟测试会跳过）。
     */
    public static Long accountIdOrNull(WowsServer server) {
        return switch (server) {
            case ASIA -> ACCOUNT_ASIA;
            case CN -> ACCOUNT_CN;
            case RU -> ACCOUNT_RU;
            default -> null;
        };
    }

    public static Long clanIdOrNull(WowsServer server) {
        return switch (server) {
            case ASIA -> CLAN_ASIA;
            case CN -> CLAN_CN;
            case RU -> CLAN_RU;
            default -> null;
        };
    }

    public static String searchUser(WowsServer server) {
        return switch (server) {
            case CN -> SEARCH_USER_CN;
            case RU -> SEARCH_USER_RU;
            default -> SEARCH_USER_ASIA;
        };
    }

    // ------------------------------------------------------------ 令牌

    /**
     * {@link #ACCOUNT_CN} 专享的私有 vortex access token。
     * <p>
     * 私有分享凭证（{@code ?ac=} 参数）：只对该账号本身有效，换账号必须换 token，勿复用到其它账号。
     */
    public static final String VORTEX_TOKEN_CN = "xDeD0N4-iB8AbwxVfH0a-4sqoWQ";

    private static final String DEFAULT_APP_ID = "907d9c6bfc0d896a2c156e57194a97cf";
    private static final String DEFAULT_APP_ID_RU = "c984faa7dc529f4cb0139505d5e8043c";

    /**
     * 官方开发者API令牌；未配置返回 {@code null}（SchemaMonitorTest 等用它保持"没配就跳过"语义）。
     */
    public static String appIdOrNull(WowsServer server) {
        return server == WowsServer.RU
                ? first(System.getenv("WOWS_APP_ID_RU"), System.getProperty("wows.appIdRu"))
                : first(System.getenv("WOWS_APP_ID"), System.getProperty("wows.appId"));
    }

    /**
     * 官方开发者API令牌；未配置时回退到内置测试 key。
     */
    public static String appId(WowsServer server) {
        String value = appIdOrNull(server);
        if (value != null) {
            return value;
        }
        return server == WowsServer.RU ? DEFAULT_APP_ID_RU : DEFAULT_APP_ID;
    }

    /**
     * 指定账号的私有 vortex access token；未配置返回 {@code null}。
     * <p>
     * 覆盖键按账号走：环境变量 {@code WOWS_VORTEX_TOKEN_<accountId>} / 系统属性 {@code wows.vortexToken.<accountId>}。
     */
    public static String vortexTokenOrNull(long accountId) {
        return first(System.getenv("WOWS_VORTEX_TOKEN_" + accountId), System.getProperty("wows.vortexToken." + accountId));
    }

    /**
     * 指定账号的私有 vortex access token；未配置时回退到内置值（当前只有 {@link #ACCOUNT_CN}）。
     * <p>
     * token 与账号绑定、必须成对使用——调用处直接用目标账号的 {@code accountId} 查询即可，不会配错号。
     */
    public static String vortexToken(long accountId) {
        String value = vortexTokenOrNull(accountId);
        if (value != null) {
            return value;
        }
        return accountId == ACCOUNT_CN ? VORTEX_TOKEN_CN : null;
    }

    private static String first(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return null;
    }

    // ------------------------------------------------------------ 共享客户端

    /**
     * 共享 {@link HttpClient} 的测试入口（测试里一律用它，别再直接 {@code HttpCodec.client()}）。
     * <p>
     * 与直接调用 {@code HttpCodec.client()} 的区别：本方法会先确保 {@link TestConfig} 完成类初始化
     * （代理配置已挂到 {@link HttpCodec}），因此第一次取到的实例就是"已按服务器配好代理"的，
     * 避免"先取 client、后触发代理配置"导致代理不生效。
     */
    public static HttpClient client() {
        return HttpCodec.client();
    }

    // ------------------------------------------------------------ 代理（按服务器）

    static {
        //类加载时把代理规则挂到共享 HttpClient——测试里统一用 HttpCodec.client() 即自动生效
        Map<String, InetSocketAddress> rules = proxyRules();
        if (!rules.isEmpty()) {
            HttpCodec.updateProxySelector(new HostProxySelector(rules));
        }
    }

    /**
     * 解析 {@code wows.proxy*} 配置：host 后缀 -&gt; 代理地址。
     * <p>
     * 未配置任何代理时为空表（全部直连）；按服配了 direct/none 的不会进表（即直连）。
     */
    public static Map<String, InetSocketAddress> proxyRules() {
        String global = System.getProperty("wows.proxy");
        Map<String, InetSocketAddress> rules = new LinkedHashMap<>();
        for (WowsServer server : WowsServer.values()) {
            String value = System.getProperty("wows.proxy." + server.getCode());
            if (value == null || value.isBlank()) {
                value = global;
            }
            if (value == null || value.isBlank()) {
                continue;
            }
            value = value.trim();
            if (value.equalsIgnoreCase("direct") || value.equalsIgnoreCase("none")) {
                continue;
            }
            rules.put(domainOf(server), parseAddress(server, value));
        }
        return rules;
    }

    /**
     * 某服务器生效的代理描述：{@code direct} 或 {@code host:port}。
     */
    public static String proxyOf(WowsServer server) {
        InetSocketAddress address = proxyRules().get(domainOf(server));
        return address == null ? "direct" : address.getHostString() + ":" + address.getPort();
    }

    /**
     * 全服代理速览，如 {@code cn=direct；asia=direct；ru=127.0.0.1:7890}。
     */
    public static String proxySummary() {
        StringBuilder builder = new StringBuilder();
        for (WowsServer server : WowsServer.values()) {
            if (!builder.isEmpty()) {
                builder.append("；");
            }
            builder.append(server.getCode()).append('=').append(proxyOf(server));
        }
        return builder.toString();
    }

    /**
     * 服务器主域名（去掉 api. 前缀）；代理规则按 host 后缀匹配 vortex./clans. 等所有子域。
     */
    public static String domainOf(WowsServer server) {
        String host = URI.create(server.api()).getHost();
        int index = host.indexOf('.');
        return index < 0 ? host : host.substring(index + 1);
    }

    private static InetSocketAddress parseAddress(WowsServer server, String value) {
        int index = value.lastIndexOf(':');
        if (index <= 0 || index == value.length() - 1) {
            throw new IllegalArgumentException("测试代理配置不合法：wows.proxy." + server.getCode() + "=" + value + "（应为 host:port 或 direct）");
        }
        try {
            int port = Integer.parseInt(value.substring(index + 1).trim());
            return new InetSocketAddress(value.substring(0, index), port);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("测试代理配置不合法：wows.proxy." + server.getCode() + "=" + value + "（端口不是数字）");
        }
    }
}
