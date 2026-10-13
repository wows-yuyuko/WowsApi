package com.shinoaki.wows;

import com.shinoaki.wows.api.WowsApi;
import com.shinoaki.wows.api.codec.ClanRankUtils;
import com.shinoaki.wows.api.codec.HttpCodec;
import com.shinoaki.wows.api.codec.http.WowsHttpClanTools;
import com.shinoaki.wows.api.codec.http.WowsEncyclopediaTools;
import com.shinoaki.wows.api.developers.clan.DevelopersSearchClan;
import com.shinoaki.wows.api.developers.clan.DevelopersSearchUserClan;
import com.shinoaki.wows.api.developers.clan.seasion.DevelopersSeasonInfo;
import com.shinoaki.wows.api.developers.encyclopedia.glossary.DevelopersGlossary;
import com.shinoaki.wows.api.error.HttpStatusException;
import com.shinoaki.wows.api.server.ServerProfile;
import com.shinoaki.wows.api.type.WowsServer;
import com.shinoaki.wows.api.utils.JsonUtils;
import com.shinoaki.wows.api.vortex.clan.VortexSearchClan;
import com.shinoaki.wows.api.vortex.clan.account.VortexSearchClanUser;
import com.shinoaki.wows.api.vortex.clan.base.VortexClanInfo;
import com.shinoaki.wows.api.vortex.clan.members.VortexClanUserInfo;
import com.shinoaki.wows.api.vortex.clan.rank.ClanRankInfo;
import com.shinoaki.wows.testkit.CanonicalJson;
import com.shinoaki.wows.testkit.FakeUpstream;
import com.shinoaki.wows.testkit.TestConfig;
import com.shinoaki.wows.testkit.TestReport;
import tools.jackson.core.type.TypeReference;

import java.net.URI;
import java.net.http.HttpClient;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * 3.0 重写「公会相关」域新旧等价测试：所属军团、军团详情/成员/搜索、军团排行（分页游标），
 * 官方API 军团搜索/赛季/词典，以及 CN 404→空 等语义。
 * <p>
 * 默认跑<b>离线段</b>（假上游回放 {@code target/schema-raw}，不打真实服务）；
 * 传 {@code --online} 追加<b>在线段</b>（真实服务新旧对比。本机直连 WG 主机不稳定，
 * 建议带 {@code -Dwows.proxy=127.0.0.1:7890}）；{@code --full} 追加整季排行全量对比（RU，较重）。
 * <p>
 * 用法：{@code java -cp ... com.shinoaki.wows.RewriteClanTest [--online] [--full]}；退出码 = 失败项数。
 *
 * @author Xun
 */
public class RewriteClanTest {

    private static final Path RAW = Path.of("target/schema-raw");
    private static final long ACCOUNT_ASIA = 2022515210L;
    private static final long ACCOUNT_RU = 212112137L;
    private static final long CLAN_RU = 451731L;
    private static final long CLAN_MEMBERS_ASIA = 2000025691L;
    private static final String APP = "TESTAPP";

