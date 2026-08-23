package com.shinoaki.wows.api.codec.http;

import com.shinoaki.wows.api.codec.HttpCodec;
import com.shinoaki.wows.api.data.AccountClanInfo;
import com.shinoaki.wows.api.data.AccountInfo;
import com.shinoaki.wows.api.developers.account.DevelopersSearchUser;
import com.shinoaki.wows.api.developers.account.DevelopersUserInfo;
import com.shinoaki.wows.api.developers.clan.DevelopersClanInfo;
import com.shinoaki.wows.api.error.BasicException;
import com.shinoaki.wows.api.error.HttpThrowableStatus;
import com.shinoaki.wows.api.type.WowsServer;
import com.shinoaki.wows.api.utils.JsonUtils;
import com.shinoaki.wows.api.vortex.account.VortexSearchUser;
import com.shinoaki.wows.api.vortex.account.VortexUserInfo;

import java.net.URI;
import java.net.http.HttpClient;
import java.util.List;

/**
 * 用户信息查询
 * <p>
 * 说明：本类只提供同步方法；需要异步时建议使用线程池或JDK21虚拟线程自行包装（参考README）。
 *
 * @author Xun
 * @date 2023/5/22 21:42 星期一
 */
public record WowsHttpUserTools(HttpClient httpClient, WowsServer server) {

    public List<VortexSearchUser> searchUserVortexCn(String userName) throws BasicException {

        try {
            return VortexSearchUser.parse( HttpCodec.response(HttpCodec.send(httpClient, HttpCodec.request(uriVortex(userName)))));
        } catch (BasicException e) {
            if (e.getCode() == HttpThrowableStatus.HTTP_STATUS && (e.getMessage().contains("503"))) {
                return List.of();
            }
            throw e;
        }
    }

    public List<VortexSearchUser> searchUserVortex(String userName) throws BasicException {

        return VortexSearchUser.parse( HttpCodec.response(HttpCodec.send(httpClient, HttpCodec.request(uriVortex(userName)))));
    }

    public VortexUserInfo userVortex(long accountId) throws BasicException {

        return VortexUserInfo.parse(JsonUtils.json().parse(HttpCodec.response(HttpCodec.send(httpClient, HttpCodec.request(uriVortex(accountId))))), accountId);
    }

    public List<DevelopersSearchUser> searchUserDevelopers(String token, String userName) throws BasicException {

        return DevelopersSearchUser.parse( HttpCodec.response(HttpCodec.send(httpClient, HttpCodec.request(uriDeveloper(token, userName)))));
    }

    public AccountInfo accountInfoDevelopers(String token, long accountId) throws BasicException {

        var baseJson = HttpCodec.send(httpClient, HttpCodec.request(uriDeveloperUserInfo(token, accountId, "")));
        //检查公会是否存在
        var accountInfo = HttpCodec.send(httpClient, HttpCodec.request(WowsHttpClanTools.Developers.userSearchClanDevelopersUri(server, token, accountId)));
        var accountClan = AccountClanInfo.accountClan( accountId, HttpCodec.response(accountInfo));
        //检测是否有公会，有则继续执行
        if (accountClan.clanId() > 0) {
            var clanInfo = HttpCodec.send(httpClient, HttpCodec.request(WowsHttpClanTools.Developers.clanInfoDevelopersUri(server, token, accountClan.clanId())));
            var clan = DevelopersClanInfo.parse( accountClan.clanId(), HttpCodec.response(clanInfo));
            accountClan = AccountClanInfo.of(accountClan, clan);
        }
        return AccountInfo.parse( accountId, HttpCodec.response(baseJson), accountClan);
    }

    public DevelopersUserInfo userInfoDevelopers(String token, long accountId) throws BasicException {
        return userInfoDevelopers(token, accountId, "");
    }

    public DevelopersUserInfo userInfoDevelopers(String token, long accountId, String accessToken) throws BasicException {

        return DevelopersUserInfo.parse( accountId, HttpCodec.response(HttpCodec.send(httpClient, HttpCodec.request(uriDeveloperUserInfo(token,
                accountId, accessToken)))));
    }

    private URI uriVortex(String userName) {
        return URI.create(server.vortex() + String.format("/api/accounts/search/autocomplete/%s/", HttpCodec.encodeURIComponent(userName)));
    }

    private URI uriVortex(long accountId) {
        return URI.create(server.vortex() + String.format("/api/accounts/%s/", accountId));
    }

    private URI uriDeveloper(String token, String userName) {
        return URI.create(server.api() + String.format("/wows/account/list/?application_id=%s&search=%s", token, HttpCodec.encodeURIComponent(userName)));
    }

    private URI uriDeveloperUserInfo(String token, long accountId, String accessToken) {
        final String extra;
        if (server == WowsServer.RU) {
            extra = "private.grouped_contacts,private.port,statistics.club,statistics.oper_div,statistics.oper_div_hard,statistics" +
                    ".oper_solo,statistics.pve,statistics.pve_div2,statistics.pve_div3,statistics.pve_solo,statistics.pvp_div2,statistics.pvp_div3," +
                    "statistics.pvp_solo,statistics.rank_div2,statistics.rank_div3,statistics.rank_solo";
        } else {
            extra = "private.grouped_contacts,private.port,statistics.clan,statistics.club,statistics.oper_div,statistics.oper_div_hard,statistics" +
                    ".oper_solo,statistics.pve,statistics.pve_div2,statistics.pve_div3,statistics.pve_solo,statistics.pvp_div2,statistics.pvp_div3," +
                    "statistics.pvp_solo,statistics.rank_div2,statistics.rank_div3,statistics.rank_solo";
        }
        if (accessToken.isBlank()) {
            return URI.create(server.api() + String.format("/wows/account/info/?application_id=%s&account_id=%s&extra=%s", token, accountId, extra));
        }
        return URI.create(server.api() + String.format("/wows/account/info/?application_id=%s&access_token=%s&account_id=%s&extra=%s", token, accessToken, accountId, extra));
    }
}
