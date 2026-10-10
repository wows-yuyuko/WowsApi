package com.shinoaki.wows;

import com.shinoaki.wows.api.data.AccountInfo;
import com.shinoaki.wows.api.data.ship.ShipExpansion;
import com.shinoaki.wows.api.developers.account.DevelopersSearchUser;
import com.shinoaki.wows.api.developers.account.DevelopersUserInfoPrivate;
import com.shinoaki.wows.api.developers.account.statistics.DevelopersUserInfoStatistics;
import com.shinoaki.wows.api.developers.clan.DevelopersClanInfo;
import com.shinoaki.wows.api.developers.clan.DevelopersSearchClan;
import com.shinoaki.wows.api.developers.clan.DevelopersSearchUserClan;
import com.shinoaki.wows.api.developers.clan.seasion.DevelopersSeasonInfo;
import com.shinoaki.wows.api.developers.clan.seasion.DevelopersSeasonLeaguesInfo;
import com.shinoaki.wows.api.developers.encyclopedia.glossary.DevelopersGlossary;
import com.shinoaki.wows.api.developers.encyclopedia.glossary.DevelopersGlossaryBuilding;
import com.shinoaki.wows.api.developers.encyclopedia.glossary.DevelopersGlossaryBuildingType;
import com.shinoaki.wows.api.developers.encyclopedia.glossary.DevelopersGlossarySettings;
import com.shinoaki.wows.api.developers.warships.DevelopersDataBattery;
import com.shinoaki.wows.api.developers.warships.DevelopersDataBatteryV2;
import com.shinoaki.wows.api.developers.warships.DevelopersMeta;
import com.shinoaki.wows.api.developers.warships.DevelopersShipBattleInfo;
import com.shinoaki.wows.api.developers.warships.DevelopersShipOperBattleInfo;
import com.shinoaki.wows.api.developers.warships.type.DevelopersOperBattleType;
import com.shinoaki.wows.api.developers.warships.type.DevelopersShipBattleType;
import com.shinoaki.wows.api.type.WowsBattlesType;
import com.shinoaki.wows.api.utils.JsonUtils;
import tools.jackson.databind.JsonNode;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.RecordComponent;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * 官方开发者API「响应字段是否都有对应结构体」的校验。
 * <p>
 * 数据用监视采集到的原始响应（{@code target/schema-raw/api_asia_*.json}），没有采集时跳过对应接口。
 * 做法与 {@link VortexFieldCoverageTest} 相同：逐层比对 JSON 字段名与结构体声明的字段名。
 * <p>
 * 用法：{@code java -cp ... com.shinoaki.wows.DevelopersFieldCoverageTest}；退出码 0 = 全部字段都有结构体。
 *
 * @author Xun
 */
public class DevelopersFieldCoverageTest {

    private static final Path RAW = Path.of("target/schema-raw");
    private static final String ACC = "api_asia_wows_account_info_account_id_id_extra_private_grouped_c_c5266c85.json";
    private static final String SHIPS = "api_asia_wows_ships_stats_account_id_id_extra_pvp_div2_pvp_div3__4c0b4c30.json";
    private static final String OPER = "api_asia_wows_ships_stats_account_id_id_extra_oper_div_oper_div__6a74bb79.json";

    private static int failed = 0;
    private static int checked = 0;

    public static void main(String[] args) throws Exception {
        System.out.println("数据来源: " + RAW.toAbsolutePath() + "（监视采集的原始响应）");
        checkAccountList();
        checkAccountInfo();
        checkShipsStats();
        checkShipsOper();
        checkBadges();
        checkClanInfo();
        checkClanList();
        checkClanAccountInfo();
        checkSeason();
        checkGlossary();

        System.out.println();
        System.out.println(failed == 0 ? "结果: 已检查 " + checked + " 处节点，全部响应字段都有对应结构体"
                : "结果: " + failed + " 处字段缺少结构体（已检查 " + checked + " 处）");
        System.exit(failed == 0 ? 0 : 1);
    }

    //----------------------------------------------------------------- 各接口

    private static void checkAccountList() throws Exception {
        JsonNode root = load("api_asia_wows_account_list_search_text_.json");
        if (root == null) {
            return;
        }
        section("/wows/account/list/ 玩家搜索");
        unknown(root.path("meta"), DevelopersMeta.class, "meta");
        unknown(root.path("data").path(0), DevelopersSearchUser.class, "data[0]");
    }

