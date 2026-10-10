package com.shinoaki.wows;

import com.shinoaki.wows.api.codec.HttpCodec;
import com.shinoaki.wows.api.codec.http.WowsEncyclopediaTools;
import com.shinoaki.wows.api.codec.http.WowsHttpClanTools;
import com.shinoaki.wows.api.codec.http.WowsHttpShipTools;
import com.shinoaki.wows.api.codec.http.WowsHttpUserTools;
import com.shinoaki.wows.api.monitor.SchemaBaseline;
import com.shinoaki.wows.api.monitor.SchemaDiff;
import com.shinoaki.wows.api.monitor.SchemaExtractor;
import com.shinoaki.wows.api.monitor.SchemaKeys;
import com.shinoaki.wows.api.monitor.SchemaMonitor;
import com.shinoaki.wows.api.monitor.SchemaSnapshot;
import com.shinoaki.wows.api.monitor.SchemaRawStore;
import com.shinoaki.wows.api.monitor.SchemaRecorder;
import com.shinoaki.wows.api.monitor.SchemaRules;
import com.shinoaki.wows.api.monitor.SchemaTracker;
import com.shinoaki.wows.api.type.WowsBattlesType;
import com.shinoaki.wows.api.type.WowsServer;
import com.shinoaki.wows.api.utils.JsonUtils;
import com.shinoaki.wows.api.vortex.VortexResourcesLanguage;
import com.shinoaki.wows.api.vortex.account.VortexUserInfo;
import com.shinoaki.wows.api.vortex.resources.RequestVortexResourcesInfo;
import com.shinoaki.wows.api.vortex.resources.WowsResources;
import com.shinoaki.wows.api.vortex.resources.box.WowsBox;
import com.shinoaki.wows.api.vortex.resources.box.WowsLootBox;
import com.shinoaki.wows.api.vortex.ship.VortexShipInfo;
import tools.jackson.databind.JsonNode;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

/**
 * 接口字段漂移监视（Schema Drift Monitor）——采集 + 比对 + 基线维护。
 * <p>
 * 监视本身不在库里"另写一份 URL/query"：它打开 {@link SchemaRecorder} 后照常调用库的公开方法，
 * 响应由 {@code HttpCodec.response} 顺带采集，因此请求与响应与真实调用完全一致。
 *
 * <pre>
 * 用法（仓库根目录执行）：
 *   java -cp ... com.shinoaki.wows.SchemaMonitorTest                 # 在线探测 + 与基线比对（只读）
 *   java -cp ... com.shinoaki.wows.SchemaMonitorTest --update        # 顺带写回基线（供 git review）
 *   java -cp ... com.shinoaki.wows.SchemaMonitorTest --precise       # 追加"解析器实际读取/未消费"报告
 *   java -cp ... com.shinoaki.wows.SchemaMonitorTest --replay        # 离线回放上次采集的原始响应（不打网络）
 *
 * 可选参数：--baseline=&lt;目录&gt;（默认 src/test/resources/schema-baseline）
 *           --raw=&lt;目录&gt;（默认 target/schema-raw）
 * 开发者接口需要令牌：环境变量 WOWS_APP_ID 或 -Dwows.appId=xxx；未配置时自动跳过（vortex 接口不受影响）。
 * </pre>
 *
 * 退出码：0 = 无破坏性变更；1 = 出现 removed/typeChanged（疑似接口变更，需要人工确认）。
 *
 * @author Xun
 */
public class SchemaMonitorTest {

    private static final Path BASELINE_DIR = Path.of("src/test/resources/schema-baseline");
    private static final Path RAW_DIR = Path.of("target/schema-raw");

    private static final WowsServer SERVER = WowsServer.ASIA;
    private static final long ACCOUNT_ID = 2022515210L;
    private static final long CLAN_ID = 2000016057L;
    private static final long CLAN_MEMBERS_ID = 2000025691L;
    private static final long LOOTBOX_ID = 4288861104L;

    /**
     * graphql 一个 URL 对应多个 schema，URI 区分不了，必须显式声明 key。
     */
    private static final String GRAPHQL = "vortex:asia:/api/graphql/glossary/#";

