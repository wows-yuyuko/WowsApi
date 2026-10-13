package com.shinoaki.wows.v2;

import com.shinoaki.wows.api.error.BasicException;
import com.shinoaki.wows.api.type.WowsServer;
import com.shinoaki.wows.api.vortex.VortexResourcesLanguage;
import com.shinoaki.wows.api.vortex.resources.box.WowsLootBox;
import com.shinoaki.wows.api.vortex.resources.box.WowsLootBoxReward;
import com.shinoaki.wows.api.vortex.resources.box.WowsLootBoxRewardGroup;
import com.shinoaki.wows.api.vortex.resources.box.WowsLootBoxSlot;
import com.shinoaki.wows.testkit.TestConfig;

import java.util.Map;

public class LootBoxTest {
    public static void main(String[] args) {
        try {
            var box = WowsLootBox.request(WowsServer.ASIA, VortexResourcesLanguage.SG, TestConfig.LOOTBOX_ASIA);
            System.out.println("status     = " + box.status());
            System.out.println("id         = " + box.id());
            System.out.println("name       = " + box.name());
            System.out.println("title      = " + box.title() + " / " + box.shortTitle());
            System.out.println("isPremium  = " + box.isPremium());
            System.out.println("savePoint  = " + box.savePoint());
            System.out.println("filler     = " + box.filler());
            System.out.println("tags       = " + box.tags());
            System.out.println("icons      = " + box.icons());
            System.out.println("slots      = " + box.slots().size());
            for (WowsLootBoxSlot slot : box.slots()) {
                System.out.println("---- slot: " + slot.title() + " (continuousRewards=" + slot.continuousRewards() + ")");
                printGroups("common  ", slot.commonRewards());
                printGroups("valuable", slot.valuableRewards());
            }
        } catch (BasicException e) {
            throw new RuntimeException(e);
        }
    }

    private static void printGroups(String label, Map<String, WowsLootBoxRewardGroup> groups) {
        for (var entry : groups.entrySet()) {
            WowsLootBoxRewardGroup g = entry.getValue();
            System.out.printf("[%s] %s: %s prob=%s weight=%d displayed=%s rewards=%d%n",
                    label, entry.getKey(), g.title(), g.probability(), g.weight(), g.probabilityDisplayed(), g.rewards().size());
            int limit = Math.min(3, g.rewards().size());
            for (int i = 0; i < limit; i++) {
                WowsLootBoxReward r = g.rewards().get(i);
                System.out.printf("      - type=%s id=%s amount=%d prob=%s crewLevel=%s addData=%s%n",
                        r.type(), r.id(), r.amount(), r.probabilityDisplayed(), r.crewLevel(),
                        r.additionalData() == null ? null : r.additionalData().title());
            }
        }
    }
}
