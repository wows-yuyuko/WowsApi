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
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * @author Xun
 * @date 2023/4/24 23:49 星期一
 */
public class WowsHttpUrl {
    private WowsHttpUrl() {

    }

    /**
     * 异步请求
     *
     * @return {@link SyncResult} 里面包含了http异常信息，使用前须提前判断 然后调用 {@link DevelopersUserShip}parse()方法
     */
    public static SyncResult sendRequestShipListDevelopersAsync(HttpClient httpClient, WowsServer server, String token, long accountId) throws InterruptedException
            , ExecutionException {
        return httpClient.sendAsync(shipListDevelopers(server, token, accountId), HttpResponse.BodyHandlers.ofByteArray())
                .thenApplyAsync(SyncResult::data).get();
    }

    /**
     * 异步请求
     *
     * @return {@link SyncResult} 里面包含了http异常信息，使用前须提前判断 然后调用 {@link VortexUserShip}parse()方法
     */
    public static Map<WowsBattlesType, SyncResult> sendRequestShipListVortexAsync(HttpClient httpClient, WowsServer server, long accountId) throws InterruptedException
            , ExecutionException {
        List<WowsBattlesType> list = Arrays.stream(WowsBattlesType.values()).toList();
        Map<WowsBattlesType, CompletableFuture<SyncResult>> futureMap = new EnumMap<>(WowsBattlesType.class);
        list.forEach(type -> futureMap.put(type, httpClient.sendAsync(shipListVortex(server, type, accountId), HttpResponse.BodyHandlers.ofByteArray())
                .thenApplyAsync(SyncResult::data)));
        Map<WowsBattlesType, SyncResult> map = new EnumMap<>(WowsBattlesType.class);
        for (var future : futureMap.entrySet()) {
            map.put(future.getKey(), future.getValue().get());
        }
        return map;
    }

    public record SyncResult(IOException io, HttpStatusException http, String data) {

        public boolean isErr() {
            return io != null || http != null;
        }

        public static SyncResult data(HttpResponse<byte[]> response) {
            try {
                return new SyncResult(null, null, new String(HttpCodec.response(response)));
            } catch (IOException e) {
                return new SyncResult(e, null, null);
            } catch (HttpStatusException e) {
                return new SyncResult(null, e, null);
            }
        }
    }

    public static Map<WowsBattlesType, VortexUserShip> sendRequestShipListVortex(HttpClient httpClient, WowsServer server, long accountId) throws IOException,
            InterruptedException, StatusException,
            HttpStatusException {
        Map<WowsBattlesType, VortexUserShip> map = new EnumMap<>(WowsBattlesType.class);
        for (var bt : WowsBattlesType.values()) {
            byte[] response = HttpCodec.response(httpClient.send(shipListVortex(server, bt, accountId), HttpResponse.BodyHandlers.ofByteArray()));
            map.put(bt, VortexUserShip.parse(bt, new JsonUtils().parse(new String(response, StandardCharsets.UTF_8))));
        }
        return map;
    }

    public static DevelopersUserShip sendRequestShipListDevelopers(HttpClient httpClient, WowsServer server, String token, long accountId) throws StatusException, IOException, InterruptedException, HttpStatusException {
        byte[] response = HttpCodec.response(httpClient.send(shipListDevelopers(server, token, accountId),
                HttpResponse.BodyHandlers.ofByteArray()));
        String body = new String(response, StandardCharsets.UTF_8);
        return DevelopersUserShip.parse(new JsonUtils().parse(body));
    }

    public static HttpRequest shipListVortex(WowsServer server, WowsBattlesType type, long accountId) {
        return HttpRequest.newBuilder().uri(WowsHttpUrl.vortexShipList(server,
                        type, accountId))
                .setHeader("Accept-Encoding", "gzip, deflate, br")
                .build();
    }

    public static HttpRequest shipListDevelopers(WowsServer server, String token, long accountId) {
        return HttpRequest.newBuilder().uri(WowsHttpUrl.developersShipList(server,
                        token, accountId))
                .setHeader("Accept-Encoding", "gzip, deflate, br")
                .build();
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
