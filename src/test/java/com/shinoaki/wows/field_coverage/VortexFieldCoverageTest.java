package com.shinoaki.wows.field_coverage;

import com.shinoaki.wows.api.codec.HttpCodec;
import com.shinoaki.wows.api.codec.http.WowsHttpClanTools;
import com.shinoaki.wows.api.codec.http.WowsHttpShipTools;
import com.shinoaki.wows.api.codec.http.WowsHttpUserTools;
import com.shinoaki.wows.api.data.DogTag;
import com.shinoaki.wows.api.monitor.SchemaRecorder;
import com.shinoaki.wows.api.type.WowsBattlesType;
import com.shinoaki.wows.api.type.WowsServer;
import com.shinoaki.wows.api.utils.JsonUtils;
import com.shinoaki.wows.api.vortex.VortexUserShip;
import com.shinoaki.wows.api.vortex.account.VortexUserInfo;
import com.shinoaki.wows.api.vortex.account.statistics.VortexMarkGradeInfo;
import com.shinoaki.wows.api.vortex.account.statistics.VortexRankInfo;
import com.shinoaki.wows.api.vortex.account.statistics.VortexUserInfoStatistics;
import com.shinoaki.wows.api.vortex.clan.base.VortexClanInfo;
import com.shinoaki.wows.api.vortex.clan.members.VortexClanStatisticsInfo;
import com.shinoaki.wows.api.vortex.resources.RequestVortexResourcesInfo;
import com.shinoaki.wows.api.vortex.resources.glossary.VortexAchievement;
import com.shinoaki.wows.api.vortex.resources.glossary.VortexAchievementGrouping;
import com.shinoaki.wows.api.vortex.resources.glossary.VortexAchievementIcons;
import com.shinoaki.wows.api.vortex.resources.glossary.VortexAchievementsGroup;
import com.shinoaki.wows.api.vortex.resources.glossary.VortexAchievementsSubgroup;
import com.shinoaki.wows.api.vortex.resources.glossary.VortexBattleType;
import com.shinoaki.wows.api.vortex.resources.glossary.VortexNation;
import com.shinoaki.wows.api.vortex.resources.glossary.VortexNationIcons;
import com.shinoaki.wows.api.vortex.resources.glossary.VortexVehicleType;
import com.shinoaki.wows.api.vortex.resources.glossary.VortexVehicleTypeIcons;
import com.shinoaki.wows.api.vortex.ship.VortexShipInfo;
import com.shinoaki.wows.api.vortex.ship.VortexShipMarkGrade;
import com.shinoaki.wows.api.vortex.ship.VortexShipMarkInfo;
import tools.jackson.databind.JsonNode;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.RecordComponent;
import java.net.http.HttpClient;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * vortex 接口「响应字段是否都有对应结构体」的校验（参考国服账号 7060948811）。
 * <p>
 * 做法：打开 {@link SchemaRecorder} 采集真实响应，再用库的解析类解析同一份 JSON，
 * 逐层比对「JSON 里出现的字段」与「结构体声明的字段」，把多出来的字段报出来。
 * <p>
 * 用法：{@code java -cp ... com.shinoaki.wows.field_coverage.VortexFieldCoverageTest}；退出码 0 = 全部字段都有结构体。
 *
 * @author Xun
 */
public class VortexFieldCoverageTest {

    private static final WowsServer SERVER = WowsServer.CN;
    private static final long ACCOUNT_ID = 7060948811L;
    private static final long CLAN_ID = 7000009259L;
    private static final Path DUMP_DIR = Path.of("target/cn-dump");

    private static int failed = 0;
    private static JsonNode accountJson;
    private static JsonNode accountClansJson;
    private static JsonNode shipsJson;
    private static JsonNode clanInfoJson;
    private static JsonNode clanMembersJson;

    public static void main(String[] args) throws Exception {
        if (Files.isRegularFile(DUMP_DIR.resolve("account.json"))) {
            System.out.println("数据来源: " + DUMP_DIR.toAbsolutePath() + "（离线）");
            loadDumps();
        } else {
            System.out.println("数据来源: 在线请求 " + SERVER.vortex());
            fetch();
        }

        checkAccount();
        checkShips();
        checkClanInfo();
        checkClanMembers();
        checkResources();

        System.out.println();
        System.out.println(failed == 0 ? "结果: 全部响应字段都有对应结构体" : "结果: " + failed + " 处字段缺少结构体");
        System.exit(failed == 0 ? 0 : 1);
    }

