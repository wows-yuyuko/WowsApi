package com.shinoaki.wows.field_coverage;

import com.shinoaki.wows.api.data.ShipInfo;
import com.shinoaki.wows.api.data.ShipInfoSource;
import com.shinoaki.wows.api.developers.DevelopersUserShip;
import com.shinoaki.wows.api.type.WowsBattlesType;
import com.shinoaki.wows.api.utils.JsonUtils;
import com.shinoaki.wows.api.vortex.VortexUserShip;
import com.shinoaki.wows.testkit.TestConfig;
import tools.jackson.databind.JsonNode;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.ToLongFunction;

/**
 * 莱服两条通道（vortex.korabli.su / 官方API api.korabli.su 的 {@code /mk/}）转成 {@link ShipInfo} 后的一致性校验。
 * <p>
 * 数据用 {@code target/ru-dump/} 里的两份真实响应（离线，不入 git）：
 * <ul>
 *     <li>{@code ships-pvp.json}：{@code https://vortex.korabli.su/api/accounts/212112137/ships/pvp/}</li>
 *     <li>{@code official-ships.json}：{@code https://api.korabli.su/mk/ships/stats/?...&extra=pvp_solo,pvp_div2,pvp_div3,rank_solo,pve,oper_*}</li>
 * </ul>
 * 两边共有的字段必须完全相等；莱服新增的助攻要能在两边都对上；水雷只有 vortex 有。
 * <p>
 * 用法：{@code java -cp ... com.shinoaki.wows.field_coverage.RuShipInfoCrossSourceTest}；退出码 0 = 两边一致。
 *
 * @author Xun
 */
public class RuShipInfoCrossSourceTest {

    private static final Path DUMP_DIR = Path.of("target/ru-dump");
    private static final long ACCOUNT_ID = TestConfig.ACCOUNT_RU;

    /**
     * 两边都应该有且相等的字段。
     */
    private static final Map<String, ToLongFunction<ShipInfo>> SHARED = new LinkedHashMap<>();

    static {
        SHARED.put("battle.battle", info -> info.battle().battle());
        SHARED.put("battle.wins", info -> info.battle().wins());
        SHARED.put("battle.losses", info -> info.battle().losses());
        SHARED.put("battle.survived", info -> info.battle().survived());
        SHARED.put("battle.winAndSurvived", info -> info.battle().winAndSurvived());
        SHARED.put("xp(会员经验)", ShipInfo::xp);
        SHARED.put("damageDealt", ShipInfo::damageDealt);
        SHARED.put("shipsSpotted", info -> info.shipsSpotted());
        SHARED.put("planesKilled", info -> info.planesKilled());
        SHARED.put("artAgro", ShipInfo::artAgro);
        SHARED.put("tpdAgro", ShipInfo::tpdAgro);
        SHARED.put("fragsInfo.frags", info -> info.fragsInfo().frags());
        SHARED.put("assistInfo.assistDamage", info -> info.assistInfo().assistDamage());
        SHARED.put("assistInfo.assistKills", info -> info.assistInfo().assistKills());
        SHARED.put("assistInfo.assistSpotKills", info -> info.assistInfo().assistSpotKills());
        SHARED.put("assistInfo.assistDamageKills", info -> info.assistInfo().assistDamageKills());
        SHARED.put("maxInfo.maxAssistDamage.value", info -> info.maxInfo().maxAssistDamage().value());
        SHARED.put("maxInfo.maxAssistKills.value", info -> info.maxInfo().maxAssistKills().value());
        SHARED.put("maxInfo.maxDamageDealt.value", info -> info.maxInfo().maxDamageDealt().value());
        SHARED.put("maxInfo.maxFrags.value", info -> info.maxInfo().maxFrags().value());
        SHARED.put("maxInfo.maxTotalAgro.value", info -> info.maxInfo().maxTotalAgro().value());
        SHARED.put("maxInfo.maxXp.value", info -> info.maxInfo().maxXp().value());
        SHARED.put("ratioMain.shots", info -> info.ratioMain().shots());
        SHARED.put("ratioMain.hits", info -> info.ratioMain().hits());
        SHARED.put("ratioAtba.shots(副炮)", info -> info.ratioAtba().shots());
        SHARED.put("ratioAtba.hits(副炮)", info -> info.ratioAtba().hits());
        SHARED.put("ratioTpd.shots", info -> info.ratioTpd().shots());
        SHARED.put("ratioTpd.hits", info -> info.ratioTpd().hits());
        SHARED.put("control.teamCaptured", info -> info.controlCapturedAndDroppedPoints().teamControlCapturedPoints());
        SHARED.put("control.teamDropped", info -> info.controlCapturedAndDroppedPoints().teamControlDroppedPoints());
    }

    private static int failed = 0;

