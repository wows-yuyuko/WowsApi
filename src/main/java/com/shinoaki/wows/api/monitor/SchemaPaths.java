package com.shinoaki.wows.api.monitor;

/**
 * 结构路径拼接工具：统一「对象字段用 . 连接、数组用 [] 表示」的规范。
 * <p>
 * 例：{@code join("data", "name") -> "data.name"}；{@code array("rewards") -> "rewards[]"}。
 *
 * @author Xun
 */
public final class SchemaPaths {

    /**
     * 数组标记，不记录下标（下标变化不代表结构变化）。
     */
    public static final String ARRAY = "[]";

    private SchemaPaths() {
    }

    public static String join(String prefix, String name) {
        return prefix == null || prefix.isEmpty() ? name : prefix + "." + name;
    }

    public static String array(String prefix) {
        return prefix + ARRAY;
    }
}
