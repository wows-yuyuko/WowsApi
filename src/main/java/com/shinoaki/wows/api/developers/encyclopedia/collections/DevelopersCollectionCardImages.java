package com.shinoaki.wows.api.developers.encyclopedia.collections;

/**
 * 收藏卡牌图片（尺寸 -&gt; URL；large 可能为 null）。
 *
 * @author Xun
 */
public record DevelopersCollectionCardImages(String small, String medium, String large) {
}
