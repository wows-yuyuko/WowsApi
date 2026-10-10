package com.shinoaki.wows.api.codec.http;

import com.shinoaki.wows.api.codec.ApiHttp;
import com.shinoaki.wows.api.codec.VortexHttp;
import com.shinoaki.wows.api.data.ship.ShipExpansion;
import com.shinoaki.wows.api.developers.DevelopersUserShip;
import com.shinoaki.wows.api.developers.DevelopersUserShipOper;
import com.shinoaki.wows.api.error.BasicException;
import com.shinoaki.wows.api.type.WowsBattlesType;
import com.shinoaki.wows.api.type.WowsBattlesTypeRu;
import com.shinoaki.wows.api.type.WowsServer;
import com.shinoaki.wows.api.utils.JsonUtils;
import com.shinoaki.wows.api.vortex.VortexUserShip;

import java.net.URI;
import java.net.http.HttpClient;
import java.util.EnumMap;
import java.util.Locale;
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

        public URI shipListUri(WowsBattlesType type) {
            return shipListUri(WowsBattlesTypeRu.of(type));
        }

        public VortexUserShip shipList(WowsBattlesType type) throws BasicException {
            return shipList(WowsBattlesTypeRu.of(type));
        }

        public Map<WowsBattlesType, VortexUserShip> shipListMap(WowsBattlesType[] types) throws BasicException {
            Map<WowsBattlesType, VortexUserShip> shipMap = new EnumMap<>(WowsBattlesType.class);
            for (WowsBattlesType type : types) {
                shipMap.put(type, shipList(type));
            }
            return shipMap;
        }

        //------------------------------------------------------------------------- 莱服专属战斗类型

        /**
         * 莱服专属战斗类型({@code brawl}/{@code clan}/{@code rating*} 等)的战舰列表地址。
         * <p>
         * 莱服独有的类型只在 {@code korabli.su} 上有效，其它服调用会抛 {@link IllegalArgumentException}。
         */
        public URI shipListUri(WowsBattlesTypeRu type) {
            return vortexShipList(server, type, accountId);
        }

        /**
         * 莱服专属战斗类型的战舰列表。
         */
        public VortexUserShip shipList(WowsBattlesTypeRu type) throws BasicException {
            return VortexUserShip.parse(type, JsonUtils.json().parse(VortexHttp.response(VortexHttp.send(httpClient, VortexHttp.request(shipListUri(type))))));
        }

        /**
         * 批量取莱服战斗类型的战舰列表：类型 -> 战舰列表。
         */
        public Map<WowsBattlesTypeRu, VortexUserShip> shipListMap(WowsBattlesTypeRu[] types) throws BasicException {
            Map<WowsBattlesTypeRu, VortexUserShip> shipMap = new EnumMap<>(WowsBattlesTypeRu.class);
            for (WowsBattlesTypeRu type : types) {
                shipMap.put(type, shipList(type));
            }
            return shipMap;
        }

        private URI vortexShipList(WowsServer server, WowsBattlesTypeRu type, long accountId) {
            if (type.isRuOnly() && server != WowsServer.RU) {
                throw new IllegalArgumentException(type + " 是莱服(korabli.su)专属战斗类型，当前服务器=" + server.getCode());
            }
            return URI.create(server.vortex() + String.format("/api/accounts/%s/ships/%s/", accountId, type.code()));
        }
    }

    public record Developers(HttpClient httpClient, WowsServer server, long accountId, String token) {

        public DevelopersUserShip shipList() throws BasicException {
            return DevelopersUserShip.parse(JsonUtils.json().parse(ApiHttp.response(ApiHttp.send(httpClient, ApiHttp.request(shipListUri())))));
        }

        /**
         * 行动模式
         */
        public DevelopersUserShipOper shipOperList() throws BasicException {
            return DevelopersUserShipOper.parse(JsonUtils.json().parse(ApiHttp.response(ApiHttp.send(httpClient, ApiHttp.request(shipOperListUri(""))))));
        }

        public Map<Long, ShipExpansion> shipBadges() throws BasicException {
            //不支持莱服
            if (server() == WowsServer.RU) {
                return Map.of();
            }
            return ShipExpansion.parseDevelopers(accountId, JsonUtils.json().parse(ApiHttp.response(ApiHttp.send(httpClient, ApiHttp.request(shipBadgesUri())))));
        }

        public DevelopersUserShip shipListOa(String accessToken) throws BasicException {
            return DevelopersUserShip.parse(JsonUtils.json().parse(ApiHttp.response(ApiHttp.send(httpClient, ApiHttp.request(shipListUri(accessToken))))));
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
            for (var type : WowsBattlesType.values()) {
                if (WowsBattlesType.PVP != type) {
                    builder.append(type.name().toLowerCase(Locale.ROOT)).append(",");
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
