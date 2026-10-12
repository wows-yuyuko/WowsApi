package com.shinoaki.wows.api.type;

import java.io.Serializable;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * 战斗类型——全量集合，每个值自带「通用 / 服务器专属」标识。
 * <p>
 * 2026-10 由旧的 WowsBattlesType（通用 5 值）与 WowsBattlesTypeRu（莱服扩展）合并而来。两家分家后
 * 莱服多出一批战斗类型（乱斗 {@code brawl}、军团战 {@code clan}、评级 {@code rating*}、行动 {@code oper_mm}、
 * 新排位 {@code rank} 等），合并后统一用 {@link #servers()} 等标记表达服务器归属，不再按服拆枚举。
 * 实测（2026-10）：分叉目前单向——只有莱服侧领先，直营/国服 statistics 块完全一致。
 * <p>
 * {@link #code()} 是 URL/JSON 里的段名（小写）。两个通道的块集合不同：
 * {@code pve_solo}/{@code pve_div2}/{@code pve_div3} 见官方开发者 API（vortex 账号汇总里没有对应块），
 * {@code oper_mm}/{@code brawl}/{@code clan}/{@code rating*} 见莱服 vortex 官网接口。
 * <p>
 * <b>调用方注意</b>：{@code values()} 现在是全量（22 个），业务遍历要选视图——
 * 官方开发者 API 的请求/解析用 {@link #DEVELOPERS_STATS_TYPES}（轻量集合）或 {@link #DEVELOPERS_ALL_STATS_EXTRA}（全量串），按服务器过滤用 {@link #supportedOn(WowsServer)}。
 * 存储侧：{@code EnumMap} 序列化「旧写新读」安全、「新写旧读」遇新常量会抛；
 * 给枚举加值时 {@code switch} 语句不报编译错、会静默走 default。
 *
 * @author Xun
 * @date 2023/3/18 11:26 星期六
 */
public enum WowsBattlesType implements Serializable {
    // ------------------------------------------------------------------ 通用（所有服务器）
    /**
     * PVP对局
     */
    PVP("pvp"),
    /**
     * PVP单人
     */
    PVP_SOLO("pvp_solo"),
    /**
     * PVP2人
     */
    PVP_DIV2("pvp_div2"),
    /**
     * PVP3人
     */
    PVP_DIV3("pvp_div3"),
    /**
     * 排位
     */
    RANK_SOLO("rank_solo"),
    /**
     * 剧情(合作)模式
     */
    PVE("pve"),
    /**
     * 剧情单人（官方开发者API 通道）
     */
    PVE_SOLO("pve_solo"),
    /**
     * 剧情2人（官方开发者API 通道）
     */
    PVE_DIV2("pve_div2"),
    /**
     * 剧情3人（官方开发者API 通道）
     */
    PVE_DIV3("pve_div3"),
    /**
     * 新排位2人
     */
    RANK_DIV2("rank_div2"),
    /**
     * 新排位3人
     */
    RANK_DIV3("rank_div3"),
    /**
     * 旧排位单人
     */
    RANK_OLD_SOLO("rank_old_solo"),
    /**
     * 旧排位2人
     */
    RANK_OLD_DIV2("rank_old_div2"),
    /**
     * 旧排位3人
     */
    RANK_OLD_DIV3("rank_old_div3"),
    // ------------------------------------------------------------------ 莱服(korabli.su)专属
    /**
     * 行动模式(匹配)
     */
    OPER_MM("oper_mm", WowsServer.RU),
    /**
     * 乱斗
     */
    BRAWL("brawl", WowsServer.RU),
    /**
     * 军团战(账号汇总里有该数据块，但没有对应的单船接口)
     */
    CLAN("clan", WowsServer.RU),
    /**
     * 新排位(莱服)
     */
    RANK("rank", WowsServer.RU),
    /**
     * 旧排位(莱服)
     */
    RANK_OLD("rank_old", WowsServer.RU),
    /**
     * 评级战(莱服)
     */
    RATING("rating", WowsServer.RU),
    /**
     * 评级战单人
     */
    RATING_SOLO("rating_solo", WowsServer.RU),
    /**
     * 评级战组队
     */
    RATING_DIV("rating_div", WowsServer.RU);

    /**
     * 官方开发者 API {@code /wows/ships/stats/} 默认请求的战斗类型集合。
     * <p>
     * 请求端（{@code extra} 参数，PVP 为顶层默认块、不进 extra）与解析端（{@code DevelopersShipBattleInfo}）共用。
     * 历史集合，改动前先看这两处调用方。
     */
    public static final List<WowsBattlesType> DEVELOPERS_STATS_TYPES = List.of(PVP, PVP_SOLO, PVP_DIV2, PVP_DIV3, RANK_SOLO);

    /**
     * 官方开发者 API {@code /wows/ships/stats/} 的<b>全量</b> extra 串（不含默认返回的 pvp 块）。
     * <p>
     * 组成：随机战 pvp_solo/div2/div3、联合作战 pve(_solo/_div2/_div3)、排位 rank_solo/div2/div3、
     * 行动模式 oper_solo/div/div_hard、军团战 clan。{@code club} 已被官方禁用（请求不报错但不返回块）、
     * {@code rank_old_*} 不在有效枚举内（INVALID_EXTRA），两者都不请求。
     * <p>
     * 2026-10 实测：亚服 / 欧服 / 莱服({@code /mk/}) 均接受该串，响应为「默认 pvp + 全部 14 个块」。
     */
    public static final String DEVELOPERS_ALL_STATS_EXTRA = "pvp_solo,pvp_div2,pvp_div3,pve,pve_solo,pve_div2,pve_div3,rank_solo,rank_div2,rank_div3,oper_solo,oper_div,oper_div_hard,clan";

    /**
     * 所有服务器——通用类型的即此集合。
     * <p>
     * 包一层嵌套类：枚举构造器先于其它静态字段执行，不能直接引用声明在枚举常量之后的静态字段。
     */
    private static final class AllServers {
        private static final Set<WowsServer> SET = Set.of(WowsServer.values());
    }

    /**
     * JSON/URL 里的段名（小写）
     */
    private final String code;
    /**
     * 该类型存在的服务器；构造时不传专属服务器即所有服务器通用
     */
    private final Set<WowsServer> servers;

    WowsBattlesType(String code, WowsServer... onlyOn) {
        this.code = code;
        this.servers = onlyOn.length == 0 ? AllServers.SET : Set.of(onlyOn);
    }

    /**
     * JSON/URL 里的段名（小写）。
     */
    public String code() {
        return code;
    }

    /**
     * 该战斗类型在哪些服务器存在（当前只有两种：全部 / 仅莱服）。
     */
    public Set<WowsServer> servers() {
        return servers;
    }

    /**
     * 是否所有服务器通用。
     */
    public boolean isCommon() {
        return servers.size() == AllServers.SET.size();
    }

    /**
     * 是否服务器专属（只在部分服务器存在）。
     */
    public boolean isExclusive() {
        return !isCommon();
    }

    /**
     * 该类型在指定服务器是否存在。
     */
    public boolean supportedOn(WowsServer server) {
        return servers.contains(server);
    }

    @Override
    public String toString() {
        return super.toString().toUpperCase(Locale.ROOT);
    }

    /**
     * 按 {@link #code()} 匹配（忽略大小写）。
     */
    public static WowsBattlesType findCode(String code) {
        for (var v : WowsBattlesType.values()) {
            if (v.code.equalsIgnoreCase(code)) {
                return v;
            }
        }
        throw new IllegalArgumentException(code + " 匹配不到对应战斗类型");
    }
}
