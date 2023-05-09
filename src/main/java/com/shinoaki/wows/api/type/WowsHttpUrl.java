package com.shinoaki.wows.api.type;

import com.shinoaki.wows.api.codec.HttpCodec;
import com.shinoaki.wows.api.developers.DevelopersUserShip;
import com.shinoaki.wows.api.error.HttpStatusException;
import com.shinoaki.wows.api.error.StatusException;
import com.shinoaki.wows.api.utils.JsonUtils;
import com.shinoaki.wows.api.vortex.VortexUserShip;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

/**
 * @author Xun
 * @date 2023/4/24 23:49 星期一
 */
public class WowsHttpUrl {
    private WowsHttpUrl() {

    }

    public static Map<WowsBattlesType, VortexUserShip> sendRequestShipListVortex(HttpClient httpClient, WowsServer server, long accountId) throws IOException,
            InterruptedException, StatusException,
            HttpStatusException {
        Map<WowsBattlesType, VortexUserShip> map = new EnumMap<>(WowsBattlesType.class);
        for (var bt : WowsBattlesType.values()) {
            byte[] response = HttpCodec.response(httpClient.send(HttpRequest.newBuilder().uri(WowsHttpUrl.vortexShipList(server,
                                    bt, accountId))
                            .setHeader("Accept-Encoding", "gzip, deflate, br")
                            .build(),
                    HttpResponse.BodyHandlers.ofByteArray()));
            map.put(bt, VortexUserShip.parse(bt, new JsonUtils().parse(new String(response, StandardCharsets.UTF_8))));
        }
        return map;
    }

    public static DevelopersUserShip sendRequestShipListDevelopers(HttpClient httpClient, WowsServer server, String token, long accountId) throws StatusException, IOException, InterruptedException, HttpStatusException {
        byte[] response = HttpCodec.response(httpClient.send(HttpRequest.newBuilder().uri(WowsHttpUrl.developersShipList(server,
                                token, accountId))
                        .setHeader("Accept-Encoding", "gzip, deflate, br")
                        .build(),
                HttpResponse.BodyHandlers.ofByteArray()));
        String body = new String(response, StandardCharsets.UTF_8);
        return DevelopersUserShip.parse(new JsonUtils().parse(body));
    }

    public static URI vortexShipList(WowsServer server, WowsBattlesType type, long accountId) {
        return URI.create(server.vortex() + String.format("/api/accounts/%s/ships/%s/", accountId, type.name().toLowerCase(Locale.ROOT)));
    }

    public static URI developersShipList(WowsServer server, String token, long accountId) {
        StringBuilder builder = new StringBuilder();
        for (var type : WowsBattlesType.values()) {
            if (WowsBattlesType.PVP != type) {
                builder.append(type.name().toLowerCase(Locale.ROOT)).append(",");
            }
        }
        builder.deleteCharAt(builder.length() - 1);
        return URI.create(server.api() + String.format("/wows/ships/stats/?application_id=%s&account_id=%s&extra=%s", token, accountId, builder));
    }
}
