package com.shinoaki.wows;

import com.shinoaki.wows.api.error.BasicException;
import com.shinoaki.wows.api.type.WowsServer;
import com.shinoaki.wows.api.vortex.VortexResourcesLanguage;
import com.shinoaki.wows.api.vortex.resources.RequestVortexResourcesInfo;

import java.io.IOException;

public class RequestVortexResourcesInfoTest {
    public static void main(String[] args) throws BasicException, IOException, InterruptedException {
        var data = RequestVortexResourcesInfo.request(WowsServer.CN, VortexResourcesLanguage.CN);
        System.out.println(data);
    }
}
