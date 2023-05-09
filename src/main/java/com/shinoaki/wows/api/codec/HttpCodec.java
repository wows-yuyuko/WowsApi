package com.shinoaki.wows.api.codec;

import com.shinoaki.wows.api.error.HttpStatusException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * @author Xun
 * @date 2023/3/18 14:31 星期六
 */
public class HttpCodec {
    private HttpCodec() {

    }

    public static final String MARK = "?";
    public static final String CONTENT_ENCODING = "Content-Encoding";
    public static final String GZIP = "gzip";

    public static String fromDataAsString(Map<String, String> fromData) {
        StringBuilder builder = new StringBuilder();
        Charset charset = StandardCharsets.UTF_8;
        fromData.forEach((k, v) -> {
            if (builder.length() > 0) {
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

    /**
     * 解析获取url code
     *
     * @param url url code
     * @return code
     */
    public static String urlLastCode(String url) {
        //清理?后面的信息
        if (url.contains(MARK)) {
            url = url.substring(0, url.indexOf(MARK));
        }
        int i = url.lastIndexOf("/");
        if (i < (url.length() - 1)) {
            return url.substring(i + 1);
        }
        return urlLastCode(url.substring(0, i - 1));
    }

    /**
     * 移除?参数
     *
     * @param url 链接
     * @return 结果
     */
    public static String removeUrlParameter(String url) {
        if (url.contains(MARK)) {
            return url.substring(0, url.indexOf(MARK));
        }
        return url;
    }

    public static byte[] response(HttpResponse<byte[]> response) throws IOException, HttpStatusException {
        if (response.statusCode() == 200) {
            if (response.headers().firstValue(HttpCodec.CONTENT_ENCODING).isPresent()) {
                return HttpCodec.unGzip(response.body());
            }
            return response.body();
        }
        throw new HttpStatusException(response.statusCode());
    }

    public static String removeHttpHost(String url) {
        String replace = url.replace("http://", "").replace("https://", "");
        if (replace.contains("/")) {
            return replace.substring(0, replace.indexOf("/"));
        }
        return replace;
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
