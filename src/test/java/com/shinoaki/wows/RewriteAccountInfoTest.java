package com.shinoaki.wows;

import com.shinoaki.wows.api.WowsApi;
import com.shinoaki.wows.api.aggregate.AccountAssembler;
import com.shinoaki.wows.api.auth.AccessToken;
import com.shinoaki.wows.api.auth.ShareToken;
import com.shinoaki.wows.api.codec.HttpCodec;
import com.shinoaki.wows.api.codec.http.WowsHttpUserTools;
import com.shinoaki.wows.api.data.AccountClanInfo;
import com.shinoaki.wows.api.data.AccountInfo;
import com.shinoaki.wows.api.developers.account.DevelopersSearchUser;
import com.shinoaki.wows.api.developers.account.DevelopersUserInfo;
import com.shinoaki.wows.api.developers.clan.DevelopersClanInfo;
import com.shinoaki.wows.api.endpoint.RateLimitPolicy;
import com.shinoaki.wows.api.error.HttpStatusException;
import com.shinoaki.wows.api.error.MissingCredentialException;
import com.shinoaki.wows.api.error.UnsupportedOnServerException;
import com.shinoaki.wows.api.monitor.SchemaRecorder;
import com.shinoaki.wows.api.server.ServerProfile;
import com.shinoaki.wows.api.type.WowsServer;
import com.shinoaki.wows.api.utils.JsonUtils;
import com.shinoaki.wows.api.vortex.account.VortexSearchUser;
import com.shinoaki.wows.api.vortex.account.VortexUserInfo;
import com.shinoaki.wows.testkit.FakeUpstream;
import com.shinoaki.wows.testkit.RecordedRequest;
import com.shinoaki.wows.testkit.TestConfig;
import com.shinoaki.wows.testkit.TestReport;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * 3.0 重写「账号信息」域新旧等价测试：vortex 玩家详情/搜索、官方API 玩家详情/搜索、3 请求聚合、
 * 凭证/限流/失败分支与监控键。
 * <p>
 * 默认跑<b>离线段</b>（假上游回放 {@code target/schema-raw}，不打真实服务）；
 * 传 {@code --online} 追加<b>在线段</b>（真实服务新旧对比。本机直连 WG 主机不稳定，
 * 建议带 {@code -Dwows.proxy=127.0.0.1:7890}）。
 * <p>
 * 用法：{@code java -cp ... com.shinoaki.wows.RewriteAccountInfoTest [--online]}；退出码 = 失败项数。
 *
 * @author Xun
 */
public class RewriteAccountInfoTest {

    private static final Path RAW = Path.of("target/schema-raw");
    private static final long ACCOUNT_ASIA = 2022515210L;
    private static final long ACCOUNT_RU = 212112137L;
    private static final long CLAN_RU = 451731L;
    private static final String APP = "TESTAPP";
    private static final String TOKEN = "TESTTOKEN";

    public static void main(String[] args) throws Exception {
        boolean online = flag(args, "--online");
        TestReport report = new TestReport("账号信息");
        System.out.println("── 账号信息 · 3.0 新旧等价测试 ──");
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
        String vortexAsia = read("vortex_asia_api_accounts_id_.json");
        String vortexRu = read("vortex_ru_api_accounts_id_.json");
        String searchAsia = read("vortex_asia_api_accounts_search_autocomplete_text_.json");
        String searchRu = read("vortex_ru_api_accounts_search_autocomplete_text_.json");
        String devAsiaUser = read("api_asia_wows_account_info_account_id_id_extra_private_grouped_c_c5266c85.json");
        String devRuUser = read("api_ru_mk_account_info_account_id_id_extra_private_grouped_conta_9251f0d3.json");
        String devAsiaSearch = read("api_asia_wows_account_list_search_text_.json");
        String devRuSearch = read("api_ru_mk_account_list_search_text_.json");
        String devRuClanRef = read("api_ru_wows_clans_accountinfo_account_id_id_extra_clan.json");
        String devRuClanInfo = read("api_ru_wows_clans_info_clan_id_id_extra_members.json");

        try (FakeUpstream upstream = FakeUpstream.start()) {
            upstream.vortex().exact("/api/accounts/" + ACCOUNT_ASIA + "/", vortexAsia);
            upstream.vortex().exact("/api/accounts/" + ACCOUNT_RU + "/", vortexRu);
            upstream.vortex().exact("/api/accounts/search/autocomplete/JustOn/", searchAsia);
            upstream.vortex().exact("/api/accounts/search/autocomplete/yu_yu_ko/", searchRu);
            // 中文搜索词：编码后的路径（验证路径占位符按 encodeURIComponent 语义编码）
            upstream.vortex().exact("/api/accounts/search/autocomplete/" + HttpCodec.encodeURIComponent("西行寺") + "/", searchAsia);
            // 限流场景（搜索路径按搜索词区分）
            upstream.vortex().status("/api/accounts/search/autocomplete/Overload/", 503);
            upstream.vortex().status("/api/accounts/search/autocomplete/Busy/", 429);
            // gzip 解码场景
            upstream.vortex().gzip("/api/accounts/search/autocomplete/Gzipped/", searchAsia);

            upstream.api().exact("/wows/account/info/", devAsiaUser);
            upstream.api().exact("/mk/account/info/", devRuUser);
            upstream.api().exact("/wows/account/list/", devAsiaSearch);
            upstream.api().exact("/mk/account/list/", devRuSearch);
            // 聚合链第 2/3 步（所属军团 + 军团详情）
            upstream.api().perId("/wows/clans/accountinfo/", ACCOUNT_RU, devRuClanRef);
            upstream.api().perId("/wows/clans/info/", CLAN_RU, devRuClanInfo);

            checks(report, upstream, vortexAsia, searchAsia, devAsiaUser, devRuUser, devAsiaSearch,
                    devRuClanRef, devRuClanInfo);
        }
    }

