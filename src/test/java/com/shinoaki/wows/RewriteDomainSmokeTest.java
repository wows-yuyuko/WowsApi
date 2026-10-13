package com.shinoaki.wows;

import com.shinoaki.wows.api.WowsApi;
import com.shinoaki.wows.api.codec.ClanRankUtils;
import com.shinoaki.wows.api.codec.HttpCodec;
import com.shinoaki.wows.api.codec.http.WowsEncyclopediaTools;
import com.shinoaki.wows.api.codec.http.WowsHttpClanTools;
import com.shinoaki.wows.api.codec.http.WowsHttpShipTools;
import com.shinoaki.wows.api.error.UnsupportedOnServerException;
import com.shinoaki.wows.api.type.WowsBattlesType;
import com.shinoaki.wows.api.type.WowsServer;
import com.shinoaki.wows.api.utils.JsonUtils;
import com.shinoaki.wows.api.vortex.VortexResourcesLanguage;
import com.shinoaki.wows.api.vortex.clan.rank.ClanRankInfo;
import com.shinoaki.wows.api.vortex.resources.RequestVortexResourcesInfo;
import com.shinoaki.wows.api.vortex.resources.WowsResources;
import com.shinoaki.wows.api.vortex.resources.box.WowsBox;
import com.shinoaki.wows.api.vortex.resources.box.WowsLootBox;
import com.shinoaki.wows.testkit.TestConfig;
import tools.jackson.core.type.TypeReference;

import java.net.URI;
import java.net.http.HttpClient;
import java.util.List;

/**
 * 3.0 重写 Phase 2：战舰 / 军团 / 资源·词典 / 军团排行「新旧等价」在线烟测（打真实服务）。
 * <p>
 * 对 ASIA / RU（+ CN 覆盖所在处）逐端点执行旧实现与新实现各一次，把两侧结果序列化为 JSON 对比。
 * 军团排行：单页与游标页做新旧对比（旧侧按 {@code ClanRankUtils} 的请求语句原样复刻）；
 * 传入 {@code --full} 追加「整季全量拉取」的新旧对比（RU，内部分页，较重）。
 * <p>
 * 负载较重（多组 MB 级响应双份下载），可传 {@code --only=asia} / {@code --only=ru} / {@code --only=cn}
 * 分块运行。
 * <p>
 * <b>代理</b>：实测本机直连 WG 主机偶发抖动（vortex 直连单次 13s+，逼近超时；本地代理稳定 &lt;2s），
 * 建议带 {@code -Dwows.proxy=127.0.0.1:7890} 运行（按服绕开：{@code -Dwows.proxy.cn=direct}）。
 * <p>
 * 令牌 / 账号 / 代理均由 {@link TestConfig} 提供（零输入可跑）。
 *
 * @author Xun
 */
public class RewriteDomainSmokeTest {

    private static int passed;
    private static int failed;

    public static void main(String[] args) throws Exception {
        boolean full = false;
        String only = null;
        for (String arg : args) {
            if (arg.equalsIgnoreCase("--full")) {
                full = true;
            } else if (arg.toLowerCase().startsWith("--only=")) {
                only = arg.substring("--only=".length()).toLowerCase();
            }
        }
        HttpClient client = TestConfig.client();
        //排行榜等大响应给足超时；共享传输配置，新旧两侧同时生效
        WowsApi.of(WowsServer.ASIA).timeout(20);

        for (WowsServer server : List.of(WowsServer.ASIA, WowsServer.RU)) {
            if (only != null && !only.equals(server.getCode())) {
                continue;
            }
            ships(server, client);
            clans(server, client);
            ladder(server, client);
            resources(server, client);
            developers(server, client);
        }
        if (only == null || only.equals("cn")) {
            shipsCn(client);
            clansCn(client);
            ladderCn(client);
            cnDevUnsupported();
        }
        if (full) {
            ladderFull(client);
        }

        System.out.println();
        System.out.printf("── 汇总 ── 通过 %d，失败 %d%n", passed, failed);
        System.exit(failed == 0 ? 0 : 1);
    }

