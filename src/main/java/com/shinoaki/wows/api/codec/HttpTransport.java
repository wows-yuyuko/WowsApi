package com.shinoaki.wows.api.codec;

import com.shinoaki.wows.api.error.BasicException;
import com.shinoaki.wows.api.error.HttpThrowableStatus;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * 低层HTTP传输:{@link ApiHttp} 与 {@link VortexHttp} 共用的发送实现。
 * <p>
 * <b>Cookie 管理 + 重定向跟随按主机开关,默认仅对注册主机生效</b>,避免让暂不需要的服务器付出额外开销:
 * <ul>
 *     <li>注册主机(默认仅 {@code api.korabli.su},莱服官方API)——存在 {@code 307 + Set-Cookie: bp_chl=...}
 *     反爬握手:无 cookie 首次请求自动带上下发的 cookie 重放(最多{@value #MAX_REDIRECTS}次),
 *     进程内共享Cookie容器按域名隔离、线程安全;重放复用原请求头(仅去掉旧cookie),保证服务器指纹校验前后一致。
 *     并发安全:冷启动(无有效cookie)时同主机只允许一个线程发起 握手,其余线程等待后复用其cookie,避免并发307风暴;</li>
 *     <li>该WAF对并发突发返回429限流:自动按 Retry-After / 指数退避等待后重试,最多{@value #MAX_RATE_LIMIT_RETRIES}次;</li>
 *     <li>其它主机——普通快速通道,单次 {@code client.send} 直接返回,不做 cookie 存取、头拷贝或重定向处理。</li>
 * </ul>
 * 未来若其它服务器出现同样防护,调用 {@link #enableCookieRedirect(String)} 一行开启即可;
 * 不需要默认的莱服处理时可用 {@link #disableCookieRedirect(String)} 关闭。
 *
 * @author Xun
 */
@Slf4j
public final class HttpTransport {

    private HttpTransport() {
    }

    /**
     * 重定向最大跟随次数(单次send调用内,仅注册主机生效)。
     */
    private static final int MAX_REDIRECTS = 2;

    /**
     * 需要cookie管理+重定向跟随的主机集合(小写、不含端口与末尾点)。默认仅注册 api.korabli.su。
     */
    private static final Set<String> CHALLENGE_HOSTS = ConcurrentHashMap.newKeySet();

    static {
        CHALLENGE_HOSTS.add("api.korabli.su");
    }

    private static final CookieManager COOKIE_MANAGER = new CookieManager();

    /**
     * 429限流自动重试的最大次数(仅注册主机,0表示只请求一次就返回)。
     */
    private static final int MAX_RATE_LIMIT_RETRIES = 3;

    /**
     * 429重试单次最长等待(毫秒,也作为 Retry-After 的上限)。
     */
    private static final long MAX_RATE_LIMIT_SLEEP_MS = 5_000L;

    /**
     * 冷启动单飞用的每主机锁:同一主机没有有效cookie时,并发请求只让一个线程执行 握手,
     * 其余线程在锁上等待后复用其写入的cookie,避免并发307风暴触发限流。
     */
    private static final Map<String, Object> HOST_LOCKS = new ConcurrentHashMap<>();

    /**
     * 为指定主机启用cookie管理+重定向跟随(注册后,该主机的请求自动完成307 握手)。
     *
     * @param host 主机名,如 api.korabli.su
     */
    public static void enableCookieRedirect(String host) {
        CHALLENGE_HOSTS.add(normalizeHost(host));
    }

    /**
     * 取消指定主机的cookie管理+重定向跟随(默认注册的 api.korabli.su 也可取消)。
     */
    public static void disableCookieRedirect(String host) {
        CHALLENGE_HOSTS.remove(normalizeHost(host));
    }

    /**
     * 同步发送请求并返回最终响应。
     * <p>
     * 注册主机:自动附带该主机已保存的cookie -> 发送 -> 保存响应下发的cookie ->
     * 若为可跟随的重定向(301/302/303/307/308)则按规则重新请求(307/308保持方法,其余转GET),最多{@value #MAX_REDIRECTS}次;
     * 其它主机:普通发送直接返回。
     *
     * @param client  执行请求的HttpClient(无需自行配置cookie handler或跟随重定向)
     * @param request 请求
     * @return 最终响应
     * @throws BasicException 网络异常或重定向次数超限
     */
    public static HttpResponse<byte[]> send(HttpClient client, HttpRequest request) throws BasicException {
        if (needsCookieRedirect(request.uri())) {
            return sendWithCookieRedirect(client, request);
        }
        return sendPlain(client, request);
    }

    /**
     * 异步发送请求,分发规则与{@link #send(HttpClient, HttpRequest)}一致。
     */
    public static CompletableFuture<HttpResponse<byte[]>> sendAsync(HttpClient client, HttpRequest request) {
        if (needsCookieRedirect(request.uri())) {
            return sendWithCookieRedirectAsync(client, request, 0, 0);
        }
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofByteArray());
    }

    private static boolean needsCookieRedirect(URI uri) {
        String host = uri.getHost();
        return host != null && CHALLENGE_HOSTS.contains(normalizeHost(host));
    }

    private static String normalizeHost(String host) {
        String h = host == null ? "" : host.toLowerCase(Locale.ROOT).trim();
        return h.endsWith(".") ? h.substring(0, h.length() - 1) : h;
    }

    /**
     * 普通发送(非注册主机,零cookie/重定向开销)。
     */
    private static HttpResponse<byte[]> sendPlain(HttpClient client, HttpRequest request) throws BasicException {
        try {
            return client.send(request, HttpResponse.BodyHandlers.ofByteArray());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BasicException(e);
        } catch (IOException e) {
            log.error("网络请求异常！", e);
            throw new BasicException(HttpThrowableStatus.HTTP_IO, e);
        }
    }

    /**
     * 带cookie管理+重定向跟随的发送入口(仅注册主机)。
     * <p>
     * 冷启动单飞:该主机还没有有效cookie时,并发请求先在主机锁上排队,
     * 只有拿到锁的线程真正执行 握手;成功后其余线程直接复用cookie单跳返回。
     */
    private static HttpResponse<byte[]> sendWithCookieRedirect(HttpClient client, HttpRequest request) throws BasicException {
        String host = normalizeHost(request.uri().getHost());
        if (!host.isEmpty() && !hasValidCookie(request.uri())) {
            synchronized (hostLock(host)) {
                if (!hasValidCookie(request.uri())) {
                    return sendWithCookieRedirectLoop(client, request);
                }
            }
        }
        return sendWithCookieRedirectLoop(client, request);
    }

    /**
     * 带cookie管理+重定向跟随的发送循环(仅注册主机)。
     * 附加429限流处理:遇429按 Retry-After/退避等待后重发,最多{@value #MAX_RATE_LIMIT_RETRIES}次。
     */
    private static HttpResponse<byte[]> sendWithCookieRedirectLoop(HttpClient client, HttpRequest request) throws BasicException {
        try {
            HttpRequest current = request;
            int redirects = 0;
            int rateLimitRetry = 0;
            while (true) {
                current = attachCookie(current);
                HttpResponse<byte[]> response = client.send(current, HttpResponse.BodyHandlers.ofByteArray());
                storeCookie(current.uri(), response);
                int code = response.statusCode();
                if (code == 429 && rateLimitRetry < MAX_RATE_LIMIT_RETRIES) {
                    rateLimitRetry++;
                    Thread.sleep(rateLimitDelayMillis(rateLimitRetry, response.headers().firstValue("Retry-After").orElse(null)));
                    continue;
                }
                if (!isRedirect(response)) {
                    return response;
                }
                if (redirects >= MAX_REDIRECTS) {
                    throw new BasicException(HttpThrowableStatus.HTTP_STATUS, "http 重定向次数超过上限(" + MAX_REDIRECTS + "),已终止。最近一次 code="
                            + response.statusCode() + ", location=" + response.headers().firstValue("Location").orElse(""));
                }
                redirects++;
                current = follow(current, response);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BasicException(e);
        } catch (IOException e) {
            log.error("网络请求异常！", e);
            throw new BasicException(HttpThrowableStatus.HTTP_IO, e);
        }
    }

    /**
     * 异步发送(仅注册主机,含429退避;冷启动单飞仅作用于同步路径,异步调用方自行控制并发即可)。
     */
    private static CompletableFuture<HttpResponse<byte[]>> sendWithCookieRedirectAsync(HttpClient client, HttpRequest request, int redirects, int rateLimitRetry) {
        HttpRequest current = attachCookie(request);
        return client.sendAsync(current, HttpResponse.BodyHandlers.ofByteArray()).thenCompose(response -> {
            storeCookie(current.uri(), response);
            int code = response.statusCode();
            if (code == 429 && rateLimitRetry < MAX_RATE_LIMIT_RETRIES) {
                long delay = rateLimitDelayMillis(rateLimitRetry + 1, response.headers().firstValue("Retry-After").orElse(null));
                return CompletableFuture.supplyAsync(() -> null, CompletableFuture.delayedExecutor(delay, TimeUnit.MILLISECONDS))
                        .thenCompose(v -> sendWithCookieRedirectAsync(client, current, redirects, rateLimitRetry + 1));
            }
            if (!isRedirect(response) || redirects >= MAX_REDIRECTS) {
                return CompletableFuture.completedFuture(response);
            }
            try {
                return sendWithCookieRedirectAsync(client, follow(current, response), redirects + 1, rateLimitRetry);
            } catch (BasicException e) {
                return CompletableFuture.failedFuture(e);
            }
        });
    }

    private static Object hostLock(String host) {
        return HOST_LOCKS.computeIfAbsent(host, k -> new Object());
    }

    private static boolean hasValidCookie(URI uri) {
        return !validCookies(uri).isEmpty();
    }

    /**
     * 计算429重试等待时长(毫秒)。有 Retry-After 秒数时以其为准(上限{@value #MAX_RATE_LIMIT_SLEEP_MS}ms);
     * 否则按 200ms 起步翻倍(上限1s)并附加随机抖动,避免重试再次撞在同一时间点。
     */
    private static long rateLimitDelayMillis(int attempt, String retryAfter) {
        long millis = 0;
        if (retryAfter != null) {
            try {
                millis = Math.min(Long.parseLong(retryAfter.trim()) * 1000L, MAX_RATE_LIMIT_SLEEP_MS);
            } catch (NumberFormatException ignore) {
                // Retry-After可能是HTTP日期等格式,无法解析时退回退避策略
            }
        }
        if (millis <= 0) {
            long base = Math.min(200L << (attempt - 1), 1000L);
            millis = base + ThreadLocalRandom.current().nextLong(0, 150);
        }
        return millis;
    }

    /**
     * 请求头中显式携带了Cookie时不覆盖;否则从共享Cookie容器中取出该主机未过期的cookie附加到请求。
     */
    private static HttpRequest attachCookie(HttpRequest request) {
        if (request.headers().firstValue("Cookie").isPresent()) {
            return request;
        }
        List<String> cookies = validCookies(request.uri());
        if (cookies.isEmpty()) {
            return request;
        }
        return HttpRequest.newBuilder(request, (name, value) -> true)
                .setHeader("Cookie", String.join("; ", cookies))
                .build();
    }

    private static List<String> validCookies(URI uri) {
        List<String> result = new ArrayList<>();
        try {
            var cookieHeader = COOKIE_MANAGER.get(uri, Map.of()).get("Cookie");
            if (cookieHeader == null) {
                return result;
            }
            for (String line : cookieHeader) {
                for (HttpCookie cookie : HttpCookie.parse(line)) {
                    if (!cookie.hasExpired()) {
                        result.add(cookie.getName() + "=" + cookie.getValue());
                    }
                }
            }
        } catch (IOException e) {
            log.warn("读取cookie失败 uri={}", uri, e);
        }
        return result;
    }

    /**
     * 把响应中的 Set-Cookie 保存到共享Cookie容器。CookieManager会按域名、路径与有效期规则管理。
     */
    private static void storeCookie(URI uri, HttpResponse<?> response) {
        try {
            COOKIE_MANAGER.put(uri, response.headers().map());
        } catch (IOException e) {
            log.warn("保存cookie失败 uri={}", uri, e);
        }
    }

    private static boolean isRedirect(HttpResponse<?> response) {
        if (response.statusCode() != 301 && response.statusCode() != 302
                && response.statusCode() != 303 && response.statusCode() != 307 && response.statusCode() != 308) {
            return false;
        }
        return response.headers().firstValue("Location").isPresent();
    }

    /**
     * 按重定向语义构造下一个请求。
     * 307/308 保持原方法与请求体(同时去掉已注入的Cookie,由下一轮重新挂载);
     * 301/302/303 统一转为GET(当前库内请求均为GET,兼容未来POST场景)。
     */
    private static HttpRequest follow(HttpRequest from, HttpResponse<?> response) throws BasicException {
        URI location = from.uri().resolve(response.headers().firstValue("Location")
                .orElseThrow(() -> new BasicException(HttpThrowableStatus.HTTP_STATUS, "重定向响应缺少Location头")));
        int code = response.statusCode();
        if (code == 307 || code == 308) {
            return HttpRequest.newBuilder(from, (name, value) -> !name.equalsIgnoreCase("Cookie"))
                    .uri(location)
                    .build();
        }
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(location)
                .timeout(from.timeout().orElse(Duration.ofSeconds(HttpCodec.requestTimeout())))
                .GET();
        from.headers().map().forEach((name, values) -> {
            if (GET_CONVERSION_DROP_HEADERS.contains(name.toLowerCase(Locale.ROOT))) {
                return;
            }
            values.forEach(value -> builder.header(name, value));
        });
        return builder.build();
    }

    /**
     * 301/302/303 转GET时需要丢弃的与请求体/会话相关的头。
     */
    private static final Set<String> GET_CONVERSION_DROP_HEADERS = Set.of(
            "cookie", "content-length", "content-type", "transfer-encoding", "host", "connection");
}
