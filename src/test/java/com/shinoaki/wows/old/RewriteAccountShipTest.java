package com.shinoaki.wows.old;

import com.shinoaki.wows.api.WowsApi;
import com.shinoaki.wows.api.codec.http.WowsHttpShipTools;
import com.shinoaki.wows.api.error.UnsupportedOnServerException;
import com.shinoaki.wows.api.type.WowsBattlesType;
import com.shinoaki.wows.api.type.WowsServer;
import com.shinoaki.wows.api.utils.JsonUtils;
import com.shinoaki.wows.testkit.TestConfig;
import com.shinoaki.wows.testkit.TestReport;

import java.net.http.HttpClient;
import java.util.List;

/**
 * 「账号战舰」新旧等价测试：真实服务上跑新旧对比（vortex 战绩列表、dev 统计/全量战绩/徽章），
 * 另有两条不联网的支持面拒绝断言。
 * <p>
 * 用法：{@code java -cp ... com.shinoaki.wows.old.RewriteAccountShipTest}（建议带 {@code -Dwows.proxy=127.0.0.1:7890}）；
 * 退出码 = 失败项数。
 *
 * @author Xun
 */
public class RewriteAccountShipTest {

    public static void main(String[] args) {
        TestReport report = new TestReport("账号战舰");
        System.out.println("── 账号战舰 · 新旧等价测试（在线） ──");
        local(report);
        online(report);
        report.finish();
    }

    // ================= 本地（无网络）：支持面拒绝 =================

    private static void local(TestReport report) {
        report.section("本地 · 支持面拒绝（无网络）");
        report.expectThrows("非支持服战斗类型拒绝", UnsupportedOnServerException.class,
                () -> WowsApi.of(WowsServer.ASIA).vortex().ships(1L, WowsBattlesType.BRAWL));
        report.expectThrows("莱服战舰徽章显式拒绝", UnsupportedOnServerException.class,
                () -> WowsApi.of(WowsServer.RU).developers().shipBadges(1L));
    }

    // ================= 在线：真实服务新旧对比 =================

    private static void online(TestReport report) {
        HttpClient client = TestConfig.client();
        WowsApi.of(WowsServer.ASIA).timeout(20);
        for (WowsServer server : List.of(WowsServer.ASIA, WowsServer.RU)) {
            vortex(report, server, client);
            developers(report, server, client);
        }
        vortexCn(report, client);
    }

    private static void vortex(TestReport report, WowsServer server, HttpClient client) {
        String label = server.getCode();
        Long accountId = TestConfig.accountIdOrNull(server);
        if (accountId == null) {
            return;
        }
        report.section("在线 · " + label + " vortex 战舰（玩家 " + accountId + "）");
        WowsHttpShipTools old = new WowsHttpShipTools(client, server, accountId);
        WowsApi api = WowsApi.of(server);
        report.compare(label + " vortex.ships（pvp）",
                () -> JsonUtils.json().toJson(old.vortex().shipList(WowsBattlesType.PVP)),
                () -> JsonUtils.json().toJson(api.vortex().ships(accountId, WowsBattlesType.PVP)));
        if (server == WowsServer.RU) {
            report.compare(label + " vortex.ships（rating_solo）",
                    () -> JsonUtils.json().toJson(old.vortex().shipList(WowsBattlesType.RATING_SOLO)),
                    () -> JsonUtils.json().toJson(api.vortex().ships(accountId, WowsBattlesType.RATING_SOLO)));
        }
    }

    private static void vortexCn(TestReport report, HttpClient client) {
        long accountId = TestConfig.ACCOUNT_CN;
        String token = TestConfig.vortexToken(accountId);
        report.section("在线 · cn vortex 战舰（玩家 " + accountId + "，带分享凭证——无凭证时服务端按隐藏战绩处理）");
        WowsHttpShipTools old = new WowsHttpShipTools(client, WowsServer.CN, accountId);
        WowsApi api = WowsApi.of(WowsServer.CN).credentials(c -> c.shareToken(accountId, token));
        report.compare("cn vortex.ships（pvp）",
                () -> JsonUtils.json().toJson(old.vortex().shipList(WowsBattlesType.PVP, token)),
                () -> JsonUtils.json().toJson(api.vortex().ships(accountId, WowsBattlesType.PVP)));
    }

    private static void developers(TestReport report, WowsServer server, HttpClient client) {
        String label = server.getCode();
        Long accountId = TestConfig.accountIdOrNull(server);
        if (accountId == null) {
            return;
        }
        String appId = TestConfig.appId(server);
        report.section("在线 · " + label + " dev 战舰（玩家 " + accountId + "）");
        WowsHttpShipTools old = new WowsHttpShipTools(client, server, accountId);
        WowsApi api = WowsApi.of(server).credentials(c -> c.applicationId(appId));
        report.compare(label + " dev.ships（统计）",
                () -> JsonUtils.json().toJson(old.developers(appId).shipList()),
                () -> JsonUtils.json().toJson(api.developers().ships(accountId)));
        report.compare(label + " dev.shipsOper（全量战绩）",
                () -> JsonUtils.json().toJson(old.developers(appId).shipOperList()),
                () -> JsonUtils.json().toJson(api.developers().shipsOper(accountId)));
        if (server == WowsServer.ASIA) {
            report.compare(label + " dev.shipBadges（徽章）",
                    () -> JsonUtils.json().toJson(old.developers(appId).shipBadges()),
                    () -> JsonUtils.json().toJson(api.developers().shipBadges(accountId)));
        }
    }
}
