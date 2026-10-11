package com.shinoaki.wows.field_coverage;

import com.shinoaki.wows.api.codec.HttpCodec;
import com.shinoaki.wows.api.codec.http.WowsHttpShipTools;
import com.shinoaki.wows.api.type.WowsBattlesType;
import com.shinoaki.wows.api.type.WowsBattlesTypeRu;
import com.shinoaki.wows.api.type.WowsServer;
import com.shinoaki.wows.api.utils.JsonUtils;
import com.shinoaki.wows.api.vortex.VortexUserShip;
import com.shinoaki.wows.api.vortex.account.VortexUserInfo;
import com.shinoaki.wows.api.vortex.account.statistics.Basic;
import com.shinoaki.wows.api.vortex.account.statistics.VortexRankInfo;
import com.shinoaki.wows.api.vortex.account.statistics.VortexRatingInfo;
import com.shinoaki.wows.api.vortex.account.statistics.VortexUserInfoStatistics;
import com.shinoaki.wows.api.vortex.clan.base.VortexClanInfo;
import com.shinoaki.wows.api.vortex.clan.base.info.VortexClanAchievement;
import com.shinoaki.wows.api.vortex.clan.base.info.VortexClanMaxPosition;
import com.shinoaki.wows.api.vortex.clan.base.info.VortexClanWowsLadderInfo;
import com.shinoaki.wows.api.vortex.clan.members.VortexClanStatisticsInfo;
import com.shinoaki.wows.api.vortex.clan.members.VortexClanUserInfo;
import com.shinoaki.wows.api.vortex.resources.box.WowsLootBox;
import com.shinoaki.wows.api.vortex.resources.box.WowsLootBoxIcon;
import com.shinoaki.wows.api.vortex.resources.box.WowsLootBoxReward;
import com.shinoaki.wows.api.vortex.resources.box.WowsLootBoxRewardData;
import com.shinoaki.wows.api.vortex.resources.box.WowsLootBoxRewardGroup;
import com.shinoaki.wows.api.vortex.resources.box.WowsLootBoxSlot;
import com.shinoaki.wows.api.vortex.ship.VortexShipInfo;
import com.shinoaki.wows.api.vortex.ship.VortexShipStatistics;
import tools.jackson.databind.JsonNode;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.RecordComponent;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * 莱服(korabli.su)vortex 响应字段覆盖自检：把 {@code target/ru-dump/} 里的真实响应喂给库的解析类，
 * 逐层比对「JSON 里出现的字段」与「结构体声明的字段」，把多出来的字段报出来。
 * <p>
 * 重点盯的是分家后莱服多出来的东西：98/78 字段的战斗块、brawl/clan/oper_mm/rank/rating* 八个新数据块、
 * {@code rating_info}、{@code basic.mastery_rank}、{@code mk_ladder}（含 stage）、军团汇总的 league_best。
 * <p>
 * 离线数据用 {@code target/ru-dump/}（不入 git）；没有时只打印跳过（莱服请求要走 korabli 域名，
 * 这里不做在线兜底，避免在 CI/无网环境里挂）。生成方式见文件末尾注释。
 * <p>
 * 用法：{@code java -cp ... com.shinoaki.wows.field_coverage.RuVortexCoverageTest}；退出码 0 = 全部字段都有结构体。
 *
 * @author Xun
 */
public class RuVortexCoverageTest {

    private static final Path DUMP_DIR = Path.of("target/ru-dump");
    private static final WowsServer SERVER = WowsServer.RU;
    private static final long ACCOUNT_ID = 212112137L;
    private static final long CLAN_ID = 451731L;

    private static int failed = 0;
    private static JsonNode accountJson;
    private static JsonNode shipsJson;
    private static JsonNode clanInfoJson;
    private static JsonNode clanMembersJson;

