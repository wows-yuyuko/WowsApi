package com.shinoaki.wows;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.shinoaki.wows.api.data.ShipInfo;
import com.shinoaki.wows.api.developers.DevelopersUserShip;
import com.shinoaki.wows.api.error.HttpStatusException;
import com.shinoaki.wows.api.error.StatusException;
import com.shinoaki.wows.api.pr.PrData;
import com.shinoaki.wows.api.pr.PrUtils;
import com.shinoaki.wows.api.type.WowsBattlesType;
import com.shinoaki.wows.api.type.WowsHttpUrl;
import com.shinoaki.wows.api.type.WowsServer;
import com.shinoaki.wows.api.utils.JsonUtils;
import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.http.HttpClient;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * @author Xun
 * @date 2023/4/24 21:13 星期一
 */
public class DevelopersTest {
    private static final long id = 2022515210;
    String token = "907d9c6bfc0d896a2c156e57194a97cf";
    WowsServer server = WowsServer.ASIA;
    HttpClient client = HttpClient.newBuilder().proxy(ProxySelector.of(new InetSocketAddress("127.0.0.1", 7890))).build();

    @Test
    public void shipTest() throws IOException, InterruptedException, StatusException, HttpStatusException {
        DevelopersUserShip developers = WowsHttpUrl.sendRequestShipListDevelopers(client, server, token, id);
        System.out.println(developers);
        Map<WowsBattlesType, List<ShipInfo>> shipInfoMap = developers.toShipInfoMap();
        System.out.println(shipInfoMap);
    }

    @Test
    public void shipTestPr() throws IOException, InterruptedException, StatusException, HttpStatusException {
        //检查山雾的  期望值:1631
        long s = 4178458320L;
        PrData serverPR = PrData.server(51931.05774853801, 0.7302046783625733, 48.481991959064615);
        DevelopersUserShip developers = WowsHttpUrl.sendRequestShipListDevelopers(client, server, token, id);
        Map<WowsBattlesType, List<ShipInfo>> shipInfoMap = developers.toShipInfoMap();
        List<ShipInfo> infoList = shipInfoMap.get(WowsBattlesType.PVP);
        ShipInfo shipInfo = infoList.stream().filter(x -> x.shipId() == s).findFirst().get();
        PrData user = PrData.user(shipInfo);
        System.out.println(PrUtils.pr(user, serverPR));
    }

    @Test
    public void shipTestAsync() throws ExecutionException, InterruptedException, JsonProcessingException, StatusException {
        WowsHttpUrl.SyncResult syncResult = WowsHttpUrl.sendRequestShipListDevelopersAsync(client, server, token, id);
        if (syncResult.isErr()) {
            System.out.println("请求异常！");
        } else {
            System.out.println(DevelopersUserShip.parse(new JsonUtils().parse(syncResult.data())).accountId());
        }
    }
}
