package com.shinoaki.wows.api.codec.http;

import com.shinoaki.wows.api.codec.HttpCodec;
import com.shinoaki.wows.api.developers.clan.DevelopersClanInfo;
import com.shinoaki.wows.api.developers.clan.DevelopersSearchClan;
import com.shinoaki.wows.api.developers.clan.DevelopersSearchUserClan;
import com.shinoaki.wows.api.developers.clan.seasion.DevelopersSeasonInfo;
import com.shinoaki.wows.api.error.BasicException;
import com.shinoaki.wows.api.type.WowsServer;
import com.shinoaki.wows.api.utils.JsonUtils;
import com.shinoaki.wows.api.vortex.clan.VortexSearchClan;
import com.shinoaki.wows.api.vortex.clan.account.VortexSearchClanUser;
import com.shinoaki.wows.api.vortex.clan.base.VortexClanInfo;
import com.shinoaki.wows.api.vortex.clan.members.VortexClanStatisticsInfo;
import com.shinoaki.wows.api.vortex.clan.members.VortexClanUserInfo;

import java.net.URI;
import java.net.http.HttpClient;
import java.util.List;

/**
 * 用户公会信息o
 * <p>
 * 说明：本类只提供同步方法；需要异步时建议使用线程池或JDK21虚拟线程自行包装（参考README）。
 *
 * @author Xun
 * @date 2023/5/22 21:42 星期一
 */
public record WowsHttpClanTools(HttpClient httpClient, WowsServer server) {

    public Developers developers(String token) {
        return new Developers(httpClient, server, token);
    }

    public Vortex vortex() {
        return new Vortex(httpClient, server);
    }

    public record Developers(HttpClient httpClient, WowsServer server, String token) {

        public DevelopersSearchUserClan userSearchClanDevelopers(long accountId) throws BasicException {
            return DevelopersSearchUserClan.parse(accountId, HttpCodec.response(HttpCodec.send(httpClient,
                    HttpCodec.request(userSearchClanDevelopersUri(accountId)))));
        }

        public DevelopersClanInfo clanInfoDevelopers(long clanId) throws BasicException {
            return DevelopersClanInfo.parse(clanId, HttpCodec.response(HttpCodec.send(httpClient,
                    HttpCodec.request(clanInfoDevelopersUri(clanId)))));
        }

        public List<DevelopersSearchClan> searchClanDevelopers(String clanTag) throws BasicException {
            return DevelopersSearchClan.parse(HttpCodec.response(HttpCodec.send(httpClient, HttpCodec.request(searchClanDevelopersUri(clanTag)))));
        }

        public List<DevelopersSeasonInfo> season() throws BasicException {
            return DevelopersSeasonInfo.parse(HttpCodec.response(HttpCodec.send(httpClient, HttpCodec.request(seasonUri()))));
        }

        public URI seasonUri() {
            return URI.create(server.api() + "/wows/clans/season/?language=zh-cn&application_id=" + token);
        }

        public URI userSearchClanDevelopersUri(long accountId) {
//            return URI.create(server.api() + String.format("/wows/clans/accountinfo/?application_id=%s&account_id=%s&extra=clan", token, accountId));
            return userSearchClanDevelopersUri(server, token, accountId);
        }

        public static URI userSearchClanDevelopersUri(WowsServer server, String token, long accountId) {
            return URI.create(server.api() + String.format("/wows/clans/accountinfo/?application_id=%s&account_id=%s&extra=clan", token, accountId));
        }

        public URI clanInfoDevelopersUri(long clanId) {
//            return URI.create(server.api() + String.format("/wows/clans/info/?extra=members&application_id=%s&clan_id=%s", token, clanId));
            return clanInfoDevelopersUri(server, token, clanId);
        }

        public static URI clanInfoDevelopersUri(WowsServer server, String token, long clanId) {
            return URI.create(server.api() + String.format("/wows/clans/info/?extra=members&application_id=%s&clan_id=%s", token, clanId));
        }

