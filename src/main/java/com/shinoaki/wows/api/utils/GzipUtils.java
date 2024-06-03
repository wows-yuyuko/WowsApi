package com.shinoaki.wows.api.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * @author Xun
 * @date 2023/4/3 18:02 星期一
 */
@Slf4j
public class GzipUtils {
    private GzipUtils() {

    }

    public static byte[] compress(String data) {
        return compress(data, StandardCharsets.UTF_8.name());
    }

    public static byte[] compress(String str, String encoding) {
        if (str == null || str.isEmpty()) {
            return new byte[0];
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        GZIPOutputStream gzip;
        try {
            gzip = new GZIPOutputStream(out);
            gzip.write(str.getBytes(encoding));
            gzip.close();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return out.toByteArray();
    }

    public static byte[] compress(byte[] data) throws IOException {
        if (data == null || data.length == 0) {
            return new byte[0];
        }
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            try (GZIPOutputStream gzip = new GZIPOutputStream(out)) {
                gzip.write(data);
            }
            return out.toByteArray();
        }
    }

    public static byte[] uncompress(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return new byte[0];
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        try {
            GZIPInputStream ungzip = new GZIPInputStream(in);
            byte[] buffer = new byte[256];
            int n;
            while ((n = ungzip.read(buffer)) >= 0) {
                out.write(buffer, 0, n);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return out.toByteArray();
    }
}
