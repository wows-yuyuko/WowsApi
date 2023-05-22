package com.shinoaki.wows.api.codec.http;

import com.shinoaki.wows.api.codec.HttpCodec;
import com.shinoaki.wows.api.codec.SyncResult;
import com.shinoaki.wows.api.developers.DevelopersUserShip;
import com.shinoaki.wows.api.error.HttpStatusException;
import com.shinoaki.wows.api.error.StatusException;
import com.shinoaki.wows.api.type.WowsBattlesType;
import com.shinoaki.wows.api.type.WowsServer;
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
 * 用户战舰信息查询
 *
 * @author Xun
 * @date 2023/4/24 23:49 星期一
 */
public class WowsHttpShipTools {
    private final HttpClient httpClient;
    private final WowsServer server;
    private final long accountId;

    public WowsHttpShipTools(HttpClient httpClient, WowsServer server, long accountId) {
        this.httpClient = httpClient;
        this.server = server;
        this.accountId = accountId;
    }

    /**
     * 异步请求
     *
     * @return {@link SyncResult} 里面包含了http异常信息，使用前须提前判断 然后调用 {@link DevelopersUserShip}parse()方法
     */
    public SyncResult sendRequestShipListDevelopersAsync(String token) throws InterruptedException, ExecutionException {
        return httpClient.sendAsync(shipListDevelopers(server, token, accountId), HttpResponse.BodyHandlers.ofByteArray()).thenApplyAsync(SyncResult::data).get();
    }

    /**
     * 异步请求
     *
     * @return {@link SyncResult} 里面包含了http异常信息，使用前须提前判断 然后调用 {@link VortexUserShip}parse()方法
     */
    public Map<WowsBattlesType, SyncResult> sendRequestShipListVortexAsync() throws InterruptedException, ExecutionException {
        List<WowsBattlesType> list = Arrays.stream(WowsBattlesType.values()).toList();
        Map<WowsBattlesType, CompletableFuture<SyncResult>> futureMap = new EnumMap<>(WowsBattlesType.class);
        list.forEach(type -> futureMap.put(type,
                httpClient.sendAsync(shipListVortex(server, type, accountId), HttpResponse.BodyHandlers.ofByteArray()).thenApplyAsync(SyncResult::data)));
        Map<WowsBattlesType, SyncResult> map = new EnumMap<>(WowsBattlesType.class);
        for (var future : futureMap.entrySet()) {
            map.put(future.getKey(), future.getValue().get());
        }
        return map;
    }


    public Map<WowsBattlesType, VortexUserShip> sendRequestShipListVortex() throws IOException, InterruptedException, StatusException, HttpStatusException {
        Map<WowsBattlesType, VortexUserShip> map = new EnumMap<>(WowsBattlesType.class);
        for (var bt : WowsBattlesType.values()) {
            byte[] response = HttpCodec.response(httpClient.send(shipListVortex(server, bt, accountId), HttpResponse.BodyHandlers.ofByteArray()));
            map.put(bt, VortexUserShip.parse(bt, new JsonUtils().parse(new String(response, StandardCharsets.UTF_8))));
        }
        return map;
    }

    public DevelopersUserShip sendRequestShipListDevelopers(String token) throws StatusException, IOException, InterruptedException, HttpStatusException {
        byte[] response = HttpCodec.response(httpClient.send(shipListDevelopers(server, token, accountId), HttpResponse.BodyHandlers.ofByteArray()));
        String body = new String(response, StandardCharsets.UTF_8);
        return DevelopersUserShip.parse(new JsonUtils().parse(body));
    }

    private HttpRequest shipListVortex(WowsServer server, WowsBattlesType type, long accountId) {
        return HttpCodec.request(vortexShipList(server, type, accountId));
    }

    private HttpRequest shipListDevelopers(WowsServer server, String token, long accountId) {
        return HttpCodec.request(developersShipList(server, token, accountId));
    }

    private static URI vortexShipList(WowsServer server, WowsBattlesType type, long accountId) {
        return URI.create(server.vortex() + String.format("/api/accounts/%s/ships/%s/", accountId, type.name().toLowerCase(Locale.ROOT)));
    }

    private static URI developersShipList(WowsServer server, String token, long accountId) {
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