    private static void checks(TestReport report, FakeUpstream upstream, String vortexAsia, String searchAsia,
                               String devAsiaUser, String devRuUser, String devAsiaSearch,
                               String devRuClanRef, String devRuClanInfo) throws Exception {
        var log = upstream.requests();
        ServerProfile asia = ServerProfile.of(WowsServer.ASIA).withHosts(upstream.vortexBase(), upstream.clansBase(), upstream.apiBase());
        ServerProfile ru = ServerProfile.of(WowsServer.RU).withHosts(upstream.vortexBase(), upstream.clansBase(), upstream.apiBase());
        ServerProfile cn = ServerProfile.of(WowsServer.CN).withHosts(upstream.vortexBase(), upstream.clansBase(), upstream.apiBase());
        Function<Object, String> toJson = JsonUtils.json()::toJson;
        String searchAsiaCount = String.valueOf(VortexSearchUser.parse(JsonUtils.json().parse(searchAsia)).size());

        // ================= A. vortex：URL/参数/认证注入 =================
        report.section("A. vortex 账号（URL / 参数 / 认证注入）");
        {
            WowsApi api = WowsApi.of(asia).credentials(c -> c.shareToken(ACCOUNT_ASIA, TOKEN));
            VortexUserInfo actual = api.vortex().user(ACCOUNT_ASIA);
            RecordedRequest req = log.last();
            report.expect("A1 方法=GET", "GET", req.method());
            report.expect("A1 路径", "/api/accounts/" + ACCOUNT_ASIA + "/", req.rawPath());
            report.expectQuery("A1 查询=ac 注入", Map.of("ac", TOKEN), req.queryMap());
            report.expect("A1 结果=旧解析", toJson.apply(VortexUserInfo.parse(JsonUtils.json().parse(vortexAsia), ACCOUNT_ASIA)), toJson.apply(actual));
            report.expect("A1 UA 头存在", "true", String.valueOf(req.headers().getFirst("User-Agent") != null && req.headers().getFirst("User-Agent").contains("Mozilla")));
            report.expect("A1 Accept-Encoding", "gzip, deflate", req.headers().getFirst("Accept-Encoding"));
        }
        {
            WowsApi api = WowsApi.of(asia);
            api.vortex().user(ACCOUNT_ASIA);
            report.expectQuery("A2 匿名请求不带 ac", Map.of(), log.last().queryMap());
        }
        {
            WowsApi api = WowsApi.of(asia);
            List<VortexSearchUser> result = api.vortex().search("西行寺");
            report.expect("A3 路径编码=旧实现语义", "/api/accounts/search/autocomplete/%E8%A5%BF%E8%A1%8C%E5%AF%BA/", log.last().rawPath());
            report.expect("A3 编码实测与 HttpCodec 一致", HttpCodec.encodeURIComponent("西行寺"), "%E8%A5%BF%E8%A1%8C%E5%AF%BA");
            report.expect("A3 中文搜索可正常解析", searchAsiaCount, String.valueOf(result.size()));
        }
        {
            WowsApi api = WowsApi.of(asia).credentials(c -> c.shareToken(ACCOUNT_ASIA, TOKEN));
            api.vortex().user(ACCOUNT_ASIA, ShareToken.of(ACCOUNT_ASIA, "OVERRIDE"));
            report.expectQuery("A4 覆盖凭证生效", Map.of("ac", "OVERRIDE"), log.last().queryMap());
        }
        {
            WowsApi api = WowsApi.of(asia).credentials(c -> c.shareToken(999, "WRONG"));
            api.vortex().user(ACCOUNT_ASIA);
            report.expectQuery("A5 凭证账号不匹配不注入", Map.of(), log.last().queryMap());
        }
        {
            WowsApi api = WowsApi.of(asia).credentials(c -> c.shareToken(ACCOUNT_ASIA, TOKEN));
            List<VortexSearchUser> result = api.vortex().search("JustOn");
            report.expectQuery("A6 搜索不带 ac", Map.of(), log.last().queryMap());
            report.expect("A6 搜索解析条目数", searchAsiaCount, String.valueOf(result.size()));
        }

        // ================= B. dev 账号（前缀/extra/认证） =================
        report.section("B. dev 账号（前缀 / extra / 认证）");
        {
            WowsApi api = WowsApi.of(asia).credentials(c -> c.applicationId(APP));
            DevelopersUserInfo actual = api.developers().user(ACCOUNT_ASIA);
            RecordedRequest req = log.last();
            report.expect("B1 ASIA 前缀", "/wows/account/info/", req.rawPath());
            report.expectQuery("B1 查询参数", Map.of("application_id", APP, "account_id", String.valueOf(ACCOUNT_ASIA),
                    "extra", asia.accountInfoExtra()), req.queryMap());
            report.expect("B1 extra 含 statistics.clan", "true", String.valueOf(asia.accountInfoExtra().contains("statistics.clan")));
            report.expect("B1 结果=旧解析", toJson.apply(DevelopersUserInfo.parse(ACCOUNT_ASIA, devAsiaUser)), toJson.apply(actual));
        }
        {
            WowsApi api = WowsApi.of(ru).credentials(c -> c.applicationId(APP));
            DevelopersUserInfo actual = api.developers().user(ACCOUNT_RU);
            RecordedRequest req = log.last();
            report.expect("B2 RU 前缀=/mk/", "/mk/account/info/", req.rawPath());
            report.expect("B2 extra 无 statistics.clan", "false", String.valueOf(ru.accountInfoExtra().contains("statistics.clan")));
            report.expect("B2 extra 与查询一致", ru.accountInfoExtra(), req.queryMap().get("extra"));
            report.expect("B2 结果=旧解析", toJson.apply(DevelopersUserInfo.parse(ACCOUNT_RU, devRuUser)), toJson.apply(actual));
        }
        {
            WowsApi api = WowsApi.of(asia).credentials(c -> c.applicationId(APP));
            api.developers().user(ACCOUNT_ASIA, AccessToken.of(ACCOUNT_ASIA, "AT"));
            report.expect("B3 access_token 注入", "AT", log.last().queryMap().get("access_token"));
        }
        {
            WowsApi api = WowsApi.of(asia).credentials(c -> c.applicationId(APP));
            List<DevelopersSearchUser> result = api.developers().search("JustOn");
            report.expect("B4 搜索路径", "/wows/account/list/", log.last().rawPath());
            report.expectQuery("B4 搜索参数", Map.of("application_id", APP, "search", "JustOn"), log.last().queryMap());
            report.expect("B4 结果=旧解析", toJson.apply(DevelopersSearchUser.parse(devAsiaSearch)), toJson.apply(result));
        }
        {
            WowsApi api = WowsApi.of(ru).credentials(c -> c.applicationId(APP));
            api.developers().search("yu_yu_ko");
            report.expect("B5 RU 搜索前缀=/mk/", "/mk/account/list/", log.last().rawPath());
        }

        // ================= C. 聚合：新旧逐字段等价（RU 三份数据自洽） =================
        report.section("C. 聚合（AccountAssembler 3 请求链）");
        {
            WowsApi api = WowsApi.of(ru).credentials(c -> c.applicationId(APP));
            log.clear();
            AccountInfo actual = AccountAssembler.of(api).info(ACCOUNT_RU);

            AccountClanInfo oldClan = AccountClanInfo.accountClan(ACCOUNT_RU, devRuClanRef);
            if (oldClan.clanId() > 0) {
                oldClan = AccountClanInfo.of(oldClan, DevelopersClanInfo.parse(oldClan.clanId(), devRuClanInfo));
            }
            AccountInfo expected = AccountInfo.parse(ACCOUNT_RU, devRuUser, oldClan);
            report.expect("C1 聚合结果=旧链", toJson.apply(expected), toJson.apply(actual));
            report.expect("C2 请求数=3", "3", String.valueOf(log.size()));
            report.expect("C2 请求顺序", "/mk/account/info/ -> /wows/clans/accountinfo/ -> /wows/clans/info/",
                    log.get(0).rawPath() + " -> " + log.get(1).rawPath() + " -> " + log.get(2).rawPath());
        }

        // ================= D. 发送前失败 / 限流 / 解码 =================
        report.section("D. 失败分支与容错");
        {
            WowsApi api = WowsApi.of(asia);
            log.clear();
            Throwable e = report.expectThrows("D1 缺 appId 抛 MissingCredentialException", MissingCredentialException.class,
                    () -> api.developers().user(ACCOUNT_ASIA));
            report.expect("D1 零网络请求（fast-fail）", "0", String.valueOf(log.size()));
            if (e instanceof MissingCredentialException missing) {
                report.expect("D1 端点 id", "dev.account.info", missing.getEndpointId());
            }
        }
        {
            WowsApi api = WowsApi.of(cn).credentials(c -> c.applicationId(APP));
            log.clear();
            report.expectThrows("D2 CN 调 dev 抛 UnsupportedOnServerException", UnsupportedOnServerException.class,
                    () -> api.developers().user(ACCOUNT_ASIA));
            report.expect("D2 零网络请求（fast-fail）", "0", String.valueOf(log.size()));
        }
        {
            WowsApi api = WowsApi.of(asia);
            Throwable e = report.expectThrows("D3 STRICT：503 抛 HttpStatusException", HttpStatusException.class,
                    () -> api.vortex().search("Overload"));
            if (e instanceof HttpStatusException status) {
                report.expect("D3 状态码", "503", String.valueOf(status.getStatusCode()));
            }
        }
        {
            WowsApi api = WowsApi.of(asia);
            List<VortexSearchUser> result = api.vortex().search("Overload", RateLimitPolicy.TOLERANT);
            report.expect("D4 TOLERANT：503 返回空表", "0", String.valueOf(result.size()));
        }
        {
            WowsApi api = WowsApi.of(asia);
            List<VortexSearchUser> result = api.vortex().search("Busy", RateLimitPolicy.TOLERANT);
            report.expect("D5 TOLERANT：429 返回空表", "0", String.valueOf(result.size()));
        }
        {
            WowsApi api = WowsApi.of(asia);
            List<VortexSearchUser> result = api.vortex().search("Gzipped");
            report.expect("D6 gzip 响应可解码", searchAsiaCount, String.valueOf(result.size()));
        }

        // ================= E. 监控挂钩（endpoint id 键） =================
        report.section("E. 字段漂移监控挂钩");
        {
            WowsApi api = WowsApi.of(asia);
            SchemaRecorder.enable();
            try {
                api.vortex().user(ACCOUNT_ASIA);
            } finally {
                List<SchemaRecorder.Record> records = SchemaRecorder.drain();
                SchemaRecorder.disable();
                report.expect("E1 vortex 记录 key=endpoint id", "vortex.account.info", records.isEmpty() ? "(空)" : records.getFirst().schemaKey());
                report.expect("E1 记录响应体非空", "true", String.valueOf(!records.isEmpty() && records.getFirst().json().length() > 0));
            }
            WowsApi dev = WowsApi.of(asia).credentials(c -> c.applicationId(APP));
            SchemaRecorder.enable();
            try {
                dev.developers().user(ACCOUNT_ASIA);
            } finally {
                List<SchemaRecorder.Record> records = SchemaRecorder.drain();
                SchemaRecorder.disable();
                report.expect("E2 dev 记录 key=endpoint id", "dev.account.info", records.isEmpty() ? "(空)" : records.getFirst().schemaKey());
            }
        }
    }

