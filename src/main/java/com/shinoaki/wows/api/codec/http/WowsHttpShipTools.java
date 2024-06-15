package com.shinoaki.wows.api.codec.http;

import com.shinoaki.wows.api.codec.HttpCodec;
import com.shinoaki.wows.api.developers.DevelopersUserShip;
import com.shinoaki.wows.api.error.BasicException;
import com.shinoaki.wows.api.error.CompletableInfo;
import com.shinoaki.wows.api.type.WowsBattlesType;
import com.shinoaki.wows.api.type.WowsServer;
import com.shinoaki.wows.api.utils.WowsJsonUtils;
import com.shinoaki.wows.api.vortex.VortexUserShip;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * 用户战舰信息查询
 *
 * @author Xun
 * @date 2023/4/24 23:49 星期一
 */
public record WowsHttpShipTools(HttpClient httpClient, WowsServer server, long accountId) {
    public Vortex vortex() {
        return new Vortex(new WowsJsonUtils(), httpClient, server, accountId);
    }

    public Developers developers(String token) {
        return new Developers(new WowsJsonUtils(), httpClient, server, accountId, token);
    }

    public record Vortex(WowsJsonUtils utils, HttpClient httpClient, WowsServer server, long accountId) {

        public URI shipListUri(WowsBattlesType type) {
            return vortexShipList(server, type, accountId);
        }

        public CompletableFuture<CompletableInfo<VortexUserShip>> shipListAsync(WowsBattlesType type) {
            return HttpCodec.sendAsync(httpClient, HttpCodec.request(shipListUri(type))).thenApplyAsync(data -> {
                        try {
                            return CompletableInfo.ok(VortexUserShip.parse(type, utils.parse(HttpCodec.response(data))));
                        } catch (BasicException e) {
                            return CompletableInfo.error(e);
                        }
                    }
            );
        }

        public VortexUserShip shipList(WowsBattlesType type) throws IOException, BasicException {
            return VortexUserShip.parse(type, utils.parse(HttpCodec.response(HttpCodec.send(httpClient, HttpCodec.request(shipListUri(type))))));
        }

        public CompletableFuture<CompletableInfo<Map<WowsBattlesType, VortexUserShip>>> shipListMapAsync(WowsBattlesType[] types) {
            return CompletableFuture.supplyAsync(() -> {
                Map<WowsBattlesType, VortexUserShip> shipMap = new EnumMap<>(WowsBattlesType.class);
                List<CompletableFuture<CompletableInfo<VortexUserShip>>> list = new ArrayList<>();
                for (final WowsBattlesType type : types) {
                    list.add(HttpCodec.sendAsync(httpClient, HttpCodec.request(shipListUri(type))).thenApplyAsync(data -> {
                                try {
                                    return CompletableInfo.ok(VortexUserShip.parse(type, utils.parse(HttpCodec.response(data))));
                                } catch (BasicException e) {
                                    return CompletableInfo.error(e);
                                }
                            }
                    ));
                }
                try {
                    CompletableFuture.allOf(list.toArray(new CompletableFuture[0])).get();
                    for (CompletableFuture<CompletableInfo<VortexUserShip>> v : list) {
                        var value = v.get();
                        if (value.isErr()) {
                            return CompletableInfo.copy(value, shipMap);
                        } else {
                            shipMap.put(value.data().type(), value.data());
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return CompletableInfo.error(new BasicException(e), shipMap);
                } catch (ExecutionException e) {
                    return CompletableInfo.error(new BasicException(e), shipMap);
                }
                return CompletableInfo.ok(shipMap);
            });
        }

        public Map<WowsBattlesType, VortexUserShip> shipListMap(WowsBattlesType[] types) throws IOException, BasicException {
            Map<WowsBattlesType, VortexUserShip> shipMap = new EnumMap<>(WowsBattlesType.class);
            for (WowsBattlesType type : types) {
                shipMap.put(type, VortexUserShip.parse(type,
                        utils.parse(HttpCodec.response(HttpCodec.send(httpClient, HttpCodec.request(shipListUri(type)))))));
            }
            return shipMap;
        }

        private URI vortexShipList(WowsServer server, WowsBattlesType type, long accountId) {
            return URI.create(server.vortex() + String.format("/api/accounts/%s/ships/%s/", accountId, type.name().toLowerCase(Locale.ROOT)));
        }
    }

    public record Developers(WowsJsonUtils utils, HttpClient httpClient, WowsServer server, long accountId, String token) {

        public CompletableFuture<CompletableInfo<DevelopersUserShip>> shipListAsync() {
            return HttpCodec.sendAsync(httpClient, HttpCodec.request(shipListUri())).thenApplyAsync(data -> {
                try {
                    return CompletableInfo.ok(DevelopersUserShip.parse(utils.parse(HttpCodec.response(data))));
                } catch (BasicException e) {
                    return CompletableInfo.error(e);
                }
            });
        }

        public DevelopersUserShip shipList() throws IOException, BasicException {
            return DevelopersUserShip.parse(utils.parse(HttpCodec.response(HttpCodec.send(httpClient, HttpCodec.request(shipListUri())))));
        }

        public CompletableFuture<CompletableInfo<DevelopersUserShip>> shipListOaAsync(String accessToken) {
            return HttpCodec.sendAsync(httpClient, HttpCodec.request(shipListUri(accessToken))).thenApplyAsync(data -> {
                try {
                    return CompletableInfo.ok(DevelopersUserShip.parse(utils.parse(HttpCodec.response(data))));
                } catch (BasicException e) {
                    return CompletableInfo.error(e);
                }
            });
        }

        public DevelopersUserShip shipListOa(String accessToken) throws IOException, BasicException {
            return DevelopersUserShip.parse(utils.parse(HttpCodec.response(HttpCodec.send(httpClient, HttpCodec.request(shipListUri(accessToken))))));
        }

        public URI shipListUri() {
            return shipListUri("");
        }

        public URI shipListUri(String accessToken) {
            StringBuilder builder = new StringBuilder();
            for (var type : WowsBattlesType.values()) {
                if (WowsBattlesType.PVP != type) {
                    builder.append(type.name().toLowerCase(Locale.ROOT)).append(",");
                }
            }
            builder.deleteCharAt(builder.length() - 1);
            if (accessToken.isEmpty()) {
                return URI.create(server.api() + String.format("/wows/ships/stats/?application_id=%s&account_id=%s&extra=%s", token, accountId, builder));
            }
            return URI.create(server.api() + String.format("/wows/ships/stats/?application_id=%s&account_id=%s&access_token=%s&in_garage=1&extra=%s", token,
                    accountId, accessToken, builder));
        }
    }
}
