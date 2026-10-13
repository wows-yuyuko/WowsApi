package com.shinoaki.wows.v2;

import com.shinoaki.wows.api.codec.http.WowsHttpClanTools;
import com.shinoaki.wows.api.codec.http.WowsHttpShipTools;
import com.shinoaki.wows.api.codec.http.WowsHttpUserTools;
import com.shinoaki.wows.api.type.WowsServer;
import com.shinoaki.wows.testkit.TestConfig;

import java.net.http.HttpClient;
import java.util.concurrent.Callable;

/**
 * 多服务器 <b>官方开发者API</b> 请求冒烟（与 vortex 分开、各自独立）：按 {@link TestConfig} 对每个配置了账号
 * 且支持官方API 的服务器跑一遍——玩家详情 / 战舰统计 / 军团详情（莱服自动走 {@code /mk/} 路径，国服无官方API 自动跳过）。
 * <p>
 * 用法（仓库根目录）：
 * <pre>
 *   java -cp ... com.shinoaki.wows.v2.AllServersDevelopersSmokeTest          # 在线执行（会打真实接口）
 *   java -cp ... com.shinoaki.wows.v2.AllServersDevelopersSmokeTest --dry    # 只打印计划与代理路由，不发请求
 * </pre>
 * 令牌由 {@link TestConfig} 内置（公开测试 key，env/-D 可覆盖）；代理按服务器配置见 TestConfig。退出码：0 = 全部通过；1 = 有失败项。
 *
 * @author Xun
 */
public class AllServersDevelopersSmokeTest {

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
        if (!server.isApi()) {
            System.out.println("  [skip] 该服务器不支持官方API（无 api 域名）");
            return;
        }
        Long accountId = TestConfig.accountIdOrNull(server);
        if (accountId == null) {
            System.out.println("  [skip] 未配置测试账号（可在 TestConfig 里补）");
            return;
        }
        Long clanId = TestConfig.clanIdOrNull(server);
        String appId = TestConfig.appId(server);
        System.out.println("  账号=" + accountId + "，军团=" + (clanId == null ? "无" : clanId) + "，官方API令牌=已配置");

        if (dry) {
            System.out.println("  [plan] 玩家详情 / 战舰统计" + (clanId == null ? "" : " / 军团详情"));
            return;
        }

        WowsHttpUserTools user = new WowsHttpUserTools(client, server);
        WowsHttpShipTools ship = new WowsHttpShipTools(client, server, accountId);
        WowsHttpClanTools clan = new WowsHttpClanTools(client, server);

        check("玩家详情", () -> user.accountInfoDevelopers(appId, accountId));
        check("战舰统计", () -> ship.developers(appId).shipList());
        if (clanId != null) {
            check("军团详情", () -> clan.developers(appId).clanInfoDevelopers(clanId));
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
