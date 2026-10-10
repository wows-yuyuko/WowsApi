package com.shinoaki.wows.api.monitor;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * 「精确模式」用的可观测对象节点：解析器每次 {@code path()/get()/properties()} 都会被登记。
 * <p>
 * 只在监视测试里临时包裹整棵响应树使用，不参与正常业务流程。
 * 数组元素在构建时已包裹，因此即使 {@code iterator()} 是 final 也能覆盖到遍历读取。
 *
 * @author Xun
 */
public final class TrackingObjectNode extends ObjectNode {

    private final String prefix;
    private final Set<String> reads;

    TrackingObjectNode(JsonNodeFactory nodeFactory, String prefix, Set<String> reads, Map<String, JsonNode> children) {
        super(nodeFactory, children);
        this.prefix = prefix;
        this.reads = reads;
    }

    @Override
    public JsonNode path(String name) {
        reads.add(SchemaPaths.join(prefix, name));
        return super.path(name);
    }

    @Override
    public JsonNode get(String name) {
        reads.add(SchemaPaths.join(prefix, name));
        return super.get(name);
    }

    /**
     * 记录 {@code prefix.*} 表示「整个对象被遍历」，并把子节点（已在构建时包裹）暴露出去，
     * 这样 {@code for (var e : node.properties())} 后续的 path 读取依然会被登记。
     */
    @Override
    public Set<Map.Entry<String, JsonNode>> properties() {
        if (!prefix.isEmpty()) {
            reads.add(prefix + ".*");
        }
        Set<Map.Entry<String, JsonNode>> wrapped = new LinkedHashSet<>();
        for (Map.Entry<String, JsonNode> e : super.properties()) {
            wrapped.add(Map.entry(e.getKey(), e.getValue()));
        }
        return wrapped;
    }
}
