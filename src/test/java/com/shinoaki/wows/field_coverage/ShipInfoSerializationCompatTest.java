package com.shinoaki.wows.field_coverage;

import com.shinoaki.wows.api.data.ShipInfo;
import com.shinoaki.wows.api.data.ship.AssistInfo;
import com.shinoaki.wows.api.data.ShipInfoSource;
import com.shinoaki.wows.api.data.ship.MaxInfo;
import com.shinoaki.wows.api.utils.JsonUtils;
import com.shinoaki.wows.api.vortex.ship.VortexShipInfo;
import com.shinoaki.wows.api.vortex.ship.VortexShipMarkOfExcellence;
import com.shinoaki.wows.api.vortex.ship.VortexShipStatistics;
import tools.jackson.databind.JsonNode;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Function;

/**
 * 序列化兼容性回归：用"加 {@code source} 字段之前"（2.5.1）写出的数据，验证新版本仍能正确读取。
 * <p>
 * fixture 是用 git 里改动前的 {@code ShipInfo} 类编译后现场写出的：
 * {@code src/test/resources/serialization/ShipInfo-2.5.1.ser}，同一份原始数据留了一份
 * {@code ship-block.json}，测试用它按当前代码重建一份 {@link ShipInfo} 做逐字段对照。
 * <p>
 * 覆盖三条读取路径：Java 原生反序列化（旧缓存）、Java 序列化往返（新数据）、Jackson JSON（缺字段）。
 * <p>
 * 用法：{@code java -cp ... com.shinoaki.wows.field_coverage.ShipInfoSerializationCompatTest}；退出码 0 = 兼容。
 *
 * @author Xun
 */
public class ShipInfoSerializationCompatTest {

    private static final String SER = "/serialization/ShipInfo-2.5.1.ser";
    private static final String BLOCK = "/serialization/ship-block.json";
    private static int failed = 0;

