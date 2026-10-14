package com.shinoaki.wows.field_coverage;

import com.shinoaki.wows.api.WowsApi;
import com.shinoaki.wows.api.codec.HttpCodec;
import com.shinoaki.wows.api.codec.http.WowsEncyclopediaTools;
import com.shinoaki.wows.api.codec.http.WowsHttpClanTools;
import com.shinoaki.wows.api.codec.http.WowsHttpShipTools;
import com.shinoaki.wows.api.codec.http.WowsHttpUserTools;
import com.shinoaki.wows.api.data.AccountInfo;
import com.shinoaki.wows.api.data.ship.ShipExpansion;
import com.shinoaki.wows.api.developers.account.DevelopersSearchUser;
import com.shinoaki.wows.api.developers.account.DevelopersUserAchievements;
import com.shinoaki.wows.api.developers.account.DevelopersUserInfoPrivate;
import com.shinoaki.wows.api.developers.account.statistics.DevelopersUserInfoStatistics;
import com.shinoaki.wows.api.developers.clan.DevelopersClanInfo;
import com.shinoaki.wows.api.developers.clan.DevelopersClanSeasonPlayer;
import com.shinoaki.wows.api.developers.clan.DevelopersClanSeasonStats;
import com.shinoaki.wows.api.developers.clan.DevelopersSearchClan;
import com.shinoaki.wows.api.developers.clan.DevelopersSearchUserClan;
import com.shinoaki.wows.api.developers.clan.seasion.DevelopersSeasonInfo;
import com.shinoaki.wows.api.developers.clan.seasion.DevelopersSeasonLeaguesInfo;
import com.shinoaki.wows.api.developers.encyclopedia.accountlevels.DevelopersAccountLevel;
import com.shinoaki.wows.api.developers.encyclopedia.achievements.DevelopersEncyclopediaAchievement;
import com.shinoaki.wows.api.developers.encyclopedia.achievements.DevelopersEncyclopediaAchievements;
import com.shinoaki.wows.api.developers.encyclopedia.battlearenas.DevelopersBattleArena;
import com.shinoaki.wows.api.developers.encyclopedia.battletypes.DevelopersBattleType;
import com.shinoaki.wows.api.developers.encyclopedia.collections.DevelopersCollection;
import com.shinoaki.wows.api.developers.encyclopedia.collections.DevelopersCollectionCard;
import com.shinoaki.wows.api.developers.encyclopedia.collections.DevelopersCollectionCardImages;
import com.shinoaki.wows.api.developers.encyclopedia.consumables.DevelopersConsumableEffect;
import com.shinoaki.wows.api.developers.encyclopedia.consumables.DevelopersEncyclopediaConsumable;
import com.shinoaki.wows.api.developers.encyclopedia.consumables.DevelopersEncyclopediaConsumables;
import com.shinoaki.wows.api.developers.encyclopedia.crewranks.DevelopersCommanderRank;
import com.shinoaki.wows.api.developers.encyclopedia.crews.DevelopersEncyclopediaCommander;
import com.shinoaki.wows.api.developers.encyclopedia.crewskills.DevelopersEncyclopediaSkill;
import com.shinoaki.wows.api.developers.encyclopedia.crewskills.DevelopersSkillCustomization;
import com.shinoaki.wows.api.developers.encyclopedia.crewskills.DevelopersSkillPerk;
import com.shinoaki.wows.api.developers.encyclopedia.glossary.DevelopersGlossary;
import com.shinoaki.wows.api.developers.encyclopedia.glossary.DevelopersGlossaryBuilding;
import com.shinoaki.wows.api.developers.encyclopedia.glossary.DevelopersGlossaryBuildingType;
import com.shinoaki.wows.api.developers.encyclopedia.glossary.DevelopersGlossarySettings;
import com.shinoaki.wows.api.developers.encyclopedia.info.DevelopersEncyclopediaInfo;
import com.shinoaki.wows.api.developers.encyclopedia.info.DevelopersShipTypeImages;
import com.shinoaki.wows.api.developers.encyclopedia.modules.DevelopersEncyclopediaModule;
import com.shinoaki.wows.api.developers.encyclopedia.modules.DevelopersEncyclopediaModules;
import com.shinoaki.wows.api.developers.encyclopedia.modules.DevelopersModuleArtillery;
import com.shinoaki.wows.api.developers.encyclopedia.modules.DevelopersModuleProfile;
import com.shinoaki.wows.api.developers.encyclopedia.modules.DevelopersModuleTorpedoes;
import com.shinoaki.wows.api.developers.encyclopedia.shipprofile.DevelopersShipAASlot;
import com.shinoaki.wows.api.developers.encyclopedia.shipprofile.DevelopersShipAntiAircraftProfile;
import com.shinoaki.wows.api.developers.encyclopedia.shipprofile.DevelopersShipArmourProfile;
import com.shinoaki.wows.api.developers.encyclopedia.shipprofile.DevelopersShipArtilleryProfile;
import com.shinoaki.wows.api.developers.encyclopedia.shipprofile.DevelopersShipArtillerySlot;
import com.shinoaki.wows.api.developers.encyclopedia.shipprofile.DevelopersShipAtbaProfile;
import com.shinoaki.wows.api.developers.encyclopedia.shipprofile.DevelopersShipAtbaSlot;
import com.shinoaki.wows.api.developers.encyclopedia.shipprofile.DevelopersShipConcealment;
import com.shinoaki.wows.api.developers.encyclopedia.shipprofile.DevelopersShipDepthCharge;
import com.shinoaki.wows.api.developers.encyclopedia.shipprofile.DevelopersShipDiveBomber;
import com.shinoaki.wows.api.developers.encyclopedia.shipprofile.DevelopersShipEngineProfile;
import com.shinoaki.wows.api.developers.encyclopedia.shipprofile.DevelopersShipFighters;
import com.shinoaki.wows.api.developers.encyclopedia.shipprofile.DevelopersShipFireControl;
import com.shinoaki.wows.api.developers.encyclopedia.shipprofile.DevelopersShipFlightControl;
import com.shinoaki.wows.api.developers.encyclopedia.shipprofile.DevelopersShipHullProfile;
import com.shinoaki.wows.api.developers.encyclopedia.shipprofile.DevelopersShipIntRange;
import com.shinoaki.wows.api.developers.encyclopedia.shipprofile.DevelopersShipMobility;
import com.shinoaki.wows.api.developers.encyclopedia.shipprofile.DevelopersShipProfile;
import com.shinoaki.wows.api.developers.encyclopedia.shipprofile.DevelopersShipShell;
import com.shinoaki.wows.api.developers.encyclopedia.shipprofile.DevelopersShipSonarProfile;
import com.shinoaki.wows.api.developers.encyclopedia.shipprofile.DevelopersShipSubmarineBattery;
import com.shinoaki.wows.api.developers.encyclopedia.shipprofile.DevelopersShipSubmarineMobility;
import com.shinoaki.wows.api.developers.encyclopedia.shipprofile.DevelopersShipTorpedoBomber;
import com.shinoaki.wows.api.developers.encyclopedia.shipprofile.DevelopersShipTorpedoProfile;
import com.shinoaki.wows.api.developers.encyclopedia.shipprofile.DevelopersShipTorpedoSlot;
import com.shinoaki.wows.api.developers.encyclopedia.shipprofile.DevelopersShipWeaponry;
import com.shinoaki.wows.api.developers.encyclopedia.ships.DevelopersEncyclopediaShip;
import com.shinoaki.wows.api.developers.encyclopedia.ships.DevelopersEncyclopediaShips;
import com.shinoaki.wows.api.developers.encyclopedia.ships.DevelopersShipImages;
import com.shinoaki.wows.api.developers.encyclopedia.ships.DevelopersShipModuleTree;
import com.shinoaki.wows.api.developers.encyclopedia.ships.DevelopersShipModules;
import com.shinoaki.wows.api.developers.seasons.DevelopersRankedAccount;
import com.shinoaki.wows.api.developers.seasons.DevelopersRankedLeague;
import com.shinoaki.wows.api.developers.seasons.DevelopersRankedLeagueImages;
import com.shinoaki.wows.api.developers.seasons.DevelopersRankedRank;
import com.shinoaki.wows.api.developers.seasons.DevelopersRankedRankImages;
import com.shinoaki.wows.api.developers.seasons.DevelopersRankedRankInfo;
import com.shinoaki.wows.api.developers.seasons.DevelopersRankedSeason;
import com.shinoaki.wows.api.developers.seasons.DevelopersRankedShipSeason;
import com.shinoaki.wows.api.developers.seasons.DevelopersRankedShipSeasonBlock;
import com.shinoaki.wows.api.developers.seasons.DevelopersRankedShipStats;
import com.shinoaki.wows.api.developers.warships.DevelopersDataBattery;
import com.shinoaki.wows.api.developers.warships.DevelopersDataBatteryV2;
import com.shinoaki.wows.api.developers.warships.DevelopersMeta;
import com.shinoaki.wows.api.developers.warships.DevelopersShipBattleInfo;
import com.shinoaki.wows.api.developers.warships.DevelopersShipOperBattleInfo;
import com.shinoaki.wows.api.developers.warships.type.DevelopersOperBattleType;
import com.shinoaki.wows.api.developers.warships.type.DevelopersShipBattleType;
import com.shinoaki.wows.api.monitor.SchemaRecorder;
import com.shinoaki.wows.api.type.WowsBattlesType;
import com.shinoaki.wows.api.type.WowsServer;
import com.shinoaki.wows.api.utils.JsonUtils;
import com.shinoaki.wows.testkit.TestConfig;
import tools.jackson.databind.JsonNode;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.RecordComponent;
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
import java.util.stream.Stream;

