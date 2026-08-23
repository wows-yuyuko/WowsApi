package com.shinoaki.wows;


import com.shinoaki.wows.api.codec.http.WowsHttpUserTools;
import com.shinoaki.wows.api.data.AccountInfo;
import com.shinoaki.wows.api.error.BasicException;
import com.shinoaki.wows.api.type.WowsServer;
import com.shinoaki.wows.api.utils.JsonUtils;
import com.shinoaki.wows.api.vortex.account.VortexSearchUser;
import com.shinoaki.wows.api.vortex.account.VortexUserInfo;
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
    public void searchUserAsia() {
        try {
            String u1 = "JustOn";
            HttpClient client = HttpClient.newBuilder().build();
            WowsHttpUserTools asia = new WowsHttpUserTools(client, WowsServer.ASIA);
            System.out.println(asia.searchUserVortex(u1));
            System.out.println("=================");
            System.out.println(asia.searchUserDevelopers(DevelopersTest.token, u1));
            System.out.println(asia.userInfoDevelopers(DevelopersTest.token, 2022515210));
            System.out.println(asia.userInfoDevelopers(DevelopersTest.token, 2022515211));
            System.out.println(JsonUtils.json().toJson(asia.userInfoDevelopers(DevelopersTest.token, 2022515210, "e460fce93ab430baedf283e5f0a7d761c50b7a72")));
        } catch (BasicException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void searchUserRu()  {

        HttpClient client = HttpClient.newBuilder().build();
        WowsHttpUserTools asia = new WowsHttpUserTools(client, WowsServer.RU);
        try {
            System.out.println(JsonUtils.json().toJson(asia.userInfoDevelopers("c984faa7dc529f4cb0139505d5e8043c", 253171807, "b23c6ac59298c2dafdda2a405593f4f950eab316")));
        } catch (BasicException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void searchUserCn() throws BasicException {
        String u1 = "西行寺雨季";
        HttpClient client = HttpClient.newBuilder().build();
        WowsHttpUserTools asia = new WowsHttpUserTools(client, WowsServer.CN);
        List<VortexSearchUser> vortex = asia.searchUserVortex(u1);
        List<VortexSearchUser> vortex2 = asia.searchUserVortexCn("西行寺");
        System.out.println(vortex);
        System.out.println("=============================");
        System.out.println(vortex2);
    }

    @Test
    public void userInfo() throws BasicException {
        HttpClient client = HttpClient.newBuilder().build();
        WowsHttpUserTools asia = new WowsHttpUserTools(client, WowsServer.CN);
        var data = asia.userVortex(7048262455L);
        var data1 = asia.userVortex(7050218428L);
        var data2 = new WowsHttpUserTools(client, WowsServer.ASIA).userInfoDevelopers("907d9c6bfc0d896a2c156e57194a97cf", 2022515210L);
        System.out.println(data);
        System.out.println("===========");
        System.out.println(data1);
        System.out.println("===========");
        System.out.println(data2);
        System.out.println();
    }

    @Test
    public void accountInfo()  {
        HttpClient client = HttpClient.newBuilder().build();
        WowsHttpUserTools asia = new WowsHttpUserTools(client, WowsServer.ASIA);
        AccountInfo data = null;
        try {
            data = asia.accountInfoDevelopers("907d9c6bfc0d896a2c156e57194a97cf", 2022515210L);
        } catch (BasicException e) {
            throw new RuntimeException(e);
        }
        System.out.println(JsonUtils.json().toJson(data));
        System.out.println("===========");
    }


    @Test
    public void userInfoVortex()  {
        HttpClient client = HttpClient.newBuilder().build();
        WowsHttpUserTools asia = new WowsHttpUserTools(client, WowsServer.RU);
        VortexUserInfo data = null;
        try {
            data = asia.userVortex(267485138L);
        } catch (BasicException e) {
            throw new RuntimeException(e);
        }
        System.out.println(JsonUtils.json().toJson(data));
        System.out.println("===========");
    }
}
