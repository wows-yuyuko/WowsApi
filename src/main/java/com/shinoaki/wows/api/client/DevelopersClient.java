package com.shinoaki.wows.api.client;

import com.shinoaki.wows.api.auth.AccessToken;
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
import com.shinoaki.wows.api.developers.encyclopedia.consumables.DevelopersEncyclopediaConsumable;
import com.shinoaki.wows.api.developers.encyclopedia.consumables.DevelopersEncyclopediaConsumables;
import com.shinoaki.wows.api.developers.encyclopedia.crewranks.DevelopersCommanderRank;
import com.shinoaki.wows.api.developers.encyclopedia.crews.DevelopersEncyclopediaCommander;
import com.shinoaki.wows.api.developers.encyclopedia.crewskills.DevelopersEncyclopediaSkill;
import com.shinoaki.wows.api.developers.encyclopedia.glossary.DevelopersGlossary;
import com.shinoaki.wows.api.developers.encyclopedia.info.DevelopersEncyclopediaInfo;
import com.shinoaki.wows.api.developers.encyclopedia.modules.DevelopersEncyclopediaModule;
import com.shinoaki.wows.api.developers.encyclopedia.modules.DevelopersEncyclopediaModules;
import com.shinoaki.wows.api.developers.encyclopedia.shipprofile.DevelopersShipProfile;
import com.shinoaki.wows.api.developers.encyclopedia.ships.DevelopersEncyclopediaShip;
import com.shinoaki.wows.api.developers.encyclopedia.ships.DevelopersEncyclopediaShips;
import com.shinoaki.wows.api.developers.seasons.DevelopersRankedAccount;
import com.shinoaki.wows.api.developers.seasons.DevelopersRankedSeason;
import com.shinoaki.wows.api.developers.seasons.DevelopersRankedShipStats;
import com.shinoaki.wows.api.endpoint.DevelopersEndpoints;
import com.shinoaki.wows.api.endpoint.EndpointRequest;
import com.shinoaki.wows.api.error.BasicException;
import com.shinoaki.wows.api.error.HttpThrowableStatus;
import com.shinoaki.wows.api.transport.HttpPipeline;
import com.shinoaki.wows.api.type.WowsBattlesType;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 官方开发者API 视图：1 调用 = 1 请求。application_id 必填、access_token 可选（按账号匹配）；
 * CN 无该通道（发送前抛 {@code UnsupportedOnServerException}）。
 * <p>
 * 覆盖官方 5 个分区：Account（账号/成就）、Encyclopedia（百科字典）、Warships（战舰统计/徽章）、
 * Seasons（排位赛季）、Clans（军团/军团战）。带文本的接口默认 language=zh-cn，均提供显式 language 重载。
 *
 * @author Xun
 */
public final class DevelopersClient {

    /**
     * 文本类接口的默认语言。
     */
    private static final String DEFAULT_LANGUAGE = "zh-cn";

    private final HttpPipeline pipeline;

    public DevelopersClient(HttpPipeline pipeline) {
        this.pipeline = Objects.requireNonNull(pipeline, "pipeline");
    }

    // ------------------------------------------------------------------ 账号

    /**
     * 玩家搜索（1 请求）。
     */
    public List<DevelopersSearchUser> search(String text) throws BasicException {
        return pipeline.execute(DevelopersEndpoints.ACCOUNT_SEARCH, EndpointRequest.of(Map.of("search", text)));
    }

    /**
     * 玩家详情；实例配置了该账号 access_token 时返回 private.* 私有数据。
     */
    public DevelopersUserInfo user(long accountId) throws BasicException {
        return user(accountId, null);
    }

    /**
     * 玩家详情，调用级覆盖 access_token（优先于实例配置；null = 用实例配置）。
     */
    public DevelopersUserInfo user(long accountId, AccessToken accessToken) throws BasicException {
        EndpointRequest request = EndpointRequest.account(accountId,
                Map.of("extra", pipeline.profile().accountInfoExtra()));
        if (accessToken != null) {
            request = request.credentials(accessToken);
        }
        return pipeline.execute(DevelopersEndpoints.ACCOUNT_INFO, request);
    }

