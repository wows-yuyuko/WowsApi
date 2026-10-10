package com.shinoaki.wows.api.monitor;

import com.shinoaki.wows.api.utils.JsonUtils;
import tools.jackson.databind.JsonNode;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * 结构快照的落盘与读取（baseline 目录，通常纳入 git 作为「接口契约」）。
 * <p>
 * 文件名由 schemaKey 转义而来，例如
 * {@code vortex:/api/accounts/{id}/ -> vortex_api_accounts_id_.json}。
 *
 * @author Xun
 */
public final class SchemaBaseline {

    private final Path dir;

    public SchemaBaseline(Path dir) {
        this.dir = dir;
    }

    public Path dir() {
        return dir;
    }

    public Path file(String schemaKey) {
        return dir.resolve(fileName(schemaKey));
    }

    /**
     * @return 已存在的快照；不存在返回 null
     */
    public SchemaSnapshot load(String schemaKey) {
        Path file = file(schemaKey);
        if (!Files.isRegularFile(file)) {
            return null;
        }
        try {
            JsonNode node = JsonUtils.json().parse(file.toFile());
            Map<String, String> fields = new TreeMap<>();
            for (Map.Entry<String, JsonNode> e : node.path("fields").properties()) {
                fields.put(e.getKey(), e.getValue().asString(""));
            }
            return new SchemaSnapshot(node.path("schemaKey").asString(schemaKey), node.path("capturedAt").asString(""), fields);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void save(SchemaSnapshot snapshot) {
        try {
            Files.createDirectories(dir);
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("schemaKey", snapshot.schemaKey());
            out.put("capturedAt", snapshot.capturedAt());
            out.put("fields", snapshot.fields());
            JsonUtils.json().toJsonPrettyPrinter(file(snapshot.schemaKey()).toFile(), out);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * schemaKey -> 文件名。
     * <p>
     * 部分接口的 key 很长（例如官方 account/info 的 {@code extra} 参数列了十几个数据块），
     * Windows 的路径长度限制会让长文件名直接写不进去；超长时截断并追加 key 的稳定哈希，
     * 既避开限制又不丢唯一性（{@link String#hashCode()} 的算法由规范固定，跨 JVM 稳定）。
     */
    public static String fileName(String schemaKey) {
        String sanitized = schemaKey.replaceAll("[^A-Za-z0-9]+", "_");
        if (sanitized.length() <= MAX_NAME) {
            return sanitized + ".json";
        }
        return sanitized.substring(0, MAX_NAME) + "_" + Integer.toHexString(schemaKey.hashCode()) + ".json";
    }

    /**
     * 文件名（不含扩展名）的最大长度。
     */
    private static final int MAX_NAME = 64;
}