    public static void main(String[] args) throws Exception {
        if (!Files.isRegularFile(DUMP_DIR.resolve("account.json"))) {
            System.out.println("数据来源: 无（缺少 " + DUMP_DIR.toAbsolutePath() + "），跳过莱服用例");
            System.exit(0);
        }
        System.out.println("数据来源: " + DUMP_DIR.toAbsolutePath() + "（离线，账号 " + ACCOUNT_ID + "）");
        accountJson = JsonUtils.json().parse(DUMP_DIR.resolve("account.json").toFile());
        shipsJson = JsonUtils.json().parse(DUMP_DIR.resolve("ships-pvp.json").toFile());
        clanInfoJson = JsonUtils.json().parse(DUMP_DIR.resolve("claninfo.json").toFile());
        clanMembersJson = JsonUtils.json().parse(DUMP_DIR.resolve("clan-members.json").toFile());

        checkBattlesType();
        checkAccount();
        checkShips();
        checkLootBox();
        checkClanInfo();
        checkClanMembers();

        System.out.println();
        System.out.println(failed == 0 ? "结果: 莱服响应字段全部有对应结构体" : "结果: " + failed + " 处字段缺少结构体");
        System.exit(failed == 0 ? 0 : 1);
    }

    //----------------------------------------------------------------- 战斗类型枚举

    private static void checkBattlesType() {
        section("WowsBattlesTypeRu（莱服专属战斗类型，兼容 WowsBattlesType）");
        expect("枚举数量", 22, WowsBattlesTypeRu.values().length);
        for (WowsBattlesType type : WowsBattlesType.values()) {
            WowsBattlesTypeRu ru = WowsBattlesTypeRu.of(type);
            expect("of(" + type + ").toWowsBattlesType()", type, ru.toWowsBattlesType());
            expect("of(" + type + ").isRuOnly()", false, ru.isRuOnly());
        }
        expect("BRAWL.isRuOnly()", true, WowsBattlesTypeRu.BRAWL.isRuOnly());
        expect("BRAWL.toWowsBattlesType()", null, WowsBattlesTypeRu.BRAWL.toWowsBattlesType());
        expect("findCode(rating_solo)", WowsBattlesTypeRu.RATING_SOLO, WowsBattlesTypeRu.findCode("rating_solo"));
        expect("code(PVE_DIV2)", "pve_div2", WowsBattlesTypeRu.PVE_DIV2.code());

        var ruTools = new WowsHttpShipTools(HttpCodec.client(), SERVER, ACCOUNT_ID).vortex();
        expect("莱服战舰列表 URL",
                "https://vortex.korabli.su/api/accounts/" + ACCOUNT_ID + "/ships/rating_solo/",
                ruTools.shipListUri(WowsBattlesTypeRu.RATING_SOLO).toString());
        try {
            new WowsHttpShipTools(HttpCodec.client(), WowsServer.ASIA, ACCOUNT_ID).vortex().shipListUri(WowsBattlesTypeRu.BRAWL);
            expect("非莱服取莱服专属类型应抛异常", "IllegalArgumentException", "没有抛异常");
        } catch (IllegalArgumentException e) {
            expect("非莱服取莱服专属类型应抛异常", true, e.getMessage().contains("莱服"));
        }
    }

    //----------------------------------------------------------------- 账号