    private static void checkAccountInfo() throws Exception {
        JsonNode root = load(ACC);
        if (root == null) {
            return;
        }
        section("/wows/account/info/ 账号详情");
        JsonNode data = onlyData(root);
        //结构体里这几个字段是驼峰命名(手工映射)
        unknown(data, AccountInfo.class, "data.<accountId>", Set.of("account_id", "created_at", "hidden_profile",
                "last_battle_time", "stats_updated_at", "logout_at", "nickname", "private", "statistics",
                "leveling_points", "leveling_tier", "updated_at"));
        unknown(data.path("private"), DevelopersUserInfoPrivate.class, "data.<accountId>.private");
        unknown(data.path("statistics"), DevelopersUserInfoStatistics.class, "data.<accountId>.statistics");
        for (Map.Entry<String, JsonNode> entry : data.path("statistics").properties()) {
            battleBlock(entry.getValue(), "statistics." + entry.getKey());
        }
        AccountInfo info = AccountInfo.parse(firstKey(root.path("data")), root.toString(), null);
        expect("karma", data.path("karma").asInt(0), info.karma());
        expect("leveling_tier", data.path("leveling_tier").asInt(0), info.levelingTier());
        expect("updated_at", data.path("updated_at").asLong(0), info.updatedAt());
        expect("statistics.pvp.battles", data.path("statistics").path("pvp").path("battles").asLong(0), info.statistics().pvp().battles());
        expect("statistics.pvp.max_xp_ship_id", data.path("statistics").path("pvp").path("max_xp_ship_id").asLong(0), info.statistics().pvp().max_xp_ship_id());
        expect("statistics.pvp.main_battery.shots", data.path("statistics").path("pvp").path("main_battery").path("shots").asInt(0),
                info.statistics().pvp().main_battery().shots());
        expect("statistics.pvp.main_battery.max_frags_ship_id", data.path("statistics").path("pvp").path("main_battery").path("max_frags_ship_id").asLong(0),
                info.statistics().pvp().main_battery().max_frags_ship_id());
        expect("statistics.distance", data.path("statistics").path("distance").asInt(0), info.statistics().distance());
    }

    private static void checkShipsStats() throws Exception {
        JsonNode root = load(SHIPS);
        if (root == null) {
            return;
        }
        section("/wows/ships/stats/ 战舰统计");
        unknown(root.path("meta"), DevelopersMeta.class, "meta");
        JsonNode items = onlyData(root);
        //shipBattleTypeMap 手工映射；private 对应 privateInfo
        Set<String> allowed = new LinkedHashSet<>(Set.of("private"));
        for (WowsBattlesType type : WowsBattlesType.values()) {
            allowed.add(type.name().toLowerCase(Locale.ROOT));
        }
        int blocks = 0;
        for (JsonNode item : items) {
            unknown(item, DevelopersShipBattleInfo.class, "data.<accountId>[" + item.path("ship_id").asLong() + "]", allowed);
            for (WowsBattlesType type : WowsBattlesType.values()) {
                JsonNode block = item.get(type.name().toLowerCase(Locale.ROOT));
                if (block != null && block.isObject() && !block.isEmpty()) {
                    battleBlock(block, "ship." + item.path("ship_id").asLong() + "." + type.name().toLowerCase(Locale.ROOT));
                    blocks++;
                }
            }
        }
        System.out.println("   战舰 " + items.size() + " 条, 战斗数据块 " + blocks + " 个");
        List<DevelopersShipBattleInfo> parsed = DevelopersShipBattleInfo.parse(items);
        expect("解析条数", items.size(), parsed.size());
        JsonNode first = items.path(0);
        expect("ship_id", first.path("ship_id").asLong(0), parsed.get(0).ship_id());
        expect("battles", first.path("battles").asInt(0), parsed.get(0).battles());
        expect("updated_at", first.path("updated_at").asLong(0), parsed.get(0).updated_at());
    }

