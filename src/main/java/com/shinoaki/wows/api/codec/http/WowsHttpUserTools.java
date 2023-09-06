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

    public CompletableFuture<CompletableInfo<List<VortexSearchUser>>> searchUserVortexCn(String userName) {
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

    public CompletableFuture<CompletableInfo<List<VortexSearchUser>>> searchUserVortex(String userName) {
        final WowsJsonUtils json = new WowsJsonUtils();
        return HttpCodec.sendAsync(httpClient, HttpCodec.request(uriVortex(userName))).thenApplyAsync(data -> {
            try {
                return CompletableInfo.ok(VortexSearchUser.parse(json, HttpCodec.response(data)));
            } catch (BasicException e) {
                return CompletableInfo.error(e);
            }
        });
    }

    public CompletableFuture<CompletableInfo<List<DevelopersSearchUser>>> searchUserDevelopers(String token, String userName) {
        final WowsJsonUtils json = new WowsJsonUtils();
        return HttpCodec.sendAsync(httpClient, HttpCodec.request(uriDeveloper(token, userName))).thenApplyAsync(data -> {
            try {
                return CompletableInfo.ok(DevelopersSearchUser.parse(json, HttpCodec.response(data)));
            } catch (BasicException e) {
                return CompletableInfo.error(e);
            }
        });
    }

    public CompletableFuture<CompletableInfo<DevelopersUserInfo>> userInfoDevelopers(String token, long accountId) {
        final WowsJsonUtils json = new WowsJsonUtils();
        return HttpCodec.sendAsync(httpClient, HttpCodec.request(uriDeveloperUserInfo(token, accountId))).thenApplyAsync(data -> {
            try {
                return CompletableInfo.ok(DevelopersUserInfo.parse(json, accountId, HttpCodec.response(data)));
            } catch (BasicException e) {
                return CompletableInfo.error(e);
            }
        });
    }

    private URI uriVortex(String userName) {
        return URI.create(server.vortex() + String.format("/api/accounts/search/autocomplete/%s/", HttpCodec.encodeURIComponent(userName)));
    }

    private URI uriDeveloper(String token, String userName) {
        return URI.create(server.api() + String.format("/wows/account/list/?application_id=%s&search=%s", token, HttpCodec.encodeURIComponent(userName)));
    }

    private URI uriDeveloperUserInfo(String token, long accountId) {
        return URI.create(server.api() + String.format("/wows/account/info/?application_id=%s&account_id=%s", token, accountId));
    }
}
