package com.shinoaki.wows.api.vortex.resources;

import com.shinoaki.wows.api.codec.HttpCodec;
import com.shinoaki.wows.api.error.BasicException;
import com.shinoaki.wows.api.error.ResponseParseException;
import com.shinoaki.wows.api.type.WowsServer;
import com.shinoaki.wows.api.utils.JsonUtils;
import com.shinoaki.wows.api.vortex.VortexResourcesLanguage;
import lombok.Data;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Xun
 * @date 2023/12/7 星期四
 */
@Data
public class WowsResources {
    private String title;
    private String description;
    private String id;
    private String titleShort;
    private JsonNode tags;
    private String typeName;
    private JsonNode slot;
    private JsonNode prices;
    private WowsIcons icons;
    private JsonNode type;
    private JsonNode ttc;
    private JsonNode restrictions;

    public static List<WowsResources> request(WowsServer server, VortexResourcesLanguage language) throws IOException, InterruptedException, BasicException {
        return request(false, server, language);
    }

    public static List<WowsResources> request(boolean isPtServer, WowsServer server, VortexResourcesLanguage language) throws IOException, InterruptedException, BasicException {
        final String json = requestBody(language.getLanguage());
        var url = isPtServer ? server.ptVortexServer() : server.vortex();
        var req = HttpRequest.newBuilder(URI.create(url + "/api/graphql/glossary/"))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(HttpCodec.requestTimeout()))
                .POST(HttpRequest.BodyPublishers.ofString(json)).build();
        var resp = HttpCodec.client().send(req, HttpResponse.BodyHandlers.ofByteArray());
        var data = HttpCodec.response(resp);
        try {
            return WowsResources.parse(JsonUtils.json().parseResponse(data));
        } catch (JacksonException e) {
            throw new ResponseParseException("响应解析失败: " + e.getMessage(), e);
        }
    }

    /**
     * graphql（Items schema）请求体（3.0 管线复用）。
     *
     * @param language 内容语言代码，如 {@code zh-sg}
     */
    public static String requestBody(String language) {
        return """
                [
                    {
                        "query": "query Items($languageCode: String, $id: String) {\\n  items(lang: $languageCode, itemId: $id) {\\n    title\\n    description\\n    id\\n    titleShort\\n    tags\\n    typeName\\n    slot\\n    prices {\\n      credit\\n      gold\\n      xp\\n    }\\n    icons {\\n      default\\n    }\\n    type {\\n      name\\n      title\\n    }\\n    ttc {\\n      name\\n      value\\n      title\\n    }\\n    restrictions {\\n      levels\\n    }\\n  }\\n}",
                        "variables": {
                            "languageCode": "${language}"
                        }
                    }
                ]
                """.replace("${language}", language);
    }

    public static List<WowsResources> parse(JsonNode jsonNode) {
        List<WowsResources> list = new ArrayList<>();
        for (var nodeList : jsonNode) {
            var data = nodeList.path("data");
            if (data != null) {
                var items = data.path("items");
                if (items != null) {
                    for (var info : items) {
                        WowsResources w = new WowsResources();
                        w.setTitle(info.path("title").asString());
                        w.setDescription(info.path("description").asString());
                        w.setId(info.path("id").asString());
                        w.setTitleShort(info.path("titleShort").asString());
                        w.setTags(info.path("tags"));
                        w.setTypeName(info.path("typeName").asString());
                        w.setSlot(info.path("slot"));
                        w.setPrices(info.path("prices"));
                        w.setIcons(WowsIcons.parse(info.path("icons")));
                        w.setType(info.path("type"));
                        w.setTtc(info.path("ttc"));
                        w.setRestrictions(info.path("restrictions"));
                        list.add(w);
                    }
                }
            }
        }
        return list;
    }
}