    private static void checkShipsOper() throws Exception {
        JsonNode root = load(OPER);
        if (root == null) {
            return;
        }
        section("/wows/ships/stats/(oper) 行动模式统计");
        JsonNode items = onlyData(root);
        Set<String> allowed = Set.of("private", "oper_div", "oper_div_hard", "oper_solo", "pvp");
        for (JsonNode item : items) {
            unknown(item, DevelopersShipOperBattleInfo.class, "data.<accountId>[" + item.path("ship_id").asLong() + "]", allowed);
            unknown(item.path("oper_solo"), DevelopersOperBattleType.class, "oper_solo");
            JsonNode pvp = item.path("pvp");
            if (pvp.isObject() && !pvp.isEmpty()) {
                unknown(pvp, DevelopersShipBattleType.class, "pvp");
            }
        }
        List<DevelopersShipOperBattleInfo> parsed = DevelopersShipOperBattleInfo.parse(items);
        expect("解析条数", items.size(), parsed.size());
        JsonNode first = items.path(0);
        expect("oper_solo.xp", first.path("oper_solo").path("xp").asLong(0), parsed.get(0).shipBattleTypeMap().get("oper_solo").xp());
        expect("pvp.xp", first.path("pvp").path("xp").asLong(0), parsed.get(0).pvp().xp());
    }

    private static void checkBadges() throws Exception {
        JsonNode root = load("api_asia_wows_ships_badges_account_id_id_.json");
        if (root == null) {
            return;
        }
        section("/wows/ships/badges/ 战舰徽章");
        unknown(root.path("meta"), DevelopersMeta.class, "meta");
        JsonNode items = onlyData(root);
        unknown(items.path(0), ShipExpansion.class, "data.<accountId>[0]", Set.of("ship_id", "top_grade_class"));
        Map<Long, ShipExpansion> map = ShipExpansion.parseDevelopers(firstKey(root.path("data")), root);
        expect("徽章数量", items.size(), map.size());
        expect("top_grade_class", items.path(0).path("top_grade_class").asInt(5), map.get(items.path(0).path("ship_id").asLong()).topGrade());
    }

    private static void checkClanInfo() throws Exception {
        JsonNode root = load("api_asia_wows_clans_info_clan_id_id_extra_members.json");
        if (root == null) {
            return;
        }
        section("/wows/clans/info/ 军团详情");
        JsonNode data = onlyData(root);
        unknown(data, DevelopersClanInfo.class, "data.<clanId>");
        JsonNode members = data.path("members");
        if (members.isObject() && !members.isEmpty()) {
            unknown(members.path(members.properties().iterator().next().getKey()),
                    DevelopersClanInfo.DevelopersClanUserInfo.class, "members.<accountId>");
        }
        DevelopersClanInfo info = DevelopersClanInfo.parse(firstKey(root.path("data")), root.toString());
        expect("members_ids 数量", data.path("members_ids").size(), info.members_ids().size());
        expect("members 数量", members.size(), info.members().size());
    }

    private static void checkClanList() throws Exception {
        JsonNode root = load("api_asia_wows_clans_list_search_text_.json");
        if (root == null) {
            return;
        }
        section("/wows/clans/list/ 军团搜索");
        unknown(root.path("meta"), DevelopersMeta.class, "meta");
        unknown(root.path("data").path(0), DevelopersSearchClan.class, "data[0]");
        List<DevelopersSearchClan> list = DevelopersSearchClan.parse(root.toString());
        expect("军团数量", root.path("data").size(), list.size());
    }

    private static void checkClanAccountInfo() throws Exception {
        JsonNode root = load("api_asia_wows_clans_accountinfo_account_id_id_extra_clan.json");
        if (root == null) {
            return;
        }
        section("/wows/clans/accountinfo/ 账号所属军团");
        JsonNode data = onlyData(root);
        unknown(data, DevelopersSearchUserClan.class, "data.<accountId>");
        unknown(data.path("clan"), DevelopersSearchUserClan.Clan.class, "data.<accountId>.clan");
        DevelopersSearchUserClan info = DevelopersSearchUserClan.parse(firstKey(root.path("data")), root.toString());
        expect("role", data.path("role").asString(""), info.role());
        expect("clan.name", data.path("clan").path("name").asString(""), info.clan().name());
    }

    private static void checkSeason() throws Exception {
        JsonNode root = load("api_asia_wows_clans_season_.json");
        if (root == null) {
            return;
        }
        section("/wows/clans/season/ 军团赛季");
        JsonNode data = onlyData(root);
        unknown(data, DevelopersSeasonInfo.class, "data.<seasonId>");
        unknown(data.path("leagues").path(0), DevelopersSeasonLeaguesInfo.class, "leagues[0]");
        List<DevelopersSeasonInfo> list = DevelopersSeasonInfo.parse(root.toString());
        expect("赛季数量", root.path("data").size(), list.size());
    }