    private static final String KEY_LOOTBOX = "vortex:asia:/api/get_lootbox/{lang}/{id}/";
    private static final String KEY_ACCOUNT = "vortex:asia:/api/accounts/{id}/";
    private static final String KEY_SHIPS = "vortex:asia:/api/accounts/{id}/ships/pvp/";

    /**
     * 精确模式：schemaKey -> 该接口的解析器（用真实解析器跑一遍被包裹的树，登记实际读取的路径）。
     * 只登记 parse 为 public 的接口，避免为了监视去改解析器可见性。
     */
    private static final Map<String, Parser> PARSERS = Map.of(
            KEY_LOOTBOX, WowsLootBox::parse,
            KEY_ACCOUNT, node -> VortexUserInfo.parse(node, ACCOUNT_ID),
            KEY_SHIPS, VortexShipInfo::parse
    );

    public static void main(String[] args) {
        if (flag(args, "--selftest")) {
            System.exit(selftest());
        }
        boolean update = flag(args, "--update");
        boolean replay = flag(args, "--replay");
        boolean precise = flag(args, "--precise");
        Path baselineDir = Path.of(value(args, "--baseline", BASELINE_DIR.toString()));
        Path rawDir = Path.of(value(args, "--raw", RAW_DIR.toString()));

        SchemaMonitor monitor = new SchemaMonitor(baselineDir, SchemaRules.defaults(), update, System.out);
        System.out.println("模式      : " + (replay ? "离线回放" : "在线探测") + (precise ? " + 精确模式" : ""));
        System.out.println("基线目录  : " + baselineDir.toAbsolutePath() + (update ? "（写回）" : "（只读）"));
        System.out.println("响应仓库  : " + rawDir.toAbsolutePath());
        System.out.println("图例      : + 新增字段 / - 字段消失 / ~ 类型变化 / ! 解析器读取但响应缺失 / ? 响应有但解析器未消费");
        System.out.println();

        List<SchemaMonitor.Result> results = replay
                ? replay(monitor, new SchemaRawStore(rawDir))
                : online(monitor, new SchemaRawStore(rawDir), precise);

        if (update && !replay) {
            prune(baselineDir, results);
            Set<String> keys = new HashSet<>();
            for (SchemaMonitor.Result result : results) {
                keys.add(result.schemaKey());
            }
            new SchemaRawStore(rawDir).prune(keys);
        }

        int breaking = 0;
        int created = 0;
        for (SchemaMonitor.Result result : results) {
            monitor.log(result);
            if (result.baselineCreated()) {
                created++;
            } else if (result.diff().hasBreaking()) {
                breaking++;
            }
        }

        Set<String> distinct = new HashSet<>();
        for (SchemaMonitor.Result result : results) {
            distinct.add(result.schemaKey());
        }
        System.out.println("── 汇总 ─────────────────────────────────");
        System.out.printf("接口 %d 个（去重 %d）；新建基线 %d 个；疑似破坏性变更 %d 个%n",
                results.size(), distinct.size(), created, breaking);
        if (!update && created > 0) {
            System.out.println("提示: 有接口尚无基线，确认无异常后加 --update 建立基线。");
        }
        if (breaking > 0 && !update) {
            System.out.println("结论: 出现 removed/typeChanged，请核对上方明细后决定是否更新基线。");
            System.exit(1);
        }
        System.out.println("结论: 未发现破坏性变更。");
    }

