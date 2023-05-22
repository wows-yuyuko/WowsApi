package com.shinoaki.wows.api.codec.http;

import com.shinoaki.wows.api.codec.HttpCodec;
import com.shinoaki.wows.api.codec.HttpSend;
import com.shinoaki.wows.api.data.account.DevelopersSearchUser;
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
public record WowsHttpUserTools(HttpClient httpClient, WowsServer server) {


    public List<VortexSearchUser> searchUserVortexCn(String userName) throws IOException, InterruptedException, HttpStatusException {
        try {
            String sent = HttpSend.sendGet(httpClient, uriVortex(userName));
            JsonUtils utils = new JsonUtils();
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
        JsonUtils utils = new JsonUtils();
        return VortexSearchUser.parse(utils, utils.parse(sent));
    }

    public List<DevelopersSearchUser> searchUserDevelopers(String token, String userName) throws IOException, HttpStatusException, InterruptedException,
            StatusException {
        String sent = HttpSend.sendGet(httpClient, uriDeveloper(token, userName));
        JsonUtils utils = new JsonUtils();
        return DevelopersSearchUser.parse(utils, utils.parse(sent));
    }

    private URI uriVortex(String userName) {
        return URI.create(server.vortex() + String.format("/api/accounts/search/autocomplete/%s/", HttpCodec.encodeURIComponent(userName)));
    }

    private URI uriDeveloper(String token, String userName) {
        return URI.create(server.api() + String.format("/wows/account/list/?application_id=%s&search=%s", token, HttpCodec.encodeURIComponent(userName)));
    }
}
