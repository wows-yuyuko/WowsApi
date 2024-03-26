package com.shinoaki.wows.api.codec.http;

import com.shinoaki.wows.api.codec.HttpCodec;
import com.shinoaki.wows.api.developers.clan.DevelopersClanInfo;
import com.shinoaki.wows.api.developers.clan.DevelopersSearchClan;
import com.shinoaki.wows.api.developers.clan.DevelopersSearchUserClan;
import com.shinoaki.wows.api.developers.clan.seasion.DevelopersSeasonInfo;
import com.shinoaki.wows.api.error.BasicException;
import com.shinoaki.wows.api.error.CompletableInfo;
import com.shinoaki.wows.api.type.WowsServer;
import com.shinoaki.wows.api.utils.WowsJsonUtils;
import com.shinoaki.wows.api.vortex.clan.VortexSearchClan;
import com.shinoaki.wows.api.vortex.clan.account.VortexSearchClanUser;
import com.shinoaki.wows.api.vortex.clan.base.VortexClanInfo;
import com.shinoaki.wows.api.vortex.clan.members.VortexClanStatisticsInfo;
import com.shinoaki.wows.api.vortex.clan.members.VortexClanUserInfo;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 用户公会信息o
 *
 * @author Xun
 * @date 2023/5/22 21:42 星期一
 */
public record WowsHttpClanTools(HttpClient httpClient, WowsServer server) {

    public Developers developers(String token) {
        return new Developers(new WowsJsonUtils(), httpClient, server, token);
    }

    public Vortex vortex() {
        return new Vortex(new WowsJsonUtils(), httpClient, server);
    }

    public record Developers(WowsJsonUtils utils, HttpClient httpClient, WowsServer server, String token) {

        public CompletableFuture<CompletableInfo<DevelopersSearchUserClan>> userSearchClanDevelopersAsync(long accountId) {
            return HttpCodec.sendAsync(httpClient, HttpCodec.request(userSearchClanDevelopersUri(accountId))).thenApplyAsync(data -> {
                try {
                    return CompletableInfo.ok(DevelopersSearchUserClan.parse(utils, accountId, HttpCodec.response(data)));
                } catch (BasicException e) {
                    return CompletableInfo.error(e);
                }
            });
        }

        public DevelopersSearchUserClan userSearchClanDevelopers(long accountId) throws IOException, InterruptedException, BasicException {
            return DevelopersSearchUserClan.parse(utils, accountId, HttpCodec.response(HttpCodec.send(httpClient,
                    HttpCodec.request(userSearchClanDevelopersUri(accountId)))));
        }

        public CompletableFuture<CompletableInfo<DevelopersClanInfo>> clanInfoDevelopersAsync(long clanId) {
            return HttpCodec.sendAsync(httpClient, HttpCodec.request(clanInfoDevelopersUri(clanId))).thenApplyAsync(data -> {
                try {
                    return CompletableInfo.ok(DevelopersClanInfo.parse(utils, clanId, HttpCodec.response(data)));
                } catch (BasicException e) {
                    return CompletableInfo.error(e);
                }
            });
        }

        public DevelopersClanInfo clanInfoDevelopers(long clanId) throws IOException, InterruptedException, BasicException {
            return DevelopersClanInfo.parse(utils, clanId, HttpCodec.response(HttpCodec.send(httpClient,
                    HttpCodec.request(clanInfoDevelopersUri(clanId)))));
        }

        public CompletableFuture<CompletableInfo<List<DevelopersSearchClan>>> searchClanDevelopersAsync(String clanTag) {
            return HttpCodec.sendAsync(httpClient, HttpCodec.request(searchClanDevelopersUri(clanTag))).thenApplyAsync(data -> {
                try {
                    return CompletableInfo.ok(DevelopersSearchClan.parse(utils, HttpCodec.response(data)));
                } catch (BasicException e) {
                    return CompletableInfo.error(e);
                }
            });
        }

        public List<DevelopersSearchClan> searchClanDevelopers(String clanTag) throws IOException, InterruptedException, BasicException {
            return DevelopersSearchClan.parse(utils, HttpCodec.response(HttpCodec.send(httpClient, HttpCodec.request(searchClanDevelopersUri(clanTag)))));
        }

        public CompletableFuture<CompletableInfo<List<DevelopersSeasonInfo>>> seasonAsync() {
            return HttpCodec.sendAsync(httpClient, HttpCodec.request(seasonUri())).thenApplyAsync(data -> {
                try {
                    return CompletableInfo.ok(DevelopersSeasonInfo.parse(utils, HttpCodec.response(data)));
                } catch (BasicException e) {
                    return CompletableInfo.error(e);
                }
            });
        }

        public List<DevelopersSeasonInfo> season() throws IOException, InterruptedException, BasicException {
            return DevelopersSeasonInfo.parse(utils, HttpCodec.response(HttpCodec.send(httpClient, HttpCodec.request(seasonUri()))));
        }

        public URI seasonUri() {
            return URI.create(server.api() + "/wows/clans/season/?language=zh-cn&application_id=" + token);
        }

        public URI userSearchClanDevelopersUri(long accountId) {
            return URI.create(server.api() + String.format("/wows/clans/accountinfo/?application_id=%s&account_id=%s&extra=clan", token, accountId));
        }

        public URI clanInfoDevelopersUri(long clanId) {
            return URI.create(server.api() + String.format("/wows/clans/info/?extra=members&application_id=%s&clan_id=%s", token, clanId));
        }

        public URI searchClanDevelopersUri(String clanTag) {
            return URI.create(server.api() + String.format("/wows/clans/list/?application_id=%s&search=%s", token, HttpCodec.encodeURIComponent(clanTag)));
        }
    }

