package com.shinoaki.wows;

import com.shinoaki.wows.api.WowsApi;
import com.shinoaki.wows.api.aggregate.AccountAssembler;
import com.shinoaki.wows.api.auth.AccessToken;
import com.shinoaki.wows.api.auth.ShareToken;
import com.shinoaki.wows.api.codec.HttpCodec;
import com.shinoaki.wows.api.data.AccountClanInfo;
import com.shinoaki.wows.api.data.AccountInfo;
import com.shinoaki.wows.api.developers.account.DevelopersSearchUser;
import com.shinoaki.wows.api.developers.account.DevelopersUserInfo;
import com.shinoaki.wows.api.developers.clan.DevelopersClanInfo;
import com.shinoaki.wows.api.developers.clan.DevelopersSearchUserClan;
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
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.zip.GZIPOutputStream;

/**
 * 3.0 重写 Phase 1：account 域「新旧等价」离线自检。
 * <p>
 * 全部请求打到本地假上游（JDK 自带 {@code com.sun.net.httpserver}），<b>不打任何真实服务</b>：
 * <ul>
 *     <li>假上游按路径回放 {@code target/schema-raw} 里的真实响应（含莱服 /mk/ 与 /wows/clans 的路径差异）；</li>
 *     <li>断言「新管线发出的请求」= 旧实现的 URL/参数语义（路径、查询参数、编码、认证注入）；</li>
 *     <li>断言「新管线解析结果」与旧静态解析链逐字节等价（聚合走 RU 的三份自洽数据）；</li>
 *     <li>覆盖发送前失败分支（缺失凭证 / CN 无 dev API）、限流容错（429/503）、gzip 解码、监控挂钩。</li>
 * </ul>
 * 结论以统计输出；失败项数作为退出码。需要先跑过 {@code SchemaMonitorTest}（或已有 raw 仓库）。
 *
 * @author Xun
 */
public class RewriteAccountEquivalenceTest {

    private static final Path RAW = Path.of("target/schema-raw");
    private static final long ACCOUNT_ASIA = 2022515210L;
    private static final long ACCOUNT_RU = 212112137L;
    private static final long CLAN_RU = 451731L;
    private static final String APP = "TESTAPP";
    private static final String TOKEN = "TESTTOKEN";

    private static int passed;
    private static int failed;

