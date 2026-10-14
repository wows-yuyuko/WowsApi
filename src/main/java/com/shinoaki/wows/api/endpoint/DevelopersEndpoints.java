package com.shinoaki.wows.api.endpoint;

import com.shinoaki.wows.api.auth.AuthKind;
import com.shinoaki.wows.api.auth.AuthSlot;
import com.shinoaki.wows.api.data.ship.ShipExpansion;
import com.shinoaki.wows.api.developers.DevelopersUserShip;
import com.shinoaki.wows.api.developers.DevelopersUserShipOper;
import com.shinoaki.wows.api.developers.account.DevelopersSearchUser;
import com.shinoaki.wows.api.developers.account.DevelopersStatsByDate;
import com.shinoaki.wows.api.developers.account.DevelopersUserAchievements;
import com.shinoaki.wows.api.developers.account.DevelopersUserInfo;
import com.shinoaki.wows.api.developers.clan.DevelopersClanInfo;
import com.shinoaki.wows.api.developers.clan.DevelopersClanSeasonStats;
import com.shinoaki.wows.api.developers.clan.DevelopersSearchClan;
import com.shinoaki.wows.api.developers.clan.DevelopersSearchUserClan;
import com.shinoaki.wows.api.developers.clan.seasion.DevelopersSeasonInfo;
import com.shinoaki.wows.api.developers.encyclopedia.accountlevels.DevelopersAccountLevel;
import com.shinoaki.wows.api.developers.encyclopedia.achievements.DevelopersEncyclopediaAchievements;
import com.shinoaki.wows.api.developers.encyclopedia.battlearenas.DevelopersBattleArena;
import com.shinoaki.wows.api.developers.encyclopedia.battletypes.DevelopersBattleType;
import com.shinoaki.wows.api.developers.encyclopedia.collections.DevelopersCollection;
import com.shinoaki.wows.api.developers.encyclopedia.collections.DevelopersCollectionCard;
import com.shinoaki.wows.api.developers.encyclopedia.consumables.DevelopersEncyclopediaConsumables;
import com.shinoaki.wows.api.developers.encyclopedia.crewranks.DevelopersCommanderRank;
import com.shinoaki.wows.api.developers.encyclopedia.crews.DevelopersEncyclopediaCommander;
import com.shinoaki.wows.api.developers.encyclopedia.crewskills.DevelopersEncyclopediaSkill;
import com.shinoaki.wows.api.developers.encyclopedia.glossary.DevelopersGlossary;
import com.shinoaki.wows.api.developers.encyclopedia.info.DevelopersEncyclopediaInfo;
import com.shinoaki.wows.api.developers.encyclopedia.modules.DevelopersEncyclopediaModules;
import com.shinoaki.wows.api.developers.encyclopedia.shipprofile.DevelopersShipProfile;
import com.shinoaki.wows.api.developers.encyclopedia.ships.DevelopersEncyclopediaShips;
import com.shinoaki.wows.api.developers.seasons.DevelopersRankedAccount;
import com.shinoaki.wows.api.developers.seasons.DevelopersRankedSeason;
import com.shinoaki.wows.api.developers.seasons.DevelopersRankedShipStats;
import com.shinoaki.wows.api.server.DevNamespace;
import com.shinoaki.wows.api.type.WowsBattlesType;
import com.shinoaki.wows.api.type.WowsServer;

import java.util.List;
import java.util.Map;

/**
 * 官方开发者API 端点表：主机由 ServerProfile 注入，路径为命名空间内路径（前缀 /wows/ 或 /mk/ 按服务器）；
 * CN 无该通道（发送前拒绝）。
 *
 * @author Xun
 */
public final class DevelopersEndpoints {

    private DevelopersEndpoints() {
    }

