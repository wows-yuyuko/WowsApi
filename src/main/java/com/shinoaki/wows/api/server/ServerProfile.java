package com.shinoaki.wows.api.server;

import com.shinoaki.wows.api.transport.Channel;
import com.shinoaki.wows.api.type.WowsServer;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/**
 * 服务器档案：主机 + dev 前缀（按域）+ extra（按服）+ 排行 realm + CN dev 拒绝的数据化「服务器事实表」。
 * 测试 / 自建镜像用 {@link #withHosts(String, String, String)} 替换主机；端点侧零分支。
 *
 * @param server          服务器
 * @param vortexHost      vortex 主机（https://vortex.*）
 * @param vortexPtHost    测试服（PT）vortex 主机
 * @param clansHost       clans 主机（https://clans.*）
 * @param apiHost         官方开发者API 主机（https://api.*）
 * @param devApiSupported 是否提供官方开发者API（CN=false）
 * @param devPrefixes     dev 路径前缀（按域，莱服 /mk/ 只覆盖 account/ships/glossary）
 * @param accountInfoExtra account/info 的 extra 串（莱服无 statistics.clan）
 * @param ladderRealm     军团排行 realm（CN/RU=global、ASIA=sg、EU=eu、NA=us）
 * @author Xun
 */
public record ServerProfile(
        WowsServer server,
        String vortexHost,
        String vortexPtHost,
        String clansHost,
        String apiHost,
        boolean devApiSupported,
        Map<DevNamespace, String> devPrefixes,
        String accountInfoExtra,
        String ladderRealm
) {

    /**
     * WG 直营服（ASIA/EU/NA）的 account/info extra：含 statistics.clan。
     */
    private static final String ACCOUNT_INFO_EXTRA_WG =
            "private.grouped_contacts,private.port,statistics.clan,statistics.club,statistics.oper_div,statistics.oper_div_hard,"
                    + "statistics.oper_solo,statistics.pve,statistics.pve_div2,statistics.pve_div3,statistics.pve_solo,"
                    + "statistics.pvp_div2,statistics.pvp_div3,statistics.pvp_solo,statistics.rank_div2,statistics.rank_div3,statistics.rank_solo";

    /**
     * 莱服的 account/info extra：无 statistics.clan（该块不存在）。
     */
    private static final String ACCOUNT_INFO_EXTRA_RU =
            "private.grouped_contacts,private.port,statistics.club,statistics.oper_div,statistics.oper_div_hard,"
                    + "statistics.oper_solo,statistics.pve,statistics.pve_div2,statistics.pve_div3,statistics.pve_solo,"
                    + "statistics.pvp_div2,statistics.pvp_div3,statistics.pvp_solo,statistics.rank_div2,statistics.rank_div3,statistics.rank_solo";

    private static final Map<WowsServer, ServerProfile> PROFILES = buildProfiles();

    public ServerProfile {
        Objects.requireNonNull(server, "server");
        devPrefixes = Collections.unmodifiableMap(new EnumMap<>(devPrefixes));
    }

    /**
     * 内置服务器档案（每服单例）。
     */
    public static ServerProfile of(WowsServer server) {
        return PROFILES.get(server);
    }

    /**
     * 替换三个业务主机（测试 / 自建镜像）；其余事实不变。
     */
    public ServerProfile withHosts(String vortexHost, String clansHost, String apiHost) {
        return new ServerProfile(server, vortexHost, vortexPtHost, clansHost, apiHost, devApiSupported, devPrefixes, accountInfoExtra, ladderRealm);
    }

    /**
     * 测试服（PT）变体：仅 vortex 主机替换为 PT（clans / dev 不变）。
     */
    public ServerProfile withPtServer() {
        return new ServerProfile(server, vortexPtHost, vortexPtHost, clansHost, apiHost, devApiSupported, devPrefixes, accountInfoExtra, ladderRealm);
    }

    /**
     * 通道对应的主机地址（不含路径）。
     */
    public String host(Channel channel) {
        return switch (channel) {
            case VORTEX -> vortexHost;
            case CLANS -> clansHost;
            case DEV -> apiHost;
        };
    }

    /**
     * dev 端点路径前缀（含首尾斜杠），如 /wows/ 或 /mk/。
     */
    public String devPrefix(DevNamespace namespace) {
        String prefix = devPrefixes.get(namespace);
        if (prefix == null) {
            throw new IllegalStateException("服务器 " + server.getCode() + " 未定义 dev 命名空间前缀: " + namespace);
        }
        return prefix;
    }

    private static Map<WowsServer, ServerProfile> buildProfiles() {
        Map<WowsServer, ServerProfile> profiles = new EnumMap<>(WowsServer.class);
        for (WowsServer server : WowsServer.values()) {
            profiles.put(server, create(server));
        }
        return Collections.unmodifiableMap(profiles);
    }

    private static ServerProfile create(WowsServer server) {
        Map<DevNamespace, String> prefixes = new EnumMap<>(DevNamespace.class);
        for (DevNamespace namespace : DevNamespace.values()) {
            prefixes.put(namespace, "/wows/");
        }
        String extra = ACCOUNT_INFO_EXTRA_WG;
        if (server == WowsServer.RU) {
            // 莱服：account / ships / glossary 走 /mk/；clans 系列仍在 /wows/ 下（实测）
            prefixes.put(DevNamespace.ACCOUNT, "/mk/");
            prefixes.put(DevNamespace.SHIP, "/mk/");
            prefixes.put(DevNamespace.GLOSSARY, "/mk/");
            extra = ACCOUNT_INFO_EXTRA_RU;
        }
        return new ServerProfile(server, server.vortex(), server.ptVortexServer(), server.clans(), server.api(),
                server.isApi(), prefixes, extra, ladderRealm(server));
    }

    /**
     * 军团排行 realm：CN/RU=global、ASIA=sg、EU=eu、NA=us。
     */
    private static String ladderRealm(WowsServer server) {
        return switch (server) {
            case CN, RU -> "global";
            case ASIA -> "sg";
            case EU -> "eu";
            case NA -> "us";
        };
    }
}
