package com.shinoaki.wows.old;

import com.shinoaki.wows.api.WowsApi;
import com.shinoaki.wows.api.aggregate.AccountAssembler;
import com.shinoaki.wows.api.codec.http.WowsHttpUserTools;
import com.shinoaki.wows.api.error.MissingCredentialException;
import com.shinoaki.wows.api.error.UnsupportedOnServerException;
import com.shinoaki.wows.api.monitor.SchemaRecorder;
import com.shinoaki.wows.api.type.WowsServer;
import com.shinoaki.wows.api.utils.JsonUtils;
import com.shinoaki.wows.testkit.TestConfig;
import com.shinoaki.wows.testkit.TestReport;

import java.net.http.HttpClient;
import java.util.List;

/**
 * 「账号信息」新旧等价测试：真实服务上跑新旧对比（vortex 详情/搜索、dev 详情/搜索、聚合、CN 带凭证），
 * 另有两条不联网的 fast-fail 语义断言与一条监控键断言。
 * <p>
 * 用法：{@code java -cp ... com.shinoaki.wows.old.RewriteAccountInfoTest}（建议带 {@code -Dwows.proxy=127.0.0.1:7890}）；
 * 退出码 = 失败项数。
 *
 * @author Xun
 */
public class RewriteAccountInfoTest {

    public static void main(String[] args) throws Exception {
        TestReport report = new TestReport("账号信息");
        System.out.println("── 账号信息 · 新旧等价测试（在线） ──");
        local(report);
        online(report);
        report.finish();
    }

    // ================= 本地（无网络）：fast-fail 语义 =================

    private static void local(TestReport report) {
        report.section("本地 · 发送前 fast-fail（无网络）");
        Throwable missing = report.expectThrows("缺 appId 抛 MissingCredentialException", MissingCredentialException.class,
                () -> WowsApi.of(WowsServer.ASIA).developers().user(1L));
        if (missing instanceof MissingCredentialException e) {
            report.expect("缺失端点 id", "dev.account.info", e.getEndpointId());
        }
        report.expectThrows("CN 调 dev 抛 UnsupportedOnServerException", UnsupportedOnServerException.class,
                () -> WowsApi.of(WowsServer.CN).developers().search("x"));
    }

    // ================= 在线：真实服务新旧对比 =================

    private static void online(TestReport report) throws Exception {
        HttpClient client = TestConfig.client();
        WowsApi.of(WowsServer.ASIA).timeout(20);
        for (WowsServer server : List.of(WowsServer.ASIA, WowsServer.RU)) {
            vortex(report, server, client);
            developers(report, server, client);
        }
        vortexCn(report, client);
        monitorKey(report);
    }

    private static void vortex(TestReport report, WowsServer server, HttpClient client) {
        String label = server.getCode();
        Long accountId = TestConfig.accountIdOrNull(server);
        if (accountId == null) {
            return;
        }
        report.section("在线 · " + label + " vortex（玩家 " + accountId + "）");
        WowsHttpUserTools old = new WowsHttpUserTools(client, server);
        WowsApi api = WowsApi.of(server);
        report.compare(label + " vortex.user（详情）",
                () -> JsonUtils.json().toJson(old.userVortex(accountId)),
                () -> JsonUtils.json().toJson(api.vortex().user(accountId)));
        report.compare(label + " vortex.search（搜索）",
                () -> JsonUtils.json().toJson(old.searchUserVortex(TestConfig.searchUser(server))),
                () -> JsonUtils.json().toJson(api.vortex().search(TestConfig.searchUser(server))));
    }

    private static void developers(TestReport report, WowsServer server, HttpClient client) {
        String label = server.getCode();
        Long accountId = TestConfig.accountIdOrNull(server);
        if (accountId == null) {
            return;
        }
        String appId = TestConfig.appId(server);
        report.section("在线 · " + label + " 官方开发者API（玩家 " + accountId + "）");
        WowsHttpUserTools old = new WowsHttpUserTools(client, server);
        WowsApi api = WowsApi.of(server).credentials(c -> c.applicationId(appId));
        report.compare(label + " dev.user（详情）",
                () -> JsonUtils.json().toJson(old.userInfoDevelopers(appId, accountId)),
                () -> JsonUtils.json().toJson(api.developers().user(accountId)));
        report.compare(label + " dev.search（搜索）",
                () -> JsonUtils.json().toJson(old.searchUserDevelopers(appId, TestConfig.searchUser(server))),
                () -> JsonUtils.json().toJson(api.developers().search(TestConfig.searchUser(server))));
        report.compare(label + " aggregate.accountInfo（3 请求聚合）",
                () -> JsonUtils.json().toJson(old.accountInfoDevelopers(appId, accountId)),
                () -> JsonUtils.json().toJson(AccountAssembler.of(api).info(accountId)));
    }

    private static void vortexCn(TestReport report, HttpClient client) {
        long accountId = TestConfig.ACCOUNT_CN;
        String token = TestConfig.vortexToken(accountId);
        report.section("在线 · cn vortex（玩家 " + accountId + "，带分享凭证）");
        WowsApi api = WowsApi.of(WowsServer.CN).credentials(c -> c.shareToken(accountId, token));
        report.compare("cn vortex.user（详情+token）",
                () -> JsonUtils.json().toJson(new WowsHttpUserTools(client, WowsServer.CN).userVortex(accountId, token)),
                () -> JsonUtils.json().toJson(api.vortex().user(accountId)));
    }

    /**
     * 监控键 = endpoint id（Phase 3 基线切换的契约）。
     */
    private static void monitorKey(TestReport report) throws Exception {
        Long accountId = TestConfig.accountIdOrNull(WowsServer.ASIA);
        if (accountId == null) {
            return;
        }
        report.section("在线 · 字段漂移监控挂钩");
        WowsApi api = WowsApi.of(WowsServer.ASIA);
        SchemaRecorder.enable();
        try {
            api.vortex().user(accountId);
        } finally {
            List<SchemaRecorder.Record> records = SchemaRecorder.drain();
            SchemaRecorder.disable();
            report.expect("监控键=vortex.account.info", "vortex.account.info",
                    records.isEmpty() ? "(空)" : records.getFirst().schemaKey());
        }
    }
}
