package com.drppp.drtech;

import com.drppp.drtech.common.Items.ItemsInit;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;

public class MyCreativeTabs extends CreativeTabs {
    public MyCreativeTabs(String label) {
        super(label);
    }

    @Override
    public ItemStack createIcon() {
        return new ItemStack(ItemsInit.ITEM_BLOCK_GRAVITATIONAL_ANOMALY);
    }
}
