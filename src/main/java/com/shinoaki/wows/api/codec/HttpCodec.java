package com.shinoaki.wows.api.codec;

import com.shinoaki.wows.api.error.BasicException;
import com.shinoaki.wows.api.error.HttpThrowableStatus;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.zip.GZIPInputStream;

/**
 * @author Xun
 * @date 2023/3/18 14:31 星期六
 */
public class HttpCodec {
    private HttpCodec() {

    }

    public static final String CONTENT_ENCODING = "Content-Encoding";

    public static HttpRequest request(URI uri) {
        return HttpRequest.newBuilder().uri(uri).setHeader("Accept-Encoding", "gzip, deflate, br")
                .setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.0.0 Safari/537.36")
                .setHeader("Sec-Ch-Ua", "\"Google Chrome\";v=\"125\", \"Chromium\";v=\"125\", \"Not.A/Brand\";v=\"24\"")
                .setHeader("Sec-Ch-Ua-Mobile", "?0")
                .setHeader("Sec-Ch-Ua-Platform", "\"Windows\"")
                .setHeader("Sec-Fetch-Mode", "navigate")
                .setHeader("Sec-Fetch-Dest", "document")
                .setHeader("Sec-Fetch-User", "?1")
                .setHeader("Sec-Fetch-Site", "none")
                .build();
    }

    public static CompletableFuture<HttpResponse<byte[]>> sendAsync(HttpClient client, HttpRequest request) {
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofByteArray());
    }

    public static HttpResponse<byte[]> send(HttpClient client, HttpRequest request) throws IOException, BasicException {
        try {
            return client.send(request, HttpResponse.BodyHandlers.ofByteArray());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BasicException(e);
        }
    }


    public static String fromDataAsString(Map<String, String> fromData) {
        StringBuilder builder = new StringBuilder();
        Charset charset = StandardCharsets.UTF_8;
        fromData.forEach((k, v) -> {
            if (!builder.isEmpty()) {
                builder.append("&");
            }
            builder.append(URLEncoder.encode(k, charset)).append("=").append(URLEncoder.encode(v, charset));
        });
        return builder.toString();
    }

    /**
     * url特殊字符解析
     *
     * @param s url信息
     * @return 结果
     */
    public static String encodeURIComponent(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8)
                .replace("+", "%20")
                .replace("%21", "!")
                .replace("%27", "'")
                .replace("%28", "(")
                .replace("%29", ")")
                .replace("%7E", "~");
    }

    public static String decodeURIComponent(String s) {
        return URLDecoder.decode(s, StandardCharsets.UTF_8);
    }


    public static String response(HttpResponse<byte[]> response) throws BasicException {
        try {
            if (response.statusCode() == 200) {
                if (response.headers().firstValue(HttpCodec.CONTENT_ENCODING).isPresent()) {
                    return new String(HttpCodec.unGzip(response.body()), StandardCharsets.UTF_8);
                }
                return new String(response.body(), StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            throw new BasicException(HttpThrowableStatus.HTTP_IO, e);
        }
        throw new BasicException(HttpThrowableStatus.HTTP_STATUS, "http状态码异常 code=" + response.statusCode());
    }

    public static byte[] unGzip(byte[] bytes) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        GZIPInputStream gzipInputStream = new GZIPInputStream(new ByteArrayInputStream(bytes));
        byte[] buffer = new byte[gzipInputStream.available()];
        int n;
        while ((n = gzipInputStream.read(buffer)) >= 0) {
            out.write(buffer, 0, n);
        }
        return out.toByteArray();
    }
}
