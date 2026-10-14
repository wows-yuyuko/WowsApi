package com.shinoaki.wows.api.developers.encyclopedia.shipprofile;

/**
 * 装甲：血量、防雷（flood_damage/flood_prob）、各部位厚度区间（casemate/citadel/deck/extremities/range）。
 *
 * @author Xun
 */
public record DevelopersShipArmourProfile(int flood_damage, int flood_prob, int health, int total,
                                          DevelopersShipIntRange casemate, DevelopersShipIntRange citadel,
                                          DevelopersShipIntRange deck, DevelopersShipIntRange extremities,
                                          DevelopersShipIntRange range) {
}