    // ================= 在线：真实服务新旧对比 =================

    private static void online(TestReport report) {
        var client = TestConfig.client();
        WowsApi.of(WowsServer.ASIA).timeout(20);
        for (WowsServer server : List.of(WowsServer.ASIA, WowsServer.RU)) {
            vortex(report, server, client);
            developers(report, server, client);
        }
        vortexCn(report, client);
        cnDevUnsupported(report);
    }

    private static void vortex(TestReport report, WowsServer server, java.net.http.HttpClient client) {
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

    private static void developers(TestReport report, WowsServer server, java.net.http.HttpClient client) {
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

    private static void vortexCn(TestReport report, java.net.http.HttpClient client) {
        long accountId = TestConfig.ACCOUNT_CN;
        String token = TestConfig.vortexToken(accountId);
        report.section("在线 · cn vortex（玩家 " + accountId + "，带分享凭证）");
        WowsApi api = WowsApi.of(WowsServer.CN).credentials(c -> c.shareToken(accountId, token));
        report.compare("cn vortex.user（详情+token）",
                () -> JsonUtils.json().toJson(new WowsHttpUserTools(client, WowsServer.CN).userVortex(accountId, token)),
                () -> JsonUtils.json().toJson(api.vortex().user(accountId)));
    }

    private static void cnDevUnsupported(TestReport report) {
        report.section("在线 · cn 官方开发者API（应显式拒绝）");
        report.expectThrows("cn dev 抛 UnsupportedOnServerException", UnsupportedOnServerException.class,
                () -> WowsApi.of(WowsServer.CN).developers().search("x"));
    }

    // ================= 辅助 =================

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