    // ================= 战舰 =================

    private static void ships(WowsServer server, HttpClient client) {
        String label = server.getCode();
        Long accountId = TestConfig.accountIdOrNull(server);
        if (accountId == null) {
            return;
        }
        System.out.println();
        System.out.println("== " + label + " 战舰（玩家 " + accountId + "） ==");
        WowsHttpShipTools old = new WowsHttpShipTools(client, server, accountId);
        WowsApi api = WowsApi.of(server);
        compare(label + " vortex.ships（pvp）",
                () -> JsonUtils.json().toJson(old.vortex().shipList(WowsBattlesType.PVP)),
                () -> JsonUtils.json().toJson(api.vortex().ships(accountId, WowsBattlesType.PVP)));
        if (server == WowsServer.RU) {
            compare(label + " vortex.ships（rating_solo）",
                    () -> JsonUtils.json().toJson(old.vortex().shipList(WowsBattlesType.RATING_SOLO)),
                    () -> JsonUtils.json().toJson(api.vortex().ships(accountId, WowsBattlesType.RATING_SOLO)));
        }
    }

    private static void shipsCn(HttpClient client) {
        long accountId = TestConfig.ACCOUNT_CN;
        String token = TestConfig.vortexToken(accountId);
        System.out.println();
        System.out.println("== cn 战舰（玩家 " + accountId + "，带分享凭证——无凭证时服务端按隐藏战绩处理） ==");
        WowsHttpShipTools old = new WowsHttpShipTools(client, WowsServer.CN, accountId);
        WowsApi api = WowsApi.of(WowsServer.CN).credentials(c -> c.shareToken(accountId, token));
        compare("cn vortex.ships（pvp）",
                () -> JsonUtils.json().toJson(old.vortex().shipList(WowsBattlesType.PVP, token)),
                () -> JsonUtils.json().toJson(api.vortex().ships(accountId, WowsBattlesType.PVP)));
    }

    // ================= 军团 =================

    private static void clans(WowsServer server, HttpClient client) {
        String label = server.getCode();
        Long accountId = TestConfig.accountIdOrNull(server);
        Long clanId = TestConfig.clanIdOrNull(server);
        if (accountId == null || clanId == null) {
            return;
        }
        System.out.println();
        System.out.println("== " + label + " 军团 vortex（军团 " + clanId + "） ==");
        WowsHttpClanTools old = new WowsHttpClanTools(client, server);
        WowsApi api = WowsApi.of(server);
        compare(label + " clans.account.clan（所属军团）",
                () -> JsonUtils.json().toJson(old.vortex().userSearchClanVortex(accountId)),
                () -> JsonUtils.json().toJson(api.clans().accountClan(accountId)));
        compare(label + " clans.info（详情）",
                () -> JsonUtils.json().toJson(old.vortex().clanInfoVortex(clanId)),
                () -> JsonUtils.json().toJson(api.clans().clanInfo(clanId)));
        compare(label + " clans.search（搜索）",
                () -> JsonUtils.json().toJson(old.vortex().searchClanVortex(TestConfig.SEARCH_CLAN)),
                () -> JsonUtils.json().toJson(api.clans().search(TestConfig.SEARCH_CLAN)));
        if (server == WowsServer.ASIA) {
            long membersId = TestConfig.CLAN_MEMBERS_ASIA;
            compare(label + " clans.members（成员）",
                    () -> JsonUtils.json().toJson(old.vortex().clanUserListInfoVortex(membersId)),
                    () -> JsonUtils.json().toJson(api.clans().members(membersId)));
        }
    }

    private static void clansCn(HttpClient client) {
        long clanId = TestConfig.CLAN_CN;
        System.out.println();
        System.out.println("== cn 军团 vortex（军团 " + clanId + "） ==");
        WowsHttpClanTools old = new WowsHttpClanTools(client, WowsServer.CN);
        WowsApi api = WowsApi.of(WowsServer.CN);
        compare("cn clans.account.clan（所属军团）",
                () -> JsonUtils.json().toJson(old.vortex().userSearchClanVortex(TestConfig.ACCOUNT_CN)),
                () -> JsonUtils.json().toJson(api.clans().accountClan(TestConfig.ACCOUNT_CN)));
        compare("cn clans.info（详情）",
                () -> JsonUtils.json().toJson(old.vortex().clanInfoVortex(clanId)),
                () -> JsonUtils.json().toJson(api.clans().clanInfo(clanId)));
    }