/**
 * 官方开发者API「响应字段是否都有对应结构体」的校验。
 * <p>
 * 数据优先用监视采集到的原始响应（{@code target/schema-raw/api_asia_*.json}）；
 * 没有采集时用下面的亚服测试账号与 token 现场抓一次（走 {@link SchemaRecorder} 钩子，拿到的就是库真实请求的返回）。
 * 做法与 {@link VortexFieldCoverageTest} 相同：逐层比对 JSON 字段名与结构体声明的字段名。
 * <p>
 * 用法：{@code java -cp ... com.shinoaki.wows.field_coverage.DevelopersFieldCoverageTest}；退出码 0 = 全部字段都有结构体。
 * <br>可选参数：{@code -Dcoverage.live=true} 强制现场抓取（忽略本地采集）、{@code -Draw.dir=<目录>} 指定采集目录。
 *
 * @author Xun
 */
public class DevelopersFieldCoverageTest {
    private static final String token = TestConfig.appId(WowsServer.ASIA);
    private static final long account_id = TestConfig.ACCOUNT_ASIA;
    private static final WowsServer server = WowsServer.ASIA;

    private static final Path RAW = Path.of(System.getProperty("raw.dir", "target/schema-raw"));
    /**
     * 采集文件名(监视的 SchemaRawStore 命名规则)，前缀用于容忍 key 变化导致的哈希后缀变化。
     */
    private static final String ACC = "api_asia_wows_account_info_account_id_id_extra_private_grouped_c_c5266c85.json";
    private static final String SHIPS = "api_asia_wows_ships_stats_account_id_id_extra_pvp_div2_pvp_div3__4c0b4c30.json";
    /**
     * 全量战绩的原始响应（2026-10 全量 extra 采集，单船 15 块）；key 哈希后缀变化时按前缀兜底。
     */
    private static final String OPER = "api_asia_wows_ships_stats_account_id_id_extra_clan_oper_div_oper_bae61ea0.json";
    private static final long CLAN_ID = TestConfig.CLAN_ASIA;
    private static final String SEARCH_USER = TestConfig.SEARCH_USER_ASIA;
    private static final String SEARCH_CLAN = TestConfig.SEARCH_CLAN;

    /**
     * 本地没有采集时的现场抓取表(文件 -> 调用)。
     * <p>
     * 用 {@link SchemaRecorder} 挂钩子采集原始响应，所以拿到的就是库真实请求的返回；
     * 抓到后只放在内存里，不写回 {@code target/schema-raw}(那是监视的仓库，由 SchemaMonitorTest 维护)。
     */
    private static final Map<String, Endpoint> FETCH = new LinkedHashMap<>();
    private static final Map<String, JsonNode> LIVE = new LinkedHashMap<>();

    private static int failed = 0;
    private static int checked = 0;

