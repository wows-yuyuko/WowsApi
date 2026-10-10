package com.shinoaki.wows.api.monitor;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Pattern;

/**
 * 由请求 URI 推导接口标识（{@code schemaKey}），作为 {@link SchemaRecorder} 未显式声明 key 时的兜底。
 * <p>
 * 标识格式：{@code 通道:服务器域:路径模板[?归一后的查询参数]}，例如
 * <ul>
 *     <li>{@code vortex:asia:/api/accounts/{id}/}</li>
 *     <li>{@code api:asia:/wows/account/info/?account_id={id}&extra=statistics}</li>
 *     <li>{@code clans:asia:/api/members/{id}/?battle_type=pvp}</li>
 * </ul>
 * 归一化的目的：同一接口的「一次真实调用」只对应一份基线，玩家 ID、语言、令牌这些每次都变的
 * 内容不能进入标识，否则每跑一次就生成一份新基线。
 * <p>
 * <b>密钥绝不入库</b>：{@code application_id} / {@code access_token} 直接从标识中剔除。
 * <p>
 * 影响返回结构的查询参数（如 {@code extra} / {@code battle_type} / {@code type} / {@code in_garage}）
 * 原样保留 —— 它们是接口契约的一部分，去掉会互相污染。
 * <p>
 * graphql 的 {@code /api/graphql/glossary/} 是一个 URL 多个 schema，无法从 URI 区分，
 * 需要调用方用 {@link SchemaRecorder#key(String)} 显式声明（如 {@code #Lootbox}）。
 *
 * @author Xun
 */
public final class SchemaKeys {

    private SchemaKeys() {
    }

    /**
     * 语言段归一为 {@code {lang}}：接口结构不受语言影响，4 种语言共用一份基线。
     */
    private static final Set<String> LANGUAGES = Set.of(
            "zh-sg", "zh-cn", "zh-tw", "en", "ru", "ja", "ko", "pl", "de", "fr", "es", "tr", "th", "vi", "uk", "cs", "pt-br");

    /**
     * 不进入标识的查询参数：密钥类（安全）与语言类（只影响内容）。
     */
    private static final Set<String> DROPPED_QUERY = Set.of("application_id", "access_token", "language", "lang", "locale");

    /**
     * 归一为 {@code {num}} 的查询参数：数值会随赛季/分页变化，但不改变结构。
     */
    private static final Set<String> NUMBER_QUERY = Set.of("season", "limit", "page", "offset");

    private static final Pattern NUMERIC = Pattern.compile("\\d+");
    private static final Pattern HASH = Pattern.compile("[0-9a-fA-F]{16,}");
    private static final Pattern ENCODED = Pattern.compile("%[0-9a-fA-F]{2}");

    public static String of(URI uri) {
        if (uri == null) {
            return "unknown";
        }
        String host = uri.getHost() == null ? "" : uri.getHost().toLowerCase(Locale.ROOT);
        StringBuilder sb = new StringBuilder();
        sb.append(channel(host)).append(':').append(scope(host)).append(':').append(template(uri.getPath()));
        String query = query(uri.getRawQuery());
        if (!query.isEmpty()) {
            sb.append('?').append(query);
        }
        return sb.toString();
    }

    /**
     * 请求通道：由主机前缀判定（与 {@code ApiHttp} / {@code VortexHttp} 的划分一致）。
     */
    public static String channel(String host) {
        if (host.startsWith("vortex-rpt.")) {
            return "vortex-rpt";
        }
        if (host.startsWith("vortex.")) {
            return "vortex";
        }
        if (host.startsWith("clans.")) {
            return "clans";
        }
        if (host.startsWith("api.")) {
            return "api";
        }
        return "other";
    }

