package com.shinoaki.wows;

import com.shinoaki.wows.api.codec.http.WowsHttpClanTools;
import com.shinoaki.wows.api.codec.http.WowsHttpShipTools;
import com.shinoaki.wows.api.error.BasicException;
import com.shinoaki.wows.api.type.WowsServer;
import com.shinoaki.wows.api.utils.JsonUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.http.HttpClient;

public class ZstdWows {
    private static final String TOKEN = "907d9c6bfc0d896a2c156e57194a97cf";
    private static final HttpClient client = HttpClient.newBuilder()
            .proxy(ProxySelector.of(new InetSocketAddress("127.0.0.1", 7890)))
            .build();

    public static void main(String[] args) throws BasicException, IOException, InterruptedException {

        WowsHttpClanTools clanTools = new WowsHttpClanTools(client, WowsServer.ASIA);
        clanTools.developers(TOKEN).clanInfoDevelopers(2000022706L).members().forEach(x -> {
            w(x.account_id());
        });
        clanTools.developers(TOKEN).clanInfoDevelopers(2000015816L).members().forEach(x -> {
            w(x.account_id());
        });

    }

    private static void w(long accountId) {
        try {

            var tools = new WowsHttpShipTools(client, WowsServer.ASIA, accountId);
            var data = tools.developers(TOKEN).shipList().toShipInfoMap();
            File file = new File(System.getProperty("user.dir") + File.separator + "dict" + File.separator + accountId + "ship.json");
            try (FileOutputStream out = new FileOutputStream(file)) {
                out.write(new JsonUtils().toJson(data).getBytes());
                out.flush();
            }
        } catch (BasicException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