    public static void main(String[] args) throws Exception {
        if (!Files.isDirectory(RAW)) {
            System.out.println("!! 缺少原始响应仓库 " + RAW + "（先跑一次 SchemaMonitorTest 采集）");
            System.exit(0);
        }

        // ---- 真实响应素材 ----
        String vortexAsia = read("vortex_asia_api_accounts_id_.json");
        String vortexRu = read("vortex_ru_api_accounts_id_.json");
        String searchAsia = read("vortex_asia_api_accounts_search_autocomplete_text_.json");
        String searchRu = read("vortex_ru_api_accounts_search_autocomplete_text_.json");
        String devAsiaUser = read("api_asia_wows_account_info_account_id_id_extra_private_grouped_c_c5266c85.json");
        String devRuUser = read("api_ru_mk_account_info_account_id_id_extra_private_grouped_conta_9251f0d3.json");
        String devAsiaSearch = read("api_asia_wows_account_list_search_text_.json");
        String devRuSearch = read("api_ru_mk_account_list_search_text_.json");
        String devAsiaClanRef = read("api_asia_wows_clans_accountinfo_account_id_id_extra_clan.json");
        String devRuClanRef = read("api_ru_wows_clans_accountinfo_account_id_id_extra_clan.json");
        String devRuClanInfo = read("api_ru_wows_clans_info_clan_id_id_extra_members.json");

        // ---- 假上游 ----
        Responses responses = new Responses();
        responses.exact("/api/accounts/" + ACCOUNT_ASIA + "/", vortexAsia);
        responses.exact("/api/accounts/" + ACCOUNT_RU + "/", vortexRu);
        responses.exact("/api/accounts/search/autocomplete/JustOn/", searchAsia);
        responses.exact("/api/accounts/search/autocomplete/yu_yu_ko/", searchRu);
        // 中文搜索词：编码后的路径（用于验证路径占位符按 encodeURIComponent 语义编码）
        responses.exact("/api/accounts/search/autocomplete/" + HttpCodec.encodeURIComponent("西行寺") + "/", searchAsia);
        responses.exact("/wows/account/info/", devAsiaUser);
        responses.exact("/mk/account/info/", devRuUser);
        responses.exact("/wows/account/list/", devAsiaSearch);
        responses.exact("/mk/account/list/", devRuSearch);
        responses.perId("/wows/clans/accountinfo/", ACCOUNT_ASIA, devAsiaClanRef);
        responses.perId("/wows/clans/accountinfo/", ACCOUNT_RU, devRuClanRef);
        responses.perId("/wows/clans/info/", CLAN_RU, devRuClanInfo);
        // 限流场景（搜索路径按搜索词区分）
        responses.status("/api/accounts/search/autocomplete/Overload/", 503);
        responses.status("/api/accounts/search/autocomplete/Busy/", 429);
        // gzip 解码场景
        responses.gzip("/api/accounts/search/autocomplete/Gzipped/", searchAsia);

        RequestLog log = new RequestLog();
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> handle(exchange, responses, log));
        server.start();
        String base = "http://127.0.0.1:" + server.getAddress().getPort();
        try {
            run(base, log, vortexAsia, searchAsia, devAsiaUser, devRuUser, devAsiaSearch,
                    devRuClanRef, devRuClanInfo);
        } finally {
            server.stop(0);
        }

