package com.shinoaki.wows;

import com.shinoaki.wows.api.codec.http.WowsHttpShipTools;
import com.shinoaki.wows.api.data.ShipInfo;
import com.shinoaki.wows.api.developers.DevelopersUserShip;
import com.shinoaki.wows.api.developers.DevelopersUserShipOper;
import com.shinoaki.wows.api.error.BasicException;
import com.shinoaki.wows.api.type.WowsBattlesType;
import com.shinoaki.wows.api.type.WowsServer;
import com.shinoaki.wows.testkit.TestConfig;
import org.junit.Test;

import java.net.http.HttpClient;
import java.util.List;
import java.util.Map;

/**
 * @author Xun
 * @date 2023/4/24 21:13 星期一
 */
public class DevelopersTest {
    private static final long id = 701534238;
    WowsServer server = WowsServer.EU;
    HttpClient client = TestConfig.client();

    @Test
    public void shipTest() throws BasicException {
        WowsHttpShipTools tools = new WowsHttpShipTools(client, server, id);
        DevelopersUserShip developers = tools.developers(TestConfig.appId(server)).shipList();
        var developersUserShipOper = tools.developers(TestConfig.appId(server)).shipOperList();
        System.out.println(developers);


        Map<WowsBattlesType, List<ShipInfo>> shipInfoMap = developers.toShipInfoMap();
        var list = shipInfoMap.get(WowsBattlesType.PVP);
        var d1 = list.stream().filter(f -> f.shipId() == 4276041424L).findFirst().get();
        var a1 = d1.controlCapturedAndDroppedPoints().gameContributionToDefense();
        var a2 = d1.controlCapturedAndDroppedPoints().gameContributionToCapture();
//        System.out.println(shipInfoMap);
        var data= shipInfoMap.get(WowsBattlesType.PVP).stream().filter(f->f.shipId()==3539384016L).findFirst().get();
        System.out.println(data);
    }

    @Test
    public void shipTest2() throws BasicException {
        WowsHttpShipTools tools = new WowsHttpShipTools(client, WowsServer.CN, 7047921442L);
        var ship = tools.vortex().shipList(WowsBattlesType.PVP);
        var list = ship.toShipInfoList();
        var d1 = list.stream().filter(f -> f.shipId() == 4276041424L).findFirst().get();
        var a1 = d1.controlCapturedAndDroppedPoints().gameContributionToDefense();
        var a2 = d1.controlCapturedAndDroppedPoints().gameContributionToCapture();
        System.out.println(list);
    }

    @Test
    public void shipTest3() {
        try {
            WowsHttpShipTools tools = new WowsHttpShipTools(client, server, id);
            var developers = tools.developers(TestConfig.appId(server)).shipBadges();
            System.out.println(developers);
        } catch (BasicException e) {
            e.printStackTrace();
        }
    }

}
