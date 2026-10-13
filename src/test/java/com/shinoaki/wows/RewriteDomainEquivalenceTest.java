package com.shinoaki.wows;

import com.shinoaki.wows.api.WowsApi;
import com.shinoaki.wows.api.auth.AccessToken;
import com.shinoaki.wows.api.auth.ShareToken;
import com.shinoaki.wows.api.data.ship.ShipExpansion;
import com.shinoaki.wows.api.developers.DevelopersUserShip;
import com.shinoaki.wows.api.developers.DevelopersUserShipOper;
import com.shinoaki.wows.api.developers.clan.DevelopersSearchClan;
import com.shinoaki.wows.api.developers.clan.seasion.DevelopersSeasonInfo;
import com.shinoaki.wows.api.developers.encyclopedia.glossary.DevelopersGlossary;
import com.shinoaki.wows.api.endpoint.DevelopersEndpoints;
import com.shinoaki.wows.api.error.HttpStatusException;
import com.shinoaki.wows.api.error.UnsupportedOnServerException;
import com.shinoaki.wows.api.monitor.SchemaRecorder;
import com.shinoaki.wows.api.server.ServerProfile;
import com.shinoaki.wows.api.type.WowsBattlesType;
import com.shinoaki.wows.api.type.WowsServer;
import com.shinoaki.wows.api.utils.JsonUtils;
import com.shinoaki.wows.api.vortex.VortexResourcesLanguage;
import com.shinoaki.wows.api.vortex.VortexUserShip;
import com.shinoaki.wows.api.vortex.clan.VortexSearchClan;
import com.shinoaki.wows.api.vortex.clan.account.VortexSearchClanUser;
import com.shinoaki.wows.api.vortex.clan.base.VortexClanInfo;
import com.shinoaki.wows.api.vortex.clan.members.VortexClanUserInfo;
import com.shinoaki.wows.api.vortex.clan.rank.ClanRankInfo;
import com.shinoaki.wows.api.vortex.resources.RequestVortexResourcesInfo;
import com.shinoaki.wows.api.vortex.resources.WowsResources;
import com.shinoaki.wows.api.vortex.resources.box.WowsBox;
import com.shinoaki.wows.api.vortex.resources.box.WowsLootBox;
import com.shinoaki.wows.testkit.TestConfig;
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
 * 3.0 重写 Phase 2：战舰 / 军团 / 资源·词典 / 军团排行「新旧等价」离线自检。
 * <p>
 * 全部请求打到本地假上游（JDK 自带 {@code com.sun.net.httpserver}），<b>不打任何真实服务</b>：
 * <ul>
 *     <li>假上游按路径 / 查询参数 / 请求体回放 {@code target/schema-raw} 的真实响应；</li>
 *     <li>断言新管线请求 = 旧实现的 URL/参数语义（含莱服前缀、graphql POST 请求体逐字节、
 *     {@code in_garage} 跟随 access_token）；</li>
 *     <li>断言解析结果与旧静态解析逐字节等价；覆盖 404→空（CN）、RU 徽章拒绝、战斗类型不支持、
 *     排行分页游标与 {@code season<=0}（省略参数）修正。</li>
 * </ul>
 * 需要先跑过 {@code SchemaMonitorTest}（或已有 raw 仓库）。
 *
 * @author Xun
 */
public class RewriteDomainEquivalenceTest {

