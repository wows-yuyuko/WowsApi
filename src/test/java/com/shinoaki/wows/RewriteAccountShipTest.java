package com.shinoaki.wows;

import com.shinoaki.wows.api.WowsApi;
import com.shinoaki.wows.api.auth.AccessToken;
import com.shinoaki.wows.api.codec.http.WowsHttpShipTools;
import com.shinoaki.wows.api.data.ship.ShipExpansion;
import com.shinoaki.wows.api.developers.DevelopersUserShip;
import com.shinoaki.wows.api.developers.DevelopersUserShipOper;
import com.shinoaki.wows.api.endpoint.DevelopersEndpoints;
import com.shinoaki.wows.api.error.UnsupportedOnServerException;
import com.shinoaki.wows.api.monitor.SchemaRecorder;
import com.shinoaki.wows.api.server.ServerProfile;
import com.shinoaki.wows.api.type.WowsBattlesType;
import com.shinoaki.wows.api.type.WowsServer;
import com.shinoaki.wows.api.utils.JsonUtils;
import com.shinoaki.wows.api.vortex.VortexUserShip;
import com.shinoaki.wows.testkit.FakeUpstream;
import com.shinoaki.wows.testkit.RecordedRequest;
import com.shinoaki.wows.testkit.TestConfig;
import com.shinoaki.wows.testkit.TestReport;

import java.net.http.HttpClient;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * 3.0 重写「账号战舰」域新旧等价测试：vortex 战舰战绩列表、官方API 战舰统计/全量战绩/徽章、
 * 战斗类型支持面与 in_garage 语义、监控键。
 * <p>
 * 默认跑<b>离线段</b>（假上游回放 {@code target/schema-raw}，不打真实服务）；
 * 传 {@code --online} 追加<b>在线段</b>（真实服务新旧对比。本机直连 WG 主机不稳定，
 * 建议带 {@code -Dwows.proxy=127.0.0.1:7890}）。
 * <p>
 * 用法：{@code java -cp ... com.shinoaki.wows.RewriteAccountShipTest [--online]}；退出码 = 失败项数。
 *
 * @author Xun
 */
public class RewriteAccountShipTest {

    private static final Path RAW = Path.of("target/schema-raw");
    private static final long ACCOUNT_ASIA = 2022515210L;
    private static final long ACCOUNT_RU = 212112137L;
    private static final String APP = "TESTAPP";
    private static final String TOKEN = "TESTTOKEN";

    public static void main(String[] args) throws Exception {
        boolean online = flag(args, "--online");
        TestReport report = new TestReport("账号战舰");
        System.out.println("── 账号战舰 · 3.0 新旧等价测试 ──");
        System.out.println("模式: " + (online ? "离线（假上游）+ 在线（真实服务）" : "离线（假上游；--online 追加在线新旧对比）"));

        offline(report);
        if (online) {
            online(report);
        } else {
            System.out.println();
            System.out.println("（在线段已跳过：加 --online 跑到真实服务的新旧对比）");
        }
        report.finish();
    }

    // ================= 离线：假上游 =================

    private static void offline(TestReport report) throws Exception {
        if (!Files.isDirectory(RAW)) {
            System.out.println();
            System.out.println("!! 缺少原始响应仓库 " + RAW + "（先跑一次 SchemaMonitorTest 采集）——离线段跳过");
            return;
        }
        String asiaShipsPvp = read("vortex_asia_api_accounts_id_ships_pvp_.json");
        String ruShipsRating = read("vortex_ru_api_accounts_id_ships_rating_solo_.json");
        String asiaStats = read("api_asia_wows_ships_stats_account_id_id_extra_pvp_div2_pvp_div3__4c0b4c30.json");
        String ruOper = read("api_ru_mk_ships_stats_account_id_id_extra_clan_oper_div_oper_div_2783b05d.json");
        String asiaBadges = read("api_asia_wows_ships_badges_account_id_id_.json");

        try (FakeUpstream upstream = FakeUpstream.start()) {
            upstream.vortex().exact("/api/accounts/" + ACCOUNT_ASIA + "/ships/pvp/", asiaShipsPvp);
            upstream.vortex().exact("/api/accounts/" + ACCOUNT_RU + "/ships/rating_solo/", ruShipsRating);

            upstream.api().perExtra("/wows/ships/stats/", DevelopersEndpoints.statsExtra(), asiaStats);
            upstream.api().perExtra("/mk/ships/stats/", WowsBattlesType.DEVELOPERS_ALL_STATS_EXTRA, ruOper);
            upstream.api().exact("/wows/ships/badges/", asiaBadges);

            checks(report, upstream, asiaShipsPvp, asiaStats, asiaBadges);
        }
    }

