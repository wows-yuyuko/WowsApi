package com.shinoaki.wows;

import com.shinoaki.wows.api.error.HttpStatusException;
import com.shinoaki.wows.api.developers.DevelopersUserShip;
import com.shinoaki.wows.api.type.WowsHttpUrl;
import com.shinoaki.wows.api.type.WowsServer;
import com.shinoaki.wows.api.error.StatusException;
import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.http.HttpClient;

/**
 * @author Xun
 * @date 2023/4/24 21:13 星期一
 */
public class DevelopersTest {
    private static final long id = 2022515210;

    @Test
    public void shipTest() throws IOException, InterruptedException, StatusException, HttpStatusException {
        HttpClient.Builder builder = HttpClient.newBuilder();
        //代理
        builder.proxy(ProxySelector.of(new InetSocketAddress("127.0.0.1", 7890)));
        HttpClient client = builder.build();
        DevelopersUserShip developers = WowsHttpUrl.sendRequestShipListDevelopers(client, WowsServer.ASIA, "907d9c6bfc0d896a2c156e57194a97cf", 2022515210);
        System.out.println(developers);
    }
}