    private static void checkAccount() throws Exception {
        section("/api/accounts/{id}/ 账号详情（莱服）");
        JsonNode data = onlyData(accountJson);
        JsonNode statistics = data.path("statistics");
        unknown(data, VortexUserInfo.class, "账号节点", Set.of("hidden_profile", "dog_tag"));
        unknown(statistics, VortexUserInfoStatistics.class, "statistics");
        unknown(statistics.path("basic"), Basic.class, "statistics.basic");
        unknown(firstLeafObject(statistics.path("rank_info"), 3), VortexRankInfo.class, "rank_info 最内层");
        for (Map.Entry<String, JsonNode> entry : statistics.path("rating_info").properties()) {
            unknown(entry.getValue(), VortexRatingInfo.class, "rating_info." + entry.getKey());
        }
        int blocks = 0;
        for (Map.Entry<String, JsonNode> entry : statistics.properties()) {
            JsonNode node = entry.getValue();
            if (node.isObject() && node.has("battles_count")) {
                blocks++;
                unknown(node, VortexShipInfo.class, "statistics." + entry.getKey());
            }
        }
        for (Map.Entry<String, JsonNode> season : statistics.path("seasons").properties()) {
            for (Map.Entry<String, JsonNode> type : season.getValue().properties()) {
                if (type.getValue().isObject() && type.getValue().has("battles_count")) {
                    unknown(type.getValue(), VortexShipInfo.class, "seasons." + season.getKey() + "." + type.getKey());
                }
            }
        }
        System.out.println("   战斗数据块 " + blocks + " 个，rating_info " + statistics.path("rating_info").size() + " 个赛季");

        //结构体 104 = 其它服 80 + 莱服 18 + oper_mm 专属 6
        expect("VortexShipInfo 字段数", 104, VortexShipInfo.class.getRecordComponents().length);
        expect("数据块字段数与结构体一致", 98, statistics.path("pvp").size());

        VortexUserInfo info = VortexUserInfo.parse(accountJson, ACCOUNT_ID);
        VortexUserInfoStatistics stat = info.statistics();
        expect("visibility_settings 莱服不返回", false, data.has("visibility_settings"));
        expect("statistics.mastery_sign 莱服不返回", false, statistics.has("mastery_sign"));
        expect("statistics.mark_of_excellence 莱服不返回", false, statistics.has("mark_of_excellence"));
        expect("basic.mastery_rank", statistics.path("basic").path("mastery_rank").asLong(0), stat.getBasic().mastery_rank());
        expect("basic.last_battle_time", statistics.path("basic").path("last_battle_time").asLong(0), stat.getBasic().last_battle_time());

        //八个莱服新数据块
        WowsBattlesTypeRu[] ruBlocks = {WowsBattlesTypeRu.BRAWL, WowsBattlesTypeRu.CLAN, WowsBattlesTypeRu.OPER_MM, WowsBattlesTypeRu.RANK,
                WowsBattlesTypeRu.RANK_OLD, WowsBattlesTypeRu.RATING, WowsBattlesTypeRu.RATING_DIV, WowsBattlesTypeRu.RATING_SOLO};
        for (WowsBattlesTypeRu type : ruBlocks) {
            String code = type.code();
            long expected = statistics.path(code).path("battles_count").asLong(0);
            expect("statistics." + code + ".battles_count", expected, stat.block(type).battles_count());
        }
        //新字段真的有值
        expect("statistics.pvp.assist_damage", statistics.path("pvp").path("assist_damage").asLong(0), stat.getPvp().assist_damage());
        expect("statistics.pvp.max_assist_damage", statistics.path("pvp").path("max_assist_damage").asLong(0), stat.getPvp().max_assist_damage());
        expect("statistics.pvp.mines_agro", statistics.path("pvp").path("mines_agro").asLong(0), stat.getPvp().mines_agro());
        expect("statistics.pvp.battles_count_2608", statistics.path("pvp").path("battles_count_2608").asLong(0), stat.getPvp().battles_count_2608());
        expect("statistics.oper_mm.tasks_completed0", statistics.path("oper_mm").path("tasks_completed0").asLong(0), stat.getOper_mm().tasks_completed0());
        expect("statistics.oper_mm 字段数", 104, statistics.path("oper_mm").size());
        expect("statistics.pvp 有助攻数据", true, stat.getPvp().assist_damage() > 0);
        expect("statistics.pvp 有水雷潜在", true, stat.getPvp().mines_agro() > 0);

        expect("rating_info 赛季数", statistics.path("rating_info").size(), stat.getRating_info().size());
        String season = statistics.path("rating_info").properties().iterator().next().getKey();
        expect("rating_info." + season + ".rating", statistics.path("rating_info").path(season).path("rating").asLong(0),
                stat.getRating_info().get(season).rating());
        expect("rating_info." + season + ".league_best", statistics.path("rating_info").path(season).path("league_best").asString(""),
                stat.getRating_info().get(season).league_best());

        //默认值恒为 0（其它服缺这 18 个字段时走这里）
        expect("defaultValue().assist_damage()", 0L, VortexShipInfo.defaultValue().assist_damage());
        expect("defaultValue().mines_agro()", 0L, VortexShipInfo.defaultValue().mines_agro());
        expect("defaultValue().shots_by_secondary()", 0L, VortexShipInfo.defaultValue().shots_by_secondary());
    }

