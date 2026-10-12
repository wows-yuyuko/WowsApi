package com.shinoaki.wows.api.codec.http;

import com.shinoaki.wows.api.codec.ApiHttp;
import com.shinoaki.wows.api.codec.VortexHttp;
import com.shinoaki.wows.api.data.ship.ShipExpansion;
import com.shinoaki.wows.api.developers.DevelopersUserShip;
import com.shinoaki.wows.api.developers.DevelopersUserShipOper;
import com.shinoaki.wows.api.error.BasicException;
import com.shinoaki.wows.api.error.ResponseParseException;
import com.shinoaki.wows.api.type.WowsBattlesType;
import com.shinoaki.wows.api.type.WowsServer;
import com.shinoaki.wows.api.utils.JsonUtils;
import com.shinoaki.wows.api.vortex.VortexUserShip;
import tools.jackson.core.JacksonException;

import java.net.URI;
import java.net.http.HttpClient;
import java.util.EnumMap;
import java.util.Map;

/**
 * 用户战舰信息查询
 * <p>
 * 说明：本类只提供同步方法；需要异步时建议使用线程池或JDK21虚拟线程自行包装（参考README）。
 * <p>
 * 请求通道：vortex接口走 {@link VortexHttp}，官方开发者API走 {@link ApiHttp}（自动cookie管理与重定向处理，兼容莱服 ）。
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

        /**
         * 战舰列表地址。莱服专属类型（{@code brawl}/{@code clan}/{@code rating*} 等）只在
         * {@code korabli.su} 上有效，其它服调用会抛 {@link IllegalArgumentException}。
         */
        public URI shipListUri(WowsBattlesType type, String token) {
            return vortexShipList(server, type, accountId, token);
        }

        public VortexUserShip shipList(WowsBattlesType type) throws BasicException {
            return shipList(type, "");
        }

        public VortexUserShip shipList(WowsBattlesType type, String token) throws BasicException {
            try {
                return VortexUserShip.parse(type, JsonUtils.json().parseResponse(VortexHttp.response(VortexHttp.send(httpClient, VortexHttp.request(shipListUri(type, token))))));
            } catch (JacksonException e) {
                throw new ResponseParseException("响应解析失败: " + e.getMessage(), e);
            }
        }

        public Map<WowsBattlesType, VortexUserShip> shipListMap(WowsBattlesType[] types) throws BasicException {
            Map<WowsBattlesType, VortexUserShip> shipMap = new EnumMap<>(WowsBattlesType.class);
            for (WowsBattlesType type : types) {
                shipMap.put(type, shipList(type));
            }
            return shipMap;
        }

        public Map<WowsBattlesType, VortexUserShip> shipListMap(WowsBattlesType[] types, String token) throws BasicException {
            Map<WowsBattlesType, VortexUserShip> shipMap = new EnumMap<>(WowsBattlesType.class);
            for (WowsBattlesType type : types) {
                shipMap.put(type, shipList(type, token));
            }
            return shipMap;
        }

        private URI vortexShipList(WowsServer server, WowsBattlesType type, long accountId, String token) {
            if (!type.supportedOn(server)) {
                throw new IllegalArgumentException(type + " 在 " + server.getCode() + " 不支持，该类型仅支持: " + type.servers());
            }
            if (token.isEmpty()) {
                return URI.create(server.vortex() + String.format("/api/accounts/%s/ships/%s/", accountId, type.code()));
            }
            return URI.create(server.vortex() + String.format("/api/accounts/%s/ships/%s/?ac=%s", accountId, type.code(), token));
        }
    }

    public record Developers(HttpClient httpClient, WowsServer server, long accountId, String token) {

        public DevelopersUserShip shipList() throws BasicException {
            try {
                return DevelopersUserShip.parse(JsonUtils.json().parseResponse(ApiHttp.response(ApiHttp.send(httpClient, ApiHttp.request(shipListUri())))));
            } catch (JacksonException e) {
                throw new ResponseParseException("响应解析失败: " + e.getMessage(), e);
            }
        }

        /**
         * 行动模式
         */
        public DevelopersUserShipOper shipOperList() throws BasicException {
            try {
                return DevelopersUserShipOper.parse(JsonUtils.json().parseResponse(ApiHttp.response(ApiHttp.send(httpClient, ApiHttp.request(shipOperListUri(""))))));
            } catch (JacksonException e) {
                throw new ResponseParseException("响应解析失败: " + e.getMessage(), e);
            }
        }

        public Map<Long, ShipExpansion> shipBadges() throws BasicException {
            //不支持莱服
            if (server() == WowsServer.RU) {
                return Map.of();
            }
            try {
                return ShipExpansion.parseDevelopers(accountId, JsonUtils.json().parseResponse(ApiHttp.response(ApiHttp.send(httpClient, ApiHttp.request(shipBadgesUri())))));
            } catch (JacksonException e) {
                throw new ResponseParseException("响应解析失败: " + e.getMessage(), e);
            }
        }

        public DevelopersUserShip shipListOa(String accessToken) throws BasicException {
            try {
                return DevelopersUserShip.parse(JsonUtils.json().parseResponse(ApiHttp.response(ApiHttp.send(httpClient, ApiHttp.request(shipListUri(accessToken))))));
            } catch (JacksonException e) {
                throw new ResponseParseException("响应解析失败: " + e.getMessage(), e);
            }
        }

        public URI shipListUri() {
            return shipListUri("");
        }

        public URI shipBadgesUri() {
            return URI.create(server.api() + String.format("/wows/ships/badges/?application_id=%s&account_id=%s", token, accountId));
        }

        public URI shipOperListUri(String accessToken) {
            String builder = "oper_div,oper_div_hard,oper_solo";
            if (server() == WowsServer.RU) {
                if (accessToken.isEmpty()) {
                    return URI.create(server.api() + String.format("/mk/ships/stats/?application_id=%s&account_id=%s&extra=%s", token, accountId, builder));
                }
                return URI.create(server.api() + String.format("/mk/ships/stats/?application_id=%s&account_id=%s&access_token=%s&in_garage=1&extra=%s", token,
                        accountId, accessToken, builder));
            } else {
                if (accessToken.isEmpty()) {
                    return URI.create(server.api() + String.format("/wows/ships/stats/?application_id=%s&account_id=%s&extra=%s", token, accountId, builder));
                }
                return URI.create(server.api() + String.format("/wows/ships/stats/?application_id=%s&account_id=%s&access_token=%s&in_garage=1&extra=%s", token,
                        accountId, accessToken, builder));
            }
        }

        public URI shipListUri(String accessToken) {
            StringBuilder builder = new StringBuilder();
            for (var type : WowsBattlesType.DEVELOPERS_STATS_TYPES) {
                if (WowsBattlesType.PVP != type) {
                    builder.append(type.code()).append(",");
                }
            }
            builder.deleteCharAt(builder.length() - 1);
            if (server() == WowsServer.RU) {
                if (accessToken.isEmpty()) {
                    return URI.create(server.api() + String.format("/mk/ships/stats/?application_id=%s&account_id=%s&extra=%s", token, accountId, builder));
                }
                return URI.create(server.api() + String.format("/mk/ships/stats/?application_id=%s&account_id=%s&access_token=%s&in_garage=1&extra=%s", token,
                        accountId, accessToken, builder));
            } else {
                if (accessToken.isEmpty()) {
                    return URI.create(server.api() + String.format("/wows/ships/stats/?application_id=%s&account_id=%s&extra=%s", token, accountId, builder));
                }
                return URI.create(server.api() + String.format("/wows/ships/stats/?application_id=%s&account_id=%s&access_token=%s&in_garage=1&extra=%s", token,
                        accountId, accessToken, builder));
            }
        }
    }
}
