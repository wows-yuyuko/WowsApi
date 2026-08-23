package com.shinoaki.wows.api.codec.http;

import com.shinoaki.wows.api.codec.HttpCodec;
import com.shinoaki.wows.api.data.ship.ShipExpansion;
import com.shinoaki.wows.api.developers.DevelopersUserShip;
import com.shinoaki.wows.api.error.BasicException;
import com.shinoaki.wows.api.error.CompletableInfo;
import com.shinoaki.wows.api.error.HttpThrowableStatus;
import com.shinoaki.wows.api.type.WowsBattlesType;
import com.shinoaki.wows.api.type.WowsServer;
import com.shinoaki.wows.api.utils.JsonUtils;
import com.shinoaki.wows.api.vortex.VortexUserShip;
import tools.jackson.core.JacksonException;

import java.net.URI;
import java.net.http.HttpClient;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * 用户战舰信息查询
 *
 * @author Xun
 * @date 2023/4/24 23:49 星期一
 */
public record WowsHttpShipTools(HttpClient httpClient, WowsServer server, long accountId) {
    public Vortex vortex() {
        return new Vortex(httpClient, server, accountId);
    }

    public Developers developers(String token) {
        return new Developers(httpClient, server, accountId, token);
    }

    public record Vortex(HttpClient httpClient, WowsServer server, long accountId) {

        public URI shipListUri(WowsBattlesType type) {
            return vortexShipList(server, type, accountId);
        }

        public CompletableFuture<CompletableInfo<VortexUserShip>> shipListAsync(WowsBattlesType type) {
            return HttpCodec.sendAsync(httpClient, HttpCodec.request(shipListUri(type))).thenApplyAsync(data -> {
                        try {
                            return CompletableInfo.ok(VortexUserShip.parse(type, JsonUtils.json().parse(HttpCodec.response(data))));
                        } catch (BasicException e) {
                            return CompletableInfo.error(e);
                        } catch (JacksonException e) {
                            return CompletableInfo.error(new BasicException(HttpThrowableStatus.DATA_PARSE, e));
                        }
                    }
            );
        }

        public VortexUserShip shipList(WowsBattlesType type) throws BasicException {
            return VortexUserShip.parse(type, JsonUtils.json().parse(HttpCodec.response(HttpCodec.send(httpClient, HttpCodec.request(shipListUri(type))))));
        }

        public CompletableFuture<CompletableInfo<Map<WowsBattlesType, VortexUserShip>>> shipListMapAsync(WowsBattlesType[] types) {
            Map<WowsBattlesType, CompletableFuture<CompletableInfo<VortexUserShip>>> futures = new EnumMap<>(WowsBattlesType.class);
            for (WowsBattlesType type : types) {
                futures.put(type, shipListAsync(type));
            }
            return CompletableFuture.allOf(futures.values().toArray(new CompletableFuture<?>[0]))
                    .thenApplyAsync(v -> {
                        Map<WowsBattlesType, VortexUserShip> shipMap = new EnumMap<>(WowsBattlesType.class);
                        for (var entry : futures.entrySet()) {
                            var value = entry.getValue().join();
                            if (value.isErr()) {
                                return CompletableInfo.copy(value, shipMap);
                            }
                            shipMap.put(entry.getKey(), value.data());
                        }
                        return CompletableInfo.ok(shipMap);
                    });
        }

        public Map<WowsBattlesType, VortexUserShip> shipListMap(WowsBattlesType[] types) throws BasicException {
            Map<WowsBattlesType, VortexUserShip> shipMap = new EnumMap<>(WowsBattlesType.class);
            for (WowsBattlesType type : types) {
                shipMap.put(type, VortexUserShip.parse(type,
                        JsonUtils.json().parse(HttpCodec.response(HttpCodec.send(httpClient, HttpCodec.request(shipListUri(type)))))));
            }
            return shipMap;
        }

        private URI vortexShipList(WowsServer server, WowsBattlesType type, long accountId) {
            return URI.create(server.vortex() + String.format("/api/accounts/%s/ships/%s/", accountId, type.name().toLowerCase(Locale.ROOT)));
        }
    }

    public record Developers(HttpClient httpClient, WowsServer server, long accountId, String token) {

        public CompletableFuture<CompletableInfo<DevelopersUserShip>> shipListAsync() {
            return HttpCodec.sendAsync(httpClient, HttpCodec.request(shipListUri())).thenApplyAsync(data -> {
                try {
                    return CompletableInfo.ok(DevelopersUserShip.parse(JsonUtils.json().parse(HttpCodec.response(data))));
                } catch (BasicException e) {
                    return CompletableInfo.error(e);
                } catch (JacksonException e) {
                    return CompletableInfo.error(new BasicException(HttpThrowableStatus.DATA_PARSE, e));
                }
            });
        }

        public DevelopersUserShip shipList() throws BasicException {
            return DevelopersUserShip.parse(JsonUtils.json().parse(HttpCodec.response(HttpCodec.send(httpClient, HttpCodec.request(shipListUri())))));
        }

        public Map<Long, ShipExpansion> shipBadges() throws BasicException {
            return ShipExpansion.parseDevelopers(accountId, JsonUtils.json().parse(HttpCodec.response(HttpCodec.send(httpClient, HttpCodec.request(shipBadgesUri())))));
        }

        public CompletableFuture<CompletableInfo<DevelopersUserShip>> shipListOaAsync(String accessToken) {
            return HttpCodec.sendAsync(httpClient, HttpCodec.request(shipListUri(accessToken))).thenApplyAsync(data -> {
                try {
                    return CompletableInfo.ok(DevelopersUserShip.parse(JsonUtils.json().parse(HttpCodec.response(data))));
                } catch (BasicException e) {
                    return CompletableInfo.error(e);
                } catch (JacksonException e) {
                    return CompletableInfo.error(new BasicException(HttpThrowableStatus.DATA_PARSE, e));
                }
            });
        }

        public DevelopersUserShip shipListOa(String accessToken) throws BasicException {
            return DevelopersUserShip.parse(JsonUtils.json().parse(HttpCodec.response(HttpCodec.send(httpClient, HttpCodec.request(shipListUri(accessToken))))));
        }

        public URI shipListUri() {
            return shipListUri("");
        }

        public URI shipBadgesUri() {
            return URI.create(server.api() + String.format("/wows/ships/badges/?application_id=%s&account_id=%s", token, accountId));
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
