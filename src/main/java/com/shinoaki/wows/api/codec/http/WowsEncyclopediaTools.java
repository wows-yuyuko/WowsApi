package com.shinoaki.wows.api.codec.http;

import com.shinoaki.wows.api.codec.HttpCodec;
import com.shinoaki.wows.api.developers.encyclopedia.glossary.DevelopersGlossary;
import com.shinoaki.wows.api.error.BasicException;
import com.shinoaki.wows.api.error.CompletableInfo;
import com.shinoaki.wows.api.type.WowsServer;
import com.shinoaki.wows.api.utils.WowsJsonUtils;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.util.concurrent.CompletableFuture;

/**
 * @author Xun
 */
public record WowsEncyclopediaTools(HttpClient httpClient, WowsServer server) {

    public Developers developers(String token) {
        return new Developers(new WowsJsonUtils(), httpClient, server, token);
    }

    public Vortex vortex() {
        return new Vortex(new WowsJsonUtils(), httpClient, server);
    }


    public record Developers(WowsJsonUtils utils, HttpClient httpClient, WowsServer server, String token) {
        public CompletableFuture<CompletableInfo<DevelopersGlossary>> glossaryAsync() {
            return HttpCodec.sendAsync(httpClient, HttpCodec.request(glossaryUri())).thenApplyAsync(data -> {
                try {
                    return CompletableInfo.ok(DevelopersGlossary.parse(utils, HttpCodec.response(data)));
                } catch (BasicException e) {
                    return CompletableInfo.error(e);
                }
            });
        }

        public DevelopersGlossary glossary() throws IOException, BasicException {
            return DevelopersGlossary.parse(utils, HttpCodec.response(HttpCodec.send(httpClient, HttpCodec.request(glossaryUri()))));
        }

        public URI glossaryUri() {
            return URI.create(server().api() + "/wows/clans/glossary/?application_id=" + token);
        }
    }

    public record Vortex(WowsJsonUtils utils, HttpClient httpClient, WowsServer server) {

    }
}
