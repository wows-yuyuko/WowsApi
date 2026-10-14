package com.shinoaki.wows.api.server;

/**
 * 官方开发者API 的路径命名空间：端点只写"命名空间内路径"，由 {@link ServerProfile} 按服务器补前缀。
 * 按域区分的原因：莱服 {@code /mk/} 只覆盖 account / ships / glossary，clans 系列仍在 {@code /wows/} 下。
 *
 * @author Xun
 */
public enum DevNamespace {

    /**
     * 账号域：/mk/account/**（莱服）或 /wows/account/**（WG 服）。
     */
    ACCOUNT,

    /**
     * 战舰域：/mk/ships/**（莱服）或 /wows/ships/**（WG 服）。
     */
    SHIP,

    /**
     * 军团域：clans 系列接口在莱服仍走 /wows/clans/**（无 /mk/ 版本）。
     */
    CLAN,

    /**
     * 词典域：/mk/clans/glossary/**（莱服）或 /wows/clans/glossary/**（WG 服）。
     */
    GLOSSARY,

    /**
     * 百科域：encyclopedia 系列（舰船/成就/指挥官/模块/收藏/地图等）。
     */
    ENCYCLOPEDIA,

    /**
     * 赛季域：排位赛季系列（seasons/info、shipstats、accountinfo）。
     */
    SEASONS
}
