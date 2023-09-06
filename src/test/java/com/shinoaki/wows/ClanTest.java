package com.shinoaki.wows;

import com.shinoaki.wows.api.codec.http.WowsHttpClanTools;
import com.shinoaki.wows.api.type.WowsServer;
import org.junit.Test;

import java.net.http.HttpClient;
import java.util.concurrent.ExecutionException;

/**
 * @author Xun
 * @date 2023/5/27 1:19 星期六
 */
public class ClanTest {
    public static String token = "907d9c6bfc0d896a2c156e57194a97cf";
    HttpClient client = HttpClient.newBuilder().build();

    @Test
    public void searchDev() throws InterruptedException, ExecutionException {
        WowsServer server = WowsServer.ASIA;
        var tools = new WowsHttpClanTools(client, server);
        System.out.println(tools.developers(token).searchClanDevelopers("YU_RI").get().data());
        System.out.println(tools.developers(token).searchClanDevelopers("YU_RI2").get().data());
    }

    @Test
    public void clanInfoDev() throws InterruptedException, ExecutionException {
        WowsServer server = WowsServer.ASIA;
        var tools = new WowsHttpClanTools(client, server);
        long id = 2000022706L;
        System.out.println(tools.developers(token).clanInfoDevelopers(id).get().data());
    }

    @Test
    public void accountSearchClanDev() throws InterruptedException, ExecutionException {
        WowsServer server = WowsServer.ASIA;
        var tools = new WowsHttpClanTools(client, server);
        long id = 2007474948;
        System.out.println(tools.developers(token).userSearchClanDevelopers(id).get().data());
    }

    @Test
    public void searchTestVortex() throws InterruptedException, ExecutionException {
        searchUserVortex(WowsServer.CN, 7047921442L);
        System.out.println("===============================");
        searchUserVortex(WowsServer.ASIA, 2022515210L);
    }

    @Test
    public void clanInfoVortex() throws InterruptedException, ExecutionException {
        clanInfoVortex(WowsServer.ASIA, 2000022706L);
    }

    @Test
    public void clanMembersVortex() throws InterruptedException, ExecutionException {
        clanInfoMembersVortex(WowsServer.ASIA, 2000025691L);
    }

    public void clanInfoMembersVortex(WowsServer server, long id) throws InterruptedException, ExecutionException {
        var tools = new WowsHttpClanTools(client, server);
        System.out.println(tools.vortex().clanUserListInfoVortex(id).get().data());
    }

    public void clanInfoVortex(WowsServer server, long id) throws InterruptedException, ExecutionException {
        var tools = new WowsHttpClanTools(client, server);
        System.out.println(tools.vortex().clanInfoVortex(id).get().data());
    }

    public void searchUserVortex(WowsServer server, long id) throws InterruptedException, ExecutionException {
        var tools = new WowsHttpClanTools(client, server);
        System.out.println(tools.vortex().userSearchClanVortex(id).get().data());
    }
}
