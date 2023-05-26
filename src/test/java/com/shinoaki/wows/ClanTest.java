package com.shinoaki.wows;

import com.shinoaki.wows.api.codec.HttpSend;
import com.shinoaki.wows.api.codec.http.WowsHttpClanTools;
import com.shinoaki.wows.api.developers.clan.DevelopersClanInfo;
import com.shinoaki.wows.api.developers.clan.DevelopersSearchClan;
import com.shinoaki.wows.api.developers.clan.DevelopersSearchUserClan;
import com.shinoaki.wows.api.error.HttpStatusException;
import com.shinoaki.wows.api.error.StatusException;
import com.shinoaki.wows.api.type.WowsServer;
import com.shinoaki.wows.api.utils.JsonUtils;
import com.shinoaki.wows.api.vortex.clan.account.VortexSearchClanUser;
import com.shinoaki.wows.api.vortex.clan.base.VortexClanInfo;
import com.shinoaki.wows.api.vortex.clan.members.VortexClanUserInfo;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;

/**
 * @author Xun
 * @date 2023/5/27 1:19 星期六
 */
public class ClanTest {
    public static String token = "907d9c6bfc0d896a2c156e57194a97cf";
    HttpClient client = HttpClient.newBuilder().build();
    JsonUtils utils = new JsonUtils();

    @Test
    public void searchDev() throws HttpStatusException, IOException, InterruptedException, StatusException {
        WowsServer server = WowsServer.ASIA;
        var tools = new WowsHttpClanTools(client, server);
        System.out.println(DevelopersSearchClan.parse(utils, HttpSend.sendGet(tools.httpClient(), tools.developoers().searchClanDevelopers(token, "YU_RI"))));
        System.out.println(DevelopersSearchClan.parse(utils, HttpSend.sendGet(tools.httpClient(), tools.developoers().searchClanDevelopers(token, "YU_RI2"))));
    }

    @Test
    public void clanInfoDev() throws HttpStatusException, IOException, InterruptedException, StatusException {
        WowsServer server = WowsServer.ASIA;
        var tools = new WowsHttpClanTools(client, server);
        long id = 2000022706L;
        System.out.println(DevelopersClanInfo.parse(utils, id, HttpSend.sendGet(tools.httpClient(), tools.developoers().clanInfoDevelopers(token, id))));
    }

    @Test
    public void accountSearchClanDev() throws HttpStatusException, IOException, InterruptedException, StatusException {
        WowsServer server = WowsServer.ASIA;
        var tools = new WowsHttpClanTools(client, server);
        long id = 2007474948;
        System.out.println(DevelopersSearchUserClan.parse(utils, id, HttpSend.sendGet(tools.httpClient(), tools.developoers().userSearchClanDevelopers(token,
                id))));
    }

    @Test
    public void searchTestVortex() throws HttpStatusException, IOException, InterruptedException, StatusException {
        searchUserVortex(WowsServer.CN, 7047921442L);
        System.out.println("===============================");
        searchUserVortex(WowsServer.ASIA, 2022515210L);
    }

    @Test
    public void clanInfoVortex() throws HttpStatusException, IOException, InterruptedException {
        clanInfoVortex(WowsServer.ASIA, 2000022706L);
    }

    @Test
    public void clanMembersVortex() throws HttpStatusException, IOException, InterruptedException {
        clanInfoMembersVortex(WowsServer.ASIA, 2000022706L);
    }

    public void clanInfoMembersVortex(WowsServer server, long id) throws HttpStatusException, IOException, InterruptedException {
        var tools = new WowsHttpClanTools(client, server);
        String sent = HttpSend.sendGet(tools.httpClient(), tools.vortex().clanUserListInfoVortex(id));
        System.out.println(VortexClanUserInfo.to(server, utils.parse(sent)));
    }

    public void clanInfoVortex(WowsServer server, long id) throws HttpStatusException, IOException, InterruptedException {
        var tools = new WowsHttpClanTools(client, server);
        String sent = HttpSend.sendGet(tools.httpClient(), tools.vortex().clanInfoVortex(id));
        System.out.println(VortexClanInfo.to(server, id, utils.parse(sent)));
    }

    public void searchUserVortex(WowsServer server, long id) throws HttpStatusException, IOException, InterruptedException, StatusException {
        var tools = new WowsHttpClanTools(client, server);
        URI uri = tools.vortex().userSearchClanVortex(id);
        String sent = HttpSend.sendGet(tools.httpClient(), uri);
        VortexSearchClanUser vortexSearchClanUser = VortexSearchClanUser.to(utils.parse(sent));
        System.out.println(vortexSearchClanUser);
    }
}