    /**
     * 玩家搜索（application_id 必填）。
     */
    public static final Endpoint<List<DevelopersSearchUser>> ACCOUNT_SEARCH = Endpoint.dev(
            "dev.account.search",
            DevNamespace.ACCOUNT,
            "/account/list/",
            List.of(AuthSlot.required(AuthKind.APPLICATION_ID)),
            List.of(new ValueParam("search", "search", true)),
            (node, request, profile) -> DevelopersSearchUser.parse(node));

    /**
     * 玩家详情（extra 按服由 ServerProfile 提供；access_token 可选）。
     */
    public static final Endpoint<DevelopersUserInfo> ACCOUNT_INFO = Endpoint.dev(
            "dev.account.info",
            DevNamespace.ACCOUNT,
            "/account/info/",
            List.of(AuthSlot.required(AuthKind.APPLICATION_ID), AuthSlot.optional(AuthKind.ACCESS_TOKEN)),
            List.of(new ValueParam("account_id", "accountId", false), new ValueParam("extra", "extra", false)),
            (node, request, profile) -> DevelopersUserInfo.parse(request.accountId(), node));

    /**
     * 玩家成就（成就ID -&gt; 次数/进度；access_token 可选）。
     */
    public static final Endpoint<DevelopersUserAchievements> ACCOUNT_ACHIEVEMENTS = Endpoint.dev(
            "dev.account.achievements",
            DevNamespace.ACCOUNT,
            "/account/achievements/",
            List.of(AuthSlot.required(AuthKind.APPLICATION_ID), AuthSlot.optional(AuthKind.ACCESS_TOKEN)),
            List.of(new ValueParam("account_id", "accountId", false), new ValueParam("language", "language", false)),
            (node, request, profile) -> DevelopersUserAchievements.parse(node));

    /**
     * 玩家按日期统计（dates 为 YYYYMMDD 逗号列表，extra 可选 "pve"）。
     * <p>
     * WG 直营服已下线该接口（实测 GET/POST 均 METHOD_NOT_FOUND），仅莱服提供，故只声明支持 RU。
     */
    public static final Endpoint<DevelopersStatsByDate> ACCOUNT_STATS_BY_DATE = Endpoint.dev(
                    "dev.account.statsbydate",
                    DevNamespace.ACCOUNT,
                    "/account/statsbydate/",
                    List.of(AuthSlot.required(AuthKind.APPLICATION_ID), AuthSlot.optional(AuthKind.ACCESS_TOKEN)),
                    List.of(new ValueParam("account_id", "accountId", false), new ValueParam("dates", "dates", false),
                            new ValueParam("extra", "extra", false), new ValueParam("language", "language", false)),
                    (node, request, profile) -> DevelopersStatsByDate.parse(node))
            .withServers(WowsServer.RU);

    /**
     * 玩家所属军团（clan 命名空间：莱服仍在 /wows/ 下）。
     */
    public static final Endpoint<DevelopersSearchUserClan> CLAN_ACCOUNT = Endpoint.dev(
            "dev.clan.account",
            DevNamespace.CLAN,
            "/clans/accountinfo/",
            List.of(AuthSlot.required(AuthKind.APPLICATION_ID)),
            List.of(new ValueParam("account_id", "accountId", false), new LiteralParam("extra", "clan")),
            (node, request, profile) -> DevelopersSearchUserClan.parse(request.accountId(), node));

    /**
     * 军团详情（members 全量成员列表）。
     */
    public static final Endpoint<DevelopersClanInfo> CLAN_INFO = Endpoint.dev(
            "dev.clan.info",
            DevNamespace.CLAN,
            "/clans/info/",
            List.of(AuthSlot.required(AuthKind.APPLICATION_ID)),
            List.of(new ValueParam("clan_id", "clanId", false), new LiteralParam("extra", "members")),
            (node, request, profile) -> DevelopersClanInfo.parse(request.longParam("clanId"), node));

