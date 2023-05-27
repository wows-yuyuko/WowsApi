package com.shinoaki.wows.api.codec;

import com.shinoaki.wows.api.error.HttpStatusException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

/**
 * @author Xun
 * @date 2023/5/22 22:38 星期一
 */
public class HttpSend {
    private HttpSend() {

    }

    public static String sendGet(HttpClient client, URI uri) throws IOException, InterruptedException, HttpStatusException {
        return new String(HttpCodec.response(client.send(HttpCodec.request(uri), HttpResponse.BodyHandlers.ofByteArray())), StandardCharsets.UTF_8);
    }

    public static CompletableFuture<HttpResponse<byte[]>> sendGetAsync(HttpClient client, URI uri) {
        return client.sendAsync(HttpCodec.request(uri), HttpResponse.BodyHandlers.ofByteArray());
    }
}
