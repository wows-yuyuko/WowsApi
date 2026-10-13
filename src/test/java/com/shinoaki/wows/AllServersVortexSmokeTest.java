package com.shinoaki.wows;

import com.shinoaki.wows.api.codec.http.WowsHttpClanTools;
import com.shinoaki.wows.api.codec.http.WowsHttpShipTools;
import com.shinoaki.wows.api.codec.http.WowsHttpUserTools;
import com.shinoaki.wows.api.type.WowsBattlesType;
import com.shinoaki.wows.api.type.WowsServer;
import com.shinoaki.wows.testkit.TestConfig;

import java.net.http.HttpClient;
import java.util.concurrent.Callable;

/**
 * 多服务器 <b>vortex</b> 请求冒烟（与官方开发者API 分开、各自独立）：按 {@link TestConfig} 对每个配置了账号的服务器
 * 跑一遍 vortex 核心接口面——玩家详情 / 玩家搜索 / 战舰列表 / 军团详情；配了私有 token 的账号（国服）走带 token 版本。
 * <p>
 * 用法（仓库根目录）：
 * <pre>
 *   java -cp ... com.shinoaki.wows.AllServersVortexSmokeTest          # 在线执行（会打真实接口）
 *   java -cp ... com.shinoaki.wows.AllServersVortexSmokeTest --dry    # 只打印计划与代理路由，不发请求
 * </pre>
 * 代理按服务器配置见 {@link TestConfig}；未配置测试账号的服务器（EU/NA）自动跳过。退出码：0 = 全部通过；1 = 有失败项。
 *
 * @author Xun
 */
public class AllServersVortexSmokeTest {

    private static int failed = 0;

    public static void main(String[] args) {
        boolean dry = false;
        for (String arg : args) {
            if ("--dry".equals(arg)) {
                dry = true;
            }
        }
        HttpClient client = TestConfig.client();
        System.out.println("代理配置  : " + TestConfig.proxySummary());
        System.out.println("共享客户端: " + client.proxy().map(Object::toString).orElse("直连（未挂 ProxySelector）"));
        System.out.println("模式      : " + (dry ? "dry（只打印计划，不发请求）" : "在线（会打真实接口）"));

        for (WowsServer server : WowsServer.values()) {
            run(client, server, dry);
        }

        System.out.println();
        System.out.println(failed == 0 ? "结果: 全部通过" : "结果: " + failed + " 项失败");
        System.exit(failed == 0 ? 0 : 1);
    }

    private static void run(HttpClient client, WowsServer server, boolean dry) {
        System.out.println();
        System.out.println("── " + server + "（" + server.getName() + "，代理=" + TestConfig.proxyOf(server) + "） ──");
        Long accountId = TestConfig.accountIdOrNull(server);
        if (accountId == null) {
            System.out.println("  [skip] 未配置测试账号（可在 TestConfig 里补）");
            return;
        }
        Long clanId = TestConfig.clanIdOrNull(server);
        String vortexToken = TestConfig.vortexToken(accountId);
        System.out.println("  账号=" + accountId + "，军团=" + (clanId == null ? "无" : clanId)
                + "，vortexToken=" + (vortexToken == null ? "无" : "已配置"));

        if (dry) {
            System.out.println("  [plan] 玩家详情" + (vortexToken == null ? "" : "（带token）"));
            System.out.println("  [plan] 玩家搜索(" + TestConfig.searchUser(server) + ")" + (server == WowsServer.CN ? " + 国服专用搜索" : ""));
            System.out.println("  [plan] 战舰列表 PVP" + (vortexToken == null ? "" : "（带token）"));
            if (clanId != null) {
                System.out.println("  [plan] 军团详情(" + clanId + ")");
            }
            return;
        }

        WowsHttpUserTools user = new WowsHttpUserTools(client, server);
        WowsHttpShipTools ship = new WowsHttpShipTools(client, server, accountId);
        WowsHttpClanTools clan = new WowsHttpClanTools(client, server);

        if (vortexToken == null) {
            check("玩家详情", () -> user.userVortex(accountId));
            check("战舰列表(PVP)", () -> ship.vortex().shipList(WowsBattlesType.PVP));
        } else {
            check("玩家详情(带token)", () -> user.userVortex(accountId, vortexToken));
            check("战舰列表(PVP, 带token)", () -> ship.vortex().shipList(WowsBattlesType.PVP, vortexToken));
        }
        check("玩家搜索(" + TestConfig.searchUser(server) + ")", () -> user.searchUserVortex(TestConfig.searchUser(server)));
        if (server == WowsServer.CN) {
            check("玩家搜索(国服专用)", () -> user.searchUserVortexCn(TestConfig.SEARCH_USER_CN));
        }
        if (clanId != null) {
            check("军团详情", () -> clan.vortex().clanInfoVortex(clanId));
        }
    }

    private static void check(String name, Callable<?> action) {
        try {
            action.call();
            System.out.println("  [ok] " + name);
        } catch (Exception e) {
            failed++;
            System.out.println("  [FAIL] " + name + " -> " + e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }
}