    /**
     * 军团搜索（clan 命名空间：莱服仍走 /wows/）。
     */
    public static final Endpoint<List<DevelopersSearchClan>> CLAN_SEARCH = Endpoint.dev(
            "dev.clan.search",
            DevNamespace.CLAN,
            "/clans/list/",
            List.of(AuthSlot.required(AuthKind.APPLICATION_ID)),
            List.of(new ValueParam("search", "search", true)),
            (node, request, profile) -> DevelopersSearchClan.parse(node));

    /**
     * 军团赛季/联赛（language 参数，默认 zh-cn）。
     */
    public static final Endpoint<List<DevelopersSeasonInfo>> CLAN_SEASON = Endpoint.dev(
            "dev.clan.season",
            DevNamespace.CLAN,
            "/clans/season/",
            List.of(AuthSlot.required(AuthKind.APPLICATION_ID)),
            List.of(new ValueParam("language", "language", false)),
            (node, request, profile) -> DevelopersSeasonInfo.parse(node));

    /**
     * 军团词典（莱服前缀 /mk/）。
     */
    public static final Endpoint<DevelopersGlossary> CLAN_GLOSSARY = Endpoint.dev(
            "dev.clan.glossary",
            DevNamespace.GLOSSARY,
            "/clans/glossary/",
            List.of(AuthSlot.required(AuthKind.APPLICATION_ID)),
            List.of(),
            (node, request, profile) -> DevelopersGlossary.parse(node));

    /**
     * 战舰统计（extra 见 {@link #statsExtra()}；带 access_token 时附带 in_garage=1）。
     */
    public static final Endpoint<DevelopersUserShip> SHIP_STATS = Endpoint.dev(
            "dev.ship.stats",
            DevNamespace.SHIP,
            "/ships/stats/",
            List.of(AuthSlot.required(AuthKind.APPLICATION_ID), AuthSlot.optional(AuthKind.ACCESS_TOKEN)),
            List.of(new ValueParam("account_id", "accountId", false), new ValueParam("extra", "extra", false),
                    new CredentialParam("in_garage", "1", AuthKind.ACCESS_TOKEN)),
            (node, request, profile) -> DevelopersUserShip.parse(node));

    /**
     * 战舰全量战绩（与 stats 同路径，extra 为全量 14 类；端点 id 区分监控基线）。
     */
    public static final Endpoint<DevelopersUserShipOper> SHIP_OPER = Endpoint.dev(
            "dev.ship.oper",
            DevNamespace.SHIP,
            "/ships/stats/",
            List.of(AuthSlot.required(AuthKind.APPLICATION_ID), AuthSlot.optional(AuthKind.ACCESS_TOKEN)),
            List.of(new ValueParam("account_id", "accountId", false), new ValueParam("extra", "extra", false),
                    new CredentialParam("in_garage", "1", AuthKind.ACCESS_TOKEN)),
            (node, request, profile) -> DevelopersUserShipOper.parse(node));

    /**
     * 战舰徽章（莱服不提供——端点声明支持面，调用即拒绝）。
     */
    public static final Endpoint<Map<Long, ShipExpansion>> SHIP_BADGES = Endpoint.dev(
                    "dev.ship.badges",
                    DevNamespace.SHIP,
                    "/ships/badges/",
                    List.of(AuthSlot.required(AuthKind.APPLICATION_ID)),
                    List.of(new ValueParam("account_id", "accountId", false)),
                    (node, request, profile) -> ShipExpansion.parseDevelopers(request.accountId(), node))
            .withServers(WowsServer.ASIA, WowsServer.EU, WowsServer.NA);

    /**
     * 百科基础信息（版本/语言/模块/舰种/国家字典）。
     */
    public static final Endpoint<DevelopersEncyclopediaInfo> ENCYCLOPEDIA_INFO = Endpoint.dev(
            "dev.encyclopedia.info",
            DevNamespace.ENCYCLOPEDIA,
            "/encyclopedia/info/",
            List.of(AuthSlot.required(AuthKind.APPLICATION_ID)),
            List.of(new ValueParam("language", "language", false)),
            (node, request, profile) -> DevelopersEncyclopediaInfo.parse(node));