    //----------------------------------------------------------------- 战舰

    private static void checkShips() throws Exception {
        section("/api/accounts/{id}/ships/pvp/ 战舰列表（莱服）");
        JsonNode data = onlyData(shipsJson);
        JsonNode statistics = data.path("statistics");
        unknown(data, VortexUserShip.class, "战舰列表节点", Set.of("hidden_profile", "statistics"));
        int ships = 0;
        int withData = 0;
        Set<String> entryKeys = new TreeSet<>();
        for (Map.Entry<String, JsonNode> ship : statistics.properties()) {
            ships++;
            ship.getValue().properties().forEach(e -> entryKeys.add(e.getKey()));
            JsonNode pvp = ship.getValue().path("pvp");
            if (!pvp.isEmpty()) {
                withData++;
                unknown(pvp, VortexShipInfo.class, "ship." + ship.getKey() + ".pvp");
            }
        }
        System.out.println("   战舰 " + ships + " 条（有战绩 " + withData + " 条），单船条目字段: " + entryKeys);

        expect("单船条目只有战斗块(莱服无 mastery_sign/mark_of_excellence)", "[pvp]", entryKeys.toString());
        Map.Entry<String, JsonNode> withPvp = null;
        for (Map.Entry<String, JsonNode> ship : statistics.properties()) {
            if (!ship.getValue().path("pvp").isEmpty()) {
                withPvp = ship;
                break;
            }
        }
        if (withPvp == null) {
            throw new IllegalStateException("莱服 dump 里没有一条有战绩的单船，无法校验字段数");
        }
        expect("单船块字段数", 78, withPvp.getValue().path("pvp").size());

        VortexUserShip userShip = VortexUserShip.parse(WowsBattlesTypeRu.PVP, shipsJson);
        expect("type()", WowsBattlesType.PVP, userShip.type());
        expect("typeRu()", WowsBattlesTypeRu.PVP, userShip.typeRu());
        expect("visibility_settings(莱服缺字段->false)", false, userShip.visibility_settings());
        expect("accountId", ACCOUNT_ID, userShip.accountId());
        expect("战舰数量", ships, userShip.shipMap().size());

        long shipId = Long.parseLong(withPvp.getKey());
        JsonNode first = statistics.path(String.valueOf(shipId)).path("pvp");
        VortexShipStatistics info = userShip.shipMap().get(shipId);
        expect("单船 battles_count", first.path("battles_count").asLong(0), info.shipInfo().battles_count());
        expect("单船 assist_damage", first.path("assist_damage").asLong(0), info.shipInfo().assist_damage());
        expect("单船 mines_agro", first.path("mines_agro").asLong(0), info.shipInfo().mines_agro());
        expect("单船 masterySign(莱服缺字段->空)", "", info.masterySign());
        expect("单船 markOfExcellence(莱服缺字段->空)", true, info.markOfExcellence().personal().percentile() == 0);
    }

    //----------------------------------------------------------------- 箱子