    private static void checkGlossary() throws Exception {
        JsonNode root = load("api_asia_wows_clans_glossary_.json");
        if (root == null) {
            return;
        }
        section("/wows/clans/glossary/ 军团词典");
        JsonNode data = root.path("data");
        unknown(data, DevelopersGlossary.class, "data");
        JsonNode buildingTypes = data.path("building_types");
        if (!buildingTypes.isEmpty()) {
            unknown(buildingTypes.path(buildingTypes.properties().iterator().next().getKey()),
                    DevelopersGlossaryBuildingType.class, "building_types.<id>");
        }
        JsonNode buildings = data.path("buildings");
        if (!buildings.isEmpty()) {
            unknown(buildings.path(buildings.properties().iterator().next().getKey()),
                    DevelopersGlossaryBuilding.class, "buildings.<id>");
        }
        unknown(data.path("settings"), DevelopersGlossarySettings.class, "settings");
        DevelopersGlossary glossary = DevelopersGlossary.parse(root.toString());
        expect("building_types 数量", buildingTypes.size(), glossary.building_types().size());
        expect("buildings 数量", buildings.size(), glossary.buildings().size());
        expect("clans_roles 数量", data.path("clans_roles").size(), glossary.clans_roles().size());
        expect("settings.max_members_count", data.path("settings").path("max_members_count").asInt(0), glossary.settings().max_members_count());
    }

    //----------------------------------------------------------------- 工具

    /**
     * 战斗数据块：主体 + 5 个武器子块。
     */
    private static void battleBlock(JsonNode block, String label) {
        if (block == null || !block.isObject() || block.isEmpty() || !block.has("battles")) {
            return;
        }
        if (block.has("wins_by_tasks")) {
            //行动模式(oper_*)的数据块，字段与随机战不同
            unknown(block, DevelopersOperBattleType.class, label);
            return;
        }
        unknown(block, DevelopersShipBattleType.class, label);
        unknown(block.path("main_battery"), DevelopersDataBattery.class, label + ".main_battery");
        unknown(block.path("second_battery"), DevelopersDataBattery.class, label + ".second_battery");
        unknown(block.path("torpedoes"), DevelopersDataBattery.class, label + ".torpedoes");
        unknown(block.path("aircraft"), DevelopersDataBatteryV2.class, label + ".aircraft");
        unknown(block.path("ramming"), DevelopersDataBatteryV2.class, label + ".ramming");
    }

    private static void unknown(JsonNode node, Class<?> type, String label) {
        unknown(node, type, label, Set.of());
    }

    private static void unknown(JsonNode node, Class<?> type, String label, Set<String> allowed) {
        if (node == null || !node.isObject() || node.isEmpty()) {
            return;
        }
        checked++;
        Set<String> known = type == null ? new LinkedHashSet<>() : names(type);
        known.addAll(allowed);
        List<String> missing = new ArrayList<>();
        for (Map.Entry<String, JsonNode> entry : node.properties()) {
            if (!known.contains(entry.getKey())) {
                missing.add(entry.getKey());
            }
        }
        if (missing.isEmpty()) {
            System.out.println("  [ok] " + label + " (" + node.size() + " 字段)");
        } else {
            failed++;
            System.out.println("  [缺少结构体] " + label + " -> " + missing);
        }
    }

    private static Set<String> names(Class<?> type) {
        Set<String> names = new LinkedHashSet<>();
        if (type.isRecord()) {
            for (RecordComponent component : type.getRecordComponents()) {
                names.add(component.getName());
            }
        } else {
            for (Field field : type.getDeclaredFields()) {
                if (!Modifier.isStatic(field.getModifiers())) {
                    names.add(field.getName());
                }
            }
        }
        return names;
    }

    /**
     * data 下第一个动态 key(账号ID/军团ID/赛季ID)。
     */
    private static long firstKey(JsonNode data) {
        for (Map.Entry<String, JsonNode> entry : data.properties()) {
            return Long.parseLong(entry.getKey());
        }
        throw new IllegalStateException("data 节点是空的");
    }

    private static JsonNode onlyData(JsonNode root) {
        for (Map.Entry<String, JsonNode> entry : root.path("data").properties()) {
            return entry.getValue();
        }
        throw new IllegalStateException("响应里没有 data 节点");
    }

    private static JsonNode load(String file) {
        Path path = RAW.resolve(file);
        if (!Files.isRegularFile(path)) {
            System.out.println("跳过（没有采集）: " + file);
            return null;
        }
        try {
            return JsonUtils.json().parse(path.toFile());
        } catch (Exception e) {
            throw new IllegalStateException("解析失败: " + file, e);
        }
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