    /**
     * 舰船字典（分页；ship_id / nation / type 可选过滤）。
     */
    public static final Endpoint<DevelopersEncyclopediaShips> ENCYCLOPEDIA_SHIPS = Endpoint.dev(
            "dev.encyclopedia.ships",
            DevNamespace.ENCYCLOPEDIA,
            "/encyclopedia/ships/",
            List.of(AuthSlot.required(AuthKind.APPLICATION_ID)),
            List.of(new ValueParam("ship_id", "shipId", false), new ValueParam("nation", "nation", false),
                    new ValueParam("type", "type", false), new ValueParam("page_no", "pageNo", false),
                    new ValueParam("limit", "limit", false), new ValueParam("language", "language", false)),
            (node, request, profile) -> DevelopersEncyclopediaShips.parse(node));

    /**
     * 舰船参数明细（按默认配置；ship_id 必填）。
     */
    public static final Endpoint<DevelopersShipProfile> ENCYCLOPEDIA_SHIP_PROFILE = Endpoint.dev(
            "dev.encyclopedia.shipprofile",
            DevNamespace.ENCYCLOPEDIA,
            "/encyclopedia/shipprofile/",
            List.of(AuthSlot.required(AuthKind.APPLICATION_ID)),
            List.of(new ValueParam("ship_id", "shipId", false), new ValueParam("language", "language", false)),
            (node, request, profile) -> DevelopersShipProfile.parse(node));

    /**
     * 模块字典（分页；module_id / type 可选过滤）。
     */
    public static final Endpoint<DevelopersEncyclopediaModules> ENCYCLOPEDIA_MODULES = Endpoint.dev(
            "dev.encyclopedia.modules",
            DevNamespace.ENCYCLOPEDIA,
            "/encyclopedia/modules/",
            List.of(AuthSlot.required(AuthKind.APPLICATION_ID)),
            List.of(new ValueParam("module_id", "moduleId", false), new ValueParam("type", "type", false),
                    new ValueParam("page_no", "pageNo", false), new ValueParam("limit", "limit", false),
                    new ValueParam("language", "language", false)),
            (node, request, profile) -> DevelopersEncyclopediaModules.parse(node));

    /**
     * 服役记录等级字典。
     */
    public static final Endpoint<Map<Long, DevelopersAccountLevel>> ENCYCLOPEDIA_ACCOUNT_LEVELS = Endpoint.dev(
            "dev.encyclopedia.accountlevels",
            DevNamespace.ENCYCLOPEDIA,
            "/encyclopedia/accountlevels/",
            List.of(AuthSlot.required(AuthKind.APPLICATION_ID)),
            List.of(),
            (node, request, profile) -> DevelopersAccountLevel.parse(node));

    /**
     * 指挥官字典（commander_id 可选过滤）。
     */
    public static final Endpoint<Map<Long, DevelopersEncyclopediaCommander>> ENCYCLOPEDIA_CREWS = Endpoint.dev(
            "dev.encyclopedia.crews",
            DevNamespace.ENCYCLOPEDIA,
            "/encyclopedia/crews/",
            List.of(AuthSlot.required(AuthKind.APPLICATION_ID)),
            List.of(new ValueParam("commander_id", "commanderId", false), new ValueParam("language", "language", false)),
            (node, request, profile) -> DevelopersEncyclopediaCommander.parse(node));

    /**
     * 指挥官技能字典（skill_id 可选过滤）。
     */
    public static final Endpoint<Map<Long, DevelopersEncyclopediaSkill>> ENCYCLOPEDIA_CREW_SKILLS = Endpoint.dev(
            "dev.encyclopedia.crewskills",
            DevNamespace.ENCYCLOPEDIA,
            "/encyclopedia/crewskills/",
            List.of(AuthSlot.required(AuthKind.APPLICATION_ID)),
            List.of(new ValueParam("skill_id", "skillId", false), new ValueParam("language", "language", false)),
            (node, request, profile) -> DevelopersEncyclopediaSkill.parse(node));

