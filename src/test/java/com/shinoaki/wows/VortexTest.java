package com.shinoaki.wows;

import com.shinoaki.wows.api.error.HttpStatusException;
import com.shinoaki.wows.api.type.WowsBattlesType;
import com.shinoaki.wows.api.type.WowsHttpUrl;
import com.shinoaki.wows.api.type.WowsServer;
import com.shinoaki.wows.api.vortex.VortexUserShip;
import com.shinoaki.wows.api.error.StatusException;
import org.junit.Test;

import java.io.IOException;
import java.net.http.HttpClient;
import java.util.Map;

/**
 * @author Xun
 * @date 2023/4/9 18:41 星期日
 */
public class VortexTest {
    private static final long id = 2022515210;

    @Test
    public void shipTest() throws IOException, InterruptedException, StatusException, HttpStatusException {
        HttpClient.Builder builder = HttpClient.newBuilder();
        //代理
//        builder.proxy(ProxySelector.of(new InetSocketAddress("127.0.0.1", 7890)));
        HttpClient client = builder.build();
        Map<WowsBattlesType, VortexUserShip> sent = WowsHttpUrl.sendRequestShipListVortex(client, WowsServer.ASIA, 2022515210);
        System.out.println(sent);
    }
}
