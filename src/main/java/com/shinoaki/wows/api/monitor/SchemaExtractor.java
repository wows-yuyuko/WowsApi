package com.shinoaki.wows.api.monitor;

import tools.jackson.databind.JsonNode;

import java.time.Instant;
import java.util.Map;
import java.util.TreeMap;

/**
 * 响应 JSON -> {@link SchemaSnapshot} 的采集器（纯观测，不修改任何节点）。
 * <p>
 * 规则：
 * <ul>
 *     <li>对象：展开为 {@code a.b}；</li>
 *     <li>数组：记为 {@code a[]}，并对所有元素递归取并集（不记录下标）；</li>
 *     <li>叶子：记录 JSON 类型；{@code number} 不再细分整型/浮点，避免 1 与 1.0 抖动误报；</li>
 *     <li>{@code null} 记为 {@code null}，仅作可空提示，不参与告警（服务端 null 抖动频繁）。</li>
 * </ul>
 *
 * @author Xun
 */
public final class SchemaExtractor {

    public static final String TYPE_STRING = "string";
    public static final String TYPE_NUMBER = "number";
    public static final String TYPE_BOOLEAN = "boolean";
    public static final String TYPE_OBJECT = "object";
    public static final String TYPE_ARRAY = "array";
    public static final String TYPE_NULL = "null";
    public static final String TYPE_UNKNOWN = "unknown";

    private SchemaExtractor() {
    }

    public static SchemaSnapshot extract(String schemaKey, JsonNode root) {
        return extract(schemaKey, root, SchemaRules.defaults());
    }

    public static SchemaSnapshot extract(String schemaKey, JsonNode root, SchemaRules rules) {
        Map<String, String> fields = new TreeMap<>();
        walk(root, "", fields, rules);
        return new SchemaSnapshot(schemaKey, Instant.now().toString(), fields);
    }

    private static void walk(JsonNode node, String prefix, Map<String, String> out, SchemaRules rules) {
        if (node == null || node.isMissingNode()) {
            return;
        }
        if (node.isObject()) {
            if (!prefix.isEmpty()) {
                out.put(prefix, TYPE_OBJECT);
            }
            for (Map.Entry<String, JsonNode> e : node.properties()) {
                String path = SchemaPaths.join(prefix, rules.normalizeKey(e.getKey()));
                if (rules.ignore(path)) {
                    continue;
                }
                walk(e.getValue(), path, out, rules);
            }
            return;
        }
        if (node.isArray()) {
            String path = SchemaPaths.array(prefix);
            out.put(path, TYPE_ARRAY);
            for (JsonNode element : node) {
                walk(element, path, out, rules);
            }
            return;
        }
        if (node.isNull()) {
            out.put(prefix, TYPE_NULL);
            return;
        }
        out.put(prefix, typeOf(node));
    }

    private static String typeOf(JsonNode node) {
        if (node.isString()) {
            return TYPE_STRING;
        }
        if (node.isNumber()) {
            return TYPE_NUMBER;
        }
        if (node.isBoolean()) {
            return TYPE_BOOLEAN;
        }
        if (node.isBinary()) {
            return "binary";
        }
        return TYPE_UNKNOWN;
    }
}