    /**
     * 指挥官军衔字典（nation 可选过滤）。
     */
    public static final Endpoint<Map<String, List<DevelopersCommanderRank>>> ENCYCLOPEDIA_CREW_RANKS = Endpoint.dev(
            "dev.encyclopedia.crewranks",
            DevNamespace.ENCYCLOPEDIA,
            "/encyclopedia/crewranks/",
            List.of(AuthSlot.required(AuthKind.APPLICATION_ID)),
            List.of(new ValueParam("nation", "nation", false), new ValueParam("language", "language", false)),
            (node, request, profile) -> DevelopersCommanderRank.parse(node));

    /**
     * 战斗类型字典（tag -&gt; 信息）。
     */
    public static final Endpoint<Map<String, DevelopersBattleType>> ENCYCLOPEDIA_BATTLE_TYPES = Endpoint.dev(
            "dev.encyclopedia.battletypes",
            DevNamespace.ENCYCLOPEDIA,
            "/encyclopedia/battletypes/",
            List.of(AuthSlot.required(AuthKind.APPLICATION_ID)),
            List.of(new ValueParam("language", "language", false)),
            (node, request, profile) -> DevelopersBattleType.parse(node));

    /**
     * 消耗品字典（分页；consumable_id / type 可选过滤；覆盖迷彩/涂装/皮肤/升级品/信号旗）。
     */
    public static final Endpoint<DevelopersEncyclopediaConsumables> ENCYCLOPEDIA_CONSUMABLES = Endpoint.dev(
            "dev.encyclopedia.consumables",
            DevNamespace.ENCYCLOPEDIA,
            "/encyclopedia/consumables/",
            List.of(AuthSlot.required(AuthKind.APPLICATION_ID)),
            List.of(new ValueParam("consumable_id", "consumableId", false), new ValueParam("type", "type", false),
                    new ValueParam("page_no", "pageNo", false), new ValueParam("limit", "limit", false),
                    new ValueParam("language", "language", false)),
            (node, request, profile) -> DevelopersEncyclopediaConsumables.parse(node));

    /**
     * 收藏字典。
     */
    public static final Endpoint<Map<Long, DevelopersCollection>> ENCYCLOPEDIA_COLLECTIONS = Endpoint.dev(
            "dev.encyclopedia.collections",
            DevNamespace.ENCYCLOPEDIA,
            "/encyclopedia/collections/",
            List.of(AuthSlot.required(AuthKind.APPLICATION_ID)),
            List.of(new ValueParam("language", "language", false)),
            (node, request, profile) -> DevelopersCollection.parse(node));

    /**
     * 收藏卡牌字典。
     */
    public static final Endpoint<Map<Long, DevelopersCollectionCard>> ENCYCLOPEDIA_COLLECTION_CARDS = Endpoint.dev(
            "dev.encyclopedia.collectioncards",
            DevNamespace.ENCYCLOPEDIA,
            "/encyclopedia/collectioncards/",
            List.of(AuthSlot.required(AuthKind.APPLICATION_ID)),
            List.of(new ValueParam("language", "language", false)),
            (node, request, profile) -> DevelopersCollectionCard.parse(node));

    /**
     * 地图字典。
     */
    public static final Endpoint<Map<Long, DevelopersBattleArena>> ENCYCLOPEDIA_BATTLE_ARENAS = Endpoint.dev(
            "dev.encyclopedia.battlearenas",
            DevNamespace.ENCYCLOPEDIA,
            "/encyclopedia/battlearenas/",
            List.of(AuthSlot.required(AuthKind.APPLICATION_ID)),
            List.of(new ValueParam("language", "language", false)),
            (node, request, profile) -> DevelopersBattleArena.parse(node));

