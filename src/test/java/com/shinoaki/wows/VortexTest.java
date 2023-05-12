package com.shinoaki.wows;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.shinoaki.wows.api.error.HttpStatusException;
import com.shinoaki.wows.api.error.StatusException;
import com.shinoaki.wows.api.type.WowsBattlesType;
import com.shinoaki.wows.api.type.WowsHttpUrl;
import com.shinoaki.wows.api.type.WowsServer;
import com.shinoaki.wows.api.utils.JsonUtils;
import com.shinoaki.wows.api.vortex.VortexUserShip;
import org.junit.Test;

import java.io.IOException;
import java.net.http.HttpClient;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * @author Xun
 * @date 2023/4/9 18:41 星期日
 */
public class VortexTest {
    private static final long id = 7047921442L;
    WowsServer server = WowsServer.CN;
    HttpClient client = HttpClient.newBuilder().build();

    @Test
    public void shipTest() throws IOException, InterruptedException, StatusException, HttpStatusException {
        //代理
//        builder.proxy(ProxySelector.of(new InetSocketAddress("127.0.0.1", 7890)));
        Map<WowsBattlesType, VortexUserShip> sent = WowsHttpUrl.sendRequestShipListVortex(client, server, id);
        System.out.println(sent);
    }

    @Test
    public void shipTestAsync() throws ExecutionException, InterruptedException, JsonProcessingException, StatusException {
        Map<WowsBattlesType, WowsHttpUrl.SyncResult> async = WowsHttpUrl.sendRequestShipListVortexAsync(client, server, id);
        JsonUtils utils = new JsonUtils();
        for (var data : async.entrySet()) {
            if (!data.getValue().isErr()) {
                System.out.println(VortexUserShip.parse(data.getKey(), utils.parse(data.getValue().data())).name());
            } else {
                System.out.println(data.getKey() + "请求失败");
            }
        }
    }
}
