package com.drppp.futuremc.items;

import com.drppp.futuremc.FutureMCMain;
import net.minecraft.item.Item;

public class ItemHappyGhastHarness extends Item {
    public ItemHappyGhastHarness() {
        setRegistryName(FutureMCMain.MODID, "happy_ghast_harness");
        setTranslationKey(FutureMCMain.MODID + ".happy_ghast_harness");
        setCreativeTab(FutureMCMain.TAB);
        setMaxStackSize(1);
    }
}
