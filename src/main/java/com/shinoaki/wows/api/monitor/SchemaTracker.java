package com.shinoaki.wows.api.monitor;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.JsonNodeFactory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 精确模式入口：把响应树包裹成「会登记读取路径」的树，交给现有解析器跑一遍即可得到消费路径。
 * <p>
 * 用法（<b>不需要修改任何解析器</b>）：
 * <pre>{@code
 * Set<String> reads = new LinkedHashSet<>();
 * JsonNode tracked = SchemaTracker.track(json, reads);
 * WowsLootBox.parse(tracked);   // 真实解析器照常运行，读取的路径被记录下来
 * // reads 经 SchemaRules.normalizePath 归一后即可与采集快照比较
 * }</pre>
 *
 * @author Xun
 */
public final class SchemaTracker {

    private SchemaTracker() {
    }

    /**
     * @param root  原始响应树
     * @param reads 输出：解析器实际读取的（原始）路径集合
     * @return 可观测的替换树（原始 {@code root} 不被修改）
     */
    public static JsonNode track(JsonNode root, Set<String> reads) {
        return wrap(root, "", reads);
    }

    private static JsonNode wrap(JsonNode node, String prefix, Set<String> reads) {
        if (node == null) {
            return null;
        }
        if (node.isObject()) {
            Map<String, JsonNode> children = new LinkedHashMap<>();
            for (Map.Entry<String, JsonNode> e : node.properties()) {
                children.put(e.getKey(), wrap(e.getValue(), SchemaPaths.join(prefix, e.getKey()), reads));
            }
            return new TrackingObjectNode(JsonNodeFactory.instance, prefix, reads, children);
        }
        if (node.isArray()) {
            String path = SchemaPaths.array(prefix);
            List<JsonNode> children = new ArrayList<>();
            for (JsonNode element : node) {
                children.add(wrap(element, path, reads));
            }
            return new ArrayNode(JsonNodeFactory.instance, children);
        }
        return node;
    }
}