    public record Vortex(WowsJsonUtils utils, HttpClient httpClient, WowsServer server) {

        /**
         * 搜索用户公会信息
         *
         * @param accountId aid
         */
        public CompletableFuture<CompletableInfo<VortexSearchClanUser>> userSearchClanVortexAsync(long accountId) {
            return HttpCodec.sendAsync(httpClient, HttpCodec.request(userSearchClanVortexUri(accountId))).thenApplyAsync(data -> {
                try {
                    if (server.isApi()) {
                        return CompletableInfo.ok(VortexSearchClanUser.to(utils.parse(HttpCodec.response(data))));
                    }
                    //处理404的情况
                    if (data.statusCode() == 404) {
                        return CompletableInfo.ok(new VortexSearchClanUser("", VortexSearchClanUser.VortexSearchClanInfo.empty(), "", 0));
                    }
                    return CompletableInfo.ok(VortexSearchClanUser.to(utils.parse(HttpCodec.response(data))));
                } catch (BasicException e) {
                    return CompletableInfo.error(e);
                }
            });
        }

        public VortexSearchClanUser userSearchClanVortex(long accountId) throws BasicException, IOException, InterruptedException {
            var data = HttpCodec.send(httpClient, HttpCodec.request(userSearchClanVortexUri(accountId)));
            if (!server.isApi() && (data.statusCode() == 404)) {
                return new VortexSearchClanUser("", VortexSearchClanUser.VortexSearchClanInfo.empty(), "", 0);
            }
            return VortexSearchClanUser.to(utils.parse(HttpCodec.response(data)));
        }

        /**
         * 查找公会
         *
         * @param clanTag 公会tag
         * @return
         */
        public CompletableFuture<CompletableInfo<List<VortexSearchClan>>> searchClanVortexAsync(String clanTag) {
            return HttpCodec.sendAsync(httpClient, HttpCodec.request(searchClanVortexUri(clanTag))).thenApplyAsync(data -> {
                try {
                    return CompletableInfo.ok(VortexSearchClan.parse(utils, HttpCodec.response(data)));
                } catch (BasicException e) {
                    return CompletableInfo.error(e);
                }
            });
        }

        /**
         * 查找公会
         *
         * @param clanTag 公会tag
         * @return
         */
        public List<VortexSearchClan> searchClanVortex(String clanTag) throws IOException, InterruptedException, BasicException {
            return VortexSearchClan.parse(utils, HttpCodec.response(HttpCodec.send(httpClient, HttpCodec.request(searchClanVortexUri(clanTag)))));
        }

        public CompletableFuture<CompletableInfo<VortexClanInfo>> clanInfoVortexAsync(long clanId) {
            return HttpCodec.sendAsync(httpClient, HttpCodec.request(clanInfoVortexUri(clanId))).thenApplyAsync(data -> {
                try {
                    return CompletableInfo.ok(VortexClanInfo.to(server, clanId, utils.parse(HttpCodec.response(data))));
                } catch (BasicException e) {
                    return CompletableInfo.error(e);
                }
            });
        }

        public VortexClanInfo clanInfoVortex(long clanId) throws IOException, InterruptedException, BasicException {
            return VortexClanInfo.to(server, clanId, utils.parse(HttpCodec.response(HttpCodec.send(httpClient, HttpCodec.request(clanInfoVortexUri(clanId))))));
        }

        public CompletableFuture<CompletableInfo<VortexClanStatisticsInfo>> clanUserListInfoVortexAsync(long clanId) {
            return HttpCodec.sendAsync(httpClient, HttpCodec.request(clanUserListInfoVortexUri(clanId))).thenApplyAsync(data -> {
                try {
                    return CompletableInfo.ok(VortexClanUserInfo.to(server, utils.parse(HttpCodec.response(data))));
                } catch (BasicException e) {
                    return CompletableInfo.error(e);
                }
            });
        }

        public VortexClanStatisticsInfo clanUserListInfoVortex(long clanId) throws IOException, InterruptedException, BasicException {
            return VortexClanUserInfo.to(server, utils.parse(HttpCodec.response(HttpCodec.send(httpClient,
                    HttpCodec.request(clanUserListInfoVortexUri(clanId))))));
        }

        public CompletableFuture<CompletableInfo<VortexClanStatisticsInfo>> clanUserListInfoVortexAsync(long clanId, String type, Integer season) {
            URI uri;
            if (type.equalsIgnoreCase("cvc")) {
                uri = clanUserListInfoVortexUriCvc(clanId, season);
            } else {
                uri = clanUserListInfoVortexUri(clanId, type);
            }
            return HttpCodec.sendAsync(httpClient, HttpCodec.request(uri)).thenApplyAsync(data -> {
                try {
                    return CompletableInfo.ok(VortexClanUserInfo.to(server, utils.parse(HttpCodec.response(data))));
                } catch (BasicException e) {
                    return CompletableInfo.error(e);
                }
            });
        }

        public VortexClanStatisticsInfo clanUserListInfoVortex(long clanId, String type, Integer season) throws IOException, InterruptedException,
                BasicException {
            URI uri;
            if (type.equalsIgnoreCase("cvc")) {
                uri = clanUserListInfoVortexUriCvc(clanId, season);
            } else {
                uri = clanUserListInfoVortexUri(clanId, type);
            }
            return VortexClanUserInfo.to(server, utils.parse(HttpCodec.response(HttpCodec.send(httpClient, HttpCodec.request(uri)))));
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
            return URI.create(server.clans() + String.format("/api/members/%s/?battle_type=%s", clanId, type));
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