    /**
     * 玩家成就（成就ID -&gt; 次数/进度；1 请求）。
     */
    public DevelopersUserAchievements userAchievements(long accountId) throws BasicException {
        return pipeline.execute(DevelopersEndpoints.ACCOUNT_ACHIEVEMENTS, EndpointRequest.account(accountId));
    }

    /**
     * 玩家按日期统计（不带日期段 = 服务器默认返回「date=0」汇总切片）。
     * <p>
     * 仅莱服提供该接口（WG 直营服已下线）；其它服调用发送前抛 {@code UnsupportedOnServerException}。
     */
    public DevelopersStatsByDate statsByDate(long accountId) throws BasicException {
        return statsByDate(accountId, null, null);
    }

    /**
     * 玩家按日期统计（1 请求）。
     *
     * @param dates YYYYMMDD 逗号列表（最多当前日期前 28 天；null = 不指定）
     * @param extra 附加块（"pve"；null = 不指定）
     */
    public DevelopersStatsByDate statsByDate(long accountId, String dates, String extra) throws BasicException {
        return pipeline.execute(DevelopersEndpoints.ACCOUNT_STATS_BY_DATE,
                EndpointRequest.account(accountId, params("dates", dates, "extra", extra)));
    }

    // ------------------------------------------------------------------ 军团

    /**
     * 玩家所属军团（1 请求）。
     */
    public DevelopersSearchUserClan accountClan(long accountId) throws BasicException {
        return pipeline.execute(DevelopersEndpoints.CLAN_ACCOUNT, EndpointRequest.account(accountId));
    }

    /**
     * 军团详情（1 请求，含 members 全量成员列表）。
     */
    public DevelopersClanInfo clanInfo(long clanId) throws BasicException {
        return pipeline.execute(DevelopersEndpoints.CLAN_INFO,
                EndpointRequest.of(Map.of("clanId", String.valueOf(clanId))));
    }

    /**
     * 军团搜索（1 请求）。
     */
    public List<DevelopersSearchClan> clanSearch(String clanTag) throws BasicException {
        return pipeline.execute(DevelopersEndpoints.CLAN_SEARCH, EndpointRequest.of(Map.of("search", clanTag)));
    }

    /**
     * 军团赛季/联赛；语言默认 zh-cn。
     */
    public List<DevelopersSeasonInfo> clanSeason() throws BasicException {
        return clanSeason(DEFAULT_LANGUAGE);
    }

    /**
     * 军团赛季/联赛（1 请求）。
     */
    public List<DevelopersSeasonInfo> clanSeason(String language) throws BasicException {
        return pipeline.execute(DevelopersEndpoints.CLAN_SEASON, EndpointRequest.of(Map.of("language", language)));
    }

    /**
     * 军团词典（1 请求）。
     */
    public DevelopersGlossary glossary() throws BasicException {
        return pipeline.execute(DevelopersEndpoints.CLAN_GLOSSARY, EndpointRequest.of(Map.of()));
    }

    /**
     * 军团战赛季战绩（1 请求；莱服不提供该接口，发送前拒绝）。
     */
    public DevelopersClanSeasonStats clanSeasonStats(long accountId) throws BasicException {
        return pipeline.execute(DevelopersEndpoints.CLAN_SEASON_STATS, EndpointRequest.account(accountId));
    }

    // ------------------------------------------------------------------ 战舰

    /**
     * 战舰统计（extra 为历史集合）。
     */
    public DevelopersUserShip ships(long accountId) throws BasicException {
        return ships(accountId, null);
    }

    /**
     * 战舰统计；带 access_token 时附带 {@code in_garage=1}。
     */
    public DevelopersUserShip ships(long accountId, AccessToken accessToken) throws BasicException {
        return pipeline.execute(DevelopersEndpoints.SHIP_STATS,
                shipRequest(accountId, accessToken, DevelopersEndpoints.statsExtra()));
    }

    /**
     * 战舰全量战绩（extra 为全量 14 类）。
     */
    public DevelopersUserShipOper shipsOper(long accountId) throws BasicException {
        return shipsOper(accountId, null);
    }

    /**
     * 战舰全量战绩；带 access_token 时附带 {@code in_garage=1}。
     */
    public DevelopersUserShipOper shipsOper(long accountId, AccessToken accessToken) throws BasicException {
        return pipeline.execute(DevelopersEndpoints.SHIP_OPER,
                shipRequest(accountId, accessToken, WowsBattlesType.DEVELOPERS_ALL_STATS_EXTRA));
    }

