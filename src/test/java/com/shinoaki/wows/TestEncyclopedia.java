package com.shinoaki.wows;

import com.shinoaki.wows.api.codec.http.WowsEncyclopediaTools;
import com.shinoaki.wows.api.codec.http.WowsHttpClanTools;
import com.shinoaki.wows.api.type.WowsServer;
import org.junit.Test;

import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.http.HttpClient;
import java.util.concurrent.ExecutionException;

/**
 * @author Xun
 */
public class TestEncyclopedia {
    public static String token = "907d9c6bfc0d896a2c156e57194a97cf";
    HttpClient client = HttpClient.newBuilder().build();
    WowsServer WS = WowsServer.ASIA;
    WowsEncyclopediaTools TOOLS = new WowsEncyclopediaTools(client, WS);

    @Test
    public void glossary() throws ExecutionException, InterruptedException {
        var r = TOOLS.developers(token);
        var data = r.glossaryAsync();
        System.out.println(data.get());
    }
}