        System.out.println();
        System.out.printf("── 汇总 ── 通过 %d，失败 %d%n", passed, failed);
        System.exit(failed == 0 ? 0 : 1);
    }

    private static void run(String base, RequestLog log, String vortexAsia, String searchAsia,
                            String devAsiaUser, String devRuUser, String devAsiaSearch,
                            String devRuClanRef, String devRuClanInfo) throws Exception {
        ServerProfile asia = ServerProfile.of(WowsServer.ASIA).withHosts(base, base, base);
        ServerProfile ru = ServerProfile.of(WowsServer.RU).withHosts(base, base, base);
        ServerProfile cn = ServerProfile.of(WowsServer.CN).withHosts(base, base, base);
        Function<Object, String> toJson = JsonUtils.json()::toJson;
        String searchAsiaCount = String.valueOf(VortexSearchUser.parse(JsonUtils.json().parse(searchAsia)).size());

        // ================= A. vortex：URL/参数/认证注入 =================
        section("A. vortex 通道（URL / 参数 / 认证注入）");
        {
            WowsApi api = WowsApi.of(asia).credentials(c -> c.shareToken(ACCOUNT_ASIA, TOKEN));
            VortexUserInfo actual = api.vortex().user(ACCOUNT_ASIA);
            Recorded req = log.last();
            expect("A1 方法=GET", "GET", req.method());
            expect("A1 路径", "/api/accounts/" + ACCOUNT_ASIA + "/", req.rawPath());
            expectQuery("A1 查询=ac 注入", Map.of("ac", TOKEN), req.query());
            expect("A1 结果=旧解析", toJson.apply(VortexUserInfo.parse(JsonUtils.json().parse(vortexAsia), ACCOUNT_ASIA)), toJson.apply(actual));
            expect("A1 UA 头存在", "true", String.valueOf(req.headers().getFirst("User-Agent") != null && req.headers().getFirst("User-Agent").contains("Mozilla")));
            expect("A1 Accept-Encoding", "gzip, deflate", req.headers().getFirst("Accept-Encoding"));
        }
        {
            // 未配置凭证 → 匿名请求（无 ac）
            WowsApi api = WowsApi.of(asia);
            api.vortex().user(ACCOUNT_ASIA);
            expectQuery("A2 匿名请求不带 ac", Map.of(), log.last().query());
        }
        {
            // 搜索词编码：中文必须按 encodeURIComponent 语义进路径（真实请求走管道验证）
            WowsApi api = WowsApi.of(asia);
            List<VortexSearchUser> result = api.vortex().search("西行寺");
            expect("A3 路径编码=旧实现语义", "/api/accounts/search/autocomplete/%E8%A5%BF%E8%A1%8C%E5%AF%BA/",
                    log.last().rawPath());
            expect("A3 编码实测与 HttpCodec 一致", HttpCodec.encodeURIComponent("西行寺"), "%E8%A5%BF%E8%A1%8C%E5%AF%BA");
            expect("A3 中文搜索可正常解析", searchAsiaCount, String.valueOf(result.size()));
        }
        {
            // 调用级覆盖 ShareToken 优先于实例配置
            WowsApi api = WowsApi.of(asia).credentials(c -> c.shareToken(ACCOUNT_ASIA, TOKEN));
            api.vortex().user(ACCOUNT_ASIA, ShareToken.of(ACCOUNT_ASIA, "OVERRIDE"));
            expectQuery("A4 覆盖凭证生效", Map.of("ac", "OVERRIDE"), log.last().query());
        }
        {
            // 账号不匹配的凭证不注入
            WowsApi api = WowsApi.of(asia).credentials(c -> c.shareToken(999, "WRONG"));
            api.vortex().user(ACCOUNT_ASIA);
            expectQuery("A5 凭证账号不匹配不注入", Map.of(), log.last().query());
        }
        {
            // 非账号端点不注入账号级凭证
            WowsApi api = WowsApi.of(asia).credentials(c -> c.shareToken(ACCOUNT_ASIA, TOKEN));
            List<VortexSearchUser> result = api.vortex().search("JustOn");
            expectQuery("A6 搜索不带 ac", Map.of(), log.last().query());
            expect("A6 搜索解析条目数", searchAsiaCount, String.valueOf(result.size()));
        }

        // ================= B. dev：前缀/extra/认证 =================
        section("B. dev 通道（前缀 / extra / 认证）");
        {
            WowsApi api = WowsApi.of(asia).credentials(c -> c.applicationId(APP));
            DevelopersUserInfo actual = api.developers().user(ACCOUNT_ASIA);
            Recorded req = log.last();
            expect("B1 ASIA 前缀", "/wows/account/info/", req.rawPath());
            expectQuery("B1 查询参数", Map.of("application_id", APP, "account_id", String.valueOf(ACCOUNT_ASIA),
                    "extra", asia.accountInfoExtra()), req.query());
            expect("B1 extra 含 statistics.clan", "true", String.valueOf(asia.accountInfoExtra().contains("statistics.clan")));
            expect("B1 结果=旧解析", toJson.apply(DevelopersUserInfo.parse(ACCOUNT_ASIA, devAsiaUser)), toJson.apply(actual));
        }
        {
            WowsApi api = WowsApi.of(ru).credentials(c -> c.applicationId(APP));
            DevelopersUserInfo actual = api.developers().user(ACCOUNT_RU);
            Recorded req = log.last();
            expect("B2 RU 前缀=/mk/", "/mk/account/info/", req.rawPath());
            expect("B2 extra 无 statistics.clan", "false", String.valueOf(ru.accountInfoExtra().contains("statistics.clan")));
            expect("B2 extra 与查询一致", ru.accountInfoExtra(), req.query().get("extra"));
            expect("B2 结果=旧解析", toJson.apply(DevelopersUserInfo.parse(ACCOUNT_RU, devRuUser)), toJson.apply(actual));
        }
        {
            // 调用级 access_token 覆盖
            WowsApi api = WowsApi.of(asia).credentials(c -> c.applicationId(APP));
            api.developers().user(ACCOUNT_ASIA, AccessToken.of(ACCOUNT_ASIA, "AT"));
            expect("B3 access_token 注入", "AT", log.last().query().get("access_token"));
        }
        {
            WowsApi api = WowsApi.of(asia).credentials(c -> c.applicationId(APP));
            List<DevelopersSearchUser> result = api.developers().search("JustOn");
            expect("B4 搜索路径", "/wows/account/list/", log.last().rawPath());
            expectQuery("B4 搜索参数", Map.of("application_id", APP, "search", "JustOn"), log.last().query());
            expect("B4 结果=旧解析", toJson.apply(DevelopersSearchUser.parse(devAsiaSearch)), toJson.apply(result));
        }
        {
            WowsApi api = WowsApi.of(ru).credentials(c -> c.applicationId(APP));
            api.developers().search("yu_yu_ko");
            expect("B5 RU 搜索前缀=/mk/", "/mk/account/list/", log.last().rawPath());
        }
        {
            // 莱服 clans 系列仍在 /wows/ 下（易错点，单独锁死）
            WowsApi api = WowsApi.of(ru).credentials(c -> c.applicationId(APP));
            DevelopersSearchUserClan actual = api.developers().accountClan(ACCOUNT_RU);
            expect("B6 RU 所属军团路径仍在 /wows/", "/wows/clans/accountinfo/", log.last().rawPath());
            expectQuery("B6 查询参数", Map.of("application_id", APP, "account_id", String.valueOf(ACCOUNT_RU), "extra", "clan"), log.last().query());
            expect("B6 结果=旧解析", toJson.apply(DevelopersSearchUserClan.parse(ACCOUNT_RU, devRuClanRef)), toJson.apply(actual));
        }
        {
            WowsApi api = WowsApi.of(ru).credentials(c -> c.applicationId(APP));
            DevelopersClanInfo actual = api.developers().clanInfo(CLAN_RU);
            expect("B7 RU 军团详情路径", "/wows/clans/info/", log.last().rawPath());
            expectQuery("B7 查询参数", Map.of("application_id", APP, "clan_id", String.valueOf(CLAN_RU), "extra", "members"), log.last().query());
            expect("B7 结果=旧解析", toJson.apply(DevelopersClanInfo.parse(CLAN_RU, devRuClanInfo)), toJson.apply(actual));
        }

        // ================= C. 聚合：新旧逐字段等价（RU 三份数据自洽） =================
        section("C. 聚合（AccountAssembler 3 请求链）");
        {
            WowsApi api = WowsApi.of(ru).credentials(c -> c.applicationId(APP));
            log.clear();
            AccountInfo actual = AccountAssembler.of(api).info(ACCOUNT_RU);

            // 旧静态解析链（同源数据逐字节等价）
            AccountClanInfo oldClan = AccountClanInfo.accountClan(ACCOUNT_RU, devRuClanRef);
            if (oldClan.clanId() > 0) {
                oldClan = AccountClanInfo.of(oldClan, DevelopersClanInfo.parse(oldClan.clanId(), devRuClanInfo));
            }
            AccountInfo expected = AccountInfo.parse(ACCOUNT_RU, devRuUser, oldClan);
            expect("C1 聚合结果=旧链", toJson.apply(expected), toJson.apply(actual));
            expect("C2 请求数=3", "3", String.valueOf(log.size()));
            expect("C2 请求顺序", "/mk/account/info/ -> /wows/clans/accountinfo/ -> /wows/clans/info/",
                    log.get(0).rawPath() + " -> " + log.get(1).rawPath() + " -> " + log.get(2).rawPath());
        }

        // ================= D. 发送前失败 / 限流 / 解码 =================
        section("D. 失败分支与容错");
        {
            WowsApi api = WowsApi.of(asia); // 未配 appId
            log.clear();
            Throwable e = expectThrows("D1 缺 appId 抛 MissingCredentialException", MissingCredentialException.class,
                    () -> api.developers().user(ACCOUNT_ASIA));
            expect("D1 零网络请求（fast-fail）", "0", String.valueOf(log.size()));
            if (e instanceof MissingCredentialException missing) {
                expect("D1 端点 id", "dev.account.info", missing.getEndpointId());
            }
        }
        {
            WowsApi api = WowsApi.of(cn).credentials(c -> c.applicationId(APP));
            log.clear();
            expectThrows("D2 CN 调 dev 抛 UnsupportedOnServerException", UnsupportedOnServerException.class,
                    () -> api.developers().user(ACCOUNT_ASIA));
            expect("D2 零网络请求（fast-fail）", "0", String.valueOf(log.size()));
        }
        {
            WowsApi api = WowsApi.of(asia);
            Throwable e = expectThrows("D3 STRICT：503 抛 HttpStatusException", HttpStatusException.class,
                    () -> api.vortex().search("Overload"));
            if (e instanceof HttpStatusException status) {
                expect("D3 状态码", "503", String.valueOf(status.getStatusCode()));
            }
        }
        {
            WowsApi api = WowsApi.of(asia);
            List<VortexSearchUser> result = api.vortex().search("Overload", RateLimitPolicy.TOLERANT);
            expect("D4 TOLERANT：503 返回空表", "0", String.valueOf(result.size()));
        }
        {
            WowsApi api = WowsApi.of(asia);
            List<VortexSearchUser> result = api.vortex().search("Busy", RateLimitPolicy.TOLERANT);
            expect("D5 TOLERANT：429 返回空表", "0", String.valueOf(result.size()));
        }
        {
            WowsApi api = WowsApi.of(asia);
            List<VortexSearchUser> result = api.vortex().search("Gzipped");
            expect("D6 gzip 响应可解码", searchAsiaCount, String.valueOf(result.size()));
        }

        // ================= E. 监控挂钩（endpoint id 键） =================
        section("E. 字段漂移监控挂钩");
        {
            WowsApi api = WowsApi.of(asia);
            SchemaRecorder.enable();
            try {
                api.vortex().user(ACCOUNT_ASIA);
            } finally {
                List<SchemaRecorder.Record> records = SchemaRecorder.drain();
                SchemaRecorder.disable();
                expect("E1 vortex 记录 key=endpoint id", "vortex.account.info", records.isEmpty() ? "(空)" : records.getFirst().schemaKey());
                expect("E1 记录响应体非空", "true", String.valueOf(!records.isEmpty() && records.getFirst().json().length() > 0));
            }
            WowsApi dev = WowsApi.of(asia).credentials(c -> c.applicationId(APP));
            SchemaRecorder.enable();
            try {
                dev.developers().user(ACCOUNT_ASIA);
            } finally {
                List<SchemaRecorder.Record> records = SchemaRecorder.drain();
                SchemaRecorder.disable();
                expect("E2 dev 记录 key=endpoint id", "dev.account.info", records.isEmpty() ? "(空)" : records.getFirst().schemaKey());
            }
        }
    }

    // ================= 假上游实现 =================

    private static void handle(HttpExchange exchange, Responses responses, RequestLog log) throws IOException {
        String rawPath = exchange.getRequestURI().getRawPath();
        Map<String, String> query = parseQuery(exchange.getRequestURI().getRawQuery());
        log.add(new Recorded(exchange.getRequestMethod(), rawPath, query, exchange.getRequestHeaders()));
        exchange.getRequestBody().readAllBytes();
        Canned canned = responses.find(rawPath, query);
        if (canned == null) {
            exchange.sendResponseHeaders(404, -1);
            exchange.close();
            return;
        }
        byte[] body = canned.body() == null ? new byte[0] : canned.body().getBytes(StandardCharsets.UTF_8);
        if (canned.gzip()) {
            body = gzip(body);
            exchange.getResponseHeaders().set("Content-Encoding", "gzip");
        }
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        if (canned.status() != 200) {
            exchange.sendResponseHeaders(canned.status(), -1);
            exchange.close();
            return;
        }
        exchange.sendResponseHeaders(200, body.length);
        exchange.getResponseBody().write(body);
        exchange.close();
    }

    private static byte[] gzip(byte[] bytes) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (GZIPOutputStream gzip = new GZIPOutputStream(out)) {
            gzip.write(bytes);
        }
        return out.toByteArray();
    }

    private static Map<String, String> parseQuery(String rawQuery) {
        Map<String, String> query = new LinkedHashMap<>();
        if (rawQuery == null || rawQuery.isEmpty()) {
            return query;
        }
        for (String pair : rawQuery.split("&")) {
            if (pair.isEmpty()) {
                continue;
            }
            int eq = pair.indexOf('=');
            query.put(eq < 0 ? pair : pair.substring(0, eq), eq < 0 ? "" : pair.substring(eq + 1));
        }
        return query;
    }

    private static String read(String name) throws IOException {
        return Files.readString(RAW.resolve(name));
    }

    // ================= 断言与输出 =================

    private static void section(String title) {
        System.out.println();
        System.out.println("== " + title + " ==");
    }

    private static void expect(String name, Object expected, Object actual) {
        if (String.valueOf(expected).equals(String.valueOf(actual))) {
            passed++;
            System.out.println("  [ok] " + name);
        } else {
            failed++;
            System.out.println("  [FAIL] " + name + "\n         期望: " + expected + "\n         实际: " + actual);
        }
    }

    /**
     * 查询参数断言：内容一致即可（Map.equals 与插入顺序无关）。
     */
    private static void expectQuery(String name, Map<String, String> expected, Map<String, String> actual) {
        if (expected.equals(actual)) {
            passed++;
            System.out.println("  [ok] " + name);
        } else {
            failed++;
            System.out.println("  [FAIL] " + name + "\n         期望: " + new TreeMap<>(expected) + "\n         实际: " + new TreeMap<>(actual));
        }
    }

    private static Throwable expectThrows(String name, Class<? extends Throwable> type, ThrowingRunnable runnable) {
        try {
            runnable.run();
            failed++;
            System.out.println("  [FAIL] " + name + "（没有抛异常）");
            return null;
        } catch (Throwable e) {
            if (type.isInstance(e)) {
                passed++;
                System.out.println("  [ok] " + name);
                return e;
            }
            failed++;
            System.out.println("  [FAIL] " + name + "\n         期望: " + type.getSimpleName() + "\n         实际: " + e.getClass().getSimpleName() + " " + e.getMessage());
            return e;
        }
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Exception;
    }

    private record Recorded(String method, String rawPath, Map<String, String> query, Headers headers) {
    }

    private static final class RequestLog {
        private final List<Recorded> records = new CopyOnWriteArrayList<>();

        void add(Recorded recorded) {
            records.add(recorded);
        }

        void clear() {
            records.clear();
        }

        int size() {
            return records.size();
        }

        Recorded last() {
            return records.get(records.size() - 1);
        }

        Recorded get(int index) {
            return records.get(index);
        }
    }

    private record Canned(String body, int status, boolean gzip) {
    }

    private static final class Responses {
        private final Map<String, Canned> exact = new HashMap<>();
        private final Map<String, Canned> perId = new HashMap<>();

        void exact(String path, String body) {
            exact.put(path, new Canned(body, 200, false));
        }

        void gzip(String path, String body) {
            exact.put(path, new Canned(body, 200, true));
        }

        void status(String path, int status) {
            exact.put(path, new Canned(null, status, false));
        }

        void perId(String path, long accountId, String body) {
            perId.put(path + "#" + accountId, new Canned(body, 200, false));
        }

        Canned find(String rawPath, Map<String, String> query) {
            String id = query.get("account_id");
            if (id == null) {
                id = query.get("clan_id");
            }
            if (id != null) {
                Canned canned = perId.get(rawPath + "#" + id);
                if (canned != null) {
                    return canned;
                }
            }
            return exact.get(rawPath);
        }
    }
}
