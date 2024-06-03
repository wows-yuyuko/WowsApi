package com.shinoaki.wows.api.vortex.resources;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.shinoaki.wows.api.codec.HttpCodec;
import com.shinoaki.wows.api.error.BasicException;
import com.shinoaki.wows.api.type.WowsServer;
import com.shinoaki.wows.api.utils.JsonUtils;
import com.shinoaki.wows.api.vortex.resources.dogtagcomponents.VortexClanTag;
import com.shinoaki.wows.api.vortex.resources.dogtagcomponents.VortexDogTagComponents;
import com.shinoaki.wows.api.vortex.resources.vehicles.VortexVehicles;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Xun
 * @date 2023/12/14 星期四
 */
@Data
@AllArgsConstructor
public class RequestVortexResourcesInfo {
    private List<VortexVehicles> vehicles;
    private List<VortexDogTagComponents> dogTagComponents;

    public static final String SG = "zh-sg";

    public static final String EN = "en";

    public static final String CN = "zh-cn";

    public static final String RU = "ru";

    public RequestVortexResourcesInfo() {
    }

    public static RequestVortexResourcesInfo request(WowsServer server, String language) throws IOException, InterruptedException, BasicException {
        final String body = STR."""
                {
                    "operationName": "getGlossData",
                    "variables": {
                        "lang": "\{language}"
                    },
                    "query": "query getGlossData($lang: String) {\\n  version\\n  nations(lang: $lang) {\\n    name\\n    title\\n    color\\n    icons {\\n      small\\n      tiny\\n      __typename\\n    }\\n    __typename\\n  }\\n  dogTagComponents(lang: $lang) {\\n    id\\n    type\\n    color\\n    isColorizable\\n    showClanTag\\n    clanTag {\\n      x\\n      y\\n      fontColor\\n      __typename\\n    }\\n    icons {\\n      medium\\n      __typename\\n    }\\n    textureData {\\n      id\\n      background {\\n        medium\\n        __typename\\n      }\\n      border {\\n        medium\\n        __typename\\n      }\\n      __typename\\n    }\\n    __typename\\n  }\\n  achievements(lang: $lang) {\\n    id\\n    icons {\\n      default\\n      inactive\\n      normal\\n      __typename\\n    }\\n    title\\n    description\\n    enabled\\n    multiple\\n    hidden\\n    typeTitle\\n    battleRestriction\\n    receivingRestriction\\n    battleTypes {\\n      id\\n      __typename\\n    }\\n    isProgress\\n    maxProgress\\n    grouping {\\n      sortOrder\\n      sortOrderInGroup\\n      group\\n      subgroup\\n      __typename\\n    }\\n    type\\n    __typename\\n  }\\n  nations(lang: $lang) {\\n    name\\n    title\\n    color\\n    __typename\\n  }\\n  vehicleTypes(lang: $lang) {\\n    name\\n    title\\n    icons {\\n      default\\n      special\\n      normal\\n      elite\\n      premium\\n      __typename\\n    }\\n    __typename\\n  }\\n  battleTypes(lang: $lang) {\\n    id\\n    icons {\\n      default\\n      __typename\\n    }\\n    title\\n    name\\n    dossierName\\n    __typename\\n  }\\n  achievementsGroups(lang: $lang) {\\n    id\\n    title\\n    sortOrder\\n    subgroups {\\n      id\\n      title\\n      sortOrder\\n      __typename\\n    }\\n    __typename\\n  }\\n  vehicles(lang: $lang) {\\n    isPremium\\n    isSpecial\\n    id\\n    title\\n    titleShort\\n    icons {\\n   large\\n  medium\\n   small\\n      __typename\\n    }\\n    type {\\n      name\\n      title\\n      titleShort\\n      __typename\\n    }\\n    level\\n    nation {\\n      name\\n      title\\n      color\\n      icons {\\n        small\\n        tiny\\n        __typename\\n      }\\n      __typename\\n    }\\n    nationName\\n    __typename\\n  }\\n}\\n"
                }
                """;
        JsonUtils json = new JsonUtils();
        try (HttpClient client = HttpClient.newHttpClient()) {
            var req = HttpRequest.newBuilder(URI.create(STR."\{server.vortex()}/api/graphql/glossary/"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body)).build();
            var resp = client.send(req, HttpResponse.BodyHandlers.ofByteArray());
            var jsonNode = json.parse(HttpCodec.response(resp));
            return new RequestVortexResourcesInfo(
                    vortexVehicles(json, jsonNode, language),
                    vortexDogTagComponents(jsonNode));
        }
    }

    private static List<VortexDogTagComponents> vortexDogTagComponents(JsonNode data) {
        List<VortexDogTagComponents> list = new LinkedList<>();
        var dogTagComponents = data.get("data").get("dogTagComponents");
        for (var tag : dogTagComponents) {
            VortexDogTagComponents components = new VortexDogTagComponents();
            components.setId(tag.get("id").asText());
            components.setType(tag.get("type").asText());
            components.setColor(tag.get("color").asText());
            components.setIsColorizable(tag.get("isColorizable").asBoolean());
            components.setShowClanTag(tag.get("showClanTag").asBoolean());
            components.setClanTag(VortexClanTag.parse(tag.get("clanTag")));
            components.setIcons(new WowsIcons(tag.get("icons").get("medium").asText("")));
            components.setTextureData(tag.get("textureData"));
            list.add(components);
        }
        return list;
    }

    private static List<VortexVehicles> vortexVehicles(JsonUtils json, JsonNode data, String language) throws JsonProcessingException {
        var vehicles = data.get("data").get("vehicles");
        return json.parse(vehicles.toString(), new TypeReference<List<VortexVehicles>>() {
                    @Override
                    public Type getType() {
                        return super.getType();
                    }
                })
                .stream().peek(x -> x.setLanguage(language)).toList();
    }
}
