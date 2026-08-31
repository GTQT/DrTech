package com.drppp.futuremc;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;

public final class FutureMCCreativeTabs extends CreativeTabs {
    public FutureMCCreativeTabs() {
        super(FutureMCMain.MODID);
    }

    @Override
    public ItemStack createIcon() {
        return new ItemStack(FutureMCMain.AMETHYST_SHARD);
    }

}