    //----------------------------------------------------------------- 各接口校验

    private static void checkAccount() throws Exception {
        System.out.println();
        System.out.println("── /api/accounts/{id}/ 账号详情 ──");
        JsonNode data = onlyData(accountJson);
        //dog_tag 在结构体里叫 dogTag(手工映射)
        unknown(data, VortexUserInfo.class, "账号节点", Set.of("hidden_profile", "dog_tag"));
        unknown(data.path("dog_tag"), DogTag.class, "dog_tag");
        unknown(data.path("statistics"), VortexUserInfoStatistics.class, "statistics");

        Set<String> statFields = new TreeSet<>();
        collectLeafNames(data.path("statistics"), statFields, 3);
        System.out.println("   statistics 下字段(前3层): " + statFields.size() + " 个");

        VortexUserInfo info = VortexUserInfo.parse(accountJson, ACCOUNT_ID);
        expect("name", data.path("name").asString(""), info.name());
        expect("created_at", data.path("created_at").asDouble(0), info.created_at());
        expect("visibility_settings", data.path("visibility_settings").asBoolean(false), info.visibility_settings());
        expect("dog_tag.texture_id", data.path("dog_tag").path("texture_id").asLong(0), info.dogTag().texture_id());
        expect("statistics.basic.leveling_tier", data.path("statistics").path("basic").path("leveling_tier").asInt(0), info.statistics().getBasic().leveling_tier());

        //各战斗类型数据块必须与 VortexShipInfo 完全对齐
        for (Map.Entry<String, JsonNode> entry : data.path("statistics").properties()) {
            JsonNode node = entry.getValue();
            if (node.isObject() && node.has("battles_count")) {
                unknown(node, VortexShipInfo.class, "statistics." + entry.getKey());
            }
        }
        //优秀标记
        unknown(data.path("statistics").path("mark_of_excellence"), com.shinoaki.wows.api.vortex.account.statistics.VortexMarkOfExcellence.class, "mark_of_excellence");
        unknown(data.path("statistics").path("mark_of_excellence").path("personal"), VortexMarkGradeInfo.class, "mark_of_excellence.personal");
        JsonNode firstGrade = data.path("statistics").path("mark_of_excellence").path("personal").path("grades").path(0);
        unknown(firstGrade, VortexMarkGradeInfo.class, "mark_of_excellence.personal.grades[0]");
        //排位进度
        JsonNode rankInfo = firstLeafObject(data.path("statistics").path("rank_info"), 3);
        unknown(rankInfo, VortexRankInfo.class, "rank_info 最内层");

        VortexUserInfoStatistics statistics = info.statistics();
        expect("statistics.pve.battles_count", data.path("statistics").path("pve").path("battles_count").asLong(0), statistics.getPve().battles_count());
        expect("statistics.pvp.max_damage_dealt", data.path("statistics").path("pvp").path("max_damage_dealt").asLong(0), statistics.getPvp().max_damage_dealt());
        expect("statistics.pvp.max_exp_vehicle", data.path("statistics").path("pvp").path("max_exp_vehicle").asLong(0), statistics.getPvp().max_exp_vehicle());
        expect("mark_of_excellence.personal.ships_count",
                data.path("statistics").path("mark_of_excellence").path("personal").path("ships_count").asInt(0),
                statistics.getMark_of_excellence().personal().ships_count());
        expect("mark_of_excellence.personal.grades",
                data.path("statistics").path("mark_of_excellence").path("personal").path("grades").size(),
                statistics.getMark_of_excellence().personal().grades().size());
        expect("seasons 非空", !data.path("statistics").path("seasons").isEmpty(), !statistics.getSeasons().isEmpty());
        expect("rank_info 非空", !data.path("statistics").path("rank_info").isEmpty(), !statistics.getRank_info().isEmpty());
        expect("rank_info 取值", rankInfo.path("rank").asInt(0), statistics.getRank_info().values().iterator().next().values().iterator().next().values().iterator().next().rank());
        expect("seasons 取值", data.path("statistics").path("seasons").path("1028").path("0").path("rank_solo").path("battles_count").asLong(0),
                statistics.getSeasons().stats("1028", "rank_solo").battles_count());
    }

