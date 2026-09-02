package com.shinoaki.wows.api.codec.http;

import com.shinoaki.wows.api.codec.HttpCodec;
import com.shinoaki.wows.api.data.ship.ShipExpansion;
import com.shinoaki.wows.api.developers.DevelopersUserShip;
import com.shinoaki.wows.api.error.BasicException;
import com.shinoaki.wows.api.type.WowsBattlesType;
import com.shinoaki.wows.api.type.WowsServer;
import com.shinoaki.wows.api.utils.JsonUtils;
import com.shinoaki.wows.api.vortex.VortexUserShip;

import java.net.URI;
import java.net.http.HttpClient;
import java.util.*;

/**
 * 用户战舰信息查询
 * <p>
 * 说明：本类只提供同步方法；需要异步时建议使用线程池或JDK21虚拟线程自行包装（参考README）。
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

        public VortexUserShip shipList(WowsBattlesType type) throws BasicException {
            return VortexUserShip.parse(type, JsonUtils.json().parse(HttpCodec.response(HttpCodec.send(httpClient, HttpCodec.request(shipListUri(type))))));
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

        public DevelopersUserShip shipList() throws BasicException {
            return DevelopersUserShip.parse(JsonUtils.json().parse(HttpCodec.response(HttpCodec.send(httpClient, HttpCodec.request(shipListUri())))));
        }

        public Map<Long, ShipExpansion> shipBadges() throws BasicException {
            return ShipExpansion.parseDevelopers(accountId, JsonUtils.json().parse(HttpCodec.response(HttpCodec.send(httpClient, HttpCodec.request(shipBadgesUri())))));
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
            if (server()==WowsServer.RU){
                if (accessToken.isEmpty()) {
                    return URI.create(server.api() + String.format("/mk/ships/stats/?application_id=%s&account_id=%s&extra=%s", token, accountId, builder));
                }
                return URI.create(server.api() + String.format("/mk/ships/stats/?application_id=%s&account_id=%s&access_token=%s&in_garage=1&extra=%s", token,
                        accountId, accessToken, builder));
            }else {
                if (accessToken.isEmpty()) {
                    return URI.create(server.api() + String.format("/wows/ships/stats/?application_id=%s&account_id=%s&extra=%s", token, accountId, builder));
                }
                return URI.create(server.api() + String.format("/wows/ships/stats/?application_id=%s&account_id=%s&access_token=%s&in_garage=1&extra=%s", token,
                        accountId, accessToken, builder));
            }
        }
    }
}
