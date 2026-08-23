package com.shinoaki.wows.api.codec.http;

import com.shinoaki.wows.api.codec.HttpCodec;
import com.shinoaki.wows.api.data.AccountClanInfo;
import com.shinoaki.wows.api.data.AccountInfo;
import com.shinoaki.wows.api.developers.account.DevelopersSearchUser;
import com.shinoaki.wows.api.developers.account.DevelopersUserInfo;
import com.shinoaki.wows.api.developers.clan.DevelopersClanInfo;
import com.shinoaki.wows.api.error.BasicException;
import com.shinoaki.wows.api.error.CompletableInfo;
import com.shinoaki.wows.api.error.HttpThrowableStatus;
import com.shinoaki.wows.api.type.WowsServer;
import com.shinoaki.wows.api.utils.JsonUtils;
import com.shinoaki.wows.api.vortex.account.VortexSearchUser;
import com.shinoaki.wows.api.vortex.account.VortexUserInfo;
import tools.jackson.core.JacksonException;

import java.net.URI;
import java.net.http.HttpClient;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 用户信息查询
 *
 * @author Xun
 * @date 2023/5/22 21:42 星期一
 */
public record WowsHttpUserTools(HttpClient httpClient, WowsServer server) {

    public CompletableFuture<CompletableInfo<List<VortexSearchUser>>> searchUserVortexCnAsync(String userName) {

        return HttpCodec.sendAsync(httpClient, HttpCodec.request(uriVortex(userName))).thenApplyAsync(data -> {
            try {
                return CompletableInfo.ok(VortexSearchUser.parse( HttpCodec.response(data)));
            } catch (BasicException e) {
                if (e.getCode() == HttpThrowableStatus.HTTP_STATUS && (e.getMessage().contains("503"))) {
                    return CompletableInfo.ok(List.of());
                }
                return CompletableInfo.error(e);
            } catch (JacksonException e) {
                return CompletableInfo.error(new BasicException(HttpThrowableStatus.DATA_PARSE, e));
            }
        });
    }

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

    public CompletableFuture<CompletableInfo<List<VortexSearchUser>>> searchUserVortexAsync(String userName) {

        return HttpCodec.sendAsync(httpClient, HttpCodec.request(uriVortex(userName))).thenApplyAsync(data -> {
            try {
                return CompletableInfo.ok(VortexSearchUser.parse( HttpCodec.response(data)));
            } catch (BasicException e) {
                return CompletableInfo.error(e);
            } catch (JacksonException e) {
                return CompletableInfo.error(new BasicException(HttpThrowableStatus.DATA_PARSE, e));
            }
        });
    }

    public List<VortexSearchUser> searchUserVortex(String userName) throws BasicException {

        return VortexSearchUser.parse( HttpCodec.response(HttpCodec.send(httpClient, HttpCodec.request(uriVortex(userName)))));
    }

    public CompletableFuture<CompletableInfo<VortexUserInfo>> userVortexAsync(long accountId) {

        return HttpCodec.sendAsync(httpClient, HttpCodec.request(uriVortex(accountId))).thenApplyAsync(data -> {
            try {
                return CompletableInfo.ok(VortexUserInfo.parse(JsonUtils.json().parse(HttpCodec.response(data)), accountId));
            } catch (BasicException e) {
                return CompletableInfo.error(e);
            } catch (JacksonException e) {
                return CompletableInfo.error(new BasicException(HttpThrowableStatus.DATA_PARSE, e));
            }
        });
    }

    public VortexUserInfo userVortex(long accountId) throws BasicException {

        return VortexUserInfo.parse(JsonUtils.json().parse(HttpCodec.response(HttpCodec.send(httpClient, HttpCodec.request(uriVortex(accountId))))), accountId);
    }

    public CompletableFuture<CompletableInfo<List<DevelopersSearchUser>>> searchUserDevelopersAsync(String token, String userName) {

        return HttpCodec.sendAsync(httpClient, HttpCodec.request(uriDeveloper(token, userName))).thenApplyAsync(data -> {
            try {
                return CompletableInfo.ok(DevelopersSearchUser.parse( HttpCodec.response(data)));
            } catch (BasicException e) {
                return CompletableInfo.error(e);
            } catch (JacksonException e) {
                return CompletableInfo.error(new BasicException(HttpThrowableStatus.DATA_PARSE, e));
            }
        });
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

    public CompletableFuture<CompletableInfo<DevelopersUserInfo>> userInfoDevelopersAsync(String token, long accountId) {
        return userInfoDevelopersAsync(token, accountId, "");
    }

    public CompletableFuture<CompletableInfo<DevelopersUserInfo>> userInfoDevelopersAsync(String token, long accountId, String accessToken) {

        return HttpCodec.sendAsync(httpClient, HttpCodec.request(uriDeveloperUserInfo(token, accountId, accessToken))).thenApplyAsync(data -> {
            try {
                return CompletableInfo.ok(DevelopersUserInfo.parse( accountId, HttpCodec.response(data)));
            } catch (BasicException e) {
                return CompletableInfo.error(e);
            } catch (JacksonException e) {
                return CompletableInfo.error(new BasicException(HttpThrowableStatus.DATA_PARSE, e));
            }
        });
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
