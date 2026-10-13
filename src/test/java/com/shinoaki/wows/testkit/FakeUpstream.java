package com.shinoaki.wows.testkit;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

/**
 * 本地假上游（JDK 自带 {@code com.sun.net.httpserver}，<b>不打任何真实服务</b>）。
 * <p>
 * 为 vortex / clans / api 三个主机<b>各起一个服务器</b>：路由表按主机分开，
 * 因此"请求打到错误的主机/通道"会得到 404 并被测试暴露（如曾经把 vortex 主机上的
 * 所属军团接口误标成 clans 通道）。请求按到达顺序记录在共享的 {@link RequestLog}。
 *
 * @author Xun
 */
public final class FakeUpstream implements AutoCloseable {

    private final HttpServer vortexServer;
    private final HttpServer clansServer;
    private final HttpServer apiServer;

    private final FakeRouteTable vortex = new FakeRouteTable();
    private final FakeRouteTable clans = new FakeRouteTable();
    private final FakeRouteTable api = new FakeRouteTable();

    private final RequestLog log = new RequestLog();

    private FakeUpstream() throws IOException {
        vortexServer = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        clansServer = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        apiServer = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    }

    public static FakeUpstream start() throws IOException {
        FakeUpstream upstream = new FakeUpstream();
        upstream.vortexServer.createContext("/", exchange -> handle(exchange, upstream.vortex, upstream.log));
        upstream.clansServer.createContext("/", exchange -> handle(exchange, upstream.clans, upstream.log));
        upstream.apiServer.createContext("/", exchange -> handle(exchange, upstream.api, upstream.log));
        upstream.vortexServer.start();
        upstream.clansServer.start();
        upstream.apiServer.start();
        return upstream;
    }

    public FakeRouteTable vortex() {
        return vortex;
    }

    public FakeRouteTable clans() {
        return clans;
    }

    public FakeRouteTable api() {
        return api;
    }

    public String vortexBase() {
        return "http://127.0.0.1:" + vortexServer.getAddress().getPort();
    }

    public String clansBase() {
        return "http://127.0.0.1:" + clansServer.getAddress().getPort();
    }

    public String apiBase() {
        return "http://127.0.0.1:" + apiServer.getAddress().getPort();
    }

    public RequestLog requests() {
        return log;
    }

    @Override
    public void close() {
        vortexServer.stop(0);
        clansServer.stop(0);
        apiServer.stop(0);
    }

    private static void handle(HttpExchange exchange, FakeRouteTable routes, RequestLog log) throws IOException {
        String rawPath = exchange.getRequestURI().getRawPath();
        Map<String, String> query = parseQuery(exchange.getRequestURI().getRawQuery());
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        log.add(new RecordedRequest(exchange.getRequestMethod(), rawPath, query, exchange.getRequestHeaders(), body));
        FakeRouteTable.Canned canned = routes.find(rawPath, query, body);
        if (canned == null) {
            exchange.sendResponseHeaders(404, -1);
            exchange.close();
            return;
        }
        byte[] bytes = canned.body() == null ? new byte[0] : canned.body().getBytes(StandardCharsets.UTF_8);
        if (canned.gzip()) {
            bytes = gzip(bytes);
            exchange.getResponseHeaders().set("Content-Encoding", "gzip");
        }
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        if (canned.status() != 200) {
            exchange.sendResponseHeaders(canned.status(), -1);
            exchange.close();
            return;
        }
        exchange.sendResponseHeaders(200, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }

    private static Map<String, String> parseQuery(String rawQuery) {
        Map<String, String> query = new LinkedHashMap<>();
        if (rawQuery == null || rawQuery.isEmpty()) {
            return query;
        }
        for (String pair : rawQuery.split("&")) {
            if (pair.isEmpty()) {
                continue;
            }
            int eq = pair.indexOf('=');
            query.put(eq < 0 ? pair : pair.substring(0, eq), eq < 0 ? "" : pair.substring(eq + 1));
        }
        return query;
    }

    private static byte[] gzip(byte[] bytes) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (GZIPOutputStream gzip = new GZIPOutputStream(out)) {
            gzip.write(bytes);
        }
        return out.toByteArray();
    }
}
