package com.shinoaki.wows.api.codec.http;

import com.shinoaki.wows.api.codec.HttpCodec;
import com.shinoaki.wows.api.codec.HttpSend;
import com.shinoaki.wows.api.developers.clan.DevelopersClanInfo;
import com.shinoaki.wows.api.developers.clan.DevelopersSearchClan;
import com.shinoaki.wows.api.developers.clan.DevelopersSearchUserClan;
import com.shinoaki.wows.api.error.HttpStatusException;
import com.shinoaki.wows.api.error.StatusException;
import com.shinoaki.wows.api.type.WowsServer;
import com.shinoaki.wows.api.utils.JsonUtils;
import com.shinoaki.wows.api.vortex.clan.VortexSearchClan;
import com.shinoaki.wows.api.vortex.clan.account.VortexSearchClanUser;
import com.shinoaki.wows.api.vortex.clan.base.VortexClanInfo;
import com.shinoaki.wows.api.vortex.clan.members.VortexClanUserInfo;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.util.List;

/**
 * 用户公会信息o
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

        public DevelopersSearchUserClan userSearchClanDevelopers(long accountId) throws HttpStatusException, IOException, InterruptedException,
                StatusException {
            return DevelopersSearchUserClan.parse(new JsonUtils(), accountId, HttpSend.sendGet(httpClient, userSearchClanDevelopersUri(accountId)));
        }

        public DevelopersClanInfo clanInfoDevelopers(long clanId) throws HttpStatusException, IOException, InterruptedException, StatusException {
            return DevelopersClanInfo.parse(new JsonUtils(), clanId, HttpSend.sendGet(httpClient, clanInfoDevelopersUri(clanId)));
        }

        public List<DevelopersSearchClan> searchClanDevelopers(String clanTag) throws HttpStatusException, IOException, InterruptedException, StatusException {
            return DevelopersSearchClan.parse(new JsonUtils(), HttpSend.sendGet(httpClient, searchClanDevelopersUri(clanTag)));
        }

        public URI userSearchClanDevelopersUri(long accountId) {
            return URI.create(server.api() + String.format("/wows/clans/accountinfo/?application_id=%s&account_id=%s", token, accountId));
        }

        public URI clanInfoDevelopersUri(long clanId) {
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
         * @param accountId
         * @return
         * @throws HttpStatusException
         * @throws IOException
         * @throws InterruptedException
         * @throws StatusException
         */
        public VortexSearchClanUser userSearchClanVortex(long accountId) throws HttpStatusException, IOException, InterruptedException, StatusException {
            return VortexSearchClanUser.to(new JsonUtils().parse(HttpSend.sendGet(httpClient, userSearchClanVortexUri(accountId))));
        }

        /**
         * 查找公会
         *
         * @param clanTag 公会tag
         * @return
         * @throws HttpStatusException
         * @throws IOException
         * @throws InterruptedException
         */
        public List<VortexSearchClan> searchClanVortex(String clanTag) throws HttpStatusException, IOException, InterruptedException {
            return VortexSearchClan.parse(new JsonUtils(), HttpSend.sendGet(httpClient, searchClanVortexUri(clanTag)));
        }

        public VortexClanInfo clanInfoVortex(long clanId) throws HttpStatusException, IOException, InterruptedException {
            return VortexClanInfo.to(server, clanId, new JsonUtils().parse(HttpSend.sendGet(httpClient, clanInfoVortexUri(clanId))));
        }

        public List<VortexClanUserInfo> clanUserListInfoVortex(long clanId) throws HttpStatusException, IOException, InterruptedException {
            return VortexClanUserInfo.to(server, new JsonUtils().parse(HttpSend.sendGet(httpClient, clanUserListInfoVortexUri(clanId))));
        }

        public URI userSearchClanVortexUri(long accountId) {
            return URI.create(server.vortex() + String.format("/api/accounts/%s/clans/", accountId));
        }

        public URI clanInfoVortexUri(long clanId) {
            return URI.create(server.clans() + String.format("/api/clanbase/%s/claninfo/", clanId));
        }

        public URI clanUserListInfoVortexUri(long clanId) {
            return URI.create(server.clans() + String.format("/api/members/%s/?battle_type=pvp", clanId));
        }

        public URI searchClanVortexUri(String clanTag) {
            return URI.create(server.clans() + String.format("/api/search/autocomplete/?search=%s&type=clans", HttpCodec.encodeURIComponent(clanTag)));
        }
    }

}