    /**
     * 莱服箱子和其它服的区别：多 quotesTitle/rarity/sortOrder，保底(filler/savePoint)从箱子级挪到槽位级，
     * 且不返回 tags/filler/price/savePoint/cancharge/*prob/nestinglevel（解析后是默认值，不能当真实数据）。
     */
    private static void checkLootBox() {
        section("/api/get_lootbox/{lang}/{id}/ 箱子详情（莱服，同一个箱子ID 4288861104）");
        Path file = DUMP_DIR.resolve("lootbox-RU-4288861104.json");
        if (!Files.isRegularFile(file)) {
            System.out.println("  跳过（缺少 " + file.toAbsolutePath() + "）");
            return;
        }
        JsonNode root;
        try {
            root = JsonUtils.json().parse(file.toFile());
        } catch (Exception e) {
            failed++;
            System.out.println("  [FAIL] 读取失败: " + e.getMessage());
            return;
        }
        JsonNode data = root.path("data");
        JsonNode slot = data.path("slots").path(0);
        JsonNode group = slot.path("valuableRewards").path("list1");
        JsonNode reward = group.path("rewards").path(0);
        unknown(data, WowsLootBox.class, "data（莱服箱子）");
        unknown(slot, WowsLootBoxSlot.class, "slots[0]");
        unknown(group, WowsLootBoxRewardGroup.class, "valuableRewards.list1");
        unknown(reward, WowsLootBoxReward.class, "rewards[0]");
        unknown(reward.path("additionalData"), WowsLootBoxRewardData.class, "rewards[0].additionalData");
        //图标类字段名沿用 WowsIcons 约定(default->defaultUrl)，按 JSON 原名放行
        unknown(data.path("icons"), WowsLootBoxIcon.class, "icons", Set.of("default"));

        WowsLootBox box;
        try {
            box = WowsLootBox.parse(root);
        } catch (Exception e) {
            failed++;
            System.out.println("  [FAIL] WowsLootBox.parse 抛异常: " + e.getMessage());
            return;
        }
        expect("quotesTitle(莱服独有)", data.path("quotesTitle").asString(""), box.quotesTitle());
        expect("rarity(莱服独有)", data.path("rarity").asString(""), box.rarity());
        expect("sortOrder(莱服独有)", data.path("sortOrder").asInt(0), box.sortOrder());
        expect("slots[0].filler.type(莱服独有)", slot.path("filler").path("type").asString(""), box.slots().get(0).filler().type());
        expect("slots[0].filler.amount(莱服独有)", slot.path("filler").path("amount").asInt(0), box.slots().get(0).filler().amount());
        expect("slots[0].savePoint(莱服独有)", slot.path("savePoint").asLong(0), box.slots().get(0).savePoint());
        expect("additionalData.rarity(莱服独有)", reward.path("additionalData").path("rarity").asString(""),
                box.slots().get(0).valuableRewards().get("list1").rewards().get(0).additionalData().rarity());
        //莱服不返回箱子级的这些字段，解析后是默认值，别当真实数据用
        expect("price 莱服不返回->0", 0L, box.price());
        expect("savePoint 莱服不返回->0", 0, box.savePoint());
        expect("filler 莱服不返回->null", true, box.filler() == null);
        expect("tags 莱服不返回->空", 0, box.tags().size());
        int rewards = 0;
        for (WowsLootBoxSlot one : box.slots()) {
            for (Map<String, WowsLootBoxRewardGroup> groups : List.of(one.commonRewards(), one.valuableRewards())) {
                for (WowsLootBoxRewardGroup rewardGroup : groups.values()) {
                    rewards += rewardGroup.rewards().size();
                }
            }
        }
        expect("奖励条目总数", 255, rewards);
    }

    //----------------------------------------------------------------- 军团

