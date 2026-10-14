package com.shinoaki.wows.api.transport;

import com.shinoaki.wows.api.error.BasicException;
import com.shinoaki.wows.api.error.HttpNetworkException;
import com.shinoaki.wows.api.error.HttpStatusException;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

/**
 * 响应解码：非 200 抛 {@link HttpStatusException}；gzip/deflate 解压 + UTF-8。
 * 不做字段漂移采集——采集挂钩由调用方在解码成功后触发。
 *
 * @author Xun
 */
@Slf4j
public final class ResponseCodec {

    private ResponseCodec() {
    }

    public static final String CONTENT_ENCODING = "Content-Encoding";

    /**
     * 校验状态码并解码响应体。
     *
     * @throws HttpStatusException  非 200
     * @throws HttpNetworkException 解压/读取失败
     */
    public static String decode(HttpResponse<byte[]> response) throws BasicException {
        try {
            if (response.statusCode() == 200) {
                var encoding = response.headers().firstValue(CONTENT_ENCODING).orElse("").toLowerCase(Locale.ROOT);
                if (encoding.contains("gzip")) {
                    return new String(unGzip(response.body()), StandardCharsets.UTF_8);
                }
                if (encoding.contains("deflate")) {
                    return new String(unDeflate(response.body()), StandardCharsets.UTF_8);
                }
                return new String(response.body(), StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            log.error("响应体处理异常！", e);
            throw new HttpNetworkException("响应体解压/读取失败", e);
        }
        throw new HttpStatusException(response.statusCode(), "http状态码异常 code=" + response.statusCode());
    }

    public static byte[] unGzip(byte[] bytes) throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             GZIPInputStream gzipInputStream = new GZIPInputStream(new ByteArrayInputStream(bytes))) {
            byte[] buffer = new byte[8192];
            int n;
            while ((n = gzipInputStream.read(buffer)) >= 0) {
                out.write(buffer, 0, n);
            }
            return out.toByteArray();
        }
    }

    public static byte[] unDeflate(byte[] bytes) throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             InflaterInputStream in = new InflaterInputStream(new ByteArrayInputStream(bytes))) {
            byte[] buffer = new byte[8192];
            int n;
            while ((n = in.read(buffer)) >= 0) {
                out.write(buffer, 0, n);
            }
            return out.toByteArray();
        }
    }
}
