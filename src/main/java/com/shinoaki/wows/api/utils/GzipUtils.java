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
        try (ByteArrayOutputStream out = new ByteArrayOutputStream(); GZIPOutputStream gzip = new GZIPOutputStream(out)) {
            gzip.write(str.getBytes(encoding));
            return out.toByteArray();
        } catch (IOException e) {
            log.error("gzip压缩异常", e);
            throw new IllegalStateException("gzip压缩失败", e);
        }
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
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             GZIPInputStream ungzip = new GZIPInputStream(new ByteArrayInputStream(bytes))) {
            byte[] buffer = new byte[8192];
            int n;
            while ((n = ungzip.read(buffer)) >= 0) {
                out.write(buffer, 0, n);
            }
            return out.toByteArray();
        } catch (IOException e) {
            log.error("gzip解压异常", e);
            throw new IllegalStateException("gzip解压失败", e);
        }
    }
}
