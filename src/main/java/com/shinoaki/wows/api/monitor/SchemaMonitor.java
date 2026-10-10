package com.shinoaki.wows.api.monitor;

import tools.jackson.databind.JsonNode;

import java.io.PrintStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 字段漂移监视入口（纯观测，不影响主流程）。
 * <p>
 * 一次 {@link #check} 做三件事：
 * <ol>
 *     <li><b>结构漂移</b>：本次响应结构 vs baseline -> added / removed / typeChanged；</li>
 *     <li><b>精确模式</b>：解析器实际读取的路径 vs 响应结构 -> 「未消费字段」与「读取但缺失字段」；</li>
 *     <li><b>基线维护</b>：baseline 不存在或发生变化时按需写回（供 git review）。</li>
 * </ol>
 *
 * @author Xun
 */
public final class SchemaMonitor {

    private final SchemaBaseline baseline;
    private final SchemaRules rules;
    private final boolean updateBaseline;
    private final PrintStream out;

    public SchemaMonitor(Path baselineDir, SchemaRules rules, boolean updateBaseline, PrintStream out) {
        this.baseline = new SchemaBaseline(baselineDir);
        this.rules = rules;
        this.updateBaseline = updateBaseline;
        this.out = out;
    }

    public SchemaBaseline baseline() {
        return baseline;
    }

    /**
     * 探测一个接口。
     *
     * @param label     展示用名称
     * @param schemaKey 接口标识（baseline 以此归位）
     * @param json      响应 JSON
     * @param readPaths 精确模式：解析器实际读取的路径（可为 null，表示只做结构漂移）
     */
    public Result check(String label, String schemaKey, JsonNode json, Set<String> readPaths) {
        SchemaSnapshot now = SchemaExtractor.extract(schemaKey, json, rules);
        SchemaSnapshot base = baseline.load(schemaKey);
        boolean created = base == null;
        SchemaDiff diff = created ? SchemaDiff.empty() : SchemaDiff.of(base, now);

        //未开启精确模式时不做「消费/缺失」分析：没有解析器读取记录，无从判断
        List<String> unconsumed = new ArrayList<>();
        List<String> missing = new ArrayList<>();
        int readCount = 0;
        if (readPaths != null) {
            Set<String> reads = new LinkedHashSet<>();
            for (String read : readPaths) {
                reads.add(rules.normalizePath(read));
            }
            readCount = reads.size();
            for (var e : now.fields().entrySet()) {
                if (isContainer(e.getValue())) {
                    continue;
                }
                if (!reached(e.getKey(), reads)) {
                    unconsumed.add(e.getKey());
                }
            }
            if (base != null) {
                for (String read : reads) {
                    if (!matches(now.fields().keySet(), read) && matches(base.fields().keySet(), read)) {
                        missing.add(read);
                    }
                }
            }
        }

        if (updateBaseline && (created || !diff.isEmpty())) {
            baseline.save(now);
        }
        return new Result(label, schemaKey, created, now.size(), readCount, diff, unconsumed, missing);
    }

    public void log(Result result) {
        out.print(render(result));
    }

    private static boolean isContainer(String type) {
        return SchemaExtractor.TYPE_OBJECT.equals(type) || SchemaExtractor.TYPE_ARRAY.equals(type);
    }

    /**
     * 判断某条已采集路径是否被解析器「触达」：直接读过，或解析器对它的某个祖先对象做过整体遍历。
     */
    private static boolean reached(String path, Set<String> reads) {
        if (reads.contains(path)) {
            return true;
        }
        int from = path.length();
        while (true) {
            int dot = path.lastIndexOf('.', from - 1);
            if (dot <= 0) {
                return false;
            }
            if (reads.contains(path.substring(0, dot) + ".*")) {
                return true;
            }
            from = dot;
        }
    }

    /**
     * 路径集合中是否存在 {@code r} 本身，或 {@code r} 的数组/子字段（{@code r[]} / {@code r.xxx}）。
     */
    private static boolean matches(Collection<String> keys, String r) {
        if (keys.contains(r)) {
            return true;
        }
        for (String key : keys) {
            if (key.startsWith(r + SchemaPaths.ARRAY) || key.startsWith(r + ".")) {
                return true;
            }
        }
        return false;
    }

    private static String render(Result r) {
        StringBuilder sb = new StringBuilder();
        sb.append("── ").append(r.label()).append("  [").append(r.schemaKey()).append("]\n");
        sb.append("   fields=").append(r.fieldCount());
        if (r.readCount() > 0) {
            sb.append("  parsed=").append(r.readCount());
        }
        if (r.baselineCreated()) {
            sb.append("  基线：新建");
        } else if (r.diff().isEmpty()) {
            sb.append("  基线：无变化");
        } else {
            sb.append("  基线：有变化(").append(r.diff().total()).append(")");
        }
        sb.append('\n');

        if (!r.diff().isEmpty()) {
            appendList(sb, "+ 新增", r.diff().added(), 8);
            appendList(sb, "- 消失", r.diff().removed(), 8);
            appendList(sb, "~ 类型变化", r.diff().typeChanged(), 8);
        }
        if (!r.missing().isEmpty()) {
            appendList(sb, "! 解析器读取但响应缺失", r.missing(), 8);
        }
        if (!r.unconsumed().isEmpty()) {
            appendList(sb, "? 响应有但解析器未消费", r.unconsumed(), 8);
        }
        return sb.toString();
    }

    private static void appendList(StringBuilder sb, String title, List<String> items, int limit) {
        sb.append("   ").append(title).append(" (").append(items.size()).append("): ");
        int n = Math.min(limit, items.size());
        for (int i = 0; i < n; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(items.get(i));
        }
        if (items.size() > n) {
            sb.append(" ...(+").append(items.size() - n).append(')');
        }
        sb.append('\n');
    }

    /**
     * 单次探测结果。
     *
     * @param label           展示用名称
     * @param schemaKey       接口标识
     * @param baselineCreated 本次是否新建了基线
     * @param fieldCount      采集到的字段数
     * @param readCount       解析器消费的路径数（未做精确模式时为 0）
     * @param diff            结构差异
     * @param unconsumed      响应里有、但解析器未消费的叶子字段
     * @param missing         解析器读取、但本次响应中缺失（且基线曾存在）的字段
     */
    public record Result(String label, String schemaKey, boolean baselineCreated, int fieldCount, int readCount,
                         SchemaDiff diff, List<String> unconsumed, List<String> missing) {
    }
}