    /**
     * 自检：不联网，验证 key 归一、结构采集、diff 分级、精确模式登记这些纯逻辑。
     *
     * @return 失败项数量（0 = 全部通过）
     */
    private static int selftest() {
        int failed = 0;
        System.out.println("── 自检 ──");

        failed += expect("数字/语言段归一", "vortex:asia:/api/get_lootbox/{lang}/{id}/",
                SchemaKeys.of(URI.create("https://vortex.worldofwarships.asia/api/get_lootbox/zh-sg/4288861104/")));
        failed += expect("搜索词归一", "vortex:asia:/api/accounts/search/autocomplete/{text}/",
                SchemaKeys.of(URI.create("https://vortex.worldofwarships.asia/api/accounts/search/autocomplete/JustOn/")));
        failed += expect("固定路径段 search 不被归一", "clans:asia:/api/search/autocomplete/?search={text}&type=clans",
                SchemaKeys.of(URI.create("https://clans.worldofwarships.asia/api/search/autocomplete/?search=YU_RI&type=clans")));
        failed += expect("密钥与语言不进 key", "api:asia:/wows/account/info/?account_id={id}&extra=statistics",
                SchemaKeys.of(URI.create("https://api.worldofwarships.asia/wows/account/info/?application_id=SECRET&access_token=T&language=zh-cn&account_id=2022515210&extra=statistics")));
        failed += expect("结构参数保留", "clans:asia:/api/members/{id}/?battle_type=cvc&season={num}",
                SchemaKeys.of(URI.create("https://clans.worldofwarships.asia/api/members/2000025691/?battle_type=cvc&season=35")));

        //动态 key 归一：账号ID/船ID 这类数据主键不能变成独立字段
        SchemaSnapshot dynamic = SchemaExtractor.extract("dynamic",
                JsonUtils.json().parse("{\"data\":{\"2022515210\":{\"name\":\"x\",\"battles\":1}}}"));
        failed += expect("动态 key 归一", "true", String.valueOf(dynamic.has("data.*.name") && dynamic.has("data.*.battles")));

        //结构采集 + diff 分级
        SchemaSnapshot base = SchemaExtractor.extract("base",
                JsonUtils.json().parse("{\"status\":\"ok\",\"data\":{\"id\":1,\"name\":\"a\",\"items\":[{\"x\":1}]}}"));
        SchemaSnapshot now = SchemaExtractor.extract("now",
                JsonUtils.json().parse("{\"status\":\"ok\",\"data\":{\"id\":\"1\",\"name\":\"a\",\"items\":[{\"x\":1,\"y\":true}],\"extra\":1}}"));
        SchemaDiff diff = SchemaDiff.of(base, now);
        failed += expect("新增字段", "data.extra,data.items[].y", String.join(",", diff.added()));
        failed += expect("类型变化", "data.id: number -> string", String.join(",", diff.typeChanged()));
        failed += expect("消失字段", "true", String.valueOf(diff.removed().isEmpty()));
        failed += expect("破坏性变更判定", "true", String.valueOf(diff.hasBreaking()));

        SchemaDiff reverse = SchemaDiff.of(now, base);
        failed += expect("字段消失被识别", "data.extra,data.items[].y", String.join(",", reverse.removed()));

        //精确模式：被包裹的树要能登记解析器读过的路径
        Set<String> reads = new LinkedHashSet<>();
        JsonNode tracked = SchemaTracker.track(JsonUtils.json().parse("{\"data\":{\"name\":\"a\",\"items\":[{\"x\":1}]}}"), reads);
        tracked.path("data").path("name").asString("");
        for (JsonNode item : tracked.path("data").path("items")) {
            item.path("x").asInt(0);
        }
        failed += expect("读取路径登记", "data,data.items,data.items[].x,data.name",
                String.join(",", new java.util.TreeSet<>(reads)));

        System.out.println(failed == 0 ? "自检结果: 全部通过" : "自检结果: " + failed + " 项失败");
        return failed;
    }

    private static int expect(String name, String expected, String actual) {
        if (expected.equals(actual)) {
            System.out.println("  [ok] " + name);
            return 0;
        }
        System.out.println("  [FAIL] " + name + "\n         期望: " + expected + "\n         实际: " + actual);
        return 1;
    }

    /**
     * 在线探测：打开采集器 -> 调用库方法 -> 收集响应 -> 比对基线。
     */
    private static List<SchemaMonitor.Result> online(SchemaMonitor monitor, SchemaRawStore rawStore, boolean precise) {
        //箱子/战舰列表等接口响应较大，给足超时
        HttpCodec.updateTimeout(20);
        HttpClient client = HttpCodec.client();
        String appId = appId();
        System.out.println("开发者令牌: " + (appId == null ? "未配置（跳过官方API探测）" : "已配置"));
        System.out.println();

        List<SchemaMonitor.Result> results = new ArrayList<>();
        for (Probe probe : probes(client, appId)) {
            if (probe.dev() && appId == null) {
                System.out.println("跳过 " + probe.label() + "（未配置 WOWS_APP_ID）");
                continue;
            }
            run(monitor, rawStore, results, probe, precise);
        }
        return results;
    }

