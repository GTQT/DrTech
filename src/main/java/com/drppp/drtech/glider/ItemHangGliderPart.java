package com.drppp.drtech.glider;

import com.drppp.drtech.DrTechMain;
import com.drppp.drtech.Tags;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

public final class ItemHangGliderPart extends Item {
    public static final String[] NAMES = { "wing_left", "wing_right", "scaffolding" };

    public ItemHangGliderPart() {
        setRegistryName(Tags.MODID, "hang_glider_part");
        setTranslationKey(Tags.MODID + ".hang_glider_part.");
        setCreativeTab(DrTechMain.DrTechTab);
        setHasSubtypes(true);
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (isInCreativeTab(tab)) {
            for (int meta = 0; meta < NAMES.length; meta++) {
                items.add(new ItemStack(this, 1, meta));
            }
        }
    }

    @Override
    public String getTranslationKey(ItemStack stack) {
        int meta = Math.max(0, Math.min(stack.getMetadata(), NAMES.length - 1));
        return super.getTranslationKey(stack) + NAMES[meta];
    }
}
