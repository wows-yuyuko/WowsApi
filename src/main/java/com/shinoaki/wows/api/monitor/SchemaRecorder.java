package com.shinoaki.wows.api.monitor;

import java.util.ArrayList;
import java.util.List;

/**
 * 响应采集钩子（默认关闭，零开销）。
 * <p>
 * 监视测试里按「打开 -> 声明本次调用的 schemaKey -> 调用库方法 -> 关闭」的方式使用，
 * 由 {@code HttpCodec.response} 在解码成功后把<b>原始响应体</b>记录下来。
 * 这样监视拿到的请求与响应与库真实调用完全一致，不存在「测试里另写一份 URL/query」的漂移风险。
 *
 * <pre>{@code
 * SchemaRecorder.enable();
 * SchemaRecorder.key("vortex:asia:/api/accounts/{id}/");
 * try {
 *     tools.userVortex(accountId);      // 库正常调用，响应被顺带采集
 * } finally {
 *     SchemaRecorder.disable();
 * }
 * for (SchemaRecorder.Record r : SchemaRecorder.drain()) { ... }
 * }</pre>
 *
 * @author Xun
 */
public final class SchemaRecorder {

    private SchemaRecorder() {
    }

    private static final ThreadLocal<Buffer> CURRENT = new ThreadLocal<>();

    /**
     * 开始采集（绑定到当前线程）。
     */
    public static void enable() {
        CURRENT.set(new Buffer());
    }

    /**
     * 结束采集。
     */
    public static void disable() {
        CURRENT.remove();
    }

    public static boolean enabled() {
        return CURRENT.get() != null;
    }

    /**
     * 当前线程已声明的接口标识，未声明返回 null。
     */
    public static String currentKey() {
        Buffer buffer = CURRENT.get();
        return buffer == null ? null : buffer.key;
    }

    /**
     * 声明本次调用对应的接口标识；未声明时 {@code HttpCodec} 会退化为按 URI 推导。
     */
    public static void key(String schemaKey) {
        Buffer buffer = CURRENT.get();
        if (buffer != null) {
            buffer.key = schemaKey;
        }
    }

    /**
     * 记录一次响应（由 {@code HttpCodec.response} 调用）。
     * <p>
     * 未显式声明 key 时按请求 URI 推导（见 {@link SchemaKeys#of(java.net.URI)}）。
     * graphql 这类「一个 URL 多个 schema」的接口必须在调用前用 {@link #key(String)} 声明。
     *
     * @param uri  本次请求的 URI（取自 {@code HttpResponse.uri()}）
     * @param json 已解压解码的响应体
     */
    public static void record(java.net.URI uri, String json) {
        Buffer buffer = CURRENT.get();
        if (buffer != null) {
            buffer.records.add(new Record(buffer.key != null ? buffer.key : SchemaKeys.of(uri), json));
        }
    }

    /**
     * 记录一次响应（显式指定 schemaKey）。
     */
    public static void record(String schemaKey, String json) {
        Buffer buffer = CURRENT.get();
        if (buffer != null) {
            buffer.records.add(new Record(schemaKey, json));
        }
    }

    /**
     * 取走当前线程已采集的记录（不移除绑定）。
     */
    public static List<Record> drain() {
        Buffer buffer = CURRENT.get();
        if (buffer == null) {
            return List.of();
        }
        List<Record> copy = List.copyOf(buffer.records);
        buffer.records.clear();
        return copy;
    }

    /**
     * 一条原始响应。
     *
     * @param schemaKey 接口标识
     * @param json      原始响应体
     */
    public record Record(String schemaKey, String json) {
    }

    private static final class Buffer {
        private String key;
        private final List<Record> records = new ArrayList<>();
    }
}
