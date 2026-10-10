package com.shinoaki.wows.field_coverage;

import com.shinoaki.wows.api.codec.HttpCodec;
import com.shinoaki.wows.api.codec.http.WowsHttpShipTools;
import com.shinoaki.wows.api.codec.http.WowsHttpUserTools;
import com.shinoaki.wows.api.data.ShipInfo;
import com.shinoaki.wows.api.data.ShipInfoSource;
import com.shinoaki.wows.api.data.ship.MaxInfo;
import com.shinoaki.wows.api.developers.warships.DevelopersShipBattleInfo;
import com.shinoaki.wows.api.developers.warships.type.DevelopersShipBattleType;
import com.shinoaki.wows.api.monitor.SchemaRecorder;
import com.shinoaki.wows.api.type.WowsBattlesType;
import com.shinoaki.wows.api.type.WowsServer;
import com.shinoaki.wows.api.utils.JsonUtils;
import com.shinoaki.wows.api.vortex.VortexUserShip;
import com.shinoaki.wows.api.vortex.ship.VortexShipInfo;
import com.shinoaki.wows.api.vortex.ship.VortexShipStatistics;
import tools.jackson.databind.JsonNode;

import java.net.http.HttpClient;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.ToLongFunction;
import java.util.stream.Stream;

/**
 * 两条通道（vortex / 官方开发者API）转成 {@link ShipInfo} 后的一致性校验。
 * <p>
 * 用同一个账号的两份数据（vortex 的战舰列表、官方API 的战舰统计）逐船逐字段比对：
 * 两边都有的字段必须完全相等；只有一边有的字段按 {@link ShipInfoSource} 的能力判断，必须落在预期的一侧。
 * <p>
 * 用法：{@code java -cp ... com.shinoaki.wows.field_coverage.ShipInfoCrossSourceTest}；退出码 0 = 两边一致。
 * <br>可选参数：{@code -Dcoverage.live=true} 强制现场抓取、{@code -Draw.dir=<目录>} 指定采集目录。
 *
 * @author Xun
 */
public class ShipInfoCrossSourceTest {
    public static String token = "907d9c6bfc0d896a2c156e57194a97cf";
    private static final long ACCOUNT_ID = 2022515210L;
    private static final WowsServer SERVER = WowsServer.ASIA;
    private static final Path RAW = Path.of(System.getProperty("raw.dir", "target/schema-raw"));
    private static final String VORTEX_FILE = "vortex_asia_api_accounts_id_ships_pvp_.json";
    private static final String DEV_FILE = "api_asia_wows_ships_stats_account_id_id_extra_pvp_div2_pvp_div3__4c0b4c30.json";

    /**
     * 两边都应该有且相等的字段。
     */
    private static final Map<String, ToLongFunction<ShipInfo>> SHARED = new LinkedHashMap<>();
    /**
     * 按来源区分的字段：vortex 应大于 0 / 官方API 应为 0（或反过来）。
     */
    private static int failed = 0;