    private static final Path RAW = Path.of("target/schema-raw");
    private static final long ACCOUNT_ASIA = TestConfig.ACCOUNT_ASIA;
    private static final long ACCOUNT_RU = TestConfig.ACCOUNT_RU;
    private static final long CLAN_ASIA = 2000016057L;
    private static final long CLAN_MEMBERS_ASIA = TestConfig.CLAN_MEMBERS_ASIA;
    private static final long CLAN_RU = TestConfig.CLAN_RU;
    private static final long LOOTBOX_ASIA = TestConfig.LOOTBOX_ASIA;
    private static final long LOOTBOX_RU = TestConfig.LOOTBOX_RU;
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
        Map<String, String> dumps = new HashMap<>();
        dumps.put("asiaShipsPvp", read("vortex_asia_api_accounts_id_ships_pvp_.json"));
        dumps.put("ruShipsRating", read("vortex_ru_api_accounts_id_ships_rating_solo_.json"));
        dumps.put("ruClanBase", read("clans_ru_api_clanbase_id_claninfo_.json"));
        dumps.put("asiaMembers", read("clans_asia_api_members_id_battle_type_pvp.json"));
        dumps.put("asiaClanSearch", read("clans_asia_api_search_autocomplete_search_text_type_clans.json"));
        dumps.put("vortexAsiaClan", read("vortex_asia_api_accounts_id_clans_.json"));
        dumps.put("lootboxAsia", read("vortex_asia_api_get_lootbox_lang_id_.json"));
        dumps.put("lootboxRu", read("vortex_ru_api_get_lootbox_lang_id_.json"));
        dumps.put("glossRu", read("vortex_ru_api_graphql_glossary_getGlossData.json"));
        dumps.put("devAsiaClanList", read("api_asia_wows_clans_list_search_text_.json"));
        dumps.put("devAsiaSeason", read("api_asia_wows_clans_season_.json"));
        dumps.put("devRuGlossary", read("api_ru_mk_clans_glossary_.json"));
        dumps.put("asiaStats", read("api_asia_wows_ships_stats_account_id_id_extra_pvp_div2_pvp_div3__4c0b4c30.json"));
        dumps.put("asiaBadges", read("api_asia_wows_ships_badges_account_id_id_.json"));

        // ---- 排行合成数据（无真实 dump；断 URL 与分页游标语义） ----
        String ladderPage1 = ladderJson(
                new ClanRankInfo(false, 1, "sg", 30, "测试军团A", 1, 100, 90, 1, 50, "#FF0000", "", "sg", "", 35, 11L, 1, "0x1", "AAA"),
                new ClanRankInfo(false, 2, "sg", 25, "测试军团B", 1, 95, 88, 1, 40, "#00FF00", "", "sg", "", 35, 12L, 1, "0x2", "BBB"));
        String ladderPage2 = ladderJson(
                new ClanRankInfo(false, 3, "sg", 20, "测试军团C", 2, 90, 80, 2, 30, "#0000FF", "", "sg", "", 35, 13L, 1, "0x3", "CCC"));

        // ---- 假上游：vortex / clans / api 三个主机各起一个服务器（打错主机 = 404，可被测试抓住） ----
        Responses vortex = new Responses();
        Responses clans = new Responses();
        Responses api = new Responses();
        vortex.exact("/api/accounts/" + ACCOUNT_ASIA + "/ships/pvp/", dumps.get("asiaShipsPvp"));
        vortex.exact("/api/accounts/" + ACCOUNT_RU + "/ships/rating_solo/", dumps.get("ruShipsRating"));
        vortex.exact("/api/accounts/" + ACCOUNT_ASIA + "/clans/", dumps.get("vortexAsiaClan"));
        vortex.exact("/api/get_lootbox/zh-sg/" + LOOTBOX_ASIA + "/", dumps.get("lootboxAsia"));
        vortex.exact("/api/get_lootbox/ru/" + LOOTBOX_RU + "/", dumps.get("lootboxRu"));
        vortex.graphql("getGlossData", "ru", dumps.get("glossRu"));
        vortex.graphql("getGlossData", "zh-sg", dumps.get("glossRu"));
        vortex.graphql("Lootbox", "zh-sg", read("vortex_asia_api_graphql_glossary_Lootbox.json"));
        vortex.graphql("Lootbox", "ru", read("vortex_ru_api_graphql_glossary_Lootbox.json"));
        vortex.graphql("Items", "zh-sg", read("vortex_asia_api_graphql_glossary_Items.json"));
        vortex.graphql("Items", "ru", read("vortex_ru_api_graphql_glossary_Items.json"));

        clans.exact("/api/clanbase/" + CLAN_RU + "/claninfo/", dumps.get("ruClanBase"));
        clans.exact("/api/members/" + CLAN_MEMBERS_ASIA + "/", dumps.get("asiaMembers"));
        clans.exact("/api/search/autocomplete/", dumps.get("asiaClanSearch"));
        // 排行：第一页 + 游标页（clan_id=12 → 第二页；clan_id=13 → 空页终止）
        clans.exact("/api/ladder/structure/", ladderPage1);
        clans.perId("/api/ladder/structure/", 12, ladderPage2);
        clans.perId("/api/ladder/structure/", 13, "[]");

