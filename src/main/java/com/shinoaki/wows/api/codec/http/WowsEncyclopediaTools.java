package com.shinoaki.wows.api.codec.http;

import com.shinoaki.wows.api.codec.HttpCodec;
import com.shinoaki.wows.api.developers.encyclopedia.glossary.DevelopersGlossary;
import com.shinoaki.wows.api.error.BasicException;
import com.shinoaki.wows.api.type.WowsServer;

import java.net.URI;
import java.net.http.HttpClient;

/**
 * 百科全书查询
 * <p>
 * 说明：本类只提供同步方法；需要异步时建议使用线程池或JDK21虚拟线程自行包装（参考README）。
 *
 * @author Xun
 */
public record WowsEncyclopediaTools(HttpClient httpClient, WowsServer server) {

    public Developers developers(String token) {
        return new Developers(httpClient, server, token);
    }

    public Vortex vortex() {
        return new Vortex(httpClient, server);
    }


    public record Developers(HttpClient httpClient, WowsServer server, String token) {
        public DevelopersGlossary glossary() throws BasicException {
            return DevelopersGlossary.parse(HttpCodec.response(HttpCodec.send(httpClient, HttpCodec.request(glossaryUri()))));
        }

        public URI glossaryUri() {
            if (server() == WowsServer.RU) {
                return URI.create(server().api() + "/mk/clans/glossary/?application_id=" + token);
            } else {
                return URI.create(server().api() + "/wows/clans/glossary/?application_id=" + token);
            }
        }
    }

    public record Vortex(HttpClient httpClient, WowsServer server) {

    }
}