    static {
        SHARED.put("battle.battle", s -> s.battle().battle());
        SHARED.put("battle.wins", s -> s.battle().wins());
        SHARED.put("battle.losses", s -> s.battle().losses());
        SHARED.put("battle.survived", s -> s.battle().survived());
        SHARED.put("battle.winAndSurvived", s -> s.battle().winAndSurvived());
        SHARED.put("xp", ShipInfo::xp);
        SHARED.put("damageDealt", ShipInfo::damageDealt);
        SHARED.put("scoutingDamage", ShipInfo::scoutingDamage);
        SHARED.put("shipsSpotted", s -> s.shipsSpotted());
        SHARED.put("planesKilled", s -> s.planesKilled());
        SHARED.put("artAgro", ShipInfo::artAgro);
        SHARED.put("tpdAgro", ShipInfo::tpdAgro);
        SHARED.put("fragsInfo.frags", s -> s.fragsInfo().frags());
        SHARED.put("fragsInfo.fragsByMain", s -> s.fragsInfo().fragsByMain());
        SHARED.put("fragsInfo.fragsByAtba", s -> s.fragsInfo().fragsByAtba());
        SHARED.put("fragsInfo.fragsByPlanes", s -> s.fragsInfo().fragsByPlanes());
        SHARED.put("fragsInfo.fragsByTpd", s -> s.fragsInfo().fragsByTpd());
        SHARED.put("fragsInfo.fragsByRam", s -> s.fragsInfo().fragsByRam());
        SHARED.put("maxInfo.maxFrags", s -> s.maxInfo().maxFrags().value());
        SHARED.put("maxInfo.maxDamageDealt", s -> s.maxInfo().maxDamageDealt().value());
        SHARED.put("maxInfo.maxScoutingDamage", s -> s.maxInfo().maxScoutingDamage().value());
        SHARED.put("maxInfo.maxPlanesKilled", s -> s.maxInfo().maxPlanesKilled().value());
        SHARED.put("maxInfo.maxShipsSpotted", s -> s.maxInfo().maxShipsSpotted().value());
        SHARED.put("maxInfo.maxTotalAgro", s -> s.maxInfo().maxTotalAgro().value());
        SHARED.put("maxInfo.maxXp", s -> s.maxInfo().maxXp().value());
        SHARED.put("ratioMain.shots", s -> s.ratioMain().shots());
        SHARED.put("ratioMain.hits", s -> s.ratioMain().hits());
        SHARED.put("ratioAtba.shots", s -> s.ratioAtba().shots());
        SHARED.put("ratioAtba.hits", s -> s.ratioAtba().hits());
        SHARED.put("ratioTpd.shots", s -> s.ratioTpd().shots());
        SHARED.put("ratioTpd.hits", s -> s.ratioTpd().hits());
        SHARED.put("ratioTpd.hitRatio", s -> Math.round(s.ratioTpd().hitRatio() * 1000));
        SHARED.put("control.teamControlCapturedPoints", s -> s.controlCapturedAndDroppedPoints().teamControlCapturedPoints());
        SHARED.put("control.controlCapturedPoints", s -> s.controlCapturedAndDroppedPoints().controlCapturedPoints());
        SHARED.put("control.teamControlDroppedPoints", s -> s.controlCapturedAndDroppedPoints().teamControlDroppedPoints());
        SHARED.put("control.controlDroppedPoints", s -> s.controlCapturedAndDroppedPoints().controlDroppedPoints());
        SHARED.put("control.gameContributionToCapture", s -> Math.round(s.controlCapturedAndDroppedPoints().gameContributionToCapture() * 1000));
    }

    public static void main(String[] args) throws Exception {
        Map<Long, ShipInfo> vortex = vortexShips();
        Map<Long, ShipInfo> dev = devShips();
        System.out.println("战舰数: vortex=" + vortex.size() + ", dev=" + dev.size());

        checkShared(vortex, dev);
        checkAggregate(vortex, dev);
        checkCapability(vortex, dev);
        checkTorpedoRatioRegression();
        checkMixedAggregation(vortex, dev);
        checkRecordHolder(dev);

        System.out.println();
        System.out.println(failed == 0 ? "结果: 两条通道转出的 ShipInfo 一致（共有字段全部相等，差异字段符合来源能力）"
                : "结果: " + failed + " 处不一致");
        System.exit(failed == 0 ? 0 : 1);
    }

    //----------------------------------------------------------------- 逐字段比对

