package com.shinoaki.wows;

import com.shinoaki.wows.api.codec.http.WowsHttpShipTools;
import com.shinoaki.wows.api.data.ShipInfo;
import com.shinoaki.wows.api.developers.DevelopersUserShip;
import com.shinoaki.wows.api.type.WowsBattlesType;
import com.shinoaki.wows.api.type.WowsServer;
import org.junit.Test;

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
    public static String token = "907d9c6bfc0d896a2c156e57194a97cf";
    WowsServer server = WowsServer.ASIA;
    HttpClient client = HttpClient.newBuilder().proxy(ProxySelector.of(new InetSocketAddress("127.0.0.1", 7890))).build();

    @Test
    public void shipTest() throws InterruptedException, ExecutionException {
        WowsHttpShipTools tools = new WowsHttpShipTools(client, server, id);
        DevelopersUserShip developers = tools.developers(token).shipList().get().data();
        System.out.println(developers);
        Map<WowsBattlesType, List<ShipInfo>> shipInfoMap = developers.toShipInfoMap();
        var list = shipInfoMap.get(WowsBattlesType.PVP);
        var d1 = list.stream().filter(f->f.shipId()==4276041424L).findFirst().get();
        var a1 = d1.controlCapturedAndDroppedPoints().gameContributionToDefense();
        var a2 = d1.controlCapturedAndDroppedPoints().gameContributionToCapture();
        System.out.println(shipInfoMap);
    }

    @Test
    public void shipTest2() throws InterruptedException, ExecutionException {
        WowsHttpShipTools tools = new WowsHttpShipTools(client, WowsServer.CN, 7047921442L);
        var ship = tools.vortex().shipList(WowsBattlesType.PVP).get().data();
        var list = ship.toShipInfoList();
        var d1 = list.stream().filter(f->f.shipId()==4276041424L).findFirst().get();
        var a1 = d1.controlCapturedAndDroppedPoints().gameContributionToDefense();
        var a2 = d1.controlCapturedAndDroppedPoints().gameContributionToCapture();
        System.out.println(list);
    }

}