    /**
     * 离线回放：只用上次采集的原始响应重跑"采集 -> 比对"，不打任何网络请求。
     */
    private static List<SchemaMonitor.Result> replay(SchemaMonitor monitor, SchemaRawStore rawStore) {
        List<SchemaMonitor.Result> results = new ArrayList<>();
        for (String key : rawStore.keys()) {
            String json = rawStore.load(key);
            if (json == null) {
                System.out.println("!! 响应仓库缺少: " + key);
                continue;
            }
            results.add(monitor.check(label(key), key, JsonUtils.json().parse(json), null));
        }
        return results;
    }

    private static void run(SchemaMonitor monitor, SchemaRawStore rawStore, List<SchemaMonitor.Result> results,
                            Probe probe, boolean precise) {
        SchemaRecorder.enable();
        if (probe.key() != null) {
            SchemaRecorder.key(probe.key());
        }
        try {
            probe.call().run();
        } catch (Exception e) {
            System.out.println("!! 探测失败 " + probe.label() + ": " + e.getClass().getSimpleName() + " " + e.getMessage());
        } finally {
            List<SchemaRecorder.Record> records = SchemaRecorder.drain();
            SchemaRecorder.disable();
            for (SchemaRecorder.Record record : records) {
                collect(monitor, rawStore, results, probe.label(), record, precise);
            }
        }
    }

    private static void collect(SchemaMonitor monitor, SchemaRawStore rawStore, List<SchemaMonitor.Result> results,
                                String label, SchemaRecorder.Record record, boolean precise) {
        JsonNode json;
        try {
            json = JsonUtils.json().parse(record.json());
        } catch (Exception e) {
            System.out.println("!! 响应不是合法JSON " + label + " [" + record.schemaKey() + "]: " + e.getMessage());
            return;
        }
        Set<String> reads = null;
        Parser parser = PARSERS.get(record.schemaKey());
        if (precise && parser != null) {
            reads = new LinkedHashSet<>();
            try {
                parser.parse(SchemaTracker.track(json, reads));
            } catch (Exception e) {
                System.out.println("!! 精确模式解析失败 " + label + ": " + e.getMessage());
                reads = null;
            }
        }
        rawStore.save(record.schemaKey(), record.json());
        results.add(monitor.check(label, record.schemaKey(), json, reads));
    }

    /**
     * 探测清单：每个接口一条，key 为 null 表示按请求 URI 自动推导。
     */
    private static List<Probe> probes(HttpClient client, String appId) {
        WowsHttpUserTools user = new WowsHttpUserTools(client, SERVER);
        WowsHttpShipTools ship = new WowsHttpShipTools(client, SERVER, ACCOUNT_ID);
        WowsHttpClanTools clan = new WowsHttpClanTools(client, SERVER);
        WowsEncyclopediaTools encyclopedia = new WowsEncyclopediaTools(client, SERVER);
        List<Probe> probes = new ArrayList<>();

        //---- vortex：无需令牌 ----
        probes.add(new Probe("箱子详情", null, false,
                () -> WowsLootBox.request(SERVER, VortexResourcesLanguage.SG, LOOTBOX_ID)));
        probes.add(new Probe("箱子列表", GRAPHQL + "Lootbox", false,
                () -> WowsBox.request(false, SERVER, VortexResourcesLanguage.SG)));
        probes.add(new Probe("资源列表", GRAPHQL + "Items", false,
                () -> WowsResources.request(SERVER, VortexResourcesLanguage.SG)));
        probes.add(new Probe("资源索引", GRAPHQL + "getGlossData", false,
                () -> RequestVortexResourcesInfo.request(SERVER, VortexResourcesLanguage.SG)));
        probes.add(new Probe("玩家详情", null, false, () -> user.userVortex(ACCOUNT_ID)));
        probes.add(new Probe("玩家搜索", null, false, () -> user.searchUserVortex("JustOn")));
        probes.add(new Probe("玩家战舰列表", null, false, () -> ship.vortex().shipList(WowsBattlesType.PVP)));
        probes.add(new Probe("玩家所属军团", null, false, () -> clan.vortex().userSearchClanVortex(ACCOUNT_ID)));
        probes.add(new Probe("军团详情", null, false, () -> clan.vortex().clanInfoVortex(CLAN_ID)));
        probes.add(new Probe("军团成员", null, false, () -> clan.vortex().clanUserListInfoVortex(CLAN_MEMBERS_ID)));
        probes.add(new Probe("军团搜索", null, false, () -> clan.vortex().searchClanVortex("YU_RI")));

        //---- 官方开发者API：需要 application_id ----
        if (appId != null) {
            probes.add(new Probe("官方-玩家详情", null, true, () -> user.accountInfoDevelopers(appId, ACCOUNT_ID)));
            probes.add(new Probe("官方-玩家搜索", null, true, () -> user.searchUserDevelopers(appId, "JustOn")));
            probes.add(new Probe("官方-战舰统计", null, true, () -> ship.developers(appId).shipList()));
            probes.add(new Probe("官方-战舰战绩", null, true, () -> ship.developers(appId).shipOperList()));
            probes.add(new Probe("官方-战舰徽章", null, true, () -> ship.developers(appId).shipBadges()));
            probes.add(new Probe("官方-军团详情", null, true, () -> clan.developers(appId).clanInfoDevelopers(CLAN_ID)));
            probes.add(new Probe("官方-军团搜索", null, true, () -> clan.developers(appId).searchClanDevelopers("YU_RI")));
            probes.add(new Probe("官方-军团赛季", null, true, () -> clan.developers(appId).season()));
            probes.add(new Probe("官方-词典", null, true, () -> encyclopedia.developers(appId).glossary()));
        }
        return probes;
    }

