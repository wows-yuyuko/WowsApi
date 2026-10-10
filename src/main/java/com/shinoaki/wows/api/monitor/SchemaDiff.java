package com.shinoaki.wows.api.monitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 两次结构快照的差异。
 * <p>
 * 严重程度区分（用于日志里分级展示）：
 * <ul>
 *     <li>{@code removed} / {@code typeChanged} —— 疑似破坏性变更，需要关注；</li>
 *     <li>{@code added} —— 信息性，服务端新增字段，本库可能尚未支持。</li>
 * </ul>
 *
 * @param added       新增路径（形如 {@code data.newField}）
 * @param removed     消失路径
 * @param typeChanged 类型变化（形如 {@code data.level: number -> string}）
 * @author Xun
 */
public record SchemaDiff(List<String> added, List<String> removed, List<String> typeChanged) {

    public SchemaDiff {
        added = List.copyOf(added);
        removed = List.copyOf(removed);
        typeChanged = List.copyOf(typeChanged);
    }

    public static SchemaDiff empty() {
        return new SchemaDiff(List.of(), List.of(), List.of());
    }

    /**
     * 计算 base -> now 的结构差异。
     */
    public static SchemaDiff of(SchemaSnapshot base, SchemaSnapshot now) {
        List<String> added = new ArrayList<>();
        List<String> removed = new ArrayList<>();
        List<String> typeChanged = new ArrayList<>();

        for (Map.Entry<String, String> e : now.fields().entrySet()) {
            String oldType = base.fields().get(e.getKey());
            if (oldType == null) {
                added.add(e.getKey());
            } else if (!Objects.equals(oldType, e.getValue())) {
                typeChanged.add(e.getKey() + ": " + oldType + " -> " + e.getValue());
            }
        }
        for (String path : base.fields().keySet()) {
            if (!now.fields().containsKey(path)) {
                removed.add(path);
            }
        }
        return new SchemaDiff(added, removed, typeChanged);
    }

    /**
     * 是否存在疑似破坏性变更（字段消失或类型变化）。
     */
    public boolean hasBreaking() {
        return !removed.isEmpty() || !typeChanged.isEmpty();
    }

    public boolean isEmpty() {
        return added.isEmpty() && removed.isEmpty() && typeChanged.isEmpty();
    }

    public int total() {
        return added.size() + removed.size() + typeChanged.size();
    }
}
