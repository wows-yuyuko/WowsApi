package com.shinoaki.wows.api.developers.encyclopedia.shipprofile;

/**
 * 深水炸弹：伤害、数量与装填。
 *
 * @author Xun
 */
public record DevelopersShipDepthCharge(int bomb_max_damage, int max_packs, int num_bombs_in_pack, double reload_time) {
}
