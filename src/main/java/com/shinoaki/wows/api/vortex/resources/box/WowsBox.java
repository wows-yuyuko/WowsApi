package com.shinoaki.wows.api.vortex.resources.box;

import com.fasterxml.jackson.databind.JsonNode;
import com.shinoaki.wows.api.codec.HttpCodec;
import com.shinoaki.wows.api.error.BasicException;
import com.shinoaki.wows.api.type.WowsServer;
import com.shinoaki.wows.api.utils.JsonUtils;
import com.shinoaki.wows.api.vortex.resources.WowsIcons;
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
public class WowsBox {
    private long id;
    private String title;

    private String shortTitle;
    private boolean isPremium;
    private WowsIcons icons;

    public static List<WowsBox> request(WowsServer server) throws IOException, InterruptedException, BasicException {
        final String json = """
                [
                	{
                		"query": "query Lootbox($id: String, $languageCode: String) {\\n  lootbox(lootboxId: $id, lang: $languageCode) {\\n    id\\n    title\\n    shortTitle\\n    isPremium\\n    icons {\\n      default\\n    }\\n  }\\n}",
                		"variables": {
                			"languageCode": "zh-sg"
                		}
                	}
                ]
                """;
        try (HttpClient client = HttpClient.newHttpClient()) {
            var req = HttpRequest.newBuilder(URI.create(server.vortex()+"/api/graphql/glossary/"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json)).build();
            var resp = client.send(req, HttpResponse.BodyHandlers.ofByteArray());
            var data = HttpCodec.response(resp);
            return WowsBox.parse(new JsonUtils().parse(data));
        }
    }

    private static List<WowsBox> parse(JsonNode jsonNode) {
        List<WowsBox> list = new ArrayList<>();
        for (var nodeList : jsonNode) {
            var data = nodeList.get("data");
            if (data != null) {
                var lootbox = data.get("lootbox");
                if (lootbox != null) {
                    for (var box : lootbox) {
                        WowsBox w = new WowsBox();
                        w.setId(box.get("id").asLong());
                        w.setTitle(box.get("title").asText());
                        w.setShortTitle(box.get("shortTitle").asText());
                        w.setPremium(box.get("isPremium").asBoolean());
                        w.setIcons(WowsIcons.parse(box.get("icons")));
                        list.add(w);
                    }
                }
            }
        }
        return list;
    }


}