    private static void checkShips() throws Exception {
        System.out.println();
        System.out.println("── /api/accounts/{id}/ships/pvp/ 战舰列表 ──");
        JsonNode data = onlyData(shipsJson);
        //statistics 落到结构体的 shipMap 里(手工映射)
        unknown(data, VortexUserShip.class, "战舰列表节点", Set.of("hidden_profile", "statistics"));
        JsonNode statistics = data.path("statistics");
        int ships = 0;
        int unknownShipFields = 0;
        int marks = 0;
        for (Map.Entry<String, JsonNode> ship : statistics.properties()) {
            ships++;
            Set<String> own = new LinkedHashSet<>();
            for (Map.Entry<String, JsonNode> field : ship.getValue().properties()) {
                //statistics.<shipId> 下只应有 mark_of_excellence / mastery_sign / 各战斗类型数据块
                if (!field.getKey().equals("mark_of_excellence") && !field.getKey().equals("mastery_sign")
                        && !field.getValue().isObject()) {
                    own.add(field.getKey());
                }
            }
            if (!own.isEmpty()) {
                unknownShipFields++;
                System.out.println("  [缺少] ship " + ship.getKey() + " -> " + own);
            }
            unknown(ship.getValue().path("mark_of_excellence"), com.shinoaki.wows.api.vortex.ship.VortexShipMarkOfExcellence.class, "ship.mark_of_excellence");
            if (!ship.getValue().path("mark_of_excellence").path("personal").isEmpty()) {
                marks++;
                unknown(ship.getValue().path("mark_of_excellence").path("personal"), VortexShipMarkInfo.class, "mark_of_excellence.personal");
                unknown(ship.getValue().path("mark_of_excellence").path("personal").path("grade"), VortexShipMarkGrade.class, "mark_of_excellence.personal.grade");
            }
            JsonNode pvp = ship.getValue().path("pvp");
            if (!pvp.isEmpty()) {
                unknown(pvp, VortexShipInfo.class, "ship." + ship.getKey() + ".pvp");
            }
        }
        System.out.println("   战舰 " + ships + " 条, 带优秀标记 " + marks + " 条, 结构体缺失 " + unknownShipFields + " 条");

        var userShip = com.shinoaki.wows.api.vortex.VortexUserShip.parse(WowsBattlesType.PVP, shipsJson);
        expect("战舰数量", ships, userShip.shipMap().size());
        expect("visibility_settings", data.path("visibility_settings").asBoolean(false), userShip.visibility_settings());
        long shipId = Long.parseLong(statistics.properties().iterator().next().getKey());
        JsonNode first = statistics.path(String.valueOf(shipId));
        expect("单船 pvp.battles_count", first.path("pvp").path("battles_count").asLong(0), userShip.shipMap().get(shipId).shipInfo().battles_count());
        expect("单船 mastery_sign", first.path("mastery_sign").asString(""), userShip.shipMap().get(shipId).masterySign());
        expect("单船 mark_of_excellence.personal.percentile",
                first.path("mark_of_excellence").path("personal").path("percentile").asInt(0),
                userShip.shipMap().get(shipId).markOfExcellence().personal().percentile());
    }

