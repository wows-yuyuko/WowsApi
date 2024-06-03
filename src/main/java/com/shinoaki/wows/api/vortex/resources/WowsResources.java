package com.shinoaki.wows.api.vortex.resources;

import com.fasterxml.jackson.databind.JsonNode;
import com.shinoaki.wows.api.codec.HttpCodec;
import com.shinoaki.wows.api.error.BasicException;
import com.shinoaki.wows.api.type.WowsServer;
import com.shinoaki.wows.api.utils.JsonUtils;
import lombok.Data;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
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


    public static List<WowsResources> request(WowsServer server) throws IOException, InterruptedException, BasicException {
        final String json = """
                [
                    {
                        "query": "query Items($languageCode: String, $id: String) {\\n  items(lang: $languageCode, itemId: $id) {\\n    title\\n    description\\n    id\\n    titleShort\\n    tags\\n    typeName\\n    slot\\n    prices {\\n      credit\\n      gold\\n      xp\\n    }\\n    icons {\\n      default\\n    }\\n    type {\\n      name\\n      title\\n    }\\n    ttc {\\n      name\\n      value\\n      title\\n    }\\n    restrictions {\\n      levels\\n    }\\n  }\\n}",
                        "variables": {
                            "languageCode": "zh-sg"
                        }
                    }
                ]
                """;
        try (HttpClient client = HttpClient.newHttpClient()) {
            var req = HttpRequest.newBuilder(URI.create(STR."\{server.vortex()}/api/graphql/glossary/"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json)).build();
            var resp = client.send(req, HttpResponse.BodyHandlers.ofByteArray());
            var data = HttpCodec.response(resp);
            return WowsResources.parse(new JsonUtils().parse(data));
        }
    }

    private static List<WowsResources> parse(JsonNode jsonNode) {
        List<WowsResources> list = new ArrayList<>();
        for (var nodeList : jsonNode) {
            var data = nodeList.get("data");
            if (data != null) {
                var items = data.get("items");
                if (items != null) {
                    for (var info : items) {
                        WowsResources w = new WowsResources();
                        w.setTitle(info.get("title").asText());
                        w.setDescription(info.get("description").asText());
                        w.setId(info.get("id").asText());
                        w.setTitleShort(info.get("titleShort").asText());
                        w.setTags(info.get("tags"));
                        w.setTypeName(info.get("typeName").asText());
                        w.setSlot(info.get("slot"));
                        w.setPrices(info.get("prices"));
                        w.setIcons(WowsIcons.parse(info.get("icons")));
                        w.setType(info.get("type"));
                        w.setTtc(info.get("ttc"));
                        w.setRestrictions(info.get("restrictions"));
                        list.add(w);
                    }
                }
            }
        }
        return list;
    }
}
