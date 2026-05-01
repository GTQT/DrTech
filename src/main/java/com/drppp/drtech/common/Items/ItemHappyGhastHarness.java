package com.drppp.drtech.common.Items;

import com.drppp.drtech.DrTechMain;
import com.drppp.drtech.Tags;
import net.minecraft.item.Item;

public class ItemHappyGhastHarness extends Item {
    public ItemHappyGhastHarness() {
        setRegistryName(Tags.MODID, "happy_ghast_harness");
        setTranslationKey(Tags.MODID + ".happy_ghast_harness");
        setCreativeTab(DrTechMain.DrTechTab);
        setMaxStackSize(1);
    }
}