    private static void checkShared(Map<Long, ShipInfo> vortex, Map<Long, ShipInfo> dev) {
        section("共有字段逐船比对（" + SHARED.size() + " 个字段）");
        List<Long> common = new ArrayList<>();
        for (Long id : vortex.keySet()) {
            if (dev.containsKey(id)) {
                common.add(id);
            }
        }
        common.sort(Long::compareTo);
        Map<String, Integer> mismatchCount = new LinkedHashMap<>();
        Map<String, String> sample = new LinkedHashMap<>();
        int compared = 0;
        for (Long id : common) {
            ShipInfo v = vortex.get(id);
            ShipInfo d = dev.get(id);
            for (Map.Entry<String, ToLongFunction<ShipInfo>> field : SHARED.entrySet()) {
                long a = field.getValue().applyAsLong(v);
                long b = field.getValue().applyAsLong(d);
                compared++;
                if (a != b) {
                    mismatchCount.merge(field.getKey(), 1, Integer::sum);
                    sample.putIfAbsent(field.getKey(), "ship=" + id + " vortex=" + a + " dev=" + b);
                }
            }
        }
        System.out.println("  比对 " + common.size() + " 条共有战舰 x " + SHARED.size() + " 字段 = " + compared + " 次");
        if (mismatchCount.isEmpty()) {
            System.out.println("  [ok] 全部相等");
        } else {
            failed += mismatchCount.size();
            for (Map.Entry<String, Integer> e : mismatchCount.entrySet()) {
                System.out.println("  [不一致] " + e.getKey() + " 有 " + e.getValue() + " 条不同, 例: " + sample.get(e.getKey()));
            }
        }
        // 来源标记
        expect("vortex 侧 source", ShipInfoSource.VORTEX.name(), common.isEmpty() ? "-" : vortex.get(common.get(0)).source().name());
        expect("dev 侧 source", ShipInfoSource.DEVELOPERS.name(), common.isEmpty() ? "-" : dev.get(common.get(0)).source().name());
    }

    private static void checkAggregate(Map<Long, ShipInfo> vortex, Map<Long, ShipInfo> dev) {
        section("全船队汇总");
        long[] v = totals(vortex);
        long[] d = totals(dev);
        String[] names = {"battles", "wins", "frags", "damageDealt", "scoutingDamage", "xp"};
        for (int i = 0; i < names.length; i++) {
            expect("汇总 " + names[i], v[i], d[i]);
        }
    }

    private static long[] totals(Map<Long, ShipInfo> ships) {
        long[] t = new long[6];
        for (ShipInfo s : ships.values()) {
            t[0] += s.battle().battle();
            t[1] += s.battle().wins();
            t[2] += s.fragsInfo().frags();
            t[3] += s.damageDealt();
            t[4] += s.scoutingDamage();
            t[5] += s.xp();
        }
        return t;
    }

    //----------------------------------------------------------------- 来源能力

    private static void checkCapability(Map<Long, ShipInfo> vortex, Map<Long, ShipInfo> dev) {
        section("字段可用性（ShipInfoSource 能力）");
        ShipInfo v = vortex.values().iterator().next();
        ShipInfo d = dev.values().iterator().next();
        expect("vortex.hasBasicXp", true, v.hasBasicXp());
        expect("dev.hasBasicXp", false, d.hasBasicXp());
        expect("vortex.hasDbomb", true, v.hasDbomb());
        expect("dev.hasDbomb", false, d.hasDbomb());
        expect("vortex.hasOtherHitRatio", true, v.hasOtherHitRatio());
        expect("dev.hasOtherHitRatio", false, d.hasOtherHitRatio());
        expect("vortex.hasLastBattleTime", false, v.hasLastBattleTime());
        expect("dev.hasLastBattleTime", true, d.hasLastBattleTime());
        expect("vortex.hasExpansion", true, v.hasExpansion());
        expect("dev.hasExpansion", false, d.hasExpansion());

        // 官方API 侧这些字段确实是 0：说明"0 只代表没有数据"
        expect("dev.basicXp 恒为 0", true, dev.values().stream().allMatch(s -> s.basicXp() == 0));
        expect("dev.maxBasicXp 恒为 0", true, dev.values().stream().allMatch(s -> s.maxInfo().maxBasicXp().value() == 0));
        expect("dev.fragsByDbomb 恒为 0", true, dev.values().stream().allMatch(s -> s.fragsInfo().fragsByDbomb() == 0));
        expect("dev.ratioTbomb 恒为 0", true, dev.values().stream().allMatch(s -> s.ratioTbomb().shots() == 0));
        expect("dev.expansion 恒为 5", true, dev.values().stream().allMatch(s -> s.expansion().topGrade() == 5));
        expect("vortex.lastBattleTime 恒为 0", true, vortex.values().stream().allMatch(s -> s.lastBattleTime() == 0));
    }

