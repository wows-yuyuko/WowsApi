package com.shinoaki.wows;

import com.shinoaki.wows.api.codec.ClanRankUtils;
import com.shinoaki.wows.api.codec.http.WowsHttpClanTools;
import com.shinoaki.wows.api.type.WowsServer;
import com.shinoaki.wows.api.utils.JsonUtils;
import com.shinoaki.wows.api.vortex.clan.rank.ClanRankInfo;
import org.junit.Test;

import java.net.http.HttpClient;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * @author Xun
 * @date 2023/5/27 1:19 星期六
 */
public class ClanTest {
    public static String token = "907d9c6bfc0d896a2c156e57194a97cf";
    HttpClient client = HttpClient.newBuilder().build();
    WowsServer WS = WowsServer.ASIA;
    WowsHttpClanTools WT = new WowsHttpClanTools(client, WS);

    @Test
    public void season() throws ExecutionException, InterruptedException {
        var data = WT.developers(token).seasonAsync();
        System.out.println(data.get());
    }

    @Test
    public void searchDev() throws InterruptedException, ExecutionException {
        WowsServer server = WowsServer.ASIA;
        var tools = new WowsHttpClanTools(client, server);
        System.out.println(tools.developers(token).searchClanDevelopersAsync("YU_RI").get().data());
        System.out.println(tools.developers(token).searchClanDevelopersAsync("YU_RI2").get().data());
    }

    @Test
    public void clanInfoDev() throws InterruptedException, ExecutionException {
        WowsServer server = WowsServer.ASIA;
        var tools = new WowsHttpClanTools(client, server);
        long id = 2000022706L;
        System.out.println(tools.developers(token).clanInfoDevelopersAsync(id).get().data());
    }

    @Test
    public void accountSearchClanDev() throws InterruptedException, ExecutionException {
        WowsServer server = WowsServer.ASIA;
        var tools = new WowsHttpClanTools(client, server);
        long id = 2007474948;
        System.out.println(tools.developers(token).userSearchClanDevelopersAsync(id).get().data());
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

    @Test
    public void clanRankTest(){
        ClanRankUtils utils = new ClanRankUtils(new JsonUtils());
        List<ClanRankInfo> ranks = utils.getRanks(WowsServer.CN, 0);
        System.out.println();
    }

    public void clanInfoMembersVortex(WowsServer server, long id) throws InterruptedException, ExecutionException {
        var tools = new WowsHttpClanTools(client, server);
        System.out.println(tools.vortex().clanUserListInfoVortexAsync(id).get().data());
    }

    public void clanInfoVortex(WowsServer server, long id) throws InterruptedException, ExecutionException {
        var tools = new WowsHttpClanTools(client, server);
        System.out.println(tools.vortex().clanInfoVortexAsync(id).get().data());
    }

    public void searchUserVortex(WowsServer server, long id) throws InterruptedException, ExecutionException {
        var tools = new WowsHttpClanTools(client, server);
        System.out.println(tools.vortex().userSearchClanVortexAsync(id).get().data());
    }
}
