package com.shinoaki.wows.api.codec.http;

import com.shinoaki.wows.api.codec.HttpCodec;
import com.shinoaki.wows.api.type.WowsServer;

import java.net.URI;
import java.net.http.HttpClient;

/**
 * 用户公会信息o
 *
 * @author Xun
 * @date 2023/5/22 21:42 星期一
 */
public record WowsHttpClanTools(HttpClient httpClient, WowsServer server) {

    public Developoers developoers() {
        return new Developoers(httpClient, server);
    }

    public Vortex vortex() {
        return new Vortex(httpClient, server);
    }

    public record Developoers(HttpClient httpClient, WowsServer server) {


        public URI userSearchClanDevelopers(String token, long accountId) {
            return URI.create(server.api() + String.format("/wows/clans/accountinfo/?application_id=%s&account_id=%s", token, accountId));
        }

        public URI clanInfoDevelopers(String token, long clanId) {
            return URI.create(server.api() + String.format("/wows/clans/info/?extra=members&application_id=%s&clan_id=%s", token, clanId));
        }

        public URI searchClanDevelopers(String token, String clanTag) {
            return URI.create(server.api() + String.format("/wows/clans/list/?application_id=%s&search=%s", token, HttpCodec.encodeURIComponent(clanTag)));
        }
    }

    public record Vortex(HttpClient httpClient, WowsServer server) {

        public URI userSearchClanVortex(long accountId) {
            return URI.create(server.vortex() + String.format("/api/accounts/%s/clans/", accountId));
        }

        public URI clanInfoVortex(long clanId) {
            return URI.create(server.clans() + String.format("/api/clanbase/%s/claninfo/", clanId));
        }

        public URI clanUserListInfoVortex(long clanId) {
            return URI.create(server.clans() + String.format("/api/members/%s/?battle_type=pvp", clanId));
        }

        public URI searchClanVortex(String clanTag) {
            return URI.create(server.clans() + String.format("/api/search/autocomplete/?search=%s&type=clans", HttpCodec.encodeURIComponent(clanTag)));
        }
    }

}
