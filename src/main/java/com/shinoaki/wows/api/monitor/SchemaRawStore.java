package com.shinoaki.wows.api.monitor;

import com.shinoaki.wows.api.utils.JsonUtils;
import tools.jackson.databind.JsonNode;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 原始响应体仓库：把采集到的响应按 schemaKey 落盘，供离线回放（不打真实服务）。
 * <p>
 * 回放的价值：CI 里没有网络、也不该反复打别人的接口，用一份历史响应重放即可验证
 * 「采集 -> 比对 -> 告警」这条链路，同时保证同一天的重复运行结果稳定（不影响基线）。
 * <p>
 * 目录结构（与 {@link SchemaBaseline} 同名的转义规则）：
 * <pre>
 * dir/index.json                     所有已采集的 schemaKey 列表
 * dir/vortex_asia_api_accounts_id_.json
 * </pre>
 *
 * @author Xun
 */
public final class SchemaRawStore {

    private static final String INDEX = "index.json";

    private final Path dir;

    public SchemaRawStore(Path dir) {
        this.dir = dir;
    }

    public Path dir() {
        return dir;
    }

    public Path file(String schemaKey) {
        return dir.resolve(SchemaBaseline.fileName(schemaKey));
    }

    /**
     * 保存一次原始响应，并把 schemaKey 登记进索引。
     */
    public void save(String schemaKey, String json) {
        try {
            Files.createDirectories(dir);
            Files.writeString(file(schemaKey), json, StandardCharsets.UTF_8);
            Set<String> keys = new LinkedHashSet<>(keys());
            if (keys.add(schemaKey)) {
                writeIndex(keys);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * @return 已保存的原始响应；不存在返回 null
     */
    public String load(String schemaKey) {
        Path file = file(schemaKey);
        if (!Files.isRegularFile(file)) {
            return null;
        }
        try {
            return Files.readString(file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * @return 索引中登记的 schemaKey（按首次采集顺序）
     */
    public List<String> keys() {
        Path index = dir.resolve(INDEX);
        if (!Files.isRegularFile(index)) {
            return List.of();
        }
        try {
            JsonNode node = JsonUtils.json().parse(index.toFile());
            List<String> keys = new ArrayList<>();
            for (JsonNode key : node.path("schemaKeys")) {
                keys.add(key.asString(""));
            }
            return keys;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * 删除本次没有采集到的原始响应（接口改名/归一规则调整后留下的孤儿文件）。
     *
     * @param keys 本次实际采集到的 schemaKey
     */
    public void prune(Set<String> keys) {
        Set<String> stale = new LinkedHashSet<>(keys());
        stale.removeAll(keys);
        if (stale.isEmpty()) {
            return;
        }
        try {
            for (String key : stale) {
                Files.deleteIfExists(file(key));
            }
            writeIndex(new LinkedHashSet<>(keys));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void writeIndex(Set<String> keys) throws IOException {
        JsonUtils.json().toJsonPrettyPrinter(dir.resolve(INDEX).toFile(), java.util.Map.of("schemaKeys", keys));
    }
}