    public static void main(String[] args) throws Exception {
        registry();
        System.out.println("采集目录: " + RAW.toAbsolutePath() + (Boolean.getBoolean("coverage.live") ? "（已忽略，强制现场抓取）" : ""));
        System.out.println("测试账号: " + server + " / " + account_id);
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
        //3.0 新增端点（官方 5 分区其余接口）
        checkUserAchievements();
        checkEncyclopediaInfo();
        checkEncyclopediaShips();
        checkShipProfile();
        checkEncyclopediaModules();
        checkEncyclopediaConsumables();
        checkEncyclopediaAccountLevels();
        checkEncyclopediaCommanders();
        checkEncyclopediaCommanderSkills();
        checkEncyclopediaCommanderRanks();
        checkEncyclopediaBattleTypes();
        checkEncyclopediaCollections();
        checkEncyclopediaCollectionCards();
        checkEncyclopediaBattleArenas();
        checkEncyclopediaAchievements();
        checkRankedSeasons();
        checkRankedShipStats();
        checkRankedAccountStats();
        checkClanSeasonStats();

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
        section("/wows/ships/stats/(全量) 战舰战绩");
        JsonNode items = onlyData(root);
        //顶层不限定战斗类型：响应里出现的对象块全部放行，块内字段逐个核对
        Set<String> allowed = new LinkedHashSet<>(Set.of("private"));
        for (JsonNode item : items) {
            for (Map.Entry<String, JsonNode> entry : item.properties()) {
                if (entry.getValue().isObject() && !"private".equals(entry.getKey())) {
                    allowed.add(entry.getKey());
                }
            }
        }
        int blocks = 0;
        for (JsonNode item : items) {
            unknown(item, DevelopersShipOperBattleInfo.class, "data.<accountId>[" + item.path("ship_id").asLong() + "]", allowed);
            for (Map.Entry<String, JsonNode> entry : item.properties()) {
                if (entry.getValue().isObject() && !"private".equals(entry.getKey())) {
                    battleBlock(entry.getValue(), "ship." + item.path("ship_id").asLong() + "." + entry.getKey());
                    blocks++;
                }
            }
        }
        System.out.println("   战舰 " + items.size() + " 条, 战斗数据块 " + blocks + " 个");
        List<DevelopersShipOperBattleInfo> parsed = DevelopersShipOperBattleInfo.parse(items);
        expect("解析条数", items.size(), parsed.size());
        JsonNode first = items.path(0);
        int firstBlocks = 0;
        for (Map.Entry<String, JsonNode> entry : first.properties()) {
            if (entry.getValue().isObject() && !"private".equals(entry.getKey())) {
                firstBlocks++;
            }
        }
        expect("首船解析块数", firstBlocks, parsed.get(0).battleTypeMap().size() + parsed.get(0).operBattleTypeMap().size());
        expect("oper_solo.xp", first.path("oper_solo").path("xp").asLong(0), parsed.get(0).operBattleTypeMap().get("oper_solo").xp());
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

    //----------------------------------------------------------------- 3.0 新增端点

    private static void checkUserAchievements() throws Exception {
        JsonNode root = load("dev_account_achievements.json");
        if (root == null) {
            return;
        }
        section("/wows/account/achievements/ 玩家成就");
        unknown(root.path("meta"), DevelopersMeta.class, "meta");
        JsonNode data = onlyData(root);
        DevelopersUserAchievements parsed = DevelopersUserAchievements.parse(root);
        expect("account_id", firstKey(root.path("data")), parsed.accountId());
        expect("battle 数量", data.path("battle").size(), parsed.battle().size());
        expect("progress 数量", data.path("progress").size(), parsed.progress().size());
        for (var entry : data.path("battle").properties()) {
            expect("battle[" + entry.getKey() + "]", entry.getValue().asLong(0), parsed.battle().get(entry.getKey()));
            break;
        }
    }

    private static void checkEncyclopediaInfo() throws Exception {
        JsonNode root = load("dev_encyclopedia_info.json");
        if (root == null) {
            return;
        }
        section("/wows/encyclopedia/info/ 百科信息");
        JsonNode data = root.path("data");
        unknown(data, DevelopersEncyclopediaInfo.class, "data");
        sampleValue(data.path("ship_type_images"), DevelopersShipTypeImages.class, "data.ship_type_images.<type>");
        DevelopersEncyclopediaInfo parsed = DevelopersEncyclopediaInfo.parse(root);
        expect("languages 数量", data.path("languages").size(), parsed.languages().size());
        expect("ship_modifications 数量", data.path("ship_modifications").size(), parsed.ship_modifications().size());
        expect("ship_nations 数量", data.path("ship_nations").size(), parsed.ship_nations().size());
        expect("game_version", data.path("game_version").asString(""), parsed.game_version());
        expect("ships_updated_at", data.path("ships_updated_at").asLong(0), parsed.ships_updated_at());
    }

    private static void checkEncyclopediaShips() throws Exception {
        JsonNode root = load("dev_encyclopedia_ships.json");
        if (root == null) {
            return;
        }
        section("/wows/encyclopedia/ships/ 舰船字典（分页）");
        unknown(root.path("meta"), DevelopersMeta.class, "meta");
        JsonNode data = root.path("data");
        int count = 0;
        for (var entry : data.properties()) {
            JsonNode ship = entry.getValue();
            String label = "data.<shipId>[" + entry.getKey() + "]";
            unknown(ship, DevelopersEncyclopediaShip.class, label);
            unknown(ship.path("images"), DevelopersShipImages.class, label + ".images");
            unknown(ship.path("modules"), DevelopersShipModules.class, label + ".modules");
            count++;
        }
        //default_profile 全量结构抽查第一艘（其余舰船同构；完整参数可看 shipprofile 段）
        JsonNode first = firstValue(data);
        if (first != null) {
            profileBlock(first.path("default_profile"), "data.<shipId>.default_profile");
            unknown(firstValue(first.path("modules_tree")), DevelopersShipModuleTree.class, "data.<shipId>.modules_tree.<moduleId>");
        }
        DevelopersEncyclopediaShips parsed = DevelopersEncyclopediaShips.parse(root);
        expect("解析条数", data.size(), parsed.ships().size());
        expect("meta.page_total", root.path("meta").path("page_total").asInt(0), parsed.meta().page_total());
        expect("meta.total", root.path("meta").path("total").asInt(0), parsed.meta().total());
        JsonNode firstShip = first;
        if (firstShip != null) {
            long id = firstShip.path("ship_id").asLong(0);
            expect("首船名称", firstShip.path("name").asString(""), parsed.ships().get(id).name());
            expect("首船 default_profile.hull.health", firstShip.path("default_profile").path("hull").path("health").asInt(0),
                    parsed.ships().get(id).default_profile().hull().health());
        }
        System.out.println("   舰船 " + count + " 条");
    }

    private static void checkShipProfile() throws Exception {
        JsonNode root = load("dev_encyclopedia_shipprofile.json");
        if (root == null) {
            return;
        }
        section("/wows/encyclopedia/shipprofile/ 舰船参数明细");
        JsonNode data = onlyData(root);
        profileBlock(data, "data.<shipId>");
        DevelopersShipProfile parsed = DevelopersShipProfile.parse(root);
        expect("ship_id", data.path("ship_id").asLong(0), parsed.ship_id());
        expect("hull.health", data.path("hull").path("health").asInt(0), parsed.hull().health());
        expect("armour.health", data.path("armour").path("health").asInt(0), parsed.armour().health());
        expect("artillery.shells 数量", data.path("artillery").path("shells").size(), parsed.artillery().shells().size());
        expect("mobility 数量一致", data.path("mobility").path("total").asInt(0), parsed.mobility().total());
    }

    private static void checkEncyclopediaModules() throws Exception {
        JsonNode root = load("dev_encyclopedia_modules.json");
        if (root == null) {
            return;
        }
        section("/wows/encyclopedia/modules/ 模块字典（分页）");
        unknown(root.path("meta"), DevelopersMeta.class, "meta");
        JsonNode data = root.path("data");
        for (var entry : data.properties()) {
            JsonNode module = entry.getValue();
            String label = "data.<moduleId>[" + entry.getKey() + "]";
            unknown(module, DevelopersEncyclopediaModule.class, label);
            JsonNode profile = module.path("profile");
            unknown(profile, DevelopersModuleProfile.class, label + ".profile");
            for (var block : profile.properties()) {
                Class<?> type = moduleProfileType(block.getKey());
                if (type != null) {
                    unknown(block.getValue(), type, label + ".profile." + block.getKey());
                }
            }
        }
        DevelopersEncyclopediaModules parsed = DevelopersEncyclopediaModules.parse(root);
        expect("解析条数", data.size(), parsed.modules().size());
        expect("meta.page_total", root.path("meta").path("page_total").asInt(0), parsed.meta().page_total());
        JsonNode firstModule = firstValue(data);
        if (firstModule != null) {
            long id = firstModule.path("module_id").asLong(0);
            expect("首模块名称", firstModule.path("name").asString(""), parsed.modules().get(id).name());
        }
    }

    private static void checkEncyclopediaConsumables() throws Exception {
        JsonNode root = load("dev_encyclopedia_consumables.json");
        if (root == null) {
            return;
        }
        section("/wows/encyclopedia/consumables/ 消耗品字典（分页）");
        unknown(root.path("meta"), DevelopersMeta.class, "meta");
        JsonNode data = root.path("data");
        for (var entry : data.properties()) {
            JsonNode consumable = entry.getValue();
            String label = "data.<consumableId>[" + entry.getKey() + "]";
            unknown(consumable, DevelopersEncyclopediaConsumable.class, label);
            sampleValue(consumable.path("profile"), DevelopersConsumableEffect.class, label + ".profile.<code>");
        }
        DevelopersEncyclopediaConsumables parsed = DevelopersEncyclopediaConsumables.parse(root);
        expect("解析条数", data.size(), parsed.consumables().size());
        expect("meta.total", root.path("meta").path("total").asInt(0), parsed.meta().total());
        for (var entry : data.properties()) {
            long id = entry.getValue().path("consumable_id").asLong(0);
            expect("consumable[" + id + "].name", entry.getValue().path("name").asString(""),
                    parsed.consumables().get(id).name());
            break;
        }
    }

    private static void checkEncyclopediaAccountLevels() throws Exception {
        JsonNode root = load("dev_encyclopedia_accountlevels.json");
        if (root == null) {
            return;
        }
        section("/wows/encyclopedia/accountlevels/ 服役等级");
        JsonNode data = root.path("data");
        for (var entry : data.properties()) {
            unknown(entry.getValue(), DevelopersAccountLevel.class, "data.<level>[" + entry.getKey() + "]");
        }
        Map<Long, DevelopersAccountLevel> parsed = DevelopersAccountLevel.parse(root);
        expect("解析条数", data.size(), parsed.size());
    }

    private static void checkEncyclopediaCommanders() throws Exception {
        JsonNode root = load("dev_encyclopedia_crews.json");
        if (root == null) {
            return;
        }
        section("/wows/encyclopedia/crews/ 指挥官字典");
        JsonNode data = root.path("data");
        for (var entry : data.properties()) {
            unknown(entry.getValue(), DevelopersEncyclopediaCommander.class, "data.<commanderId>[" + entry.getKey() + "]");
        }
        Map<Long, DevelopersEncyclopediaCommander> parsed = DevelopersEncyclopediaCommander.parse(root);
        expect("解析条数", data.size(), parsed.size());
    }

    private static void checkEncyclopediaCommanderSkills() throws Exception {
        JsonNode root = load("dev_encyclopedia_crewskills.json");
        if (root == null) {
            return;
        }
        section("/wows/encyclopedia/crewskills/ 指挥官技能字典");
        JsonNode data = root.path("data");
        for (var entry : data.properties()) {
            unknown(entry.getValue(), DevelopersEncyclopediaSkill.class, "data.<skillId>[" + entry.getKey() + "]");
        }
        JsonNode first = firstValue(data);
        if (first != null) {
            unknown(firstValue(first.path("customization")), DevelopersSkillCustomization.class, "data.<skillId>.customization.<type>");
            JsonNode customization = firstValue(first.path("customization"));
            if (customization != null) {
                unknown(customization.path("perks").path(0), DevelopersSkillPerk.class, "data.<skillId>.customization.<type>.perks[0]");
            }
        }
        Map<Long, DevelopersEncyclopediaSkill> parsed = DevelopersEncyclopediaSkill.parse(root);
        expect("解析条数", data.size(), parsed.size());
    }

    private static void checkEncyclopediaCommanderRanks() throws Exception {
        JsonNode root = load("dev_encyclopedia_crewranks.json");
        if (root == null) {
            return;
        }
        section("/wows/encyclopedia/crewranks/ 指挥官军衔");
        JsonNode data = root.path("data");
        for (var entry : data.properties()) {
            unknown(entry.getValue().path(0), DevelopersCommanderRank.class, "data.<nation>[0]");
        }
        Map<String, List<DevelopersCommanderRank>> parsed = DevelopersCommanderRank.parse(root);
        expect("国家数量", data.size(), parsed.size());
        for (var entry : data.properties()) {
            expect("data." + entry.getKey() + " 军衔数", entry.getValue().size(), parsed.get(entry.getKey()).size());
            break;
        }
    }

    private static void checkEncyclopediaBattleTypes() throws Exception {
        JsonNode root = load("dev_encyclopedia_battletypes.json");
        if (root == null) {
            return;
        }
        section("/wows/encyclopedia/battletypes/ 战斗类型字典");
        JsonNode data = root.path("data");
        for (var entry : data.properties()) {
            unknown(entry.getValue(), DevelopersBattleType.class, "data.<tag>[" + entry.getKey() + "]");
        }
        Map<String, DevelopersBattleType> parsed = DevelopersBattleType.parse(root);
        expect("解析条数", data.size(), parsed.size());
    }

    private static void checkEncyclopediaCollections() throws Exception {
        JsonNode root = load("dev_encyclopedia_collections.json");
        if (root == null) {
            return;
        }
        section("/wows/encyclopedia/collections/ 收藏字典");
        JsonNode data = root.path("data");
        for (var entry : data.properties()) {
            unknown(entry.getValue(), DevelopersCollection.class, "data.<collectionId>[" + entry.getKey() + "]");
        }
        Map<Long, DevelopersCollection> parsed = DevelopersCollection.parse(root);
        expect("解析条数", data.size(), parsed.size());
    }

    private static void checkEncyclopediaCollectionCards() throws Exception {
        JsonNode root = load("dev_encyclopedia_collectioncards.json");
        if (root == null) {
            return;
        }
        section("/wows/encyclopedia/collectioncards/ 收藏卡牌字典");
        JsonNode data = root.path("data");
        for (var entry : data.properties()) {
            unknown(entry.getValue(), DevelopersCollectionCard.class, "data.<cardId>[" + entry.getKey() + "]");
            unknown(entry.getValue().path("images"), DevelopersCollectionCardImages.class, "data.<cardId>.images");
        }
        Map<Long, DevelopersCollectionCard> parsed = DevelopersCollectionCard.parse(root);
        expect("解析条数", data.size(), parsed.size());
    }

    private static void checkEncyclopediaBattleArenas() throws Exception {
        JsonNode root = load("dev_encyclopedia_battlearenas.json");
        if (root == null) {
            return;
        }
        section("/wows/encyclopedia/battlearenas/ 地图字典");
        JsonNode data = root.path("data");
        for (var entry : data.properties()) {
            unknown(entry.getValue(), DevelopersBattleArena.class, "data.<arenaId>[" + entry.getKey() + "]");
        }
        Map<Long, DevelopersBattleArena> parsed = DevelopersBattleArena.parse(root);
        expect("解析条数", data.size(), parsed.size());
    }

    private static void checkEncyclopediaAchievements() throws Exception {
        JsonNode root = load("dev_encyclopedia_achievements.json");
        if (root == null) {
            return;
        }
        section("/wows/encyclopedia/achievements/ 成就字典");
        JsonNode data = root.path("data");
        for (var entry : data.path("battle").properties()) {
            unknown(entry.getValue(), DevelopersEncyclopediaAchievement.class, "data.battle.<achievementId>");
            break;
        }
        int battle = data.path("battle").size();
        int progress = data.path("progress").size();
        System.out.println("   battle=" + battle + " progress=" + progress);
        DevelopersEncyclopediaAchievements parsed = DevelopersEncyclopediaAchievements.parse(root);
        expect("battle 数量", battle, parsed.battle().size());
        expect("progress 数量", progress, parsed.progress().size());
    }

    private static void checkRankedSeasons() throws Exception {
        JsonNode root = load("dev_seasons_info.json");
        if (root == null) {
            return;
        }
        section("/wows/seasons/info/ 排位赛季");
        JsonNode data = root.path("data");
        for (var entry : data.properties()) {
            unknown(entry.getValue(), DevelopersRankedSeason.class, "data.<seasonId>[" + entry.getKey() + "]");
        }
        JsonNode first = firstValue(data);
        if (first != null) {
            JsonNode league = firstValue(first.path("leagues"));
            if (league != null) {
                unknown(league.path("images"), DevelopersRankedLeagueImages.class, "data.<seasonId>.leagues.<id>.images");
                JsonNode rank = firstValue(league.path("ranks"));
                if (rank != null) {
                    unknown(rank, DevelopersRankedRank.class, "data.<seasonId>.leagues.<id>.ranks.<rank>");
                    unknown(rank.path("images"), DevelopersRankedRankImages.class,
                            "data.<seasonId>.leagues.<id>.ranks.<rank>.images", Set.of("default"));
                }
            }
        }
        Map<Long, DevelopersRankedSeason> parsed = DevelopersRankedSeason.parse(root);
        expect("解析条数", data.size(), parsed.size());
        long latest = 0;
        for (var entry : data.properties()) {
            latest = Math.max(latest, Long.parseLong(entry.getKey()));
        }
        expect("最新赛季名称", data.path(String.valueOf(latest)).path("season_name").asString(""), parsed.get(latest).season_name());
    }

    private static void checkRankedShipStats() throws Exception {
        JsonNode root = load("dev_seasons_shipstats.json");
        if (root == null) {
            return;
        }
        section("/wows/seasons/shipstats/ 排位单船战绩");
        unknown(root.path("meta"), DevelopersMeta.class, "meta");
        JsonNode data = onlyData(root);
        int blocks = 0;
        for (JsonNode item : data) {
            unknown(item, DevelopersRankedShipSeason.class,
                    "data.<accountId>[shipId=" + item.path("ship_id").asLong(0) + "]");
            blocks += rankedBlocks(item.path("seasons"), "data.<accountId>[shipId=" + item.path("ship_id").asLong(0) + "].seasons");
        }
        DevelopersRankedShipStats parsed = DevelopersRankedShipStats.parse(root);
        expect("解析条数", data.size(), parsed.ships().size());
        expect("数据块数量", blocks, parsed.ships().stream()
                .mapToInt(ship -> ship.seasons().values().stream().mapToInt(Map::size).sum()).sum());
        System.out.println("   舰船 " + data.size() + " 条, 战斗数据块 " + blocks + " 个");
    }

    private static void checkRankedAccountStats() throws Exception {
        JsonNode root = load("dev_seasons_accountinfo.json");
        if (root == null) {
            return;
        }
        section("/wows/seasons/accountinfo/ 排位玩家战绩");
        unknown(root.path("meta"), DevelopersMeta.class, "meta");
        JsonNode data = onlyData(root);
        int leaves = 0;
        for (var season : data.path("rank_info").properties()) {
            for (var league : season.getValue().properties()) {
                for (var sprint : league.getValue().properties()) {
                    unknown(sprint.getValue(), DevelopersRankedRankInfo.class,
                            "data.<accountId>.rank_info." + season.getKey() + "." + league.getKey() + "." + sprint.getKey());
                    leaves++;
                }
            }
        }
        int blocks = rankedBlocks(data.path("seasons"), "data.<accountId>.seasons");
        DevelopersRankedAccount parsed = DevelopersRankedAccount.parse(root);
        expect("rank_info 赛季数", data.path("rank_info").size(), parsed.rank_info().size());
        expect("rank_info 叶子数", leaves, parsed.rank_info().values().stream()
                .mapToInt(league -> league.values().stream().mapToInt(Map::size).sum()).sum());
        expect("seasons 赛季数", data.path("seasons").size(), parsed.seasons().size());
        expect("seasons 数据块数", blocks, parsed.seasons().values().stream().mapToInt(Map::size).sum());
        System.out.println("   rank_info 叶子 " + leaves + " 个, seasons 数据块 " + blocks + " 个");
    }

    private static void checkClanSeasonStats() throws Exception {
        JsonNode root = load("dev_clan_seasonstats.json");
        if (root == null) {
            return;
        }
        section("/wows/clans/seasonstats/ 军团战赛季战绩");
        unknown(root.path("meta"), DevelopersMeta.class, "meta");
        JsonNode data = root.path("data");
        int blocks = 0;
        for (var entry : data.properties()) {
            String label = "data.<accountId>[" + entry.getKey() + "]";
            unknown(entry.getValue(), DevelopersClanSeasonPlayer.class, label);
            for (JsonNode block : entry.getValue().path("seasons")) {
                battleBlock(block, label + ".seasons[" + block.path("season_id").asLong(0) + "]", Set.of("season_id"));
                blocks++;
            }
        }
        DevelopersClanSeasonStats parsed = DevelopersClanSeasonStats.parse(root);
        expect("玩家账号数", data.size(), parsed.players().size());
        expect("数据块数量", blocks, parsed.players().values().stream().mapToInt(player -> player.seasons().size()).sum());
        System.out.println("   玩家 " + data.size() + " 个, 赛季数据块 " + blocks + " 个");
    }

    //----------------------------------------------------------------- 工具

    /**
     * 战斗数据块：主体 + 5 个武器子块。
     */
    private static void battleBlock(JsonNode block, String label) {
        battleBlock(block, label, Set.of());
    }

    /**
     * 战斗数据块（allowed 放行少量额外字段，如军团赛季块的 season_id）。
     */
    private static void battleBlock(JsonNode block, String label, Set<String> allowed) {
        if (block == null || !block.isObject() || block.isEmpty() || !block.has("battles")) {
            return;
        }
        if (block.has("wins_by_tasks")) {
            //行动模式(oper_*)的数据块，字段与随机战不同
            unknown(block, DevelopersOperBattleType.class, label, allowed);
            return;
        }
        unknown(block, DevelopersShipBattleType.class, label, allowed);
        unknown(block.path("main_battery"), DevelopersDataBattery.class, label + ".main_battery");
        unknown(block.path("second_battery"), DevelopersDataBattery.class, label + ".second_battery");
        unknown(block.path("torpedoes"), DevelopersDataBattery.class, label + ".torpedoes");
        unknown(block.path("aircraft"), DevelopersDataBatteryV2.class, label + ".aircraft");
        unknown(block.path("ramming"), DevelopersDataBatteryV2.class, label + ".ramming");
    }

    /**
     * 取对象第一个属性的值；非对象 / 没有属性返回 null。
     */
    private static JsonNode firstValue(JsonNode node) {
        if (node != null && node.isObject()) {
            for (var entry : node.properties()) {
                return entry.getValue();
            }
        }
        return null;
    }

    /**
     * 抽查对象第一个值是否有结构体（值类型一致的映射只验一个样本）。
     */
    private static void sampleValue(JsonNode node, Class<?> type, String label) {
        unknown(firstValue(node), type, label);
    }

    /**
     * 舰船参数块：顶部 + 各武器/系统子块与示例槽位（不适用的块为 null，自动跳过）。
     */
    private static void profileBlock(JsonNode node, String label) {
        if (node == null || node.isNull() || !node.isObject() || node.isEmpty()) {
            return;
        }
        unknown(node, DevelopersShipProfile.class, label);
        unknown(node.path("hull"), DevelopersShipHullProfile.class, label + ".hull");
        unknown(node.path("hull").path("range"), DevelopersShipIntRange.class, label + ".hull.range");
        unknown(node.path("engine"), DevelopersShipEngineProfile.class, label + ".engine");
        unknown(node.path("artillery"), DevelopersShipArtilleryProfile.class, label + ".artillery");
        sampleValue(node.path("artillery").path("slots"), DevelopersShipArtillerySlot.class, label + ".artillery.slots.<i>");
        sampleValue(node.path("artillery").path("shells"), DevelopersShipShell.class, label + ".artillery.shells.<code>");
        unknown(node.path("torpedoes"), DevelopersShipTorpedoProfile.class, label + ".torpedoes");
        sampleValue(node.path("torpedoes").path("slots"), DevelopersShipTorpedoSlot.class, label + ".torpedoes.slots.<i>");
        unknown(node.path("fire_control"), DevelopersShipFireControl.class, label + ".fire_control");
        unknown(node.path("flight_control"), DevelopersShipFlightControl.class, label + ".flight_control");
        unknown(node.path("fighters"), DevelopersShipFighters.class, label + ".fighters");
        unknown(node.path("torpedo_bomber"), DevelopersShipTorpedoBomber.class, label + ".torpedo_bomber");
        unknown(node.path("dive_bomber"), DevelopersShipDiveBomber.class, label + ".dive_bomber");
        unknown(node.path("submarine_sonar"), DevelopersShipSonarProfile.class, label + ".submarine_sonar");
        unknown(node.path("atbas"), DevelopersShipAtbaProfile.class, label + ".atbas");
        sampleValue(node.path("atbas").path("slots"), DevelopersShipAtbaSlot.class, label + ".atbas.slots.<i>");
        unknown(node.path("anti_aircraft"), DevelopersShipAntiAircraftProfile.class, label + ".anti_aircraft");
        sampleValue(node.path("anti_aircraft").path("slots"), DevelopersShipAASlot.class, label + ".anti_aircraft.slots.<i>");
        unknown(node.path("armour"), DevelopersShipArmourProfile.class, label + ".armour");
        unknown(node.path("armour").path("citadel"), DevelopersShipIntRange.class, label + ".armour.citadel");
        unknown(node.path("mobility"), DevelopersShipMobility.class, label + ".mobility");
        unknown(node.path("concealment"), DevelopersShipConcealment.class, label + ".concealment");
        unknown(node.path("weaponry"), DevelopersShipWeaponry.class, label + ".weaponry");
        unknown(node.path("submarine_battery"), DevelopersShipSubmarineBattery.class, label + ".submarine_battery");
        unknown(node.path("submarine_mobility"), DevelopersShipSubmarineMobility.class, label + ".submarine_mobility");
        unknown(node.path("depth_charge"), DevelopersShipDepthCharge.class, label + ".depth_charge");
    }

    /**
     * 模块属性块 -> 结构体（与 shipprofile 同构的块复用其结构体）。
     */
    private static Class<?> moduleProfileType(String key) {
        return switch (key) {
            case "artillery" -> DevelopersModuleArtillery.class;
            case "dive_bomber" -> DevelopersShipDiveBomber.class;
            case "engine" -> DevelopersShipEngineProfile.class;
            case "fighter" -> DevelopersShipFighters.class;
            case "fire_control" -> DevelopersShipFireControl.class;
            case "flight_control" -> DevelopersShipFlightControl.class;
            case "hull" -> DevelopersShipHullProfile.class;
            case "submarine_sonar" -> DevelopersShipSonarProfile.class;
            case "torpedo_bomber" -> DevelopersShipTorpedoBomber.class;
            case "torpedoes" -> DevelopersModuleTorpedoes.class;
            default -> null;
        };
    }

    /**
     * 排位战斗块（赛季 -&gt; 分组 -&gt; 三种模式）；返回分组数量。
     */
    private static int rankedBlocks(JsonNode seasons, String label) {
        int blocks = 0;
        for (var season : seasons.properties()) {
            for (var group : season.getValue().properties()) {
                String groupLabel = label + "." + season.getKey() + "." + group.getKey();
                unknown(group.getValue(), DevelopersRankedShipSeasonBlock.class, groupLabel);
                for (var type : group.getValue().properties()) {
                    if (type.getValue() != null && type.getValue().isObject() && !type.getValue().isEmpty()) {
                        battleBlock(type.getValue(), groupLabel + "." + type.getKey());
                    }
                }
                blocks++;
            }
        }
        return blocks;
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

    /**
     * 取某个接口的原始响应：优先用本地采集，没有就现场抓一次（{@link #FETCH}）。
     *
     * @return 响应根节点；既没有采集又抓取失败时返回 null（调用方跳过该接口）
     */
    private static JsonNode load(String file) {
        if (!Boolean.getBoolean("coverage.live")) {
            Path path = find(file);
            if (path != null) {
                try {
                    return JsonUtils.json().parse(path.toFile());
                } catch (Exception e) {
                    throw new IllegalStateException("解析失败: " + file, e);
                }
            }
        }
        JsonNode live = LIVE.get(file);
        if (live != null) {
            return live;
        }
        live = fetch(file);
        if (live == null) {
            System.out.println("跳过（没有采集，现场抓取也失败）: " + file);
            return null;
        }
        LIVE.put(file, live);
        return live;
    }

    /**
     * 本地采集文件：先按名字精确匹配，再按前缀找（key 里的哈希后缀可能随参数变化）。
     */
    private static Path find(String file) {
        Path path = RAW.resolve(file);
        if (Files.isRegularFile(path)) {
            return path;
        }
        Endpoint endpoint = FETCH.get(file);
        if (endpoint == null || !Files.isDirectory(RAW)) {
            return null;
        }
        try (Stream<Path> files = Files.list(RAW)) {
            return files.filter(Files::isRegularFile)
                    .filter(p -> {
                        String name = p.getFileName().toString();
                        return name.startsWith(endpoint.prefix()) && name.endsWith(".json");
                    })
                    .sorted()
                    .findFirst()
                    .orElse(null);
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * 现场抓一次：打开监视采集钩子 -> 调库的公开方法 -> 取原始响应。
     */
    private static JsonNode fetch(String file) {
        Endpoint endpoint = FETCH.get(file);
        if (endpoint == null) {
            return null;
        }
        System.out.println("现场抓取: " + endpoint.keyHint() + " ...");
        SchemaRecorder.enable();
        List<SchemaRecorder.Record> records;
        try {
            endpoint.call().run();
        } catch (Exception e) {
            System.out.println("  抓取失败: " + e.getClass().getSimpleName() + " " + e.getMessage());
            return null;
        } finally {
            records = SchemaRecorder.drain();
            SchemaRecorder.disable();
        }
        //一次调用可能打多个接口(例如 accountInfoDevelopers 会再查公会)，按 schemaKey 命中目标
        SchemaRecorder.Record hit = null;
        for (SchemaRecorder.Record record : records) {
            if (record.schemaKey().contains(endpoint.keyHint())) {
                hit = record;
                break;
            }
        }
        if (hit == null && !records.isEmpty()) {
            hit = records.get(0);
            System.out.println("  未命中 " + endpoint.keyHint() + "，退化为第一条: " + hit.schemaKey());
        }
        if (hit == null) {
            System.out.println("  没有采集到响应");
            return null;
        }
        try {
            return JsonUtils.json().parse(hit.json());
        } catch (Exception e) {
            System.out.println("  响应解析失败: " + e.getMessage());
            return null;
        }
    }

    /**
     * 注册现场抓取用的调用（token/account_id 见类常量）。
     */
    private static void registry() {
        HttpCodec.updateTimeout(25);
        HttpClient client = TestConfig.client();
        WowsHttpUserTools user = new WowsHttpUserTools(client, server);
        WowsHttpShipTools ship = new WowsHttpShipTools(client, server, account_id);
        WowsHttpClanTools clan = new WowsHttpClanTools(client, server);
        WowsEncyclopediaTools encyclopedia = new WowsEncyclopediaTools(client, server);

        register("api_asia_wows_account_list_search_text_.json", "api_asia_wows_account_list", "/wows/account/list/",
                () -> user.searchUserDevelopers(token, SEARCH_USER));
        register(ACC, "api_asia_wows_account_info", "/wows/account/info/", () -> user.accountInfoDevelopers(token, account_id));
        register(SHIPS, "api_asia_wows_ships_stats_account_id_id_extra_pvp", "/wows/ships/stats/", () -> ship.developers(token).shipList());
        register(OPER, "api_asia_wows_ships_stats_account_id_id_extra_clan_oper", "/wows/ships/stats/", () -> ship.developers(token).shipOperList());
        register("api_asia_wows_ships_badges_account_id_id_.json", "api_asia_wows_ships_badges", "/wows/ships/badges/", () -> ship.developers(token).shipBadges());
        register("api_asia_wows_clans_info_clan_id_id_extra_members.json", "api_asia_wows_clans_info", "/wows/clans/info/", () -> clan.developers(token).clanInfoDevelopers(CLAN_ID));
        register("api_asia_wows_clans_list_search_text_.json", "api_asia_wows_clans_list", "/wows/clans/list/", () -> clan.developers(token).searchClanDevelopers(SEARCH_CLAN));
        register("api_asia_wows_clans_accountinfo_account_id_id_extra_clan.json", "api_asia_wows_clans_accountinfo", "/wows/clans/accountinfo/",
                () -> clan.developers(token).userSearchClanDevelopers(account_id));
        register("api_asia_wows_clans_season_.json", "api_asia_wows_clans_season", "/wows/clans/season/", () -> clan.developers(token).season());
        register("api_asia_wows_clans_glossary_.json", "api_asia_wows_clans_glossary", "/wows/clans/glossary/", () -> encyclopedia.developers(token).glossary());

        //3.0 新增端点：走 WowsApi 视图，监控键 = endpoint id（与 SchemaMonitorTest 的 dev_* 原始响应同名）
        WowsApi api = WowsApi.of(server).credentials(c -> c.applicationId(token));
        register("dev_account_achievements.json", "dev_account_achievements", "dev.account.achievements",
                () -> api.developers().userAchievements(account_id));
        register("dev_encyclopedia_info.json", "dev_encyclopedia_info", "dev.encyclopedia.info",
                () -> api.developers().encyclopediaInfo());
        register("dev_encyclopedia_ships.json", "dev_encyclopedia_ships", "dev.encyclopedia.ships",
                () -> api.developers().encyclopediaShips(null, null, null, null));
        register("dev_encyclopedia_shipprofile.json", "dev_encyclopedia_shipprofile", "dev.encyclopedia.shipprofile",
                () -> api.developers().shipProfile(TestConfig.SHIP_YAMATO_ASIA));
        register("dev_encyclopedia_modules.json", "dev_encyclopedia_modules", "dev.encyclopedia.modules",
                () -> api.developers().encyclopediaModules(null, null, null));
        register("dev_encyclopedia_accountlevels.json", "dev_encyclopedia_accountlevels", "dev.encyclopedia.accountlevels",
                () -> api.developers().encyclopediaAccountLevels());
        register("dev_encyclopedia_crews.json", "dev_encyclopedia_crews", "dev.encyclopedia.crews",
                () -> api.developers().encyclopediaCommanders());
        register("dev_encyclopedia_crewskills.json", "dev_encyclopedia_crewskills", "dev.encyclopedia.crewskills",
                () -> api.developers().encyclopediaCommanderSkills());
        register("dev_encyclopedia_crewranks.json", "dev_encyclopedia_crewranks", "dev.encyclopedia.crewranks",
                () -> api.developers().encyclopediaCommanderRanks());
        register("dev_encyclopedia_battletypes.json", "dev_encyclopedia_battletypes", "dev.encyclopedia.battletypes",
                () -> api.developers().encyclopediaBattleTypes());
        register("dev_encyclopedia_consumables.json", "dev_encyclopedia_consumables", "dev.encyclopedia.consumables",
                () -> api.developers().encyclopediaConsumables(null, null, null));
        register("dev_encyclopedia_collections.json", "dev_encyclopedia_collections", "dev.encyclopedia.collections",
                () -> api.developers().encyclopediaCollections());
        register("dev_encyclopedia_collectioncards.json", "dev_encyclopedia_collectioncards", "dev.encyclopedia.collectioncards",
                () -> api.developers().encyclopediaCollectionCards());
        register("dev_encyclopedia_battlearenas.json", "dev_encyclopedia_battlearenas", "dev.encyclopedia.battlearenas",
                () -> api.developers().encyclopediaBattleArenas());
        register("dev_encyclopedia_achievements.json", "dev_encyclopedia_achievements", "dev.encyclopedia.achievements",
                () -> api.developers().encyclopediaAchievements());
        register("dev_seasons_info.json", "dev_seasons_info", "dev.seasons.info",
                () -> api.developers().rankedSeasons());
        register("dev_seasons_shipstats.json", "dev_seasons_shipstats", "dev.seasons.shipstats",
                () -> api.developers().rankedShipStats(account_id));
        register("dev_seasons_accountinfo.json", "dev_seasons_accountinfo", "dev.seasons.accountinfo",
                () -> api.developers().rankedAccountStats(account_id));
        register("dev_clan_seasonstats.json", "dev_clan_seasonstats", "dev.clan.seasonstats",
                () -> api.developers().clanSeasonStats(account_id));
    }

    private static void register(String file, String prefix, String keyHint, Call call) {
        FETCH.put(file, new Endpoint(file, prefix, keyHint, call));
    }

    /**
     * 一个接口的采集文件名、本地文件名字前缀、现场抓取时的 schemaKey 命中串与调用。
     */
    private record Endpoint(String file, String prefix, String keyHint, Call call) {
    }

    @FunctionalInterface
    private interface Call {
        void run() throws Exception;
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