    private static void checkClanInfo() {
        System.out.println();
        System.out.println("── /api/clanbase/{id}/claninfo/ 军团详情 ──");
        JsonNode clanview = clanInfoJson.path("clanview");
        JsonNode clan = clanview.path("clan");
        Set<String> expected = new TreeSet<>(Arrays.asList("accumulative_resource", "active_applications", "active_invites", "color", "community_urls",
                "created_at", "description", "id", "is_application_sent", "is_disbanded", "is_invite_received", "is_suited_for_autorecruiting",
                "leveling", "max_members_count", "members_count", "motto", "name", "personal_resource", "pre_moderation", "raw_description",
                "recruiting_policy", "recruiting_restrictions", "stats", "tag"));
        expected.remove("id");//与入参 clanId 相同, 不重复暴露
        Set<String> actual = new TreeSet<>();
        clan.properties().forEach(e -> actual.add(e.getKey()));
        actual.remove("id");
        expect("clan 字段集合", expected.toString(), actual.toString());

        unknown(clanview.path("achievements").path(0), com.shinoaki.wows.api.vortex.clan.base.info.VortexClanAchievement.class, "achievements[0]");
        unknown(clanview.path("wows_ladder").path("max_position"), com.shinoaki.wows.api.vortex.clan.base.info.VortexClanMaxPosition.class, "wows_ladder.max_position");
        unknown(clanview.path("wows_ladder").path("ratings").path(0).path("max_position"), com.shinoaki.wows.api.vortex.clan.base.info.VortexClanMaxPosition.class, "ratings[].max_position");
        unknown(clan.path("recruiting_restrictions"), com.shinoaki.wows.api.vortex.clan.base.info.VortexClanRecruitingRestrictions.class, "recruiting_restrictions");

        VortexClanInfo info = VortexClanInfo.to(SERVER, CLAN_ID, clanInfoJson);
        expect("achievements 数量", clanview.path("achievements").size(), info.achievements().size());
        expect("ladder.max_position.league", clanview.path("wows_ladder").path("max_position").path("league").asInt(0), info.wowsLadder().max_position().league());
        expect("ladder.ratings 数量", clanview.path("wows_ladder").path("ratings").size(), info.wowsLadder().ratings().size());
        expect("stats 场次数量", clan.path("stats").size(), info.stats().size());
        for (Map.Entry<String, JsonNode> stat : clan.path("stats").properties()) {
            expect("stats." + stat.getKey(), stat.getValue().asLong(0), info.stats().get(stat.getKey()));
        }
        expect("community_urls", clan.path("community_urls").size(), info.communityUrls().size());
        expect("leveling", clan.path("leveling").isNumber() ? clan.path("leveling").asInt() : null, info.leveling());
        expect("recruiting_policy", clan.path("recruiting_policy").asString(""), info.recruitingPolicy());
        expect("recruiting_restrictions.battles_count",
                clan.path("recruiting_restrictions").path("battles_count").isNumber() ? clan.path("recruiting_restrictions").path("battles_count").asInt() : null,
                info.recruitingRestrictions().battles_count());
        expect("buildings 数量", clanview.path("buildings").size(), info.clanBuildingsInfoList().size());
        expect("color", clan.path("color").asString(""), info.colorRgb());
    }

    private static void checkClanMembers() {
        System.out.println();
        System.out.println("── /api/members/{id}/ 军团成员 ──");
        Set<String> rootExpected = new TreeSet<>(Arrays.asList("clan_statistics", "is_hidden_statistics", "items", "last_updated_at", "status"));
        Set<String> rootActual = new TreeSet<>();
        clanMembersJson.properties().forEach(e -> rootActual.add(e.getKey()));
        expect("根节点字段集合", rootExpected.toString(), rootActual.toString());

        Set<String> itemExpected = new TreeSet<>(Arrays.asList("abnormal_results", "accumulative_clan_resource", "bans", "battles_count", "battles_per_day",
                "damage_per_battle", "days_in_clan", "exp_per_battle", "frags_per_battle", "id", "is_banned", "is_bonus_activated", "is_hidden_statistics",
                "is_press", "last_battle_time", "leveling", "name", "online_status", "profile_link", "rank", "role", "season_id", "season_rank",
                "wins_percentage"));
        Set<String> itemActual = new TreeSet<>();
        clanMembersJson.path("items").path(0).properties().forEach(e -> itemActual.add(e.getKey()));
        expect("items[0] 字段集合", itemExpected.toString(), itemActual.toString());
        unknown(clanMembersJson.path("items").path(0).path("role"), null, "items[].role", Set.of("name", "order", "rank"));

        VortexClanStatisticsInfo info = com.shinoaki.wows.api.vortex.clan.members.VortexClanUserInfo.to(SERVER, clanMembersJson);
        expect("成员数量", clanMembersJson.path("items").size(), info.infos().size());
        expect("status", clanMembersJson.path("status").asString(""), info.status());
        expect("last_updated_at", clanMembersJson.path("last_updated_at").asString(""), info.last_updated_at());
        expect("is_hidden_statistics", clanMembersJson.path("is_hidden_statistics").asBoolean(false), info.is_hidden_statistics());
        expect("clan_statistics.wins_percentage", clanMembersJson.path("clan_statistics").path("wins_percentage").asDouble(0), info.wins_percentage());
        JsonNode first = clanMembersJson.path("items").path(0);
        var firstUser = info.infos().get(0);
        expect("items[0].role.rank", first.path("role").path("rank").asInt(0), firstUser.roleRank());
        expect("items[0].season_rank", first.path("season_rank").isNumber() ? first.path("season_rank").asInt() : null, firstUser.seasonRank());
        expect("items[0].leveling", first.path("leveling").isNumber() ? first.path("leveling").asInt() : null, firstUser.leveling());
    }

