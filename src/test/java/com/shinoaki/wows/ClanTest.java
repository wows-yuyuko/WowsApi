package com.shinoaki.wows;

import com.shinoaki.wows.api.codec.http.WowsHttpClanTools;
import com.shinoaki.wows.api.error.HttpStatusException;
import com.shinoaki.wows.api.error.StatusException;
import com.shinoaki.wows.api.type.WowsServer;
import com.shinoaki.wows.api.utils.JsonUtils;
import org.junit.Test;

import java.io.IOException;
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
        var tools = new WowsHttpClanTools(new JsonUtils(), client, server);
        System.out.println(tools.developers(token).searchClanDevelopers("YU_RI"));
        System.out.println(tools.developers(token).searchClanDevelopers("YU_RI2"));
    }

    @Test
    public void clanInfoDev() throws HttpStatusException, IOException, InterruptedException, StatusException {
        WowsServer server = WowsServer.ASIA;
        var tools = new WowsHttpClanTools(new JsonUtils(), client, server);
        long id = 2000022706L;
        System.out.println(tools.developers(token).clanInfoDevelopers(id));
    }

    @Test
    public void accountSearchClanDev() throws HttpStatusException, IOException, InterruptedException, StatusException {
        WowsServer server = WowsServer.ASIA;
        var tools = new WowsHttpClanTools(new JsonUtils(), client, server);
        long id = 2007474948;
        System.out.println(tools.developers(token).userSearchClanDevelopers(id));
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
        var tools = new WowsHttpClanTools(new JsonUtils(), client, server);
        System.out.println(tools.vortex().clanUserListInfoVortex(id));
    }

    public void clanInfoVortex(WowsServer server, long id) throws HttpStatusException, IOException, InterruptedException {
        var tools = new WowsHttpClanTools(new JsonUtils(), client, server);
        System.out.println(tools.vortex().clanInfoVortex(id));
    }

    public void searchUserVortex(WowsServer server, long id) throws HttpStatusException, IOException, InterruptedException, StatusException {
        var tools = new WowsHttpClanTools(new JsonUtils(), client, server);
        System.out.println(tools.vortex().userSearchClanVortex(id));
    }
}
