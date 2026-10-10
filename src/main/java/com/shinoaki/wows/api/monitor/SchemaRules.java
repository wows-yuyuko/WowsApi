package com.shinoaki.wows.api.monitor;

import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * 结构采集的归一化规则：忽略名单 + 动态 key 归一。
 * <p>
 * 存在的意义是把「噪声」挡在快照之外，否则 baseline 每天都会变：
 * <ul>
 *     <li><b>忽略名单</b>：graphql 的 {@code __typename} 这类与本库解析无关的字段，按路径正则忽略；</li>
 *     <li><b>动态 key</b>：{@code data.<accountId>}、{@code statistics.<shipId>} 这类以数据主键为名的 key，
 *     必须归一成 {@code *}，否则每个玩家/每条船都会生成一条新路径。</li>
 * </ul>
 *
 * @param ignores      忽略的路径正则
 * @param dynamicKey   动态 key 判定（key 名称 -> 是否归一为 *）
 * @author Xun
 */
public record SchemaRules(List<Pattern> ignores, Predicate<String> dynamicKey) {

    /**
     * 动态 key 的通用启发式：纯数字，或长度 >= 16 的纯十六进制/数字串（各类 WG 资源 ID）。
     */
    private static final Pattern NUMERIC_KEY = Pattern.compile("\\d+");
    private static final Pattern LONG_HASH_KEY = Pattern.compile("[0-9a-f]{16,}");

    /**
     * 默认规则：忽略 graphql 的 __typename；数字/长 hash key 视为动态 key。
     */
    public static SchemaRules defaults() {
        return new SchemaRules(
                List.of(Pattern.compile("(^|\\.)__typename$")),
                key -> NUMERIC_KEY.matcher(key).matches() || LONG_HASH_KEY.matcher(key).matches()
        );
    }

    public boolean ignore(String path) {
        for (Pattern p : ignores) {
            if (p.matcher(path).find()) {
                return true;
            }
        }
        return false;
    }

    /**
     * 单个 key 段归一。
     */
    public String normalizeKey(String key) {
        return dynamicKey.test(key) ? "*" : key;
    }

    /**
     * 整条路径归一：逐段应用 {@link #normalizeKey(String)}。
     * <p>
     * 精确模式记录的是「解析器实际读取的真实 key」，需归一后才能与采集快照直接比较
     * （例如 {@code data.3751786448.name} -> {@code data.*.name}）。
     */
    public String normalizePath(String path) {
        if (path == null || path.isEmpty()) {
            return path;
        }
        StringBuilder sb = new StringBuilder(path.length());
        for (String seg : path.split("\\.", -1)) {
            if (!sb.isEmpty()) {
                sb.append('.');
            }
            int idx = seg.indexOf(SchemaPaths.ARRAY);
            if (idx < 0) {
                sb.append(normalizeKey(seg));
            } else {
                // rewards[] 这类：只归一 [] 之前的字段名，保留数组标记
                sb.append(normalizeKey(seg.substring(0, idx))).append(seg.substring(idx));
            }
        }
        return sb.toString();
    }
}