        api.exact("/wows/clans/list/", dumps.get("devAsiaClanList"));
        api.exact("/wows/clans/season/", dumps.get("devAsiaSeason"));
        api.exact("/wows/clans/glossary/", read("api_asia_wows_clans_glossary_.json"));
        api.exact("/mk/clans/glossary/", dumps.get("devRuGlossary"));
        api.perExtra("/wows/ships/stats/", DevelopersEndpoints.statsExtra(), dumps.get("asiaStats"));
        api.perExtra("/wows/ships/stats/", WowsBattlesType.DEVELOPERS_ALL_STATS_EXTRA, read("api_asia_wows_ships_stats_account_id_id_extra_clan_oper_div_oper_bae61ea0.json"));
        api.perExtra("/mk/ships/stats/", DevelopersEndpoints.statsExtra(), read("api_ru_mk_ships_stats_account_id_id_extra_pvp_div2_pvp_div3_pvp__4f49fd3.json"));
        api.perExtra("/mk/ships/stats/", WowsBattlesType.DEVELOPERS_ALL_STATS_EXTRA, read("api_ru_mk_ships_stats_account_id_id_extra_clan_oper_div_oper_div_2783b05d.json"));
        api.exact("/wows/ships/badges/", dumps.get("asiaBadges"));

        RequestLog log = new RequestLog();
        HttpServer vortexServer = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        HttpServer clansServer = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        HttpServer apiServer = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        vortexServer.createContext("/", exchange -> handle(exchange, vortex, log));
        clansServer.createContext("/", exchange -> handle(exchange, clans, log));
        apiServer.createContext("/", exchange -> handle(exchange, api, log));
        vortexServer.start();
        clansServer.start();
        apiServer.start();
        String vortexBase = "http://127.0.0.1:" + vortexServer.getAddress().getPort();
        String clansBase = "http://127.0.0.1:" + clansServer.getAddress().getPort();
        String apiBase = "http://127.0.0.1:" + apiServer.getAddress().getPort();
        try {
            run(vortexBase, clansBase, apiBase, log, dumps);
        } finally {
            vortexServer.stop(0);
            clansServer.stop(0);
            apiServer.stop(0);
        }