    /**
     * 服务器域：按站点后缀归并到服别（同一接口在亚服/欧服的形状一致，合并观察）。
     */
    public static String scope(String host) {
        if (host.endsWith("wowsgame.cn")) {
            return "cn";
        }
        if (host.endsWith("worldofwarships.asia")) {
            return "asia";
        }
        if (host.endsWith("worldofwarships.eu")) {
            return "eu";
        }
        if (host.endsWith("worldofwarships.com")) {
            return "na";
        }
        if (host.endsWith("korabli.su")) {
            return "ru";
        }
        if (host.endsWith("worldofwarships.pt")) {
            return "pt";
        }
        return host.isEmpty() ? "unknown" : host;
    }

    /**
     * 路径模板：数字段 -> {@code {id}}，长 hash -> {@code {hash}}，URL 编码过的自由文本 -> {@code {text}}，
     * 语言段 -> {@code {lang}}，其余原样保留。保留首尾斜杠。
     */
    public static String template(String path) {
        if (path == null || path.isEmpty()) {
            return "/";
        }
        String[] segments = path.split("/", -1);
        StringBuilder sb = new StringBuilder(path.length());
        String previous = "";
        for (int i = 0; i < segments.length; i++) {
            if (i > 0) {
                sb.append('/');
            }
            //autocomplete/<玩家名>/ 这类搜索词是数据、不是结构，归一为 {text}
            //注意两点：只看 autocomplete（/api/search/autocomplete/ 里的 search 是固定路径段，不是标记）；
            //空段不消费标记，否则 /api/search/autocomplete/ 的结尾会被误判成搜索词
            if (segments[i].isEmpty()) {
                continue;
            }
            sb.append(previous.equals("autocomplete") ? "{text}" : segment(segments[i]));
            previous = segments[i];
        }
        return sb.toString();
    }

    private static String segment(String segment) {
        if (segment.isEmpty()) {
            return segment;
        }
        if (NUMERIC.matcher(segment).matches()) {
            return "{id}";
        }
        if (HASH.matcher(segment).matches()) {
            return "{hash}";
        }
        if (ENCODED.matcher(segment).find()) {
            return "{text}";
        }
        if (LANGUAGES.contains(segment.toLowerCase(Locale.ROOT))) {
            return "{lang}";
        }
        return segment;
    }

    /**
     * 查询参数归一：剔除密钥/语言参数，按键排序，值按 {@link #value(String, String)} 归一。
     */
    public static String query(String rawQuery) {
        if (rawQuery == null || rawQuery.isEmpty()) {
            return "";
        }
        TreeMap<String, String> params = new TreeMap<>();
        for (String pair : rawQuery.split("&")) {
            if (pair.isEmpty()) {
                continue;
            }
            int eq = pair.indexOf('=');
            String key = eq < 0 ? pair : pair.substring(0, eq);
            String value = eq < 0 ? "" : pair.substring(eq + 1);
            if (DROPPED_QUERY.contains(key.toLowerCase(Locale.ROOT))) {
                continue;
            }
            params.put(key, value(key, value));
        }
        List<String> out = new ArrayList<>(params.size());
        for (var e : params.entrySet()) {
            out.add(e.getKey() + (e.getValue().isEmpty() ? "" : "=" + e.getValue()));
        }
        return String.join("&", out);
    }

    private static String value(String key, String value) {
        if (value.isEmpty()) {
            return value;
        }
        if (value.indexOf(',') > 0) {
            //extra=clan,statistics 与 extra=statistics,clan 是同一个形状，排序后再入 key
            String[] parts = value.split(",");
            java.util.Arrays.sort(parts);
            return String.join(",", parts);
        }
        String lower = key.toLowerCase(Locale.ROOT);
        if (lower.equals("search")) {
            return "{text}";
        }
        if (NUMERIC.matcher(value).matches()) {
            if (lower.endsWith("_id")) {
                return "{id}";
            }
            return NUMBER_QUERY.contains(lower) ? "{num}" : value;
        }
        if (HASH.matcher(value).matches()) {
            return "{hash}";
        }
        if (ENCODED.matcher(value).find()) {
            return "{text}";
        }
        return value;
    }
}
