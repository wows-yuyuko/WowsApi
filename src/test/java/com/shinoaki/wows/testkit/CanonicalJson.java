package com.shinoaki.wows.testkit;

import com.shinoaki.wows.api.utils.JsonUtils;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.List;

/**
 * 测试用 JSON 规范化（新旧对比的裁判口径）：
 * <ul>
 *     <li>剔除顶层 {@code recordTime}（解析时刻字段，两次解析必然不同）；</li>
 *     <li>递归排序对象键——服务端对同一资源的对象键序可能不稳定（实测军团详情 {@code stats} 两次请求间
 *     {@code {"cvc":0,"pvp":973}} / {@code {"pvp":973,"cvc":0}} 互换），JSON 对象键序无语义；</li>
 *     <li>数组按「元素规范化串」排序后比较——<b>多重集语义</b>：服务端对同一资源可能在两次响应间
 *     重排数组（实测资源索引 getGlossData 的 {@code achievements} 两次请求顺序互换、内容相同），
 *     而新旧两侧使用同一套解析器、不会改变数组顺序，因此在线对比只关心"内容集合"。
 *     需要保序的严格对比由离线测试（固定字节回放）承担。</li>
 * </ul>
 * 无法解析时原样返回（交给上层比较报差异）。
 *
 * @author Xun
 */
public final class CanonicalJson {

    private CanonicalJson() {
    }

    public static String of(String json) {
        try {
            JsonNode node = JsonUtils.json().parse(json);
            if (node.isObject() && node.has("recordTime")) {
                ((ObjectNode) node).remove("recordTime");
            }
            StringBuilder sb = new StringBuilder(json.length());
            append(node, sb);
            return sb.toString();
        } catch (Exception e) {
            return json;
        }
    }

    private static void append(JsonNode node, StringBuilder sb) {
        if (node.isObject()) {
            List<String> keys = new ArrayList<>();
            for (var entry : node.properties()) {
                keys.add(entry.getKey());
            }
            keys.sort(null);
            sb.append('{');
            for (int i = 0; i < keys.size(); i++) {
                if (i > 0) {
                    sb.append(',');
                }
                sb.append('"').append(keys.get(i)).append("\":");
                append(node.get(keys.get(i)), sb);
            }
            sb.append('}');
        } else if (node.isArray()) {
            List<String> elements = new ArrayList<>();
            for (JsonNode child : node) {
                StringBuilder element = new StringBuilder();
                append(child, element);
                elements.add(element.toString());
            }
            elements.sort(null);
            sb.append('[');
            for (int i = 0; i < elements.size(); i++) {
                if (i > 0) {
                    sb.append(',');
                }
                sb.append(elements.get(i));
            }
            sb.append(']');
        } else {
            sb.append(node.toString());
        }
    }
}
