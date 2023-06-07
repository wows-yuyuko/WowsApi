package com.shinoaki.wows.api.codec.http;

import com.shinoaki.wows.api.codec.HttpCodec;
import com.shinoaki.wows.api.codec.HttpSend;
import com.shinoaki.wows.api.developers.account.DevelopersSearchUser;
import com.shinoaki.wows.api.developers.account.DevelopersUserInfo;
import com.shinoaki.wows.api.error.HttpStatusException;
import com.shinoaki.wows.api.error.StatusException;
import com.shinoaki.wows.api.type.WowsServer;
import com.shinoaki.wows.api.utils.JsonUtils;
import com.shinoaki.wows.api.vortex.account.VortexSearchUser;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.util.List;

/**
 * 用户信息查询
 *
 * @author Xun
 * @date 2023/5/22 21:42 星期一
 */
public record WowsHttpUserTools(JsonUtils utils, HttpClient httpClient, WowsServer server) {


    public List<VortexSearchUser> searchUserVortexCn(String userName) throws IOException, InterruptedException, HttpStatusException {
        try {
            String sent = HttpSend.sendGet(httpClient, uriVortex(userName));
            return VortexSearchUser.parse(utils, utils.parse(sent));
        } catch (HttpStatusException e) {
            if (e.getCode() == 503) {
                return List.of();
            }
            throw e;
        }
    }

    public List<VortexSearchUser> searchUserVortex(String userName) throws HttpStatusException, IOException, InterruptedException {
        String sent = HttpSend.sendGet(httpClient, uriVortex(userName));
        return VortexSearchUser.parse(utils, utils.parse(sent));
    }

    public List<DevelopersSearchUser> searchUserDevelopers(String token, String userName) throws IOException, HttpStatusException, InterruptedException,
            StatusException {
        String sent = HttpSend.sendGet(httpClient, uriDeveloper(token, userName));
        return DevelopersSearchUser.parse(utils, utils.parse(sent));
    }

    public DevelopersUserInfo userInfoDevelopers(String token, long accountId) throws IOException, HttpStatusException, InterruptedException,
            StatusException {
        String sent = HttpSend.sendGet(httpClient, uriDeveloperUserInfo(token, accountId));
        return DevelopersUserInfo.parse(utils, accountId, utils.parse(sent));
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
