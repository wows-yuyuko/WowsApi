package com.shinoaki.wows.old;

import com.shinoaki.wows.api.WowsApi;
import com.shinoaki.wows.api.codec.ClanRankUtils;
import com.shinoaki.wows.api.codec.HttpCodec;
import com.shinoaki.wows.api.codec.http.WowsHttpClanTools;
import com.shinoaki.wows.api.codec.http.WowsEncyclopediaTools;
import com.shinoaki.wows.api.type.WowsServer;
import com.shinoaki.wows.api.utils.JsonUtils;
import com.shinoaki.wows.api.vortex.clan.rank.ClanRankInfo;
import com.shinoaki.wows.testkit.CanonicalJson;
import com.shinoaki.wows.testkit.TestConfig;
import com.shinoaki.wows.testkit.TestReport;
import tools.jackson.core.type.TypeReference;

import java.net.URI;
import java.net.http.HttpClient;
import java.util.List;

/**
 * 「公会相关」新旧等价测试：真实服务上跑新旧对比（所属军团、详情/成员/搜索、排行单页与游标页、
 * dev 搜索/赛季/词典）；{@code --full} 追加整季排行全量对比（RU，较重）。
 * <p>
 * 用法：{@code java -cp ... com.shinoaki.wows.old.RewriteClanTest [--full]}（建议带 {@code -Dwows.proxy=127.0.0.1:7890}）；
 * 退出码 = 失败项数。
 *
 * @author Xun
 */
public class RewriteClanTest {

    public static void main(String[] args) {
        boolean full = flag(args, "--full");
        TestReport report = new TestReport("公会相关");
        System.out.println("── 公会相关 · 新旧等价测试（在线" + (full ? " + 整季排行全量" : "") + "） ──");
        online(report, full);
        report.finish();
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
     * 旧实现有「吞异常」历史行为：任一分页请求失败会静默提前截断（实测曾出现 289 vs 360），
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

    private static boolean flag(String[] args, String name) {
        for (String arg : args) {
            if (arg.equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }
}
