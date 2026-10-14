package com.shinoaki.wows.api.developers.warships;

/**
 * 官方API响应的 meta 节点。
 * <p>
 * 例：{@code {"count": 1, "hidden": null}}（战舰徽章）、{@code {"count": 1, "page": 1, "total": 1}}（军团搜索）、
 * {@code {"limit": 100, "count": 100, "page": 1, "page_total": 11, "total": 1017}}（百科字典分页）。
 * 未返回的字段解析为 0/null（接口新增字段不会破坏解析）。
 *
 * @param count      本次返回条数
 * @param hidden     隐藏战绩的账号ID(部分接口返回)
 * @param page       当前页(分页接口)
 * @param total      总条数(分页接口)
 * @param limit      每页条数(百科字典分页)
 * @param page_total 总页数(百科字典分页)
 * @author Xun
 * @date 2023/4/24 20:51 星期一
 */
public record DevelopersMeta(int count, long[] hidden, int page, int total, int limit, int page_total) {
}
