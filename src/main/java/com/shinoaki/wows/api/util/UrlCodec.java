package com.shinoaki.wows.api.util;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * URL 组件编码（3.0 重写）：{@code encodeURIComponent} 语义，与旧 {@code HttpCodec.encodeURIComponent} 逐字一致。
 * <p>
 * 用途：路径占位符（如搜索词）与标记为"需编码"的查询参数值。
 * 未标记的参数值（extra 串、语言、数字 ID 等）原样拼接，不做编码——与旧实现行为一致。
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
