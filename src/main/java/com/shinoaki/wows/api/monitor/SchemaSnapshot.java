package com.shinoaki.wows.api.monitor;

import java.util.Map;
import java.util.TreeMap;

/**
 * 某个接口的「结构快照」：归一化路径 -> JSON 类型。
 * <p>
 * 用 TreeMap 保证序列化后键有序，便于入库 git 后人工 review 与稳定 diff。
 *
 * @param schemaKey   接口标识
 * @param capturedAt  采集时间
 * @param fields      路径 -> 类型（string/number/boolean/object/array/null）
 * @author Xun
 */
public record SchemaSnapshot(String schemaKey, String capturedAt, Map<String, String> fields) {

    public SchemaSnapshot(String schemaKey, String capturedAt, Map<String, String> fields) {
        this.schemaKey = schemaKey;
        this.capturedAt = capturedAt;
        this.fields = fields == null ? new TreeMap<>() : new TreeMap<>(fields);
    }

    public int size() {
        return fields.size();
    }

    public boolean has(String path) {
        return fields.containsKey(path);
    }
}
