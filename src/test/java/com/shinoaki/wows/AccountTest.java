package com.shinoaki.wows;

import com.shinoaki.wows.api.codec.http.WowsHttpUserTools;
import com.shinoaki.wows.api.type.WowsServer;
import com.shinoaki.wows.api.vortex.account.VortexSearchUser;
import org.junit.Test;

import java.net.http.HttpClient;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * @author Xun
 * @date 2023/5/22 22:48 星期一
 */
public class AccountTest {

    @Test
    public void searchUserAsia() throws InterruptedException, ExecutionException {
        String u1 = "JustOn";
        HttpClient client = HttpClient.newBuilder().build();
        WowsHttpUserTools asia = new WowsHttpUserTools(client, WowsServer.CN);
        System.out.println(asia.searchUserVortexAsync(u1).get().data());
        System.out.println("=================");
        System.out.println(asia.searchUserDevelopersAsync(DevelopersTest.token, u1));
        System.out.println(asia.userInfoDevelopersAsync(DevelopersTest.token, 2022515210));
        System.out.println(asia.userInfoDevelopersAsync(DevelopersTest.token, 2022515211));
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
}
