package com.shinoaki.wows.api.codec.http;

import com.shinoaki.wows.api.codec.HttpCodec;
import com.shinoaki.wows.api.developers.account.DevelopersSearchUser;
import com.shinoaki.wows.api.developers.account.DevelopersUserInfo;
import com.shinoaki.wows.api.error.BasicException;
import com.shinoaki.wows.api.error.CompletableInfo;
import com.shinoaki.wows.api.error.HttpThrowableStatus;
import com.shinoaki.wows.api.type.WowsServer;
import com.shinoaki.wows.api.utils.WowsJsonUtils;
import com.shinoaki.wows.api.vortex.account.VortexSearchUser;
import com.shinoaki.wows.api.vortex.account.VortexUserInfo;

import java.io.IOException;
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
        final WowsJsonUtils json = new WowsJsonUtils();
        return HttpCodec.sendAsync(httpClient, HttpCodec.request(uriVortex(userName))).thenApplyAsync(data -> {
            try {
                return CompletableInfo.ok(VortexSearchUser.parse(json, HttpCodec.response(data)));
            } catch (BasicException e) {
                if (e.getCode() == HttpThrowableStatus.HTTP_STATUS && (e.getMessage().contains("503"))) {
                    return CompletableInfo.ok(List.of());
                }
                return CompletableInfo.error(e);
            }
        });
    }

    public List<VortexSearchUser> searchUserVortexCn(String userName) throws IOException, BasicException {
        final WowsJsonUtils json = new WowsJsonUtils();
        try {
            return VortexSearchUser.parse(json, HttpCodec.response(HttpCodec.send(httpClient, HttpCodec.request(uriVortex(userName)))));
        } catch (BasicException e) {
            if (e.getCode() == HttpThrowableStatus.HTTP_STATUS && (e.getMessage().contains("503"))) {
                return List.of();
            }
            throw e;
        }
    }

    public CompletableFuture<CompletableInfo<List<VortexSearchUser>>> searchUserVortexAsync(String userName) {
        final WowsJsonUtils json = new WowsJsonUtils();
        return HttpCodec.sendAsync(httpClient, HttpCodec.request(uriVortex(userName))).thenApplyAsync(data -> {
            try {
                return CompletableInfo.ok(VortexSearchUser.parse(json, HttpCodec.response(data)));
            } catch (BasicException e) {
                return CompletableInfo.error(e);
            }
        });
    }

    public List<VortexSearchUser> searchUserVortex(String userName) throws IOException, BasicException {
        final WowsJsonUtils json = new WowsJsonUtils();
        return VortexSearchUser.parse(json, HttpCodec.response(HttpCodec.send(httpClient, HttpCodec.request(uriVortex(userName)))));
    }

    public CompletableFuture<CompletableInfo<VortexUserInfo>> userVortexAsync(long accountId) {
        final WowsJsonUtils json = new WowsJsonUtils();
        return HttpCodec.sendAsync(httpClient, HttpCodec.request(uriVortex(accountId))).thenApplyAsync(data -> {
            try {
                return CompletableInfo.ok(VortexUserInfo.parse(json.parse(HttpCodec.response(data)), accountId));
            } catch (BasicException e) {
                return CompletableInfo.error(e);
            }
        });
    }

    public VortexUserInfo userVortex(long accountId) throws IOException, BasicException {
        final WowsJsonUtils json = new WowsJsonUtils();
        return VortexUserInfo.parse(json.parse(HttpCodec.response(HttpCodec.send(httpClient, HttpCodec.request(uriVortex(accountId))))), accountId);
    }

    public CompletableFuture<CompletableInfo<List<DevelopersSearchUser>>> searchUserDevelopersAsync(String token, String userName) {
        final WowsJsonUtils json = new WowsJsonUtils();
        return HttpCodec.sendAsync(httpClient, HttpCodec.request(uriDeveloper(token, userName))).thenApplyAsync(data -> {
            try {
                return CompletableInfo.ok(DevelopersSearchUser.parse(json, HttpCodec.response(data)));
            } catch (BasicException e) {
                return CompletableInfo.error(e);
            }
        });
    }

    public List<DevelopersSearchUser> searchUserDevelopers(String token, String userName) throws IOException, BasicException {
        final WowsJsonUtils json = new WowsJsonUtils();
        return DevelopersSearchUser.parse(json, HttpCodec.response(HttpCodec.send(httpClient, HttpCodec.request(uriDeveloper(token, userName)))));
    }

    public CompletableFuture<CompletableInfo<DevelopersUserInfo>> userInfoDevelopersAsync(String token, long accountId) {
        final WowsJsonUtils json = new WowsJsonUtils();
        return HttpCodec.sendAsync(httpClient, HttpCodec.request(uriDeveloperUserInfo(token, accountId))).thenApplyAsync(data -> {
            try {
                return CompletableInfo.ok(DevelopersUserInfo.parse(json, accountId, HttpCodec.response(data)));
            } catch (BasicException e) {
                return CompletableInfo.error(e);
            }
        });
    }

    public DevelopersUserInfo userInfoDevelopers(String token, long accountId) throws IOException, BasicException {
        final WowsJsonUtils json = new WowsJsonUtils();
        return DevelopersUserInfo.parse(json, accountId, HttpCodec.response(HttpCodec.send(httpClient, HttpCodec.request(uriDeveloperUserInfo(token,
                accountId)))));
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

    private URI uriDeveloperUserInfo(String token, long accountId) {
        final String extra = "private.grouped_contacts,private.port,statistics.clan,statistics.club,statistics.oper_div,statistics.oper_div_hard,statistics" +
                             ".oper_solo,statistics.pve,statistics.pve_div2,statistics.pve_div3,statistics.pve_solo,statistics.pvp_div2,statistics.pvp_div3," +
                             "statistics.pvp_solo,statistics.rank_div2,statistics.rank_div3,statistics.rank_solo";
        return URI.create(server.api() + String.format("/wows/account/info/?application_id=%s&account_id=%s&extra=%s", token, accountId, extra));
    }
}