    private static void checks(TestReport report, FakeUpstream upstream, String asiaShipsPvp, String asiaStats, String asiaBadges) throws Exception {
        var log = upstream.requests();
        ServerProfile asia = ServerProfile.of(WowsServer.ASIA).withHosts(upstream.vortexBase(), upstream.clansBase(), upstream.apiBase());
        ServerProfile ru = ServerProfile.of(WowsServer.RU).withHosts(upstream.vortexBase(), upstream.clansBase(), upstream.apiBase());
        Function<Object, String> toJson = JsonUtils.json()::toJson;
        var json = JsonUtils.json();

        // ================= A. vortex 战舰战绩 =================
        report.section("A. vortex 战舰战绩");
        {
            WowsApi api = WowsApi.of(asia);
            VortexUserShip actual = api.vortex().ships(ACCOUNT_ASIA, WowsBattlesType.PVP);
            report.expect("A1 vortex 路径", "/api/accounts/" + ACCOUNT_ASIA + "/ships/pvp/", log.last().rawPath());
            report.expectQuery("A1 无查询参数", Map.of(), log.last().queryMap());
            //recordTime 是解析时刻（System.currentTimeMillis），两次解析必然不同——对比时剔除
            report.expect("A1 结果=旧解析（忽略 recordTime）",
                    withoutRecordTime(VortexUserShip.parse(WowsBattlesType.PVP, json.parse(asiaShipsPvp))),
                    withoutRecordTime(actual));
        }
        {
            WowsApi api = WowsApi.of(ru).credentials(c -> c.shareToken(ACCOUNT_RU, TOKEN));
            api.vortex().ships(ACCOUNT_RU, WowsBattlesType.RATING_SOLO);
            report.expect("A2 莱服专属类型路径", "/api/accounts/" + ACCOUNT_RU + "/ships/rating_solo/", log.last().rawPath());
            report.expectQuery("A2 share token 注入", Map.of("ac", TOKEN), log.last().queryMap());
        }
        {
            WowsApi api = WowsApi.of(asia);
            log.clear();
            report.expectThrows("A3 非支持服战斗类型拒绝", UnsupportedOnServerException.class,
                    () -> api.vortex().ships(ACCOUNT_ASIA, WowsBattlesType.BRAWL));
            report.expect("A3 零网络请求", "0", String.valueOf(log.size()));
        }

        // ================= B. dev 战舰统计 / 全量战绩 / 徽章 =================
        report.section("B. dev 战舰统计 / 全量战绩 / 徽章");
        {
            WowsApi api = WowsApi.of(asia).credentials(c -> c.applicationId(APP));
            DevelopersUserShip actual = api.developers().ships(ACCOUNT_ASIA);
            RecordedRequest req = log.last();
            report.expect("B1 dev 统计路径", "/wows/ships/stats/", req.rawPath());
            report.expectQuery("B1 查询参数（extra=历史集合、无 in_garage）",
                    Map.of("application_id", APP, "account_id", String.valueOf(ACCOUNT_ASIA), "extra", DevelopersEndpoints.statsExtra()),
                    req.queryMap());
            report.expect("B1 结果=旧解析",
                    toJson.apply(DevelopersUserShip.parse(json.parse(asiaStats))),
                    toJson.apply(actual));
        }
        {
            WowsApi api = WowsApi.of(asia).credentials(c -> c.applicationId(APP));
            api.developers().ships(ACCOUNT_ASIA, AccessToken.of(ACCOUNT_ASIA, "AT"));
            Map<String, String> query = log.last().queryMap();
            report.expect("B2 access_token 注入", "AT", query.get("access_token"));
            report.expect("B2 in_garage 跟随 access_token", "1", query.get("in_garage"));
        }
        {
            WowsApi api = WowsApi.of(ru).credentials(c -> c.applicationId(APP));
            DevelopersUserShipOper actual = api.developers().shipsOper(ACCOUNT_RU);
            report.expect("B3 RU 全量战绩前缀=/mk/", "/mk/ships/stats/", log.last().rawPath());
            report.expect("B3 extra=全量串", WowsBattlesType.DEVELOPERS_ALL_STATS_EXTRA, log.last().queryMap().get("extra"));
            report.expect("B3 结果非空（oper 块）", "true", String.valueOf(!actual.infoList().isEmpty()));
        }
        {
            WowsApi api = WowsApi.of(asia).credentials(c -> c.applicationId(APP));
            Map<Long, ShipExpansion> actual = api.developers().shipBadges(ACCOUNT_ASIA);
            report.expect("B4 徽章路径", "/wows/ships/badges/", log.last().rawPath());
            report.expect("B4 结果=旧解析",
                    toJson.apply(ShipExpansion.parseDevelopers(ACCOUNT_ASIA, json.parse(asiaBadges))),
                    toJson.apply(actual));
        }
        {
            WowsApi api = WowsApi.of(ru).credentials(c -> c.applicationId(APP));
            log.clear();
            report.expectThrows("B5 莱服徽章显式拒绝", UnsupportedOnServerException.class,
                    () -> api.developers().shipBadges(ACCOUNT_RU));
            report.expect("B5 零网络请求", "0", String.valueOf(log.size()));
        }

        // ================= C. 监控挂钩 =================
        report.section("C. 字段漂移监控挂钩");
        {
            WowsApi api = WowsApi.of(asia);
            SchemaRecorder.enable();
            try {
                api.vortex().ships(ACCOUNT_ASIA, WowsBattlesType.PVP);
            } finally {
                List<SchemaRecorder.Record> records = SchemaRecorder.drain();
                SchemaRecorder.disable();
                report.expect("C1 监控键=vortex.ship.list", "vortex.ship.list",
                        records.isEmpty() ? "(空)" : records.getFirst().schemaKey());
            }
        }
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

    // ================= 辅助 =================

    private static String withoutRecordTime(Object value) {
        tools.jackson.databind.node.ObjectNode node =
                (tools.jackson.databind.node.ObjectNode) JsonUtils.json().parse(JsonUtils.json().toJson(value));
        node.remove("recordTime");
        return JsonUtils.json().toJson(node);
    }

    private static String read(String name) throws java.io.IOException {
        return Files.readString(RAW.resolve(name));
    }

    private static boolean flag(String[] args, String name) {
        for (String arg : args) {
            if (arg.equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }
}
