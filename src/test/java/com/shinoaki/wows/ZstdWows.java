package com.shinoaki.wows;

import com.shinoaki.wows.api.codec.http.WowsHttpClanTools;
import com.shinoaki.wows.api.codec.http.WowsHttpShipTools;
import com.shinoaki.wows.api.developers.clan.DevelopersClanInfo;
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

    public static void main(String[] args) throws IOException {

        try {
            WowsHttpClanTools clanTools = new WowsHttpClanTools(client, WowsServer.ASIA);
            for (DevelopersClanInfo.DevelopersClanUserInfo developersClanUserInfo : clanTools.developers(TOKEN).clanInfoDevelopers(2000022706L).members()) {
                w(developersClanUserInfo.account_id());
            }
            for (DevelopersClanInfo.DevelopersClanUserInfo x : clanTools.developers(TOKEN).clanInfoDevelopers(2000015816L).members()) {
                w(x.account_id());
            }
        } catch (BasicException e) {
            e.printStackTrace();
        }

    }

    private static void w(long accountId) throws IOException {
        try {
            var tools = new WowsHttpShipTools(client, WowsServer.ASIA, accountId);
            var data = tools.developers(TOKEN).shipList().toShipInfoMap();
            File file = new File(System.getProperty("user.dir") + File.separator + "dict" + File.separator + accountId + "ship.json");
            try (FileOutputStream out = new FileOutputStream(file)) {
                out.write(JsonUtils.json().toJson(data).getBytes());
                out.flush();
            }
        } catch (BasicException e) {
            e.printStackTrace();
        }
    }
}