    /**
     * 战舰徽章；莱服不提供（发送前抛 {@code UnsupportedOnServerException}）。
     */
    public Map<Long, ShipExpansion> shipBadges(long accountId) throws BasicException {
        return pipeline.execute(DevelopersEndpoints.SHIP_BADGES, EndpointRequest.account(accountId));
    }

    // ------------------------------------------------------------------ 百科：基础与舰船

    /**
     * 百科基础信息；语言默认 zh-cn。
     */
    public DevelopersEncyclopediaInfo encyclopediaInfo() throws BasicException {
        return encyclopediaInfo(DEFAULT_LANGUAGE);
    }

    /**
     * 百科基础信息（1 请求）。
     */
    public DevelopersEncyclopediaInfo encyclopediaInfo(String language) throws BasicException {
        return pipeline.execute(DevelopersEndpoints.ENCYCLOPEDIA_INFO, languageRequest(language));
    }

    /**
     * 百科舰船字典（分页，不过滤；语言默认 zh-cn）。
     */
    public DevelopersEncyclopediaShips encyclopediaShips(String nation, String type, Integer pageNo, Integer limit) throws BasicException {
        return encyclopediaShips(nation, type, pageNo, limit, DEFAULT_LANGUAGE);
    }

    /**
     * 百科舰船字典（1 请求；nation/type/pageNo/limit 为 null 表示不过滤/不指定）。
     */
    public DevelopersEncyclopediaShips encyclopediaShips(String nation, String type, Integer pageNo, Integer limit,
                                                         String language) throws BasicException {
        return pipeline.execute(DevelopersEndpoints.ENCYCLOPEDIA_SHIPS, EndpointRequest.of(params(
                "nation", nation, "type", type, "pageNo", number(pageNo), "limit", number(limit),
                "language", language)));
    }

    /**
     * 百科单舰船；语言默认 zh-cn。
     */
    public DevelopersEncyclopediaShip encyclopediaShip(long shipId) throws BasicException {
        return encyclopediaShip(shipId, DEFAULT_LANGUAGE);
    }

    /**
     * 百科单舰船（1 请求；未找到抛业务异常）。
     */
    public DevelopersEncyclopediaShip encyclopediaShip(long shipId, String language) throws BasicException {
        DevelopersEncyclopediaShips ships = pipeline.execute(DevelopersEndpoints.ENCYCLOPEDIA_SHIPS,
                EndpointRequest.of(params("shipId", String.valueOf(shipId), "language", language)));
        DevelopersEncyclopediaShip ship = ships.ships().get(shipId);
        if (ship == null) {
            throw new BasicException(HttpThrowableStatus.DATA_STATUS, "未找到舰船: " + shipId);
        }
        return ship;
    }

    /**
     * 舰船参数明细（默认配置）；语言默认 zh-cn。
     */
    public DevelopersShipProfile shipProfile(long shipId) throws BasicException {
        return shipProfile(shipId, DEFAULT_LANGUAGE);
    }

    /**
     * 舰船参数明细（1 请求；未找到舰船时服务器返回错误状态）。
     */
    public DevelopersShipProfile shipProfile(long shipId, String language) throws BasicException {
        return pipeline.execute(DevelopersEndpoints.ENCYCLOPEDIA_SHIP_PROFILE,
                EndpointRequest.of(params("shipId", String.valueOf(shipId), "language", language)));
    }

    // ------------------------------------------------------------------ 百科：模块与消耗品

    /**
     * 模块字典（分页，不过滤；语言默认 zh-cn）。
     */
    public DevelopersEncyclopediaModules encyclopediaModules(String type, Integer pageNo, Integer limit) throws BasicException {
        return encyclopediaModules(type, pageNo, limit, DEFAULT_LANGUAGE);
    }

    /**
     * 模块字典（1 请求；type/pageNo/limit 为 null 表示不过滤/不指定）。
     */
    public DevelopersEncyclopediaModules encyclopediaModules(String type, Integer pageNo, Integer limit,
                                                             String language) throws BasicException {
        return pipeline.execute(DevelopersEndpoints.ENCYCLOPEDIA_MODULES, EndpointRequest.of(params(
                "type", type, "pageNo", number(pageNo), "limit", number(limit), "language", language)));
    }