    private static void checkMixedAggregation(Map<Long, ShipInfo> vortex, Map<Long, ShipInfo> dev) {
        section("跨来源聚合（混合来源退化为 MIXED，可用性判断变保守）");
        ShipInfo v = vortex.values().iterator().next();
        ShipInfo d = dev.values().iterator().next();
        ShipInfo mixed = v.addition(d);
        expect("mixed.source", ShipInfoSource.MIXED.name(), mixed.source().name());
        expect("mixed.hasBasicXp", false, mixed.hasBasicXp());
        expect("mixed.hasLastBattleTime", false, mixed.hasLastBattleTime());
        expect("同源聚合保留来源", ShipInfoSource.VORTEX.name(), v.addition(v).source().name());
    }

    /**
     * 回归：vortex 侧鱼雷命中率必须等于原始 hits/shots（曾经把 shots 当成 hits，恒 100%）。
     */
    private static void checkTorpedoRatioRegression() throws Exception {
        section("回归：vortex 鱼雷命中率");
        JsonNode root = load(VORTEX_FILE, "vortex_asia_api_accounts_id_ships_pvp", () -> {
            HttpClient client = HttpCodec.client();
            new WowsHttpShipTools(client, SERVER, ACCOUNT_ID).vortex().shipList(WowsBattlesType.PVP);
        });
        VortexUserShip ships = VortexUserShip.parse(WowsBattlesType.PVP, root);
        int checkedShips = 0;
        for (Map.Entry<Long, VortexShipStatistics> e : ships.shipMap().entrySet()) {
            VortexShipInfo info = e.getValue().shipInfo();
            if (info.shots_by_tpd() <= 0) {
                continue;
            }
            checkedShips++;
            ShipInfo si = ShipInfo.to(e.getKey(), e.getValue(), 0);
            double expected = 100.0 * info.hits_by_tpd() / info.shots_by_tpd();
            if (Math.abs(si.ratioTpd().hitRatio() - expected) > 0.001) {
                failed++;
                System.out.println("  [FAIL] ship=" + e.getKey() + " 期望=" + expected + " 实际=" + si.ratioTpd().hitRatio());
            }
        }
        System.out.println("  检查了 " + checkedShips + " 条有鱼雷数据的战舰" + (checkedShips == 0 ? "（本条数据里没有，跳过）" : "，命中率与原始数据一致"));
    }

    /**
     * 纪录保持战舰：账号级数据里 max_*_vehicle / max_*_ship_id 是真值，应落到 MaxShipInfo.shipId；
     * 单船数据里为 0（纪录就是本条船），回退到当前 shipId。
     */
    private static void checkRecordHolder(Map<Long, ShipInfo> dev) throws Exception {
        section("纪录保持战舰（接入 MaxShipInfo.shipId）");
        JsonNode root = load("vortex_asia_api_accounts_id_.json", "vortex_asia_api_accounts_id_.json", () -> {
            HttpClient client = HttpCodec.client();
            new WowsHttpUserTools(client, SERVER).userVortex(ACCOUNT_ID);
        });
        JsonNode pvp = root.path("data").get(String.valueOf(ACCOUNT_ID)).path("statistics").path("pvp");
        if (pvp.isEmpty()) {
            System.out.println("  账号统计里没有 pvp 数据，跳过");
            return;
        }
        MaxInfo max = MaxInfo.to(ACCOUNT_ID, VortexShipInfo.parse(pvp));
        expect("maxFrags.shipId <- max_frags_vehicle", pvp.path("max_frags_vehicle").asLong(), max.maxFrags().shipId());
        expect("maxDamageDealt.shipId <- max_damage_dealt_vehicle", pvp.path("max_damage_dealt_vehicle").asLong(), max.maxDamageDealt().shipId());
        expect("maxXp.shipId <- max_exp_vehicle", pvp.path("max_exp_vehicle").asLong(), max.maxXp().shipId());
        expect("maxBasicXp.shipId <- max_exp_vehicle", pvp.path("max_exp_vehicle").asLong(), max.maxBasicXp().shipId());
        expect("纪录船不等于账号ID", true, max.maxFrags().shipId() != ACCOUNT_ID);

        // 单船侧：vortex 的 *_vehicle 为 0、dev 的 *_ship_id 可能为 0 -> 都应回退当前船
        long fallback = 0;
        for (ShipInfo s : dev.values()) {
            if (s.maxInfo().maxFrags().shipId() == s.shipId()) {
                fallback++;
            } else {
                failed++;
                System.out.println("  [FAIL] dev ship=" + s.shipId() + " maxFrags.shipId=" + s.maxInfo().maxFrags().shipId());
                break;
            }
        }
        System.out.println("  dev 侧 " + fallback + " 条战舰的 maxFrags.shipId 都回退到自身");
    }

