package com.shinoaki.wows;

import com.shinoaki.wows.api.codec.ClanRankUtils;
import com.shinoaki.wows.api.codec.http.WowsHttpClanTools;
import com.shinoaki.wows.api.error.BasicException;
import com.shinoaki.wows.api.type.WowsServer;
import com.shinoaki.wows.api.vortex.clan.rank.ClanRankInfo;
import org.junit.Test;

import java.net.http.HttpClient;
import java.util.List;

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
    public void season() throws BasicException {
        var data = WT.developers(token).season();
        System.out.println(data);
    }

    @Test
    public void searchDev() throws BasicException {
        WowsServer server = WowsServer.ASIA;
        var tools = new WowsHttpClanTools(client, server);
        System.out.println(tools.developers(token).searchClanDevelopers("YU_RI"));
        System.out.println(tools.developers(token).searchClanDevelopers("YU_RI2"));
    }

    @Test
    public void clanInfoDev() throws BasicException {
        WowsServer server = WowsServer.ASIA;
        var tools = new WowsHttpClanTools(client, server);
        long id = 2000022706L;
        System.out.println(tools.developers(token).clanInfoDevelopers(id));
    }

    @Test
    public void accountSearchClanDev() throws BasicException {
        WowsServer server = WowsServer.ASIA;
        var tools = new WowsHttpClanTools(client, server);
        long id = 2007474948;
        System.out.println(tools.developers(token).userSearchClanDevelopers(id));
    }

    @Test
    public void searchTestVortex() throws BasicException {
        searchUserVortex(WowsServer.CN, 7047921442L);
        System.out.println("===============================");
        searchUserVortex(WowsServer.ASIA, 2022515210L);
    }

    @Test
    public void clanInfoVortex() throws BasicException {
        clanInfoVortex(WowsServer.ASIA, 2000016057L);
    }


    @Test
    public void clanInfoVortexRu() {
        try {
            var tools = new WowsHttpClanTools(client, WowsServer.RU).vortex();
            long clanId = 413663L;
            var clanFuture = tools.clanInfoVortex(clanId);
            var userFuture = tools.clanUserListInfoVortex(clanId);
            System.out.println();
        } catch (BasicException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void clanMembersVortex() throws BasicException {
        clanInfoMembersVortex(WowsServer.ASIA, 2000025691L);
    }

    @Test
    public void clanRankTest() {
        ClanRankUtils utils = new ClanRankUtils();
        List<ClanRankInfo> ranks = utils.getRanks(WowsServer.CN, 0);
        System.out.println();
    }

    public void clanInfoMembersVortex(WowsServer server, long id) throws BasicException {
        var tools = new WowsHttpClanTools(client, server);
        System.out.println(tools.vortex().clanUserListInfoVortex(id));
    }

    public void clanInfoVortex(WowsServer server, long id) throws BasicException {
        var tools = new WowsHttpClanTools(client, server);
        System.out.println(tools.vortex().clanInfoVortex(id));
    }

    public void searchUserVortex(WowsServer server, long id) throws BasicException {
        var tools = new WowsHttpClanTools(client, server);
        System.out.println(tools.vortex().userSearchClanVortex(id));
    }
}