    public static void main(String[] args) throws Exception {
        JsonNode block = JsonUtils.json().parse(read(BLOCK));
        long shipId = block.path("shipId").asLong();
        long recordTime = block.path("recordTime").asLong();
        JsonNode statistics = block.path("statistics");

        // 当前代码按同一份原始数据重建（作为期望值）
        VortexShipStatistics rebuilt = new VortexShipStatistics(
                VortexShipInfo.parse(statistics.path("pvp")),
                statistics.path("mastery_sign").asString(""),
                VortexShipMarkOfExcellence.parse(statistics.path("mark_of_excellence")));
        ShipInfo expected = ShipInfo.to(shipId, rebuilt, recordTime);

        ShipInfo old = (ShipInfo) new ObjectInputStream(new ByteArrayInputStream(read(SER))).readObject();

        section("旧数据(2.5.1)反序列化");
        expect("shipId", expected.shipId(), old.shipId());
        expect("battle", expected.battle(), old.battle());
        expect("xp", expected.xp(), old.xp());
        expect("basicXp", expected.basicXp(), old.basicXp());
        expect("damageDealt", expected.damageDealt(), old.damageDealt());
        expect("scoutingDamage", expected.scoutingDamage(), old.scoutingDamage());
        expect("fragsInfo", expected.fragsInfo(), old.fragsInfo());
        expect("shipsSpotted/planesKilled", expected.shipsSpotted() + "/" + expected.planesKilled(), old.shipsSpotted() + "/" + old.planesKilled());
        expect("artAgro/tpdAgro", expected.artAgro() + "/" + expected.tpdAgro(), old.artAgro() + "/" + old.tpdAgro());
        expect("maxInfo(2.5.1 已有的 16 项)", maxInfoBase(expected.maxInfo()), maxInfoBase(old.maxInfo()));
        expect("controlCapturedAndDroppedPoints", expected.controlCapturedAndDroppedPoints(), old.controlCapturedAndDroppedPoints());
        expect("ratioMain", expected.ratioMain(), old.ratioMain());
        expect("ratioAtba", expected.ratioAtba(), old.ratioAtba());
        expect("ratioTbomb/Bomb/Rocket/Skip",
                expected.ratioTbomb() + "/" + expected.ratioBomb() + "/" + expected.ratioRocket() + "/" + expected.ratioSkip(),
                old.ratioTbomb() + "/" + old.ratioBomb() + "/" + old.ratioRocket() + "/" + old.ratioSkip());
        expect("expansion", expected.expansion(), old.expansion());
        expect("lastBattleTime", expected.lastBattleTime(), old.lastBattleTime());
        expect("recordTime", expected.recordTime(), old.recordTime());

        section("新增字段的归一（旧数据里没有这些字段）");
        expect("assistInfo()", AssistInfo.empty(), old.assistInfo());
        expect("minesAgro()", 0L, old.minesAgro());
        expect("maxInfo.maxAssistDamage()", MaxInfo.MaxShipInfo.empty(), old.maxInfo().maxAssistDamage());
        expect("maxInfo.maxAssistKills()", MaxInfo.MaxShipInfo.empty(), old.maxInfo().maxAssistKills());

        section("新增字段 source 的归一（旧数据里没有这个字段）");
        expect("source()", ShipInfoSource.UNKNOWN.name(), old.source().name());
        expect("hasBasicXp()", false, old.hasBasicXp());
        expect("hasDbomb()", false, old.hasDbomb());
        expect("hasOtherHitRatio()", false, old.hasOtherHitRatio());
        expect("hasLastBattleTime()", false, old.hasLastBattleTime());
        expect("hasExpansion()", false, old.hasExpansion());

        section("旧版本已知问题：鱼雷命中率（2.5.1 把 shots 当成 hits，读出的是脏数据）");
        expect("shots 一致", expected.ratioTpd().shots(), old.ratioTpd().shots());
        expect("旧数据 hits==shots(历史 bug 的痕迹)", old.ratioTpd().shots(), old.ratioTpd().hits());
        expect("新代码 hits 正确", true, expected.ratioTpd().hits() <= expected.ratioTpd().shots());
        System.out.println("  提示: 旧缓存里的 ratioTpd 是错的，需要正确值请用当前版本重新取数");

        section("当前版本：Java 序列化往返");
        ShipInfo roundTrip = (ShipInfo) new ObjectInputStream(new ByteArrayInputStream(serialize(expected))).readObject();
        expect("source 保留", ShipInfoSource.VORTEX.name(), roundTrip.source().name());
        expect("equals 原对象", true, expected.equals(roundTrip));
        expect("hasBasicXp()", true, roundTrip.hasBasicXp());
        ShipInfo devLike = new ShipInfo(expected.shipId(), expected.battle(), expected.xp(), 0, expected.damageDealt(), 0,
                expected.fragsInfo(), AssistInfo.empty(), 0, 0, 0, 0, 0, MaxInfo.empty(),
                expected.controlCapturedAndDroppedPoints(), expected.ratioMain(), expected.ratioAtba(), expected.ratioTpd(),
                expected.ratioTbomb(), expected.ratioBomb(), expected.ratioRocket(), expected.ratioSkip(),
                expected.expansion(), 0, recordTime, ShipInfoSource.DEVELOPERS);
        ShipInfo devRoundTrip = (ShipInfo) new ObjectInputStream(new ByteArrayInputStream(serialize(devLike))).readObject();
        expect("DEV来源保留", ShipInfoSource.DEVELOPERS.name(), devRoundTrip.source().name());

        section("当前版本：Jackson JSON 缺 source 字段");
        String json = JsonUtils.json().toJson(expected);
        String withoutSource = json.replace(",\"source\":\"VORTEX\"", "");
        expect("JSON 里已去掉 source", true, !withoutSource.contains("source"));
        ShipInfo fromJson = JsonUtils.json().parse(withoutSource, ShipInfo.class);
        expect("source()", ShipInfoSource.UNKNOWN.name(), fromJson.source().name());
        expect("hasBasicXp()", false, fromJson.hasBasicXp());
        expect("其余字段一致", expected.damageDealt(), fromJson.damageDealt());
        ShipInfo jsonRoundTrip = JsonUtils.json().parse(json, ShipInfo.class);
        expect("JSON 带 source 往返", ShipInfoSource.VORTEX.name(), jsonRoundTrip.source().name());

        System.out.println();
        System.out.println(failed == 0 ? "结果: 序列化兼容（旧数据可读、新字段归一、往返一致）" : "结果: " + failed + " 处不兼容");
        System.exit(failed == 0 ? 0 : 1);
    }

    //----------------------------------------------------------------- 工具

    private static byte[] serialize(Object value) throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(bytes)) {
            out.writeObject(value);
        }
        return bytes.toByteArray();
    }

    private static byte[] read(String resource) throws Exception {
        try (InputStream in = ShipInfoSerializationCompatTest.class.getResourceAsStream(resource)) {
            if (in != null) {
                return in.readAllBytes();
            }
        }
        Path path = Path.of("src/test/resources" + resource);
        if (Files.isRegularFile(path)) {
            return Files.readAllBytes(path);
        }
        throw new IllegalStateException("找不到测试资源: " + resource);
    }

    /**
     * 2.5.1 时 {@link MaxInfo} 已有的 16 项（2.7.0 追加的莱服助攻纪录不在其中）。
     */
    private static String maxInfoBase(MaxInfo info) {
        return info.maxFrags() + "|" + info.maxFragsByMain() + "|" + info.maxFragsByTpd() + "|" + info.maxFragsByDbomb() + "|"
                + info.maxFragsByRam() + "|" + info.maxFragsByAtba() + "|" + info.maxDamageDealtToBuildings() + "|" + info.maxFragsByPlanes() + "|"
                + info.maxDamageDealt() + "|" + info.maxScoutingDamage() + "|" + info.maxPlanesKilled() + "|" + info.maxShipsSpotted() + "|"
                + info.maxTotalAgro() + "|" + info.maxSuppressionsCount() + "|" + info.maxXp() + "|" + info.maxBasicXp();
    }

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
