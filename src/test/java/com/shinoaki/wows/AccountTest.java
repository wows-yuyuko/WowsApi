package com.shinoaki.wows;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.shinoaki.wows.api.codec.http.WowsHttpUserTools;
import com.shinoaki.wows.api.error.BasicException;
import com.shinoaki.wows.api.type.WowsServer;
import com.shinoaki.wows.api.utils.JsonUtils;
import com.shinoaki.wows.api.utils.WowsJsonUtils;
import com.shinoaki.wows.api.vortex.account.VortexSearchUser;
import org.junit.Test;

import java.io.IOException;
import java.net.http.HttpClient;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * @author Xun
 * @date 2023/5/22 22:48 星期一
 */
public class AccountTest {

    @Test
    public void searchUserAsia() throws InterruptedException, ExecutionException, IOException, BasicException {
        String u1 = "JustOn";
        HttpClient client = HttpClient.newBuilder().build();
        WowsHttpUserTools asia = new WowsHttpUserTools(client, WowsServer.ASIA);
        System.out.println(asia.searchUserVortexAsync(u1).get().data());
        System.out.println("=================");
        System.out.println(asia.searchUserDevelopersAsync(DevelopersTest.token, u1));
        System.out.println(asia.userInfoDevelopers(DevelopersTest.token, 2022515210));
        System.out.println(asia.userInfoDevelopers(DevelopersTest.token, 2022515211));
        System.out.println(new JsonUtils().toJson(asia.userInfoDevelopers(DevelopersTest.token, 2022515210, "e460fce93ab430baedf283e5f0a7d761c50b7a72")));
    }

    @Test
    public void searchUserRu() throws InterruptedException, ExecutionException, IOException, BasicException {

        HttpClient client = HttpClient.newBuilder().build();
        WowsHttpUserTools asia = new WowsHttpUserTools(client, WowsServer.RU);
        System.out.println(new JsonUtils().toJson(asia.userInfoDevelopers("c984faa7dc529f4cb0139505d5e8043c", 253171807, "b23c6ac59298c2dafdda2a405593f4f950eab316")));
    }

    @Test
    public void searchUserCn() throws InterruptedException, ExecutionException {
        String u1 = "西行寺雨季";
        HttpClient client = HttpClient.newBuilder().build();
        WowsHttpUserTools asia = new WowsHttpUserTools(client, WowsServer.CN);
        List<VortexSearchUser> vortex = asia.searchUserVortexAsync(u1).get().data();
        List<VortexSearchUser> vortex2 = asia.searchUserVortexCnAsync("西行寺").get().data();
        System.out.println(vortex);
        System.out.println("=============================");
        System.out.println(vortex2);
    }

    @Test
    public void userInfo() throws InterruptedException, ExecutionException {
        HttpClient client = HttpClient.newBuilder().build();
        WowsHttpUserTools asia = new WowsHttpUserTools(client, WowsServer.CN);
        var data = asia.userVortexAsync(7048262455L).get().data();
        var data1 = asia.userVortexAsync(7050218428L).get().data();
        var data2 = new WowsHttpUserTools(client, WowsServer.ASIA).userInfoDevelopersAsync("907d9c6bfc0d896a2c156e57194a97cf", 2022515210L).get().data();
        System.out.println(data);
        System.out.println("===========");
        System.out.println(data1);
        System.out.println("===========");
        System.out.println(data2);
        System.out.println();
    }

    @Test
    public void accountInfo() throws InterruptedException, ExecutionException, BasicException, IOException {
        HttpClient client = HttpClient.newBuilder().build();
        WowsHttpUserTools asia = new WowsHttpUserTools(client, WowsServer.ASIA);
        var data = asia.accountInfoDevelopers("907d9c6bfc0d896a2c156e57194a97cf", 2022515210L);
        System.out.println(new JsonUtils().toJson(data));
        System.out.println("===========");
    }


    @Test
    public void userInfoVortex() throws InterruptedException, ExecutionException, BasicException, IOException {
        HttpClient client = HttpClient.newBuilder().build();
        WowsHttpUserTools asia = new WowsHttpUserTools(client, WowsServer.EU);
        var data = asia.userVortex(562058793L);
        System.out.println(new JsonUtils().toJson(data));
        System.out.println("===========");
    }
}
