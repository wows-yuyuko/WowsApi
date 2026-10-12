package com.shinoaki.wows;

import com.shinoaki.wows.api.WowsApi;
import com.shinoaki.wows.api.aggregate.AccountAssembler;
import com.shinoaki.wows.api.codec.http.WowsHttpClanTools;
import com.shinoaki.wows.api.codec.http.WowsHttpUserTools;
import com.shinoaki.wows.api.error.UnsupportedOnServerException;
import com.shinoaki.wows.api.type.WowsServer;
import com.shinoaki.wows.api.utils.JsonUtils;
import com.shinoaki.wows.testkit.TestConfig;

import java.net.http.HttpClient;
import java.util.List;

/**
 * 3.0 重写 Phase 1：account 域「新旧等价」在线烟测（打真实服务）。
 * <p>
 * 对 ASIA / RU（vortex + 官方开发者API）与 CN（vortex，带分享凭证）逐端点执行
 * <b>旧实现与新实现各一次</b>，把两侧结果序列化为 JSON 逐字节对比。
 * 任一对比不一致即判定失败；失败项数作为退出码。
 * <p>
 * 覆盖：vortex 玩家详情/搜索；dev 玩家详情/搜索、所属军团、军团详情、3 请求聚合；
 * CN 调 dev 端点的 UnsupportedOnServerException。
 * <p>
 * 令牌 / 账号 / 代理均由 {@link TestConfig} 提供（零输入可跑）。
 *
 * @author Xun
 */
public class RewriteAccountSmokeTest {

    private static int passed;
    private static int failed;

    public static void main(String[] args) throws Exception {
        HttpClient client = TestConfig.client();
        //大响应给足超时；共享传输配置，新旧两侧同时生效
        WowsApi.of(WowsServer.ASIA).timeout(20);

        for (WowsServer server : List.of(WowsServer.ASIA, WowsServer.RU)) {
            vortex(server, client);
            developers(server, client);
        }
        vortexCn(client);
        cnDevUnsupported();

        System.out.println();
        System.out.printf("── 汇总 ── 通过 %d，失败 %d%n", passed, failed);
        System.exit(failed == 0 ? 0 : 1);
    }

    private static void vortex(WowsServer server, HttpClient client) {
        String label = server.getCode();
        Long accountId = TestConfig.accountIdOrNull(server);
        if (accountId == null) {
            return;
        }
        System.out.println();
        System.out.println("== " + label + " vortex（玩家 " + accountId + "，搜索词 " + TestConfig.searchUser(server) + "） ==");
        WowsHttpUserTools old = new WowsHttpUserTools(client, server);
        WowsApi api = WowsApi.of(server);
        compare(label + " vortex.user（详情）",
                () -> JsonUtils.json().toJson(old.userVortex(accountId)),
                () -> JsonUtils.json().toJson(api.vortex().user(accountId)));
        compare(label + " vortex.search（搜索）",
                () -> JsonUtils.json().toJson(old.searchUserVortex(TestConfig.searchUser(server))),
                () -> JsonUtils.json().toJson(api.vortex().search(TestConfig.searchUser(server))));
    }

    private static void developers(WowsServer server, HttpClient client) {
        String label = server.getCode();
        Long accountId = TestConfig.accountIdOrNull(server);
        Long clanId = TestConfig.clanIdOrNull(server);
        if (accountId == null) {
            return;
        }
        String appId = TestConfig.appId(server);
        System.out.println();
        System.out.println("== " + label + " 官方开发者API（玩家 " + accountId + "，军团 " + clanId + "） ==");
        WowsHttpUserTools old = new WowsHttpUserTools(client, server);
        WowsHttpClanTools oldClan = new WowsHttpClanTools(client, server);
        WowsApi api = WowsApi.of(server).credentials(c -> c.applicationId(appId));

        compare(label + " dev.user（详情）",
                () -> JsonUtils.json().toJson(old.userInfoDevelopers(appId, accountId)),
                () -> JsonUtils.json().toJson(api.developers().user(accountId)));
        compare(label + " dev.search（搜索）",
                () -> JsonUtils.json().toJson(old.searchUserDevelopers(appId, TestConfig.searchUser(server))),
                () -> JsonUtils.json().toJson(api.developers().search(TestConfig.searchUser(server))));
        compare(label + " dev.accountClan（所属军团）",
                () -> JsonUtils.json().toJson(oldClan.developers(appId).userSearchClanDevelopers(accountId)),
                () -> JsonUtils.json().toJson(api.developers().accountClan(accountId)));
        if (clanId != null) {
            compare(label + " dev.clanInfo（军团详情）",
                    () -> JsonUtils.json().toJson(oldClan.developers(appId).clanInfoDevelopers(clanId)),
                    () -> JsonUtils.json().toJson(api.developers().clanInfo(clanId)));
        }
        compare(label + " aggregate.accountInfo（3 请求聚合）",
                () -> JsonUtils.json().toJson(old.accountInfoDevelopers(appId, accountId)),
                () -> JsonUtils.json().toJson(AccountAssembler.of(api).info(accountId)));
    }

    private static void vortexCn(HttpClient client) {
        long accountId = TestConfig.ACCOUNT_CN;
        String token = TestConfig.vortexToken(accountId);
        System.out.println();
        System.out.println("== cn vortex（玩家 " + accountId + "，带分享凭证） ==");
        WowsApi api = WowsApi.of(WowsServer.CN).credentials(c -> c.shareToken(accountId, token));
        compare("cn vortex.user（详情+token）",
                () -> JsonUtils.json().toJson(new WowsHttpUserTools(client, WowsServer.CN).userVortex(accountId, token)),
                () -> JsonUtils.json().toJson(api.vortex().user(accountId)));
    }

    private static void cnDevUnsupported() {
        System.out.println();
        System.out.println("== cn 官方开发者API（应显式拒绝） ==");
        try {
            WowsApi.of(WowsServer.CN).developers().search("x");
            failed++;
            System.out.println("  [FAIL] cn dev 应抛 UnsupportedOnServerException（没有抛异常）");
        } catch (UnsupportedOnServerException e) {
            passed++;
            System.out.println("  [ok] cn dev 抛 UnsupportedOnServerException：" + e.getMessage());
        } catch (Exception e) {
            failed++;
            System.out.println("  [FAIL] cn dev 异常类型不符：" + e);
        }
    }

    private static void compare(String name, ThrowingSupplier<String> oldCall, ThrowingSupplier<String> newCall) {
        String oldValue;
        try {
            oldValue = oldCall.get();
        } catch (Exception e) {
            failed++;
            System.out.println("  [FAIL] " + name + "（旧实现异常: " + e + "）");
            return;
        }
        String newValue;
        try {
            newValue = newCall.get();
        } catch (Exception e) {
            failed++;
            System.out.println("  [FAIL] " + name + "（新实现异常: " + e + "）");
            return;
        }
        if (oldValue.equals(newValue)) {
            passed++;
            System.out.println("  [ok] " + name + "（" + oldValue.length() + " 字符一致）");
        } else {
            failed++;
            System.out.println("  [FAIL] " + name + "\n         旧(" + oldValue.length() + "): " + truncate(oldValue)
                    + "\n         新(" + newValue.length() + "): " + truncate(newValue));
        }
    }

    private static String truncate(String value) {
        return value.length() <= 400 ? value : value.substring(0, 400) + "...";
    }

    @FunctionalInterface
    private interface ThrowingSupplier<T> {
        T get() throws Exception;
    }
}
