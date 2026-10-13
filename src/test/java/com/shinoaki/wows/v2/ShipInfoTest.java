package com.shinoaki.wows.v2;

import com.shinoaki.wows.api.codec.http.WowsHttpShipTools;
import com.shinoaki.wows.api.developers.DevelopersUserShip;
import com.shinoaki.wows.api.error.BasicException;
import com.shinoaki.wows.api.type.WowsServer;
import com.shinoaki.wows.testkit.TestConfig;
import org.junit.Test;

public class ShipInfoTest {

    @Test
    public void shipInfoDevTest() throws BasicException {
        WowsHttpShipTools tools = new WowsHttpShipTools(TestConfig.client(), WowsServer.ASIA, TestConfig.ACCOUNT_ASIA);
        DevelopersUserShip developers = tools.developers(TestConfig.appId(WowsServer.ASIA)).shipList();
    }
}
