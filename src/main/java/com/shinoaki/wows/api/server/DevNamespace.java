package com.shinoaki.wows.api.server;

/**
 * 官方开发者API 的路径命名空间（3.0 重写：服务器差异数据化）。
 * <p>
 * 端点定义只写"命名空间内路径"（如 {@code /account/info/}），由 {@link ServerProfile}
 * 按服务器补前缀。之所以按<b>域</b>而不是按服务器给一个统一前缀：
 * 莱服（korabli.su）的 {@code /mk/} 只覆盖部分接口——account / ships / glossary 走 {@code /mk/}，
 * 而 clans 系列（accountinfo / info / list / season）仍在 {@code /wows/} 下（已实测确认）。
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
    GLOSSARY
}
