package com.shinoaki.wows.api.codec;

import com.fasterxml.jackson.core.type.TypeReference;
import com.shinoaki.wows.api.error.BasicException;
import com.shinoaki.wows.api.type.WowsServer;
import com.shinoaki.wows.api.utils.JsonUtils;
import com.shinoaki.wows.api.vortex.clan.rank.ClanRankInfo;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Xun
 */
@Slf4j
public class ClanRankUtils {

    private final JsonUtils json;

    public ClanRankUtils(JsonUtils json) {
        this.json = json;
    }

    public List<ClanRankInfo> getRanks(WowsServer server, int season) {
        Map<Long, ClanRankInfo> rankMaps = new HashMap<>();
        try (HttpClient client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build()) {
            String base = baseUrl(server);
            List<ClanRankInfo> index = getRanks(client, URI.create(base + "&season=" + season), server, season);
            index.forEach(x -> rankMaps.put(x.id(), x));
            //
            var last = index.getLast();
            while (true) {
                List<ClanRankInfo> run = getRanks(client, URI.create(base + "&season=" + season + "&clan_id=" + last.id()), server, season);
                last = run.getLast();
                if (rankMaps.containsKey(last.id())) {
                    break;
                }
                run.forEach(x -> rankMaps.put(x.id(), x));
            }
        }
        return rankMaps.values().stream().toList();
    }


    private List<ClanRankInfo> getRanks(HttpClient client, URI url, WowsServer server, int season) {
        //https://clans.wowsgame.cn/api/ladder/structure/?clan_id=7000005269&season=22&realm=global
        try {
            HttpResponse<byte[]> response = client.send(HttpCodec.request(url), HttpResponse.BodyHandlers.ofByteArray());
            return json.parse(HttpCodec.response(response), new TypeReference<List<ClanRankInfo>>() {
                @Override
                public Type getType() {
                    return super.getType();
                }
            });
        } catch (IOException e) {
            log.error("{}-{} 请求公会排名数据异常！", server.getCode(), season, e);
        } catch (InterruptedException e) {
            log.error("{}-{} 请求公会排名数据线程异常！", server.getCode(), season, e);
            Thread.currentThread().interrupt();
        } catch (BasicException e) {
            log.error("{}-{} 请求公会排名数据http异常！code={}", server.getCode(), season, e.getCode(), e);
        }
        return List.of();
    }


    private static String baseUrl(WowsServer server) {
        return switch (server) {
            case CN, RU -> server.clans() + "/api/ladder/structure/?realm=global";
            case ASIA -> server.clans() + "/api/ladder/structure/?realm=sg";
            case EU -> server.clans() + "/api/ladder/structure/?realm=eu";
            case NA -> server.clans() + "/api/ladder/structure/?realm=us";
        };
    }
}