    /**
     * 单模块；语言默认 zh-cn。
     */
    public DevelopersEncyclopediaModule encyclopediaModule(long moduleId) throws BasicException {
        return encyclopediaModule(moduleId, DEFAULT_LANGUAGE);
    }

    /**
     * 单模块（1 请求；未找到抛业务异常）。
     */
    public DevelopersEncyclopediaModule encyclopediaModule(long moduleId, String language) throws BasicException {
        DevelopersEncyclopediaModules modules = pipeline.execute(DevelopersEndpoints.ENCYCLOPEDIA_MODULES,
                EndpointRequest.of(params("moduleId", String.valueOf(moduleId), "language", language)));
        DevelopersEncyclopediaModule module = modules.modules().get(moduleId);
        if (module == null) {
            throw new BasicException(HttpThrowableStatus.DATA_STATUS, "未找到模块: " + moduleId);
        }
        return module;
    }

    /**
     * 消耗品字典（分页，不过滤；语言默认 zh-cn）。
     */
    public DevelopersEncyclopediaConsumables encyclopediaConsumables(String type, Integer pageNo, Integer limit) throws BasicException {
        return encyclopediaConsumables(type, pageNo, limit, DEFAULT_LANGUAGE);
    }

    /**
     * 消耗品字典（1 请求；type/pageNo/limit 为 null 表示不过滤/不指定）。
     */
    public DevelopersEncyclopediaConsumables encyclopediaConsumables(String type, Integer pageNo, Integer limit,
                                                                     String language) throws BasicException {
        return pipeline.execute(DevelopersEndpoints.ENCYCLOPEDIA_CONSUMABLES, EndpointRequest.of(params(
                "type", type, "pageNo", number(pageNo), "limit", number(limit), "language", language)));
    }

    /**
     * 单消耗品；语言默认 zh-cn。
     */
    public DevelopersEncyclopediaConsumable encyclopediaConsumable(long consumableId) throws BasicException {
        return encyclopediaConsumable(consumableId, DEFAULT_LANGUAGE);
    }

    /**
     * 单消耗品（1 请求；未找到抛业务异常）。
     */
    public DevelopersEncyclopediaConsumable encyclopediaConsumable(long consumableId, String language) throws BasicException {
        DevelopersEncyclopediaConsumables consumables = pipeline.execute(DevelopersEndpoints.ENCYCLOPEDIA_CONSUMABLES,
                EndpointRequest.of(params("consumableId", String.valueOf(consumableId), "language", language)));
        DevelopersEncyclopediaConsumable consumable = consumables.consumables().get(consumableId);
        if (consumable == null) {
            throw new BasicException(HttpThrowableStatus.DATA_STATUS, "未找到消耗品: " + consumableId);
        }
        return consumable;
    }

    // ------------------------------------------------------------------ 百科：指挥官与词典

    /**
     * 指挥官字典；语言默认 zh-cn。
     */
    public Map<Long, DevelopersEncyclopediaCommander> encyclopediaCommanders() throws BasicException {
        return encyclopediaCommanders(DEFAULT_LANGUAGE);
    }

    /**
     * 指挥官字典（1 请求）。
     */
    public Map<Long, DevelopersEncyclopediaCommander> encyclopediaCommanders(String language) throws BasicException {
        return pipeline.execute(DevelopersEndpoints.ENCYCLOPEDIA_CREWS, languageRequest(language));
    }

    /**
     * 单指挥官；语言默认 zh-cn。
     */
    public DevelopersEncyclopediaCommander encyclopediaCommander(long commanderId) throws BasicException {
        return encyclopediaCommander(commanderId, DEFAULT_LANGUAGE);
    }

    /**
     * 单指挥官（1 请求；未找到抛业务异常）。
     */
    public DevelopersEncyclopediaCommander encyclopediaCommander(long commanderId, String language) throws BasicException {
        Map<Long, DevelopersEncyclopediaCommander> commanders = pipeline.execute(DevelopersEndpoints.ENCYCLOPEDIA_CREWS,
                EndpointRequest.of(params("commanderId", String.valueOf(commanderId), "language", language)));
        DevelopersEncyclopediaCommander commander = commanders.get(commanderId);
        if (commander == null) {
            throw new BasicException(HttpThrowableStatus.DATA_STATUS, "未找到指挥官: " + commanderId);
        }
        return commander;
    }

