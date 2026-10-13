package com.shinoaki.wows.v2;

import com.shinoaki.wows.api.codec.http.WowsEncyclopediaTools;
import com.shinoaki.wows.api.error.BasicException;
import com.shinoaki.wows.api.type.WowsServer;
import com.shinoaki.wows.testkit.TestConfig;
import org.junit.Test;

import java.net.http.HttpClient;

/**
 * @author Xun
 */
public class TestEncyclopedia {
    HttpClient client = TestConfig.client();
    WowsServer WS = WowsServer.ASIA;
    WowsEncyclopediaTools TOOLS = new WowsEncyclopediaTools(client, WS);

    @Test
    public void glossary() throws BasicException {
        var r = TOOLS.developers(TestConfig.appId(WowsServer.ASIA));
        var data = r.glossary();
        System.out.println(data);
    }
}