    public static void main(String[] args) throws Exception {
        if (!Files.isRegularFile(DUMP_DIR.resolve("official-ships.json")) || !Files.isRegularFile(DUMP_DIR.resolve("ships-pvp.json"))) {
            System.out.println("数据来源: 无（缺少 " + DUMP_DIR.toAbsolutePath() + " 的 official-ships.json / ships-pvp.json），跳过");
            System.exit(0);
        }
        System.out.println("数据来源: " + DUMP_DIR.toAbsolutePath() + "（离线，莱服账号 " + ACCOUNT_ID + "）");

        Map<Long, ShipInfo> vortex = new LinkedHashMap<>();
        for (ShipInfo info : VortexUserShip.parse(WowsBattlesType.PVP, JsonUtils.json().parse(DUMP_DIR.resolve("ships-pvp.json").toFile())).toShipInfoList()) {
            vortex.put(info.shipId(), info);
        }
        Map<Long, ShipInfo> developers = new LinkedHashMap<>();
        DevelopersUserShip userShip = DevelopersUserShip.parse(JsonUtils.json().parse(DUMP_DIR.resolve("official-ships.json").toFile()));
        List<ShipInfo> devList = userShip.toShipInfoMap().get(WowsBattlesType.PVP);
        for (ShipInfo info : devList == null ? List.<ShipInfo>of() : devList) {
            developers.put(info.shipId(), info);
        }

        section("船集合");
        expect("官方API 账号ID", ACCOUNT_ID, userShip.accountId());
        expect("官方API 船数", 298, developers.size());
        expect("vortex 船数", 313, vortex.size());
        List<Long> onlyVortex = vortex.keySet().stream().filter(id -> !developers.containsKey(id)).toList();
        expect("仅 vortex 有的船都是没有战绩的", true, onlyVortex.stream().allMatch(id -> vortex.get(id).battle().battle() == 0));
        System.out.println("   仅 vortex: " + onlyVortex.size() + " 条；两边都有: " + developers.size() + " 条");

        section("逐船逐字段（" + SHARED.size() + " 项）");
        int compared = 0;
        Map<String, String> firstDiff = new LinkedHashMap<>();
        Map<String, Integer> diffCount = new LinkedHashMap<>();
        for (Map.Entry<Long, ShipInfo> entry : developers.entrySet()) {
            ShipInfo dev = entry.getValue();
            ShipInfo vor = vortex.get(entry.getKey());
            if (vor == null || dev.battle().battle() == 0 || vor.battle().battle() == 0) {
                continue;
            }
            compared++;
            for (Map.Entry<String, ToLongFunction<ShipInfo>> field : SHARED.entrySet()) {
                long a = field.getValue().applyAsLong(dev);
                long b = field.getValue().applyAsLong(vor);
                if (a != b) {
                    diffCount.merge(field.getKey(), 1, Integer::sum);
                    firstDiff.putIfAbsent(field.getKey(), "船" + entry.getKey() + " 官方=" + a + " vortex=" + b);
                }
            }
        }
        System.out.println("   比对 " + compared + " 条有战绩的船");
        if (diffCount.isEmpty()) {
            System.out.println("  [ok] 全部字段逐条相等");
        } else {
            failed += diffCount.size();
            diffCount.forEach((k, v) -> System.out.println("  [FAIL] " + k + " -> " + v + " 条不一致，例如 " + firstDiff.get(k)));
        }

        section("两条通道的字段能力（莱服）");
        long devMines = developers.values().stream().mapToLong(ShipInfo::minesAgro).sum();
        long vorMines = vortex.values().stream().mapToLong(ShipInfo::minesAgro).sum();
        expect("水雷潜在只有 vortex 有", 0L, devMines);
        expect("vortex 水雷潜在 > 0", true, vorMines > 0);
        expect("两边的来源标记", ShipInfoSource.DEVELOPERS + "/" + ShipInfoSource.VORTEX,
                developers.values().iterator().next().source() + "/" + vortex.values().iterator().next().source());
        //官方通道(api.korabli.su /mk/)整个就没有侦查伤害字段；vortex 单船有真实值
        expect("官方通道侦查伤害恒为 0(没有该字段)", 0L, developers.values().stream().mapToLong(ShipInfo::scoutingDamage).sum());
        long vortexScouting = vortex.values().stream().mapToLong(ShipInfo::scoutingDamage).sum();
        expect("vortex 单船侦查伤害 > 0", true, vortexScouting > 0);
        //莱服 vortex 的账号级汇总没有聚合侦查伤害，恒为 0（单船才有值），这里钉住这个坑
        JsonNode accountJson = JsonUtils.json().parse(DUMP_DIR.resolve("account.json").toFile());
        expect("vortex 账号级汇总 scouting_damage 恒为 0(莱服的坑)", 0L,
                accountJson.path("data").path(String.valueOf(ACCOUNT_ID)).path("statistics").path("pvp").path("scouting_damage").asLong(0));
        long assist = vortex.values().stream().mapToLong(i -> i.assistInfo().assistDamage()).sum();
        expect("vortex 助攻伤害 > 0", true, assist > 0);

        System.out.println();
        System.out.println(failed == 0 ? "结果: 莱服两条通道一致" : "结果: " + failed + " 处不一致");
        System.exit(failed == 0 ? 0 : 1);
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