        System.out.println();
        System.out.printf("── 汇总 ── 通过 %d，失败 %d%n", passed, failed);
        System.exit(failed == 0 ? 0 : 1);
    }

    private static void run(String vortexBase, String clansBase, String apiBase, RequestLog log, Map<String, String> dumps) throws Exception {
        ServerProfile asia = ServerProfile.of(WowsServer.ASIA).withHosts(vortexBase, clansBase, apiBase);
        ServerProfile ru = ServerProfile.of(WowsServer.RU).withHosts(vortexBase, clansBase, apiBase);
        ServerProfile cn = ServerProfile.of(WowsServer.CN).withHosts(vortexBase, clansBase, apiBase);
        Function<Object, String> toJson = JsonUtils.json()::toJson;
        var json = JsonUtils.json();

        // ================= A. 战舰（vortex + dev） =================
        section("A. 战舰");
        {
            WowsApi api = WowsApi.of(asia);
            VortexUserShip actual = api.vortex().ships(ACCOUNT_ASIA, WowsBattlesType.PVP);
            expect("A1 vortex 路径", "/api/accounts/" + ACCOUNT_ASIA + "/ships/pvp/", log.last().rawPath());
            expectQuery("A1 无查询参数", Map.of(), log.last().query());
            //recordTime 是解析时刻（System.currentTimeMillis），两次解析必然不同——对比时剔除
            expect("A1 结果=旧解析（忽略 recordTime）",
                    withoutRecordTime(VortexUserShip.parse(WowsBattlesType.PVP, json.parse(dumps.get("asiaShipsPvp")))),
                    withoutRecordTime(actual));
        }
        {
            WowsApi api = WowsApi.of(ru).credentials(c -> c.shareToken(ACCOUNT_RU, TOKEN));
            api.vortex().ships(ACCOUNT_RU, WowsBattlesType.RATING_SOLO);
            expect("A2 莱服专属类型路径", "/api/accounts/" + ACCOUNT_RU + "/ships/rating_solo/", log.last().rawPath());
            expectQuery("A2 share token 注入", Map.of("ac", TOKEN), log.last().query());
        }
        {
            WowsApi api = WowsApi.of(asia);
            log.clear();
            expectThrows("A3 非支持服战斗类型拒绝", UnsupportedOnServerException.class,
                    () -> api.vortex().ships(ACCOUNT_ASIA, WowsBattlesType.BRAWL));
            expect("A3 零网络请求", "0", String.valueOf(log.size()));
        }
        {
            WowsApi api = WowsApi.of(asia).credentials(c -> c.applicationId(APP));
            DevelopersUserShip actual = api.developers().ships(ACCOUNT_ASIA);
            Recorded req = log.last();
            expect("A4 dev 统计路径", "/wows/ships/stats/", req.rawPath());
            expectQuery("A4 查询参数（extra=历史集合、无 in_garage）",
                    Map.of("application_id", APP, "account_id", String.valueOf(ACCOUNT_ASIA), "extra", DevelopersEndpoints.statsExtra()),
                    req.query());
            expect("A4 结果=旧解析",
                    toJson.apply(DevelopersUserShip.parse(json.parse(dumps.get("asiaStats")))),
                    toJson.apply(actual));
        }
        {
            WowsApi api = WowsApi.of(asia).credentials(c -> c.applicationId(APP));
            api.developers().ships(ACCOUNT_ASIA, AccessToken.of(ACCOUNT_ASIA, "AT"));
            Map<String, String> query = log.last().query();
            expect("A5 access_token 注入", "AT", query.get("access_token"));
            expect("A5 in_garage 跟随 access_token", "1", query.get("in_garage"));
        }
        {
            WowsApi api = WowsApi.of(ru).credentials(c -> c.applicationId(APP));
            DevelopersUserShipOper actual = api.developers().shipsOper(ACCOUNT_RU);
            expect("A6 RU 全量战绩前缀=/mk/", "/mk/ships/stats/", log.last().rawPath());
            expect("A6 extra=全量串", WowsBattlesType.DEVELOPERS_ALL_STATS_EXTRA, log.last().query().get("extra"));
            expect("A6 结果非空（oper 块）", "true", String.valueOf(!actual.infoList().isEmpty()));
        }
        {
            WowsApi api = WowsApi.of(asia).credentials(c -> c.applicationId(APP));
            Map<Long, ShipExpansion> actual = api.developers().shipBadges(ACCOUNT_ASIA);
            expect("A7 徽章路径", "/wows/ships/badges/", log.last().rawPath());
            expect("A7 结果=旧解析",
                    toJson.apply(ShipExpansion.parseDevelopers(ACCOUNT_ASIA, json.parse(dumps.get("asiaBadges")))),
                    toJson.apply(actual));
        }
        {
            WowsApi api = WowsApi.of(ru).credentials(c -> c.applicationId(APP));
            log.clear();
            expectThrows("A8 莱服徽章显式拒绝", UnsupportedOnServerException.class,
                    () -> api.developers().shipBadges(ACCOUNT_RU));
            expect("A8 零网络请求", "0", String.valueOf(log.size()));
        }

        // ================= B. 军团（vortex clans） =================
        section("B. 军团 vortex");
        {
            WowsApi api = WowsApi.of(asia);
            VortexSearchClanUser actual = api.clans().accountClan(ACCOUNT_ASIA);
            expect("B1 所属军团路径", "/api/accounts/" + ACCOUNT_ASIA + "/clans/", log.last().rawPath());
            expect("B1 结果=旧解析",
                    toJson.apply(VortexSearchClanUser.to(json.parse(dumps.get("vortexAsiaClan")))),
                    toJson.apply(actual));
        }
        {
            // CN（非 API 服）404 → 空对象（旧语义保留）；用未注册账号让假上游返回 404
            WowsApi api = WowsApi.of(cn);
            log.clear();
            VortexSearchClanUser actual = api.clans().accountClan(999999L);
            expect("B2 CN 404 → 空对象",
                    toJson.apply(new VortexSearchClanUser("", VortexSearchClanUser.VortexSearchClanInfo.empty(), "", 0)),
                    toJson.apply(actual));
            expect("B2 请求数=1", "1", String.valueOf(log.size()));
        }
        {
            // 非 CN 的 404 仍抛异常（未注册路径 = 404）
            WowsApi api = WowsApi.of(asia);
            expectThrows("B3 非 CN 404 抛 HttpStatusException", HttpStatusException.class,
                    () -> api.clans().accountClan(999999L));
        }
        {
            WowsApi api = WowsApi.of(ru);
            VortexClanInfo actual = api.clans().clanInfo(CLAN_RU);
            expect("B4 军团详情路径", "/api/clanbase/" + CLAN_RU + "/claninfo/", log.last().rawPath());
            expect("B4 服务器落库=RU", WowsServer.RU, actual.wowsServer());
            expect("B4 结果=旧解析（含 mk_ladder 区分）",
                    toJson.apply(VortexClanInfo.to(WowsServer.RU, CLAN_RU, json.parse(dumps.get("ruClanBase")))),
                    toJson.apply(actual));
        }
        {
            WowsApi api = WowsApi.of(asia);
            var actual = api.clans().members(CLAN_MEMBERS_ASIA);
            expect("B5 成员路径", "/api/members/" + CLAN_MEMBERS_ASIA + "/", log.last().rawPath());
            expectQuery("B5 默认 battle_type=pvp", Map.of("battle_type", "pvp"), log.last().query());
            expect("B5 结果=旧解析",
                    toJson.apply(VortexClanUserInfo.to(WowsServer.ASIA, json.parse(dumps.get("asiaMembers")))),
                    toJson.apply(actual));
        }
        {
            WowsApi api = WowsApi.of(asia);
            api.clans().members(CLAN_MEMBERS_ASIA, "cvc", 35);
            expectQuery("B6 cvc + season 参数", Map.of("battle_type", "cvc", "season", "35"), log.last().query());
        }
        {
            WowsApi api = WowsApi.of(asia);
            List<VortexSearchClan> actual = api.clans().search("YU_RI");
            expect("B7 军团搜索路径", "/api/search/autocomplete/", log.last().rawPath());
            expectQuery("B7 查询参数", Map.of("search", "YU_RI", "type", "clans"), log.last().query());
            expect("B7 结果=旧解析",
                    toJson.apply(VortexSearchClan.parse(json.parse(dumps.get("asiaClanSearch")))),
                    toJson.apply(actual));
        }

        // ================= C. 军团排行（分页游标 / realm / season） =================
        section("C. 军团排行");
        {
            WowsApi api = WowsApi.of(asia);
            log.clear();
            List<ClanRankInfo> actual = api.clans().ladder(35);
            expect("C1 请求数=3（首页+游标页+空页）", "3", String.valueOf(log.size()));
            expect("C1 第1页查询串", "?realm=sg&season=35", log.get(0).rawQuery());
            expect("C1 第2页（游标）", "?realm=sg&season=35&clan_id=12", log.get(1).rawQuery());
            expect("C1 第3页（空页终止）", "?realm=sg&season=35&clan_id=13", log.get(2).rawQuery());
            expect("C1 结果 id 集合", "[11, 12, 13]", sortedIds(actual));
        }
        {
            // season<=0：不传 season（旧实现发空值被服务端 422 → 静默空表；3.0 修正为省略参数）
            WowsApi api = WowsApi.of(ru);
            log.clear();
            List<ClanRankInfo> actual = api.clans().ladder(0);
            expect("C2 最新赛季不传 season", "?realm=global", log.get(0).rawQuery());
            expect("C2 结果 id 集合", "[11, 12, 13]", sortedIds(actual));
        }
        {
            WowsApi api = WowsApi.of(ru);
            api.clans().ladderPage(28, null);
            expect("C3 单页带赛季", "?realm=global&season=28", log.last().rawQuery());
        }

        // ================= D. 资源/箱子/词典（graphql POST） =================
        section("D. 资源/箱子/词典");
        {
            WowsApi api = WowsApi.of(asia);
            WowsLootBox actual = api.resources().lootbox(LOOTBOX_ASIA);
            expect("D1 箱子路径（默认 zh-sg）", "/api/get_lootbox/zh-sg/" + LOOTBOX_ASIA + "/", log.last().rawPath());
            expect("D1 结果=旧解析",
                    toJson.apply(WowsLootBox.parse(json.parse(dumps.get("lootboxAsia")))),
                    toJson.apply(actual));
        }
        {
            // 莱服：语言被固定为 ru（即使传 SG）
            WowsApi api = WowsApi.of(ru);
            WowsLootBox actual = api.resources().lootbox(LOOTBOX_RU, VortexResourcesLanguage.SG);
            expect("D2 莱服语言固定 ru", "/api/get_lootbox/ru/" + LOOTBOX_RU + "/", log.last().rawPath());
            expect("D2 结果=旧解析（RU 独有字段）",
                    toJson.apply(WowsLootBox.parse(json.parse(dumps.get("lootboxRu")))),
                    toJson.apply(actual));
        }
        {
            WowsApi api = WowsApi.of(asia);
            List<WowsBox> actual = api.resources().lootboxes(VortexResourcesLanguage.SG);
            Recorded req = log.last();
            expect("D3 箱子列表 POST", "POST", req.method());
            expect("D3 路径", "/api/graphql/glossary/", req.rawPath());
            expect("D3 Content-Type", "application/json", req.headers().getFirst("Content-Type"));
            expect("D3 请求体=旧模板逐字节", WowsBox.requestBody("zh-sg"), req.body());
            expect("D3 结果非空", "true", String.valueOf(!actual.isEmpty()));
        }
        {
            WowsApi api = WowsApi.of(ru);
            List<WowsResources> actual = api.resources().items(VortexResourcesLanguage.RU);
            expect("D4 Items 请求体", WowsResources.requestBody("ru"), log.last().body());
            expect("D4 结果非空", "true", String.valueOf(!actual.isEmpty()));
        }
        {
            WowsApi api = WowsApi.of(ru);
            RequestVortexResourcesInfo actual = api.resources().glossary(VortexResourcesLanguage.RU);
            expect("D5 getGlossData 请求体", RequestVortexResourcesInfo.requestBody("ru"), log.last().body());
            expect("D5 结果=旧解析",
                    toJson.apply(RequestVortexResourcesInfo.parse(json.parse(dumps.get("glossRu")), "ru")),
                    toJson.apply(actual));
        }
        {
            // PT：仅 vortex 主机切换（不联网，只验证档案）
            ServerProfile pt = ServerProfile.of(WowsServer.ASIA).withPtServer();
            expect("D6 PT 切换 vortex 主机", ServerProfile.of(WowsServer.ASIA).vortexPtHost(), pt.vortexHost());
            expect("D6 PT 不动 clans 主机", ServerProfile.of(WowsServer.ASIA).clansHost(), pt.clansHost());
        }

        // ================= E. dev 军团/词典 + 监控键 =================
        section("E. dev 军团/词典与监控键");
        {
            WowsApi api = WowsApi.of(asia).credentials(c -> c.applicationId(APP));
            List<DevelopersSearchClan> actual = api.developers().clanSearch("YU_RI");
            expect("E1 军团搜索路径", "/wows/clans/list/", log.last().rawPath());
            expectQuery("E1 查询参数", Map.of("application_id", APP, "search", "YU_RI"), log.last().query());
            expect("E1 结果=旧解析",
                    toJson.apply(DevelopersSearchClan.parse(json.parse(dumps.get("devAsiaClanList")))),
                    toJson.apply(actual));
        }
        {
            WowsApi api = WowsApi.of(asia).credentials(c -> c.applicationId(APP));
            List<DevelopersSeasonInfo> actual = api.developers().clanSeason();
            expectQuery("E2 season 默认语言 zh-cn", Map.of("application_id", APP, "language", "zh-cn"), log.last().query());
            expect("E2 结果=旧解析",
                    toJson.apply(DevelopersSeasonInfo.parse(json.parse(dumps.get("devAsiaSeason")))),
                    toJson.apply(actual));
        }
        {
            WowsApi api = WowsApi.of(ru).credentials(c -> c.applicationId(APP));
            DevelopersGlossary actual = api.developers().glossary();
            expect("E3 词典前缀 /mk/", "/mk/clans/glossary/", log.last().rawPath());
            expect("E3 结果=旧解析",
                    toJson.apply(DevelopersGlossary.parse(json.parse(dumps.get("devRuGlossary")))),
                    toJson.apply(actual));
        }
        {
            WowsApi api = WowsApi.of(asia);
            SchemaRecorder.enable();
            try {
                api.vortex().ships(ACCOUNT_ASIA, WowsBattlesType.PVP);
            } finally {
                String key = keyOf(SchemaRecorder.drain());
                SchemaRecorder.disable();
                expect("E4 监控键=vortex.ship.list", "vortex.ship.list", key);
            }
            SchemaRecorder.enable();
            try {
                api.resources().glossary(VortexResourcesLanguage.SG);
            } finally {
                String key = keyOf(SchemaRecorder.drain());
                SchemaRecorder.disable();
                expect("E4 监控键=vortex.resources.glossary（graphql 自动区分）", "vortex.resources.glossary", key);
            }
        }
    }

    private static String keyOf(List<SchemaRecorder.Record> records) {
        return records.isEmpty() ? "(空)" : records.getFirst().schemaKey();
    }

    /**
     * 序列化并剔除顶层 {@code recordTime}（解析时刻字段，两次解析必然不同）。
     */
    private static String withoutRecordTime(Object value) {
        tools.jackson.databind.node.ObjectNode node =
                (tools.jackson.databind.node.ObjectNode) JsonUtils.json().parse(JsonUtils.json().toJson(value));
        node.remove("recordTime");
        return JsonUtils.json().toJson(node);
    }

    private static String sortedIds(List<ClanRankInfo> list) {
        return list.stream().map(ClanRankInfo::id).sorted().toList().toString();
    }

    private static String ladderJson(ClanRankInfo... infos) {
        return JsonUtils.json().toJson(List.of(infos));
    }

    // ================= 假上游实现 =================

    private static void handle(HttpExchange exchange, Responses responses, RequestLog log) throws IOException {
        String rawPath = exchange.getRequestURI().getRawPath();
        Map<String, String> query = parseQuery(exchange.getRequestURI().getRawQuery());
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        log.add(new Recorded(exchange.getRequestMethod(), rawPath, query, exchange.getRequestHeaders(), body));
        Canned canned = responses.find(rawPath, query, body);
        if (canned == null) {
            exchange.sendResponseHeaders(404, -1);
            exchange.close();
            return;
        }
        byte[] bytes = canned.body() == null ? new byte[0] : canned.body().getBytes(StandardCharsets.UTF_8);
        if (canned.gzip()) {
            bytes = gzip(bytes);
            exchange.getResponseHeaders().set("Content-Encoding", "gzip");
        }
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        if (canned.status() != 200) {
            exchange.sendResponseHeaders(canned.status(), -1);
            exchange.close();
            return;
        }
        exchange.sendResponseHeaders(200, bytes.length);
        exchange.getResponseBody().write(bytes);
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

    private record Recorded(String method, String rawPath, Map<String, String> query, Headers headers, String body) {

        /**
         * 原始查询串（按请求顺序重建；与真实发送的查询串等值，编码值保持原样）。
         */
        String rawQuery() {
            if (query.isEmpty()) {
                return "";
            }
            StringBuilder sb = new StringBuilder("?");
            boolean first = true;
            for (var e : query.entrySet()) {
                if (!first) {
                    sb.append('&');
                }
                first = false;
                sb.append(e.getKey()).append('=').append(e.getValue());
            }
            return sb.toString();
        }
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
        private final Map<String, Canned> perExtra = new HashMap<>();
        private final Map<String, Canned> graphql = new HashMap<>();

        void exact(String path, String body) {
            exact.put(path, new Canned(body, 200, false));
        }

        void perId(String path, long id, String body) {
            perId.put(path + "#" + id, new Canned(body, 200, false));
        }

        void perExtra(String path, String extra, String body) {
            perExtra.put(path + "#" + extra, new Canned(body, 200, false));
        }

        void graphql(String schema, String language, String body) {
            graphql.put(schema + "|" + language, new Canned(body, 200, false));
        }

        Canned find(String rawPath, Map<String, String> query, String body) {
            if (rawPath.equals("/api/graphql/glossary/")) {
                //graphql：一个 URL 多个 schema（按请求体区分）+ 按语言区分服务器
                String schema = body.contains("getGlossData") ? "getGlossData" : body.contains("Lootbox") ? "Lootbox" : "Items";
                String language = body.contains("\"zh-sg\"") ? "zh-sg" : "ru";
                return graphql.get(schema + "|" + language);
            }
            String extra = query.get("extra");
            if (extra != null) {
                Canned canned = perExtra.get(rawPath + "#" + extra);
                if (canned != null) {
                    return canned;
                }
            }
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