    /**
     * 指挥官技能字典；语言默认 zh-cn。
     */
    public Map<Long, DevelopersEncyclopediaSkill> encyclopediaCommanderSkills() throws BasicException {
        return encyclopediaCommanderSkills(DEFAULT_LANGUAGE);
    }

    /**
     * 指挥官技能字典（1 请求）。
     */
    public Map<Long, DevelopersEncyclopediaSkill> encyclopediaCommanderSkills(String language) throws BasicException {
        return pipeline.execute(DevelopersEndpoints.ENCYCLOPEDIA_CREW_SKILLS, languageRequest(language));
    }

    /**
     * 单指挥官技能；语言默认 zh-cn。
     */
    public DevelopersEncyclopediaSkill encyclopediaCommanderSkill(long skillId) throws BasicException {
        return encyclopediaCommanderSkill(skillId, DEFAULT_LANGUAGE);
    }

    /**
     * 单指挥官技能（1 请求；未找到抛业务异常）。
     */
    public DevelopersEncyclopediaSkill encyclopediaCommanderSkill(long skillId, String language) throws BasicException {
        Map<Long, DevelopersEncyclopediaSkill> skills = pipeline.execute(DevelopersEndpoints.ENCYCLOPEDIA_CREW_SKILLS,
                EndpointRequest.of(params("skillId", String.valueOf(skillId), "language", language)));
        DevelopersEncyclopediaSkill skill = skills.get(skillId);
        if (skill == null) {
            throw new BasicException(HttpThrowableStatus.DATA_STATUS, "未找到指挥官技能: " + skillId);
        }
        return skill;
    }

    /**
     * 指挥官军衔字典（nation 不过滤）；语言默认 zh-cn。
     */
    public Map<String, List<DevelopersCommanderRank>> encyclopediaCommanderRanks() throws BasicException {
        return encyclopediaCommanderRanks(null, DEFAULT_LANGUAGE);
    }

    /**
     * 指挥官军衔字典（1 请求；nation 为 null 表示不过滤）。
     */
    public Map<String, List<DevelopersCommanderRank>> encyclopediaCommanderRanks(String nation, String language) throws BasicException {
        return pipeline.execute(DevelopersEndpoints.ENCYCLOPEDIA_CREW_RANKS,
                EndpointRequest.of(params("nation", nation, "language", language)));
    }

    /**
     * 战斗类型字典；语言默认 zh-cn。
     */
    public Map<String, DevelopersBattleType> encyclopediaBattleTypes() throws BasicException {
        return encyclopediaBattleTypes(DEFAULT_LANGUAGE);
    }

    /**
     * 战斗类型字典（1 请求）。
     */
    public Map<String, DevelopersBattleType> encyclopediaBattleTypes(String language) throws BasicException {
        return pipeline.execute(DevelopersEndpoints.ENCYCLOPEDIA_BATTLE_TYPES, languageRequest(language));
    }

    /**
     * 服役记录等级字典（1 请求；无文本字段，不接受 language）。
     */
    public Map<Long, DevelopersAccountLevel> encyclopediaAccountLevels() throws BasicException {
        return pipeline.execute(DevelopersEndpoints.ENCYCLOPEDIA_ACCOUNT_LEVELS, EndpointRequest.of(Map.of()));
    }

    /**
     * 收藏字典；语言默认 zh-cn。
     */
    public Map<Long, DevelopersCollection> encyclopediaCollections() throws BasicException {
        return encyclopediaCollections(DEFAULT_LANGUAGE);
    }

    /**
     * 收藏字典（1 请求）。
     */
    public Map<Long, DevelopersCollection> encyclopediaCollections(String language) throws BasicException {
        return pipeline.execute(DevelopersEndpoints.ENCYCLOPEDIA_COLLECTIONS, languageRequest(language));
    }

    /**
     * 收藏卡牌字典；语言默认 zh-cn。
     */
    public Map<Long, DevelopersCollectionCard> encyclopediaCollectionCards() throws BasicException {
        return encyclopediaCollectionCards(DEFAULT_LANGUAGE);
    }

    /**
     * 收藏卡牌字典（1 请求）。
     */
    public Map<Long, DevelopersCollectionCard> encyclopediaCollectionCards(String language) throws BasicException {
        return pipeline.execute(DevelopersEndpoints.ENCYCLOPEDIA_COLLECTION_CARDS, languageRequest(language));
    }