    /**
     * 离线回放的展示名：key 去掉「通道:服别」前缀，保留路径与查询参数。
     */
    private static String label(String schemaKey) {
        int first = schemaKey.indexOf(':');
        int second = first < 0 ? -1 : schemaKey.indexOf(':', first + 1);
        String path = second < 0 ? schemaKey : schemaKey.substring(second + 1);
        return path.length() <= 80 ? path : path.substring(0, 80) + "...";
    }

    /**
     * 清理本次没有出现的基线文件（接口改名/归一规则调整后留下的孤儿文件）。
     */
    private static void prune(Path baselineDir, List<SchemaMonitor.Result> results) {
        if (!Files.isDirectory(baselineDir)) {
            return;
        }
        Set<String> expected = new HashSet<>();
        for (SchemaMonitor.Result result : results) {
            expected.add(SchemaBaseline.fileName(result.schemaKey()));
        }
        try (Stream<Path> files = Files.list(baselineDir)) {
            for (Path file : files.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".json"))
                    .filter(path -> !expected.contains(path.getFileName().toString()))
                    .sorted().toList()) {
                Files.delete(file);
                System.out.println("清理过期基线: " + file.getFileName());
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static String appId() {
        String fromEnv = System.getenv("WOWS_APP_ID");
        if (fromEnv != null && !fromEnv.isBlank()) {
            return fromEnv.trim();
        }
        String fromProperty = System.getProperty("wows.appId");
        return fromProperty == null || fromProperty.isBlank() ? null : fromProperty.trim();
    }

    private static boolean flag(String[] args, String name) {
        for (String arg : args) {
            if (arg.equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    private static String value(String[] args, String name, String defaultValue) {
        String prefix = name + "=";
        for (String arg : args) {
            if (arg.startsWith(prefix)) {
                return arg.substring(prefix.length());
            }
        }
        return defaultValue;
    }

    /**
     * 精确模式用的解析器入口（只关心"读了哪些路径"）。
     */
    @FunctionalInterface
    private interface Parser {
        void parse(JsonNode node) throws Exception;
    }

    /**
     * 一次探测。
     *
     * @param label 展示用名称
     * @param key   显式 schemaKey；null 表示按请求 URI 推导
     * @param dev   是否官方开发者API（需要令牌）
     * @param call  真实调用（走库的公开方法）
     */
    private record Probe(String label, String key, boolean dev, Call call) {
    }

    @FunctionalInterface
    private interface Call {
        void run() throws Exception;
    }
}
