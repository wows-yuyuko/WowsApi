package com.shinoaki.wows.api.util;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * URL 组件编码：{@code encodeURIComponent} 语义（与旧 {@code HttpCodec.encodeURIComponent} 逐字一致）。
 * 仅用于路径占位符与标记需编码的查询参数值；其它参数值原样拼接。
 *
 * @author Xun
 */
public final class UrlCodec {

    private UrlCodec() {
    }

    /**
     * 按 encodeURIComponent 语义编码：空格转 %20，保留 {@code ! ' ( ) ~} 不转义。
     */
    public static String encodeComponent(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8)
                .replace("+", "%20")
                .replace("%21", "!")
                .replace("%27", "'")
                .replace("%28", "(")
                .replace("%29", ")")
                .replace("%7E", "~");
    }
}