    // ================= 军团排行 =================

    private static void ladder(WowsServer server, HttpClient client) {
        String label = server.getCode();
        System.out.println();
        System.out.println("== " + label + " 军团排行（单页 + 游标页） ==");
        WowsApi api = WowsApi.of(server);
        try {
            List<ClanRankInfo> first = api.clans().ladderPage(0, null);
            if (first.isEmpty()) {
                fail(label + " ladder：最新赛季第一页为空（跳过对比）");
                return;
            }
            int season = first.getFirst().season_number();
            long cursor = first.getLast().id();
            compare(label + " ladder 单页（season=" + season + "）",
                    () -> oldLadderPage(client, server, season, null),
                    () -> JsonUtils.json().toJson(api.clans().ladderPage(season, null)));
            compare(label + " ladder 游标页（clan_id=" + cursor + "）",
                    () -> oldLadderPage(client, server, season, cursor),
                    () -> JsonUtils.json().toJson(api.clans().ladderPage(season, cursor)));
        } catch (Exception e) {
            fail(label + " ladder：异常 " + e);
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

    private static void ladderCn(HttpClient client) {
        System.out.println();
        System.out.println("== cn 军团排行（单页） ==");
        WowsApi api = WowsApi.of(WowsServer.CN);
        try {
            var first = api.clans().ladderPage(0, null);
            if (first.isEmpty()) {
                fail("cn ladder：第一页为空");
                return;
            }
            int season = first.getFirst().season_number();
            compare("cn ladder 单页（season=" + season + "，realm=global）",
                    () -> oldLadderPage(client, WowsServer.CN, season, null),
                    () -> JsonUtils.json().toJson(api.clans().ladderPage(season, null)));
        } catch (Exception e) {
            fail("cn ladder：异常 " + e);
        }
    }

    /**
     * 整季全量拉取对比（RU；旧=ClanRankUtils.getRanks，新=ClansClient.ladder）。较重，需 --full。
     */
    private static void ladderFull(HttpClient client) {
        WowsServer server = WowsServer.RU;
        System.out.println();
        System.out.println("== ru 军团排行（整季全量，--full） ==");
        WowsApi api = WowsApi.of(server);
        try {
            var page = api.clans().ladderPage(0, null);
            int season = page.getFirst().season_number();
            long t0 = System.currentTimeMillis();
            var oldList = new ClanRankUtils().getRanks(client, server, season);
            long oldMs = System.currentTimeMillis() - t0;
            long t1 = System.currentTimeMillis();
            var newList = api.clans().ladder(season);
            long newMs = System.currentTimeMillis() - t1;
            System.out.printf("  旧 %d 条（%d ms） vs 新 %d 条（%d ms）%n", oldList.size(), oldMs, newList.size(), newMs);
            expect("ru ladder 全量条数一致", String.valueOf(oldList.size()), String.valueOf(newList.size()));
            expect("ru ladder 全量内容一致",
                    canonical(JsonUtils.json().toJson(oldList)), canonical(JsonUtils.json().toJson(newList)));
        } catch (Exception e) {
            fail("ru ladder 全量：异常 " + e);
        }
    }

    // ================= 资源/箱子/词典 =================

    private static void resources(WowsServer server, HttpClient client) {
        String label = server.getCode();
        long lootboxId = server == WowsServer.RU ? TestConfig.LOOTBOX_RU : TestConfig.LOOTBOX_ASIA;
        VortexResourcesLanguage language = server == WowsServer.RU ? VortexResourcesLanguage.RU : VortexResourcesLanguage.SG;
        System.out.println();
        System.out.println("== " + label + " 资源/箱子/词典（语言 " + language.getLanguage() + "） ==");
        WowsApi api = WowsApi.of(server);
        compare(label + " resources.lootbox（箱子详情）",
                () -> JsonUtils.json().toJson(WowsLootBox.request(server, language, lootboxId)),
                () -> JsonUtils.json().toJson(api.resources().lootbox(lootboxId, language)));
        compare(label + " resources.lootboxes（箱子列表）",
                () -> JsonUtils.json().toJson(WowsBox.request(false, server, language)),
                () -> JsonUtils.json().toJson(api.resources().lootboxes(language)));
        compare(label + " resources.items（资源列表）",
                () -> JsonUtils.json().toJson(WowsResources.request(server, language)),
                () -> JsonUtils.json().toJson(api.resources().items(language)));
        compare(label + " resources.glossary（资源索引）",
                () -> JsonUtils.json().toJson(RequestVortexResourcesInfo.request(server, language)),
                () -> JsonUtils.json().toJson(api.resources().glossary(language)));
    }

    // ================= 官方开发者API =================

    private static void developers(WowsServer server, HttpClient client) {
        String label = server.getCode();
        Long accountId = TestConfig.accountIdOrNull(server);
        if (accountId == null) {
            return;
        }
        String appId = TestConfig.appId(server);
        System.out.println();
        System.out.println("== " + label + " 官方开发者API（战舰/军团/词典） ==");
        WowsHttpShipTools oldShip = new WowsHttpShipTools(client, server, accountId);
        WowsHttpClanTools oldClan = new WowsHttpClanTools(client, server);
        WowsEncyclopediaTools oldEncyclopedia = new WowsEncyclopediaTools(client, server);
        WowsApi api = WowsApi.of(server).credentials(c -> c.applicationId(appId));

        compare(label + " dev.ships（统计）",
                () -> JsonUtils.json().toJson(oldShip.developers(appId).shipList()),
                () -> JsonUtils.json().toJson(api.developers().ships(accountId)));
        compare(label + " dev.shipsOper（全量战绩）",
                () -> JsonUtils.json().toJson(oldShip.developers(appId).shipOperList()),
                () -> JsonUtils.json().toJson(api.developers().shipsOper(accountId)));
        compareAllowBothFail(label + " dev.clanSearch（军团搜索）",
                () -> JsonUtils.json().toJson(oldClan.developers(appId).searchClanDevelopers(TestConfig.SEARCH_CLAN)),
                () -> JsonUtils.json().toJson(api.developers().clanSearch(TestConfig.SEARCH_CLAN)));
        compareAllowBothFail(label + " dev.clanSeason（赛季）",
                () -> JsonUtils.json().toJson(oldClan.developers(appId).season()),
                () -> JsonUtils.json().toJson(api.developers().clanSeason()));
        compare(label + " dev.glossary（军团词典）",
                () -> JsonUtils.json().toJson(oldEncyclopedia.developers(appId).glossary()),
                () -> JsonUtils.json().toJson(api.developers().glossary()));
        if (server == WowsServer.ASIA) {
            compare(label + " dev.shipBadges（徽章）",
                    () -> JsonUtils.json().toJson(oldShip.developers(appId).shipBadges()),
                    () -> JsonUtils.json().toJson(api.developers().shipBadges(accountId)));
        }
    }

    private static void cnDevUnsupported() {
        System.out.println();
        System.out.println("== cn 官方开发者API（应显式拒绝） ==");
        try {
            WowsApi.of(WowsServer.CN).developers().ships(TestConfig.ACCOUNT_CN);
            fail("cn dev.ships 应抛 UnsupportedOnServerException（没有抛）");
        } catch (UnsupportedOnServerException e) {
            pass("cn dev.ships 抛 UnsupportedOnServerException：" + e.getMessage());
        } catch (Exception e) {
            fail("cn dev.ships 异常类型不符：" + e);
        }
    }

    // ================= 对比辅助 =================

    private static void compare(String name, ThrowingSupplier<String> oldCall, ThrowingSupplier<String> newCall) {
        Comparison first = compareOnce(oldCall, newCall);
        if (first.ok()) {
            pass(name + "（" + first.oldLength() + " 字符一致）");
            return;
        }
        //真实服务存在瞬时抖动（连接超时 / 数据在两次请求之间更新 / 边缘缓存不一致）——重试一次
        System.out.println("  [..] " + name + " 首次不一致，重试一次" + first.detail());
        Comparison second = compareOnce(oldCall, newCall);
        if (second.ok()) {
            pass(name + "（重试后一致，" + second.oldLength() + " 字符）");
        } else {
            fail(name + second.detail());
        }
    }

    private static Comparison compareOnce(ThrowingSupplier<String> oldCall, ThrowingSupplier<String> newCall) {
        String oldValue;
        try {
            oldValue = oldCall.get();
        } catch (Exception e) {
            return new Comparison(false, "（旧实现异常: " + e + "）", 0, 0);
        }
        String newValue;
        try {
            newValue = newCall.get();
        } catch (Exception e) {
            return new Comparison(false, "（新实现异常: " + e + "）", 0, 0);
        }
        String canonicalOld = canonical(oldValue);
        String canonicalNew = canonical(newValue);
        if (canonicalOld.equals(canonicalNew)) {
            return new Comparison(true, null, oldValue.length(), newValue.length());
        }
        int at = firstDiff(canonicalOld, canonicalNew);
        return new Comparison(false,
                "\n         首个差异 @" + at + "（规范化后长度 " + canonicalOld.length() + " / " + canonicalNew.length() + "）"
                        + "\n         旧: ..." + around(canonicalOld, at) + "\n         新: ..." + around(canonicalNew, at),
                oldValue.length(), newValue.length());
    }

    private record Comparison(boolean ok, String detail, int oldLength, int newLength) {
    }

    private static int firstDiff(String a, String b) {
        int max = Math.min(a.length(), b.length());
        int i = 0;
        while (i < max && a.charAt(i) == b.charAt(i)) {
            i++;
        }
        return i;
    }

    private static String around(String value, int index) {
        int from = Math.max(0, index - 120);
        int to = Math.min(value.length(), index + 200);
        return value.substring(from, to);
    }

    /**
     * 旧新两侧都失败且异常类型一致 = 通过（如莱服确认不提供的接口）；否则同上。
     */
    private static void compareAllowBothFail(String name, ThrowingSupplier<String> oldCall, ThrowingSupplier<String> newCall) {
        Exception oldError = null;
        try {
            oldCall.get();
        } catch (Exception e) {
            oldError = e;
        }
        Exception newError = null;
        try {
            newCall.get();
        } catch (Exception e) {
            newError = e;
        }
        if (oldError != null && newError != null) {
            if (oldError.getClass() == newError.getClass()) {
                pass(name + "（两侧一致失败: " + oldError.getClass().getSimpleName() + "）");
            } else {
                fail(name + "（两侧失败但类型不同: " + oldError.getClass().getSimpleName() + " vs " + newError.getClass().getSimpleName() + "）");
            }
            return;
        }
        if (oldError != null) {
            fail(name + "（旧实现异常: " + oldError + "）");
            return;
        }
        if (newError != null) {
            fail(name + "（新实现异常: " + newError + "）");
            return;
        }
        compare(name, oldCall, newCall);
    }

    /**
     * 规范化对比口径：剔除 recordTime（解析时刻）+ 递归排序对象键（服务端对象键序可能不稳定），数组顺序保留。
     * 详见 {@link com.shinoaki.wows.testkit.CanonicalJson}。
     */
    private static String canonical(String json) {
        return com.shinoaki.wows.testkit.CanonicalJson.of(json);
    }

    private static void pass(String name) {
        passed++;
        System.out.println("  [ok] " + name);
    }

    private static void fail(String name) {
        failed++;
        System.out.println("  [FAIL] " + name);
    }

    private static void expect(String name, String expected, String actual) {
        if (expected.equals(actual)) {
            pass(name);
        } else {
            fail(name + "\n         期望: " + truncate(expected) + "\n         实际: " + truncate(actual));
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
