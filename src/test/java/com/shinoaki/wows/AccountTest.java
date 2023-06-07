package com.shinoaki.wows;

import com.shinoaki.wows.api.codec.http.WowsHttpUserTools;
import com.shinoaki.wows.api.error.HttpStatusException;
import com.shinoaki.wows.api.error.StatusException;
import com.shinoaki.wows.api.type.WowsServer;
import com.shinoaki.wows.api.utils.JsonUtils;
import com.shinoaki.wows.api.vortex.account.VortexSearchUser;
import org.junit.Test;

import java.io.IOException;
import java.net.http.HttpClient;
import java.util.List;

/**
 * @author Xun
 * @date 2023/5/22 22:48 星期一
 */
public class AccountTest {

    @Test
    public void searchUserAsia() throws HttpStatusException, IOException, InterruptedException, StatusException {
        String u1 = "JustOn";
        HttpClient client = HttpClient.newBuilder().build();
        WowsHttpUserTools asia = new WowsHttpUserTools(new JsonUtils(), client, WowsServer.ASIA);
        System.out.println(asia.searchUserVortex(u1));
        System.out.println("=================");
        System.out.println(asia.searchUserDevelopers(DevelopersTest.token, u1));
        System.out.println(asia.userInfoDevelopers(DevelopersTest.token, 2022515210));
        System.out.println(asia.userInfoDevelopers(DevelopersTest.token, 2022515211));
    }

    @Test
    public void searchUserCn() throws HttpStatusException, IOException, InterruptedException, StatusException {
        String u1 = "西行寺雨季";
        HttpClient client = HttpClient.newBuilder().build();
        WowsHttpUserTools asia = new WowsHttpUserTools(new JsonUtils(), client, WowsServer.CN);
        List<VortexSearchUser> vortex = asia.searchUserVortex(u1);
        List<VortexSearchUser> vortex2 = asia.searchUserVortexCn("西行寺");
        System.out.println(vortex);
        System.out.println("=============================");
        System.out.println(vortex2);
    }
}