    /**
     * 资源类接口（graphql getGlossData / get_lootbox）的字段覆盖，用监视采集到的原始响应。
     */
    private static void checkResources() throws Exception {
        Path gloss = Path.of("target/schema-raw/vortex_asia_api_graphql_glossary_getGlossData.json");
        Path loot = Path.of("target/schema-raw/vortex_asia_api_get_lootbox_lang_id_.json");
        if (!Files.isRegularFile(gloss) && !Files.isRegularFile(loot)) {
            System.out.println();
            System.out.println("── 资源接口 ── 跳过（没有 target/schema-raw 采集）");
            return;
        }
        if (Files.isRegularFile(gloss)) {
            System.out.println();
            System.out.println("── graphql getGlossData 资料库 ──");
            JsonNode root = JsonUtils.json().parse(gloss.toFile());
            JsonNode data = root.path("data");
            Set<String> allowed = Set.of("__typename");
            unknown(data.path("nations").path(0), VortexNation.class, "nations[0]", allowed);
            //图标类字段名带 Url 后缀(与 WowsIcons 的 defaultUrl 约定一致)，这里按 JSON 原名放行
            unknown(data.path("nations").path(0).path("icons"), VortexNationIcons.class, "nations[0].icons", Set.of("small", "tiny", "__typename"));
            unknown(data.path("vehicleTypes").path(0), VortexVehicleType.class, "vehicleTypes[0]", allowed);
            unknown(data.path("vehicleTypes").path(0).path("icons"), VortexVehicleTypeIcons.class, "vehicleTypes[0].icons",
                    Set.of("default", "special", "normal", "elite", "premium", "__typename"));
            unknown(data.path("battleTypes").path(0), VortexBattleType.class, "battleTypes[0]", allowed);
            unknown(data.path("achievements").path(0), VortexAchievement.class, "achievements[0]", allowed);
            unknown(data.path("achievements").path(0).path("icons"), VortexAchievementIcons.class, "achievements[0].icons",
                    Set.of("default", "inactive", "normal", "__typename"));
            unknown(data.path("achievements").path(0).path("grouping"), VortexAchievementGrouping.class, "achievements[0].grouping", allowed);
            unknown(data.path("achievementsGroups").path(0), VortexAchievementsGroup.class, "achievementsGroups[0]", allowed);
            unknown(data.path("achievementsGroups").path(0).path("subgroups").path(0), VortexAchievementsSubgroup.class, "achievementsGroups[0].subgroups[0]", allowed);
            unknown(data.path("vehicles").path(0), com.shinoaki.wows.api.vortex.resources.vehicles.VortexVehicles.class, "vehicles[0]", allowed);
            unknown(data.path("dogTagComponents").path(0), com.shinoaki.wows.api.vortex.resources.dogtagcomponents.VortexDogTagComponents.class, "dogTagComponents[0]", allowed);
            RequestVortexResourcesInfo info = RequestVortexResourcesInfo.parse(root, "zh-sg");
            expect("version", data.path("version").asString(""), info.getVersion());
            expect("nations 数量", data.path("nations").size(), info.getNations().size());
            expect("vehicleTypes 数量", data.path("vehicleTypes").size(), info.getVehicleTypes().size());
            expect("battleTypes 数量", data.path("battleTypes").size(), info.getBattleTypes().size());
            expect("achievements 数量", data.path("achievements").size(), info.getAchievements().size());
            expect("achievementsGroups 数量", data.path("achievementsGroups").size(), info.getAchievementsGroups().size());
            expect("nations[0].color", data.path("nations").path(0).path("color").asString(""), info.getNations().get(0).getColor());
            expect("achievements[0].grouping.group", data.path("achievements").path(0).path("grouping").path("group").asString(""),
                    info.getAchievements().get(0).getGrouping().getGroup());
            expect("achievements[0].battleTypes", data.path("achievements").path(0).path("battleTypes").size(),
                    info.getAchievements().get(0).getBattleTypes().size());
        }
        if (Files.isRegularFile(loot)) {
            System.out.println();
            System.out.println("── /api/get_lootbox/{lang}/{id}/ 箱子详情 ──");
            JsonNode root = JsonUtils.json().parse(loot.toFile());
            JsonNode data = root.path("data");
            unknown(data, com.shinoaki.wows.api.vortex.resources.box.WowsLootBox.class, "lootbox data");
            var box = com.shinoaki.wows.api.vortex.resources.box.WowsLootBox.parse(root);
            expect("price", data.path("price").asLong(0), box.price());
            expect("clanresource", data.path("clanresource").asLong(0), box.clanresource());
            expect("nestinglevel", data.path("nestinglevel").asInt(0), box.nestinglevel());
            expect("cardprob", data.path("cardprob").asDouble(0), box.cardprob());
            expect("cancharge", data.path("cancharge").asBoolean(false), box.cancharge());
            expect("slots 数量", data.path("slots").size(), box.slots().size());
        }
    }