        public URI searchClanDevelopersUri(String clanTag) {
            return URI.create(server.api() + String.format("/wows/clans/list/?application_id=%s&search=%s", token, HttpCodec.encodeURIComponent(clanTag)));
        }
    }

    public record Vortex(HttpClient httpClient, WowsServer server) {

        /**
         * 搜索用户公会信息
         *
         * @param accountId aid
         */
        public VortexSearchClanUser userSearchClanVortex(long accountId) throws BasicException {
            var data = HttpCodec.send(httpClient, HttpCodec.request(userSearchClanVortexUri(accountId)));
            /*
             * 说明：非API服务器（vortex代理）查无公会时返回404，这里兜底返回空数据；
             * API服务器按正常响应处理。此差异源于不同服务器的接口语义，请勿统一。
             */
            if (!server.isApi() && (data.statusCode() == 404)) {
                return new VortexSearchClanUser("", VortexSearchClanUser.VortexSearchClanInfo.empty(), "", 0);
            }
            return VortexSearchClanUser.to(JsonUtils.json().parse(HttpCodec.response(data)));
        }

        /**
         * 查找公会
         *
         * @param clanTag 公会tag
         * @return
         */
        public List<VortexSearchClan> searchClanVortex(String clanTag) throws BasicException {
            return VortexSearchClan.parse(HttpCodec.response(HttpCodec.send(httpClient, HttpCodec.request(searchClanVortexUri(clanTag)))));
        }

        public VortexClanInfo clanInfoVortex(long clanId) throws BasicException {
            return VortexClanInfo.to(server, clanId, JsonUtils.json().parse(HttpCodec.response(HttpCodec.send(httpClient, HttpCodec.request(clanInfoVortexUri(clanId))))));
        }

        public VortexClanStatisticsInfo clanUserListInfoVortex(long clanId) throws BasicException {
            return VortexClanUserInfo.to(server, JsonUtils.json().parse(HttpCodec.response(HttpCodec.send(httpClient,
                    HttpCodec.request(clanUserListInfoVortexUri(clanId))))));
        }

        public VortexClanStatisticsInfo clanUserListInfoVortex(long clanId, String type, Integer season) throws
                BasicException {
            URI uri;
            if ("cvc".equalsIgnoreCase(type)) {
                uri = clanUserListInfoVortexUriCvc(clanId, season);
            } else {
                uri = clanUserListInfoVortexUri(clanId, type);
            }
            return VortexClanUserInfo.to(server, JsonUtils.json().parse(HttpCodec.response(HttpCodec.send(httpClient, HttpCodec.request(uri)))));
        }

        public URI userSearchClanVortexUri(long accountId) {
            return URI.create(server.vortex() + String.format("/api/accounts/%s/clans/", accountId));
        }

        public URI clanInfoVortexUri(long clanId) {
            return URI.create(server.clans() + String.format("/api/clanbase/%s/claninfo/", clanId));
        }

        public URI clanUserListInfoVortexUri(long clanId) {
            return clanUserListInfoVortexUri(clanId, "pvp");
        }

        public URI clanUserListInfoVortexUri(long clanId, String type) {
            return URI.create(server.clans() + String.format("/api/members/%s/?battle_type=%s", clanId, type == null ? "pvp" : type));
        }

        public URI clanUserListInfoVortexUriCvc(long clanId, Integer season) {
            if (season != null) {
                return URI.create(server.clans() + String.format("/api/members/%s/?battle_type=cvc&season=%s", clanId, season));
            }
            return URI.create(server.clans() + String.format("/api/members/%s/?battle_type=cvc", clanId));
        }

        public URI searchClanVortexUri(String clanTag) {
            return URI.create(server.clans() + String.format("/api/search/autocomplete/?search=%s&type=clans", HttpCodec.encodeURIComponent(clanTag)));
        }
    }

}
