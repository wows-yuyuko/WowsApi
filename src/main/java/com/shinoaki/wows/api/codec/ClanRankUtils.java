package com.shinoaki.wows.api.codec;


import com.shinoaki.wows.api.error.BasicException;
import com.shinoaki.wows.api.type.WowsServer;
import com.shinoaki.wows.api.utils.JsonUtils;
import com.shinoaki.wows.api.vortex.clan.rank.ClanRankInfo;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.core.type.TypeReference;

import java.io.IOException;
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

    /**
     * 分页拉取的最大页数上限，防止服务端数据异常导致死循环
     */
    private static final int MAX_RANK_PAGE = 10_000;

    /**
     * 获取排名
     *
     * @param server 服务器
     * @param season 赛季 0表示最新赛季
     * @return 结果
     */
    public List<ClanRankInfo> getRanks(WowsServer server, int season) {
        Map<Long, ClanRankInfo> rankMaps = new HashMap<>();
        String seasonRep = season <= 0 ? "" : String.valueOf(season);
        try (HttpClient client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build()) {
            String base = baseUrl(server);
            List<ClanRankInfo> index = getRanks(client, URI.create(base + "&season=" + seasonRep), server, season);
            if (index.isEmpty()) {
                return List.of();
            }
            index.forEach(x -> rankMaps.put(x.id(), x));
            var last = index.getLast();
            for (int page = 0; page < MAX_RANK_PAGE; page++) {
                List<ClanRankInfo> run = getRanks(client, URI.create(base + "&season=" + seasonRep + "&clan_id=" + last.id()), server, season);
                if (run.isEmpty()) {
                    break;
                }
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
            return JsonUtils.json().parse(HttpCodec.response(response), new TypeReference<List<ClanRankInfo>>() {
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
