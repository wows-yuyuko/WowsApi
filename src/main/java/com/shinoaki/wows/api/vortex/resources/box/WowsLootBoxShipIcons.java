package com.shinoaki.wows.api.vortex.resources.box;

import tools.jackson.databind.JsonNode;

/**
 * 战舰图标，对应 additionalData.icons。
 *
 * @param localContour      本地轮廓图
 * @param contourAlive      轮廓图(存活)
 * @param medium            中图标
 * @param defaultUrl        默认图标
 * @param localSmall        本地小图标
 * @param contourDead       轮廓图(击沉)
 * @param large             大图标
 * @param localContourDead  本地轮廓图(击沉)
 * @param localContourAlive 本地轮廓图(存活)
 * @param small             小图标
 * @param contour           轮廓图
 * @author Xun
 */
public record WowsLootBoxShipIcons(
        String localContour,
        String contourAlive,
        String medium,
        String defaultUrl,
        String localSmall,
        String contourDead,
        String large,
        String localContourDead,
        String localContourAlive,
        String small,
        String contour
) {
    public static WowsLootBoxShipIcons parse(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return null;
        }
        return new WowsLootBoxShipIcons(
                node.path("localContour").asString(""),
                node.path("contourAlive").asString(""),
                node.path("medium").asString(""),
                node.path("default").asString(""),
                node.path("localSmall").asString(""),
                node.path("contourDead").asString(""),
                node.path("large").asString(""),
                node.path("localContourDead").asString(""),
                node.path("localContourAlive").asString(""),
                node.path("small").asString(""),
                node.path("contour").asString("")
        );
    }
}
