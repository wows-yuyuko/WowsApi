package com.shinoaki.wows.api.codec.http;

import com.shinoaki.wows.api.codec.HttpCodec;
import com.shinoaki.wows.api.codec.HttpSend;
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
import java.net.http.HttpResponse;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * 用户战舰信息查询
 *
 * @author Xun
 * @date 2023/4/24 23:49 星期一
 */
public record WowsHttpShipTools(JsonUtils utils, HttpClient httpClient, WowsServer server, long accountId) {
    public Vortex vortex() {
        return new Vortex(utils, httpClient, server, accountId);
    }

    public Developers developers(String token) {
        return new Developers(utils, httpClient, server, accountId, token);
    }

    public record Vortex(JsonUtils utils, HttpClient httpClient, WowsServer server, long accountId) {

        public URI shipListUri(WowsBattlesType type) {
            return vortexShipList(server, type, accountId);
        }

        public VortexUserShip shipList(WowsBattlesType type, JsonUtils utils) throws HttpStatusException, IOException, InterruptedException, StatusException {
            return VortexUserShip.parse(type, utils.parse(HttpSend.sendGet(httpClient, vortexShipList(server, type, accountId))));
        }

        public Map<WowsBattlesType, VortexUserShip> shipListMap() throws ExecutionException, InterruptedException, HttpStatusException, IOException,
                StatusException {
            Map<WowsBattlesType, CompletableFuture<HttpResponse<byte[]>>> map = new EnumMap<>(WowsBattlesType.class);
            for (WowsBattlesType type : WowsBattlesType.values()) {
                map.put(type, HttpSend.sendGetAsync(httpClient, shipListUri(type)));
            }
            Map<WowsBattlesType, VortexUserShip> shipMap = new EnumMap<>(WowsBattlesType.class);
            //解码
            for (var entry : map.entrySet()) {
                shipMap.put(entry.getKey(), VortexUserShip.parse(entry.getKey(), utils.parse(new String(HttpCodec.response(entry.getValue().get())))));
            }
            return shipMap;
        }

        private URI vortexShipList(WowsServer server, WowsBattlesType type, long accountId) {
            return URI.create(server.vortex() + String.format("/api/accounts/%s/ships/%s/", accountId, type.name().toLowerCase(Locale.ROOT)));
        }
    }

    public record Developers(JsonUtils utils, HttpClient httpClient, WowsServer server, long accountId, String token) {

        public DevelopersUserShip shipList() throws HttpStatusException, IOException, InterruptedException, StatusException {
            return DevelopersUserShip.parse(utils.parse(HttpSend.sendGet(httpClient, shipListUri())));
        }

        public URI shipListUri() {
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
}