    //----------------------------------------------------------------- 取数

    private static Map<Long, ShipInfo> vortexShips() throws Exception {
        JsonNode root = load(VORTEX_FILE, "vortex_asia_api_accounts_id_ships_pvp", () -> {
            HttpClient client = HttpCodec.client();
            new WowsHttpShipTools(client, SERVER, ACCOUNT_ID).vortex().shipList(WowsBattlesType.PVP);
        });
        Map<Long, ShipInfo> map = new LinkedHashMap<>();
        for (ShipInfo info : VortexUserShip.parse(WowsBattlesType.PVP, root).toShipInfoList()) {
            map.put(info.shipId(), info);
        }
        return map;
    }

    private static Map<Long, ShipInfo> devShips() throws Exception {
        JsonNode root = load(DEV_FILE, "api_asia_wows_ships_stats_account_id_id_extra_pvp", () -> {
            HttpClient client = HttpCodec.client();
            new WowsHttpShipTools(client, SERVER, ACCOUNT_ID).developers(token).shipList();
        });
        JsonNode items = root.path("data").get(String.valueOf(ACCOUNT_ID));
        List<DevelopersShipBattleInfo> list = DevelopersShipBattleInfo.parse(items);
        Map<Long, ShipInfo> map = new LinkedHashMap<>();
        for (DevelopersShipBattleInfo info : list) {
            DevelopersShipBattleType block = info.shipBattleTypeMap().get(WowsBattlesType.PVP);
            if (block != null) {
                map.put(info.ship_id(), ShipInfo.to(info.ship_id(), block, info.last_battle_time(), System.currentTimeMillis()));
            }
        }
        return map;
    }

    private interface Fetch {
        void run() throws Exception;
    }

    /**
     * 本地采集优先，没有就现场抓一次（与 DevelopersFieldCoverageTest 同样的做法）。
     */
    private static JsonNode load(String file, String prefix, Fetch fetch) throws Exception {
        if (!Boolean.getBoolean("coverage.live")) {
            Path path = find(file, prefix);
            if (path != null) {
                return JsonUtils.json().parse(path.toFile());
            }
        }
        System.out.println("现场抓取: " + prefix + " ...");
        SchemaRecorder.enable();
        List<SchemaRecorder.Record> records;
        try {
            fetch.run();
        } catch (Exception e) {
            throw new IllegalStateException("抓取失败: " + prefix, e);
        } finally {
            records = SchemaRecorder.drain();
            SchemaRecorder.disable();
        }
        if (records.isEmpty()) {
            throw new IllegalStateException("没有采集到响应: " + prefix);
        }
        return JsonUtils.json().parse(records.get(0).json());
    }

    private static Path find(String file, String prefix) {
        Path path = RAW.resolve(file);
        if (Files.isRegularFile(path)) {
            return path;
        }
        if (!Files.isDirectory(RAW)) {
            return null;
        }
        try (Stream<Path> files = Files.list(RAW)) {
            return files.filter(Files::isRegularFile)
                    .filter(p -> {
                        String name = p.getFileName().toString();
                        return name.startsWith(prefix) && name.endsWith(".json");
                    })
                    .sorted()
                    .findFirst()
                    .orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    //----------------------------------------------------------------- 工具

    private static void section(String title) {
        System.out.println();
        System.out.println("── " + title + " ──");
    }

    private static void expect(String name, Object expected, Object actual) {
        if (String.valueOf(expected).equals(String.valueOf(actual))) {
            System.out.println("  [ok] " + name + " = " + actual);
        } else {
            failed++;
            System.out.println("  [FAIL] " + name + " 期望=" + expected + " 实际=" + actual);
        }
    }
}