    //----------------------------------------------------------------- 工具

    /**
     * 比对 JSON 字段名与结构体声明的字段名（记录组件 / 实例字段），把 JSON 中多出来的字段报为缺失。
     *
     * @param type   目标结构体；null 表示只按 allowed 白名单校验
     * @param allowed 结构体之外额外允许的字段（如服务端偶尔多返回的字段）
     */
    private static void unknown(JsonNode node, Class<?> type, String label) {
        unknown(node, type, label, Set.of());
    }

    private static void unknown(JsonNode node, Class<?> type, String label, Set<String> allowed) {
        if (node == null || !node.isObject() || node.isEmpty()) {
            return;
        }
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

    private static JsonNode onlyData(JsonNode root) {
        for (Map.Entry<String, JsonNode> entry : root.path("data").properties()) {
            return entry.getValue();
        }
        throw new IllegalStateException("响应里没有 data 节点");
    }

    /**
     * 取第 depth 层里第一个「对象且没有子对象」的节点（用于 rank_info 这种三层动态 key 的结构）。
     */
    private static JsonNode firstLeafObject(JsonNode node, int depth) {
        if (depth <= 0 || node == null || !node.isObject()) {
            return node;
        }
        for (Map.Entry<String, JsonNode> entry : node.properties()) {
            return firstLeafObject(entry.getValue(), depth - 1);
        }
        return node;
    }

    private static void collectLeafNames(JsonNode node, Set<String> out, int depth) {
        if (depth == 0 || node == null || !node.isObject()) {
            return;
        }
        for (Map.Entry<String, JsonNode> entry : node.properties()) {
            out.add(entry.getKey());
            collectLeafNames(entry.getValue(), out, depth - 1);
        }
    }

    private static void expect(String name, Object expected, Object actual) {
        if (String.valueOf(expected).equals(String.valueOf(actual))) {
            System.out.println("  [ok] " + name + " = " + actual);
        } else {
            failed++;
            System.out.println("  [FAIL] " + name + " 期望=" + expected + " 实际=" + actual);
        }
    }

    //----------------------------------------------------------------- 取数

    private static void loadDumps() throws Exception {
        accountJson = JsonUtils.json().parse(DUMP_DIR.resolve("account.json").toFile());
        accountClansJson = JsonUtils.json().parse(DUMP_DIR.resolve("account-clans.json").toFile());
        shipsJson = JsonUtils.json().parse(DUMP_DIR.resolve("ships-pvp.json").toFile());
        clanInfoJson = JsonUtils.json().parse(DUMP_DIR.resolve("claninfo.json").toFile());
        clanMembersJson = JsonUtils.json().parse(DUMP_DIR.resolve("clan-members.json").toFile());
    }

    private static void fetch() throws Exception {
        HttpCodec.updateTimeout(25);
        HttpClient client = HttpCodec.client();
        WowsHttpUserTools user = new WowsHttpUserTools(client, SERVER);
        WowsHttpShipTools ship = new WowsHttpShipTools(client, SERVER, ACCOUNT_ID);
        WowsHttpClanTools clan = new WowsHttpClanTools(client, SERVER);

        accountJson = grab(() -> user.userVortex(ACCOUNT_ID));
        accountClansJson = grab(() -> clan.vortex().userSearchClanVortex(ACCOUNT_ID));
        shipsJson = grab(() -> ship.vortex().shipList(WowsBattlesType.PVP));
        clanInfoJson = grab(() -> clan.vortex().clanInfoVortex(CLAN_ID));
        clanMembersJson = grab(() -> clan.vortex().clanUserListInfoVortex(CLAN_ID));
        if (accountClansJson == null) {
            accountClansJson = JsonUtils.json().parse("{}");
        }
    }

    private interface Call {
        void run() throws Exception;
    }

    private static JsonNode grab(Call call) throws Exception {
        SchemaRecorder.enable();
        try {
            call.run();
        } catch (Exception e) {
            System.out.println("!! 请求失败: " + e.getMessage());
        } finally {
            List<SchemaRecorder.Record> records = SchemaRecorder.drain();
            SchemaRecorder.disable();
            if (!records.isEmpty()) {
                return JsonUtils.json().parse(records.get(0).json());
            }
        }
        return JsonUtils.json().parse("{}");
    }
}