    /**
     * 成就字典（battle / progress 两表）。
     */
    public static final Endpoint<DevelopersEncyclopediaAchievements> ENCYCLOPEDIA_ACHIEVEMENTS = Endpoint.dev(
            "dev.encyclopedia.achievements",
            DevNamespace.ENCYCLOPEDIA,
            "/encyclopedia/achievements/",
            List.of(AuthSlot.required(AuthKind.APPLICATION_ID)),
            List.of(new ValueParam("language", "language", false)),
            (node, request, profile) -> DevelopersEncyclopediaAchievements.parse(node));

    /**
     * 排位赛季列表（含联赛与段位；season_id 可选过滤）。
     */
    public static final Endpoint<Map<Long, DevelopersRankedSeason>> SEASONS_INFO = Endpoint.dev(
            "dev.seasons.info",
            DevNamespace.SEASONS,
            "/seasons/info/",
            List.of(AuthSlot.required(AuthKind.APPLICATION_ID)),
            List.of(new ValueParam("season_id", "seasonId", false), new ValueParam("language", "language", false)),
            (node, request, profile) -> DevelopersRankedSeason.parse(node));

    /**
     * 排位单船战绩（season_id 可选；access_token 可选）。
     */
    public static final Endpoint<DevelopersRankedShipStats> SEASONS_SHIP_STATS = Endpoint.dev(
            "dev.seasons.shipstats",
            DevNamespace.SEASONS,
            "/seasons/shipstats/",
            List.of(AuthSlot.required(AuthKind.APPLICATION_ID), AuthSlot.optional(AuthKind.ACCESS_TOKEN)),
            List.of(new ValueParam("account_id", "accountId", false), new ValueParam("season_id", "seasonId", false),
                    new ValueParam("language", "language", false)),
            (node, request, profile) -> DevelopersRankedShipStats.parse(node));

    /**
     * 排位玩家战绩（段位进度 + 各赛季统计；season_id 可选；access_token 可选）。
     */
    public static final Endpoint<DevelopersRankedAccount> SEASONS_ACCOUNT_INFO = Endpoint.dev(
            "dev.seasons.accountinfo",
            DevNamespace.SEASONS,
            "/seasons/accountinfo/",
            List.of(AuthSlot.required(AuthKind.APPLICATION_ID), AuthSlot.optional(AuthKind.ACCESS_TOKEN)),
            List.of(new ValueParam("account_id", "accountId", false), new ValueParam("season_id", "seasonId", false),
                    new ValueParam("language", "language", false)),
            (node, request, profile) -> DevelopersRankedAccount.parse(node));

    /**
     * 军团战赛季战绩（莱服不提供——响应 404，端点声明支持面，调用即拒绝）。
     */
    public static final Endpoint<DevelopersClanSeasonStats> CLAN_SEASON_STATS = Endpoint.dev(
                    "dev.clan.seasonstats",
                    DevNamespace.CLAN,
                    "/clans/seasonstats/",
                    List.of(AuthSlot.required(AuthKind.APPLICATION_ID), AuthSlot.optional(AuthKind.ACCESS_TOKEN)),
                    List.of(new ValueParam("account_id", "accountId", false), new ValueParam("language", "language", false)),
                    (node, request, profile) -> DevelopersClanSeasonStats.parse(node))
            .withServers(WowsServer.ASIA, WowsServer.EU, WowsServer.NA);

    /**
     * {@code dev.ship.stats} 的 extra 串（历史集合，pvp 除外；勿扩充，与监控基线契约绑定）。
     */
    private static final String STATS_EXTRA = String.join(",", WowsBattlesType.DEVELOPERS_STATS_TYPES.stream()
            .filter(type -> type != WowsBattlesType.PVP)
            .map(WowsBattlesType::code)
            .toList());

    public static String statsExtra() {
        return STATS_EXTRA;
    }
}
