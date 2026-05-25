package com.shinoaki.wows;

import com.shinoaki.wows.api.error.BasicException;
import com.shinoaki.wows.api.type.WowsServer;
import com.shinoaki.wows.api.vortex.VortexResourcesLanguage;
import com.shinoaki.wows.api.vortex.resources.RequestVortexResourcesInfo;
import com.shinoaki.wows.api.vortex.resources.WowsResources;
import com.shinoaki.wows.api.vortex.resources.box.WowsBox;

import java.io.IOException;
import java.util.List;

public class BoxTest {
    public static void main(String[] args) throws IOException, InterruptedException {
        try {
            var server = WowsServer.ASIA;
            List<WowsBox> boxes = WowsBox.request(true, server, VortexResourcesLanguage.SG);
            System.out.println(boxes);
            var wr = WowsResources.request(true, server, VortexResourcesLanguage.SG);
            System.out.println(wr);
            var rvr = RequestVortexResourcesInfo.request(true, server, VortexResourcesLanguage.SG);
            System.out.println(rvr);
        } catch (BasicException e) {
            throw new RuntimeException(e);
        }
    }
}
