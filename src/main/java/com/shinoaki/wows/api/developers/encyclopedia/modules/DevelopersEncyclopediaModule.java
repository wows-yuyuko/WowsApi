package com.shinoaki.wows.api.developers.encyclopedia.modules;

/**
 * 单个模块：类型（Hull/Engine/Artillery/Torpedoes/Suo/Sonar/Fighter/DiveBomber/TorpedoBomber）与属性明细。
 *
 * @author Xun
 */
public record DevelopersEncyclopediaModule(long module_id, String module_id_str, String name, String image,
                                           long price_credit, String type, String tag, DevelopersModuleProfile profile) {
}