    /**
     * 地图字典；语言默认 zh-cn。
     */
    public Map<Long, DevelopersBattleArena> encyclopediaBattleArenas() throws BasicException {
        return encyclopediaBattleArenas(DEFAULT_LANGUAGE);
    }

    /**
     * 地图字典（1 请求）。
     */
    public Map<Long, DevelopersBattleArena> encyclopediaBattleArenas(String language) throws BasicException {
        return pipeline.execute(DevelopersEndpoints.ENCYCLOPEDIA_BATTLE_ARENAS, languageRequest(language));
    }

    /**
     * 成就字典（battle / progress 两表）；语言默认 zh-cn。
     */
    public DevelopersEncyclopediaAchievements encyclopediaAchievements() throws BasicException {
        return encyclopediaAchievements(DEFAULT_LANGUAGE);
    }

    /**
     * 成就字典（1 请求）。
     */
    public DevelopersEncyclopediaAchievements encyclopediaAchievements(String language) throws BasicException {
        return pipeline.execute(DevelopersEndpoints.ENCYCLOPEDIA_ACHIEVEMENTS, languageRequest(language));
    }

    // ------------------------------------------------------------------ 排位赛季

    /**
     * 排位赛季列表（含联赛与段位）；语言默认 zh-cn。
     */
    public Map<Long, DevelopersRankedSeason> rankedSeasons() throws BasicException {
        return rankedSeasons(DEFAULT_LANGUAGE);
    }

    /**
     * 排位赛季列表（1 请求）。
     */
    public Map<Long, DevelopersRankedSeason> rankedSeasons(String language) throws BasicException {
        return pipeline.execute(DevelopersEndpoints.SEASONS_INFO, languageRequest(language));
    }

    /**
     * 排位单船战绩（该账号全部有数据的赛季；1 请求）。
     */
    public DevelopersRankedShipStats rankedShipStats(long accountId) throws BasicException {
        return pipeline.execute(DevelopersEndpoints.SEASONS_SHIP_STATS, EndpointRequest.account(accountId));
    }

    /**
     * 排位单船战绩（指定赛季；1 请求）。
     */
    public DevelopersRankedShipStats rankedShipStats(long accountId, long seasonId) throws BasicException {
        return pipeline.execute(DevelopersEndpoints.SEASONS_SHIP_STATS,
                EndpointRequest.account(accountId, Map.of("seasonId", String.valueOf(seasonId))));
    }

    /**
     * 排位玩家战绩（段位进度 + 全部赛季统计；1 请求）。
     */
    public DevelopersRankedAccount rankedAccountStats(long accountId) throws BasicException {
        return pipeline.execute(DevelopersEndpoints.SEASONS_ACCOUNT_INFO, EndpointRequest.account(accountId));
    }

    /**
     * 排位玩家战绩（指定赛季；1 请求）。
     */
    public DevelopersRankedAccount rankedAccountStats(long accountId, long seasonId) throws BasicException {
        return pipeline.execute(DevelopersEndpoints.SEASONS_ACCOUNT_INFO,
                EndpointRequest.account(accountId, Map.of("seasonId", String.valueOf(seasonId))));
    }

    // ------------------------------------------------------------------ 内部

    private EndpointRequest shipRequest(long accountId, AccessToken accessToken, String extra) {
        EndpointRequest request = EndpointRequest.account(accountId, Map.of("extra", extra));
        return accessToken == null ? request : request.credentials(accessToken);
    }

    /**
     * 仅 language 的请求（null = 不传，服务器用默认语言）。
     */
    private static EndpointRequest languageRequest(String language) {
        return EndpointRequest.of(params("language", language));
    }

    /**
     * 键值对转参数表（偶数下标为键；值为 null 的条目直接省略）。
     */
    private static Map<String, String> params(String... keyValues) {
        Map<String, String> map = new LinkedHashMap<>();
        for (int i = 0; i + 1 < keyValues.length; i += 2) {
            if (keyValues[i + 1] != null) {
                map.put(keyValues[i], keyValues[i + 1]);
            }
        }
        return map;
    }

    private static String number(Integer value) {
        return value == null ? null : String.valueOf(value);
    }
}
