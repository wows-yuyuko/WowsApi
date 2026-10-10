package com.shinoaki.wows.api.developers.warships;

/**
 * 官方API响应的 meta 节点。
 * <p>
 * 例：{@code {"count": 1, "hidden": null}}（战舰徽章）、{@code {"count": 1, "page": 1, "total": 1}}（军团搜索）。
 *
 * @param count  本次返回条数
 * @param hidden 隐藏战绩的账号ID(部分接口返回)
 * @param page   当前页(军团搜索)
 * @param total  总条数(军团搜索)
 * @author Xun
 * @date 2023/4/24 20:51 星期一
 */
public record DevelopersMeta(int count, long[] hidden, int page, int total) {
}