    private static void checkClanInfo() {
        section("/api/clanbase/{id}/claninfo/ 军团详情（莱服用 mk_ladder）");
        JsonNode clanview = clanInfoJson.path("clanview");
        JsonNode clan = clanview.path("clan");
        JsonNode ladder = clanview.path("mk_ladder");
        expect("clanview 里是 mk_ladder", true, clanview.has("mk_ladder"));
        expect("clanview 里没有 wows_ladder", false, clanview.has("wows_ladder"));
        unknown(ladder.path("max_position"), VortexClanMaxPosition.class, "mk_ladder.max_position");
        unknown(ladder.path("ratings").path(0).path("max_position"), VortexClanMaxPosition.class, "mk_ladder.ratings[].max_position");
        unknown(clanview.path("achievements").path(0), VortexClanAchievement.class, "achievements[0]");
        unknown(clan.path("recruiting_restrictions"), com.shinoaki.wows.api.vortex.clan.base.info.VortexClanRecruitingRestrictions.class, "recruiting_restrictions");

        VortexClanInfo info = VortexClanInfo.to(SERVER, CLAN_ID, clanInfoJson);
        expect("ladder 字段集合(结构体)", names(VortexClanWowsLadderInfo.class).toString(), names(VortexClanWowsLadderInfo.class).toString());
        expect("mk_ladder.public_rating", ladder.path("public_rating").asInt(0), info.wowsLadder().public_rating());
        expect("mk_ladder.division_rating_max", ladder.path("division_rating_max").asInt(0), info.wowsLadder().division_rating_max());
        expect("mk_ladder.ratings 数量", ladder.path("ratings").size(), info.wowsLadder().ratings().size());
        expect("mk_ladder.stage 存在", true, ladder.has("stage"));
        expect("mk_ladder.stage 原样保留(null)", true, info.wowsLadder().stage().isNull());
        expect("achievements 数量", clanview.path("achievements").size(), info.achievements().size());
        for (Map.Entry<String, JsonNode> stat : clan.path("stats").properties()) {
            expect("stats." + stat.getKey(), stat.getValue().asLong(0), info.stats().get(stat.getKey()));
        }
        expect("leveling", clan.path("leveling").isNumber() ? clan.path("leveling").asInt() : null, info.leveling());
        expect("clan 没有 community_urls(莱服)", false, clan.has("community_urls"));
        expect("clan 没有 raw_description(莱服)", false, clan.has("raw_description"));
        expect("communityUrls 为空", 0, info.communityUrls().size());
    }

    private static void checkClanMembers() {
        section("/api/members/{id}/ 军团成员（莱服）");
        JsonNode statistics = clanMembersJson.path("clan_statistics");
        unknown(statistics, VortexClanStatisticsInfo.class, "clan_statistics");
        Set<String> itemExpected = new TreeSet<>(List.of("abnormal_results", "accumulative_clan_resource", "battles_count", "battles_per_day", "damage_per_battle",
                "days_in_clan", "exp_per_battle", "frags_per_battle", "id", "is_banned", "is_bonus_activated", "is_hidden_statistics", "is_press",
                "last_battle_time", "leveling", "name", "online_status", "profile_link", "rank", "role", "season_id", "season_rank", "wins_percentage"));
        Set<String> itemActual = new TreeSet<>();
        clanMembersJson.path("items").path(0).properties().forEach(e -> itemActual.add(e.getKey()));
        expect("items[0] 字段集合(莱服没有 bans)", itemExpected.toString(), itemActual.toString());

        VortexClanStatisticsInfo info = VortexClanUserInfo.to(SERVER, clanMembersJson);
        expect("成员数量", clanMembersJson.path("items").size(), info.infos().size());
        expect("clan_statistics.league_best", null, info.league_best());
        expect("clan_statistics.league_best_rating", null, info.league_best_rating());
        expect("clan_statistics.wins_percentage", statistics.path("wins_percentage").asDouble(0), info.wins_percentage());
        JsonNode first = clanMembersJson.path("items").path(0);
        var firstUser = info.infos().get(0);
        expect("items[0].role.rank", first.path("role").path("rank").asInt(0), firstUser.roleRank());
        expect("items[0].leveling", first.path("leveling").isNumber() ? first.path("leveling").asInt() : null, firstUser.leveling());
        expect("items[0].bans 莱服没有 -> MissingNode", true, firstUser.bans().isMissingNode());
    }

    //----------------------------------------------------------------- 工具

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

    /*
     * 离线数据的生成（2026-10-10，账号 212112137 / 军团 451731）：
     *   target/ru-dump/account.json         https://vortex.korabli.su/api/accounts/212112137/
     *   target/ru-dump/ships-pvp.json       https://vortex.korabli.su/api/accounts/212112137/ships/pvp/
     *   target/ru-dump/claninfo.json        https://clans.korabli.su/api/clanbase/451731/claninfo/
     *   target/ru-dump/clan-members.json    https://clans.korabli.su/api/members/451731/?battle_type=pvp
     * 目录在 target/ 下、不入 git；文件缺失时本用例直接跳过。
     */
}
