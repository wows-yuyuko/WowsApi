package com.shinoaki.wows.api.developers.seasons;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 段位图片；{@code default} 是 Java 关键字，字段名用 defaultImage 映射回 JSON 的 {@code default}。
 *
 * @author Xun
 */
public record DevelopersRankedRankImages(@JsonProperty("default") String defaultImage, String small) {
}