    public static void main(String[] args) throws Exception {
        boolean online = flag(args, "--online") || flag(args, "--full");
        boolean full = flag(args, "--full");
        TestReport report = new TestReport("公会相关");
        System.out.println("── 公会相关 · 3.0 新旧等价测试 ──");
        System.out.println("模式: " + (online ? "离线（假上游）+ 在线（真实服务）" + (full ? " + 整季排行全量" : "") : "离线（假上游；--online 追加在线新旧对比）"));

        offline(report);
        if (online) {
            online(report, full);
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
        String vortexAsiaClan = read("vortex_asia_api_accounts_id_clans_.json");
        String ruClanBase = read("clans_ru_api_clanbase_id_claninfo_.json");
        String asiaMembers = read("clans_asia_api_members_id_battle_type_pvp.json");
        String asiaClanSearch = read("clans_asia_api_search_autocomplete_search_text_type_clans.json");
        String devRuClanRef = read("api_ru_wows_clans_accountinfo_account_id_id_extra_clan.json");
        String devRuClanInfo = read("api_ru_wows_clans_info_clan_id_id_extra_members.json");
        String devAsiaClanList = read("api_asia_wows_clans_list_search_text_.json");
        String devAsiaSeason = read("api_asia_wows_clans_season_.json");
        String devRuGlossary = read("api_ru_mk_clans_glossary_.json");

        // 排行合成数据（无真实 dump；断 URL 与分页游标语义）
        String ladderPage1 = ladderJson(
                new ClanRankInfo(false, 1, "sg", 30, "测试军团A", 1, 100, 90, 1, 50, "#FF0000", "", "sg", "", 35, 11L, 1, "0x1", "AAA"),
                new ClanRankInfo(false, 2, "sg", 25, "测试军团B", 1, 95, 88, 1, 40, "#00FF00", "", "sg", "", 35, 12L, 1, "0x2", "BBB"));
        String ladderPage2 = ladderJson(
                new ClanRankInfo(false, 3, "sg", 20, "测试军团C", 2, 90, 80, 2, 30, "#0000FF", "", "sg", "", 35, 13L, 1, "0x3", "CCC"));

        try (FakeUpstream upstream = FakeUpstream.start()) {
            upstream.vortex().exact("/api/accounts/" + ACCOUNT_ASIA + "/clans/", vortexAsiaClan);
            upstream.clans().exact("/api/clanbase/" + CLAN_RU + "/claninfo/", ruClanBase);
            upstream.clans().exact("/api/members/" + CLAN_MEMBERS_ASIA + "/", asiaMembers);
            upstream.clans().exact("/api/search/autocomplete/", asiaClanSearch);
            // 排行：第一页 + 游标页（clan_id=12 → 第二页；clan_id=13 → 空页终止）
            upstream.clans().exact("/api/ladder/structure/", ladderPage1);
            upstream.clans().perId("/api/ladder/structure/", 12, ladderPage2);
            upstream.clans().perId("/api/ladder/structure/", 13, "[]");

            upstream.api().perId("/wows/clans/accountinfo/", ACCOUNT_RU, devRuClanRef);
            upstream.api().perId("/wows/clans/info/", CLAN_RU, devRuClanInfo);
            upstream.api().exact("/wows/clans/list/", devAsiaClanList);
            upstream.api().exact("/wows/clans/season/", devAsiaSeason);
            upstream.api().exact("/mk/clans/glossary/", devRuGlossary);

            checks(report, upstream, vortexAsiaClan, ruClanBase, asiaMembers, asiaClanSearch, devRuClanRef,
                    devRuClanInfo, devAsiaClanList, devAsiaSeason, devRuGlossary);
        }
    }

    private static void checks(TestReport report, FakeUpstream upstream, String vortexAsiaClan, String ruClanBase,
                               String asiaMembers, String asiaClanSearch, String devRuClanRef, String devRuClanInfo,
                               String devAsiaClanList, String devAsiaSeason, String devRuGlossary) throws Exception {
        var log = upstream.requests();
        ServerProfile asia = ServerProfile.of(WowsServer.ASIA).withHosts(upstream.vortexBase(), upstream.clansBase(), upstream.apiBase());
        ServerProfile ru = ServerProfile.of(WowsServer.RU).withHosts(upstream.vortexBase(), upstream.clansBase(), upstream.apiBase());
        ServerProfile cn = ServerProfile.of(WowsServer.CN).withHosts(upstream.vortexBase(), upstream.clansBase(), upstream.apiBase());
        Function<Object, String> toJson = JsonUtils.json()::toJson;
        var json = JsonUtils.json();

        // ================= A. 所属军团（vortex + dev） =================
        report.section("A. 所属军团");
        {
            WowsApi api = WowsApi.of(asia);
            VortexSearchClanUser actual = api.clans().accountClan(ACCOUNT_ASIA);
            report.expect("A1 所属军团路径（vortex 主机）", "/api/accounts/" + ACCOUNT_ASIA + "/clans/", log.last().rawPath());
            report.expect("A1 结果=旧解析",
                    toJson.apply(VortexSearchClanUser.to(json.parse(vortexAsiaClan))),
                    toJson.apply(actual));
        }
        {
            // CN（非 API 服）404 → 空对象（旧语义保留）；用未注册账号让假上游返回 404
            WowsApi api = WowsApi.of(cn);
            log.clear();
            VortexSearchClanUser actual = api.clans().accountClan(999999L);
            report.expect("A2 CN 404 → 空对象",
                    toJson.apply(new VortexSearchClanUser("", VortexSearchClanUser.VortexSearchClanInfo.empty(), "", 0)),
                    toJson.apply(actual));
            report.expect("A2 请求数=1", "1", String.valueOf(log.size()));
        }
        {
            // 非 CN 的 404 仍抛异常（未注册路径 = 404）
            WowsApi api = WowsApi.of(asia);
            report.expectThrows("A3 非 CN 404 抛 HttpStatusException", HttpStatusException.class,
                    () -> api.clans().accountClan(999999L));
        }
        {
            // dev 所属军团：莱服仍走 /wows/（clans 命名空间无 /mk/ 版本，易错点单独锁死）
            WowsApi api = WowsApi.of(ru).credentials(c -> c.applicationId(APP));
            DevelopersSearchUserClan actual = api.developers().accountClan(ACCOUNT_RU);
            report.expect("A4 dev 所属军团 RU 路径仍在 /wows/", "/wows/clans/accountinfo/", log.last().rawPath());
            report.expectQuery("A4 查询参数", Map.of("application_id", APP, "account_id", String.valueOf(ACCOUNT_RU), "extra", "clan"), log.last().queryMap());
            report.expect("A4 结果=旧解析", toJson.apply(DevelopersSearchUserClan.parse(ACCOUNT_RU, devRuClanRef)), toJson.apply(actual));
        }

        // ================= B. 军团详情 / 成员 / 搜索 =================
        report.section("B. 军团详情 / 成员 / 搜索");
        {
            WowsApi api = WowsApi.of(ru);
            VortexClanInfo actual = api.clans().clanInfo(CLAN_RU);
            report.expect("B1 军团详情路径", "/api/clanbase/" + CLAN_RU + "/claninfo/", log.last().rawPath());
            report.expect("B1 服务器落库=RU", WowsServer.RU, actual.wowsServer());
            report.expect("B1 结果=旧解析（含 mk_ladder 区分）",
                    toJson.apply(VortexClanInfo.to(WowsServer.RU, CLAN_RU, json.parse(ruClanBase))),
                    toJson.apply(actual));
        }
        {
            WowsApi api = WowsApi.of(ru).credentials(c -> c.applicationId(APP));
            var actual = api.developers().clanInfo(CLAN_RU);
            report.expect("B2 dev 军团详情路径", "/wows/clans/info/", log.last().rawPath());
            report.expectQuery("B2 dev 军团详情参数", Map.of("application_id", APP, "clan_id", String.valueOf(CLAN_RU), "extra", "members"), log.last().queryMap());
            report.expect("B2 dev 军团详情=旧解析",
                    toJson.apply(com.shinoaki.wows.api.developers.clan.DevelopersClanInfo.parse(CLAN_RU, devRuClanInfo)),
                    toJson.apply(actual));
        }
        {
            WowsApi api = WowsApi.of(asia);
            var actual = api.clans().members(CLAN_MEMBERS_ASIA);
            report.expect("B3 成员路径", "/api/members/" + CLAN_MEMBERS_ASIA + "/", log.last().rawPath());
            report.expectQuery("B3 默认 battle_type=pvp", Map.of("battle_type", "pvp"), log.last().queryMap());
            report.expect("B3 结果=旧解析",
                    toJson.apply(VortexClanUserInfo.to(WowsServer.ASIA, json.parse(asiaMembers))),
                    toJson.apply(actual));
        }
        {
            WowsApi api = WowsApi.of(asia);
            api.clans().members(CLAN_MEMBERS_ASIA, "cvc", 35);
            report.expectQuery("B4 cvc + season 参数", Map.of("battle_type", "cvc", "season", "35"), log.last().queryMap());
        }
        {
            WowsApi api = WowsApi.of(asia);
            List<VortexSearchClan> actual = api.clans().search("YU_RI");
            report.expect("B5 军团搜索路径", "/api/search/autocomplete/", log.last().rawPath());
            report.expectQuery("B5 查询参数", Map.of("search", "YU_RI", "type", "clans"), log.last().queryMap());
            report.expect("B5 结果=旧解析",
                    toJson.apply(VortexSearchClan.parse(json.parse(asiaClanSearch))),
                    toJson.apply(actual));
        }

        // ================= C. 军团排行（分页游标 / realm / season） =================
        report.section("C. 军团排行");
        {
            WowsApi api = WowsApi.of(asia);
            log.clear();
            List<ClanRankInfo> actual = api.clans().ladder(35);
            report.expect("C1 请求数=3（首页+游标页+空页）", "3", String.valueOf(log.size()));
            report.expect("C1 第1页查询串", "?realm=sg&season=35", log.get(0).rawQuery());
            report.expect("C1 第2页（游标）", "?realm=sg&season=35&clan_id=12", log.get(1).rawQuery());
            report.expect("C1 第3页（空页终止）", "?realm=sg&season=35&clan_id=13", log.get(2).rawQuery());
            report.expect("C1 结果 id 集合", "[11, 12, 13]", sortedIds(actual));
        }
        {
            // season<=0：不传 season（旧实现发空值被服务端 422 → 静默空表；3.0 修正为省略参数）
            WowsApi api = WowsApi.of(ru);
            log.clear();
            List<ClanRankInfo> actual = api.clans().ladder(0);
            report.expect("C2 最新赛季不传 season", "?realm=global", log.get(0).rawQuery());
            report.expect("C2 结果 id 集合", "[11, 12, 13]", sortedIds(actual));
        }
        {
            WowsApi api = WowsApi.of(ru);
            api.clans().ladderPage(28, null);
            report.expect("C3 单页带赛季", "?realm=global&season=28", log.last().rawQuery());
        }

        // ================= D. dev 军团搜索 / 赛季 / 词典 =================
        report.section("D. dev 军团搜索 / 赛季 / 词典");
        {
            WowsApi api = WowsApi.of(asia).credentials(c -> c.applicationId(APP));
            List<DevelopersSearchClan> actual = api.developers().clanSearch("YU_RI");
            report.expect("D1 军团搜索路径", "/wows/clans/list/", log.last().rawPath());
            report.expectQuery("D1 查询参数", Map.of("application_id", APP, "search", "YU_RI"), log.last().queryMap());
            report.expect("D1 结果=旧解析",
                    toJson.apply(DevelopersSearchClan.parse(json.parse(devAsiaClanList))),
                    toJson.apply(actual));
        }
        {
            WowsApi api = WowsApi.of(asia).credentials(c -> c.applicationId(APP));
            List<DevelopersSeasonInfo> actual = api.developers().clanSeason();
            report.expectQuery("D2 season 默认语言 zh-cn", Map.of("application_id", APP, "language", "zh-cn"), log.last().queryMap());
            report.expect("D2 结果=旧解析",
                    toJson.apply(DevelopersSeasonInfo.parse(json.parse(devAsiaSeason))),
                    toJson.apply(actual));
        }
        {
            WowsApi api = WowsApi.of(ru).credentials(c -> c.applicationId(APP));
            DevelopersGlossary actual = api.developers().glossary();
            report.expect("D3 词典前缀 /mk/", "/mk/clans/glossary/", log.last().rawPath());
            report.expect("D3 结果=旧解析",
                    toJson.apply(DevelopersGlossary.parse(json.parse(devRuGlossary))),
                    toJson.apply(actual));
        }
    }

    // ================= 在线：真实服务新旧对比 =================

    private static void online(TestReport report, boolean full) {
        HttpClient client = TestConfig.client();
        WowsApi.of(WowsServer.ASIA).timeout(20);
        for (WowsServer server : List.of(WowsServer.ASIA, WowsServer.RU)) {
            clans(report, server, client);
            ladder(report, server, client);
            developers(report, server, client);
        }
        clansCn(report, client);
        ladderCn(report, client);
        if (full) {
            ladderFull(report, client);
        }
    }

    private static void clans(TestReport report, WowsServer server, HttpClient client) {
        String label = server.getCode();
        Long accountId = TestConfig.accountIdOrNull(server);
        Long clanId = TestConfig.clanIdOrNull(server);
        if (accountId == null || clanId == null) {
            return;
        }
        report.section("在线 · " + label + " 军团 vortex（军团 " + clanId + "）");
        WowsHttpClanTools old = new WowsHttpClanTools(client, server);
        WowsApi api = WowsApi.of(server);
        report.compare(label + " clans.account.clan（所属军团）",
                () -> JsonUtils.json().toJson(old.vortex().userSearchClanVortex(accountId)),
                () -> JsonUtils.json().toJson(api.clans().accountClan(accountId)));
        report.compare(label + " clans.info（详情）",
                () -> JsonUtils.json().toJson(old.vortex().clanInfoVortex(clanId)),
                () -> JsonUtils.json().toJson(api.clans().clanInfo(clanId)));
        report.compare(label + " clans.search（搜索）",
                () -> JsonUtils.json().toJson(old.vortex().searchClanVortex(TestConfig.SEARCH_CLAN)),
                () -> JsonUtils.json().toJson(api.clans().search(TestConfig.SEARCH_CLAN)));
        if (server == WowsServer.ASIA) {
            long membersId = TestConfig.CLAN_MEMBERS_ASIA;
            report.compare(label + " clans.members（成员）",
                    () -> JsonUtils.json().toJson(old.vortex().clanUserListInfoVortex(membersId)),
                    () -> JsonUtils.json().toJson(api.clans().members(membersId)));
        }
    }

    private static void clansCn(TestReport report, HttpClient client) {
        long clanId = TestConfig.CLAN_CN;
        report.section("在线 · cn 军团 vortex（军团 " + clanId + "）");
        WowsHttpClanTools old = new WowsHttpClanTools(client, WowsServer.CN);
        WowsApi api = WowsApi.of(WowsServer.CN);
        report.compare("cn clans.account.clan（所属军团）",
                () -> JsonUtils.json().toJson(old.vortex().userSearchClanVortex(TestConfig.ACCOUNT_CN)),
                () -> JsonUtils.json().toJson(api.clans().accountClan(TestConfig.ACCOUNT_CN)));
        report.compare("cn clans.info（详情）",
                () -> JsonUtils.json().toJson(old.vortex().clanInfoVortex(clanId)),
                () -> JsonUtils.json().toJson(api.clans().clanInfo(clanId)));
    }

    private static void ladder(TestReport report, WowsServer server, HttpClient client) {
        String label = server.getCode();
        report.section("在线 · " + label + " 军团排行（单页 + 游标页）");
        WowsApi api = WowsApi.of(server);
        try {
            List<ClanRankInfo> first = api.clans().ladderPage(0, null);
            if (first.isEmpty()) {
                report.fail(label + " ladder：最新赛季第一页为空（跳过对比）");
                return;
            }
            int season = first.getFirst().season_number();
            long cursor = first.getLast().id();
            report.compare(label + " ladder 单页（season=" + season + "）",
                    () -> oldLadderPage(client, server, season, null),
                    () -> JsonUtils.json().toJson(api.clans().ladderPage(season, null)));
            report.compare(label + " ladder 游标页（clan_id=" + cursor + "）",
                    () -> oldLadderPage(client, server, season, cursor),
                    () -> JsonUtils.json().toJson(api.clans().ladderPage(season, cursor)));
        } catch (Exception e) {
            report.fail(label + " ladder：异常 " + e);
        }
    }

    /**
     * 旧侧单页：按 {@code ClanRankUtils} 的请求语句原样复刻（不含其"吞异常"逻辑）。
     */
    private static String oldLadderPage(HttpClient client, WowsServer server, int season, Long clanId) throws Exception {
        String realm = switch (server) {
            case CN, RU -> "global";
            case ASIA -> "sg";
            case EU -> "eu";
            case NA -> "us";
        };
        String url = server.clans() + "/api/ladder/structure/?realm=" + realm + "&season=" + season
                + (clanId == null ? "" : "&clan_id=" + clanId);
        var resp = HttpCodec.send(client, HttpCodec.request(URI.create(url)));
        List<ClanRankInfo> list = JsonUtils.json().parse(HttpCodec.response(resp), new TypeReference<List<ClanRankInfo>>() {
        });
        return JsonUtils.json().toJson(list);
    }

    private static void ladderCn(TestReport report, HttpClient client) {
        report.section("在线 · cn 军团排行（单页）");
        WowsApi api = WowsApi.of(WowsServer.CN);
        try {
            var first = api.clans().ladderPage(0, null);
            if (first.isEmpty()) {
                report.fail("cn ladder：第一页为空");
                return;
            }
            int season = first.getFirst().season_number();
            report.compare("cn ladder 单页（season=" + season + "，realm=global）",
                    () -> oldLadderPage(client, WowsServer.CN, season, null),
                    () -> JsonUtils.json().toJson(api.clans().ladderPage(season, null)));
        } catch (Exception e) {
            report.fail("cn ladder：异常 " + e);
        }
    }

    /**
     * 整季全量拉取对比（RU；旧=ClanRankUtils.getRanks，新=ClansClient.ladder）。较重，需 --full。
     * <p>
     * 旧实现有「吞异常」历史行为：任一分页请求失败会<b>静默提前截断</b>（实测曾出现 289 vs 360），
     * 因此不一致时自动重跑一轮；两轮都不同才判失败。
     */
    private static void ladderFull(TestReport report, HttpClient client) {
        WowsServer server = WowsServer.RU;
        report.section("在线 · ru 军团排行（整季全量，--full）");
        WowsApi api = WowsApi.of(server);
        try {
            int season = api.clans().ladderPage(0, null).getFirst().season_number();
            for (int attempt = 1; attempt <= 2; attempt++) {
                long t0 = System.currentTimeMillis();
                var oldList = new ClanRankUtils().getRanks(client, server, season);
                long oldMs = System.currentTimeMillis() - t0;
                long t1 = System.currentTimeMillis();
                var newList = api.clans().ladder(season);
                long newMs = System.currentTimeMillis() - t1;
                System.out.printf("  第 %d 轮：旧 %d 条（%d ms） vs 新 %d 条（%d ms）%n",
                        attempt, oldList.size(), oldMs, newList.size(), newMs);
                boolean sameSize = oldList.size() == newList.size();
                boolean sameContent = sameSize
                        && CanonicalJson.of(JsonUtils.json().toJson(oldList)).equals(CanonicalJson.of(JsonUtils.json().toJson(newList)));
                if (sameContent) {
                    report.ok("ru ladder 全量一致（" + newList.size() + " 条）");
                    return;
                }
                if (attempt == 1) {
                    System.out.println("  [..] 不一致（旧实现有吞异常截断的历史行为），重试一轮");
                }
            }
            report.fail("ru ladder 全量不一致（重试后仍不同）");
        } catch (Exception e) {
            report.fail("ru ladder 全量：异常 " + e);
        }
    }

    private static void developers(TestReport report, WowsServer server, HttpClient client) {
        String label = server.getCode();
        Long accountId = TestConfig.accountIdOrNull(server);
        Long clanId = TestConfig.clanIdOrNull(server);
        if (accountId == null) {
            return;
        }
        String appId = TestConfig.appId(server);
        report.section("在线 · " + label + " dev 军团（玩家 " + accountId + "，军团 " + clanId + "）");
        WowsHttpClanTools oldClan = new WowsHttpClanTools(client, server);
        WowsEncyclopediaTools oldEncyclopedia = new WowsEncyclopediaTools(client, server);
        WowsApi api = WowsApi.of(server).credentials(c -> c.applicationId(appId));

        report.compare(label + " dev.accountClan（所属军团）",
                () -> JsonUtils.json().toJson(oldClan.developers(appId).userSearchClanDevelopers(accountId)),
                () -> JsonUtils.json().toJson(api.developers().accountClan(accountId)));
        if (clanId != null) {
            report.compare(label + " dev.clanInfo（军团详情）",
                    () -> JsonUtils.json().toJson(oldClan.developers(appId).clanInfoDevelopers(clanId)),
                    () -> JsonUtils.json().toJson(api.developers().clanInfo(clanId)));
        }
        report.compareAllowBothFail(label + " dev.clanSearch（军团搜索）",
                () -> JsonUtils.json().toJson(oldClan.developers(appId).searchClanDevelopers(TestConfig.SEARCH_CLAN)),
                () -> JsonUtils.json().toJson(api.developers().clanSearch(TestConfig.SEARCH_CLAN)));
        report.compareAllowBothFail(label + " dev.clanSeason（赛季）",
                () -> JsonUtils.json().toJson(oldClan.developers(appId).season()),
                () -> JsonUtils.json().toJson(api.developers().clanSeason()));
        report.compare(label + " dev.glossary（军团词典）",
                () -> JsonUtils.json().toJson(oldEncyclopedia.developers(appId).glossary()),
                () -> JsonUtils.json().toJson(api.developers().glossary()));
    }

    // ================= 辅助 =================

    private static String ladderJson(ClanRankInfo... infos) {
        return JsonUtils.json().toJson(List.of(infos));
    }

    private static String sortedIds(List<ClanRankInfo> list) {
        return list.stream().map(ClanRankInfo::id).sorted().toList().toString();
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
