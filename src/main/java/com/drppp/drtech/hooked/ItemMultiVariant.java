package com.drppp.drtech.hooked;

import com.drppp.drtech.DrTechMain;
import com.drppp.drtech.Tags;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;

public class ItemMultiVariant extends Item {
    private final String[] variants;

    public ItemMultiVariant(String registry, String[] variants) {
        this.variants = variants;
        setRegistryName(new ResourceLocation(Tags.MODID, registry));
        setTranslationKey(Tags.MODID + "." + registry);
        setCreativeTab(DrTechMain.DrTechTab);
        setHasSubtypes(true);
    }

    @Override
    public boolean getHasSubtypes() {
        return true;
    }

    @Override
    public int getMetadata(int damage) {
        return damage;
    }

    @Override
    public String getTranslationKey(ItemStack stack) {
        int meta = Math.max(0, Math.min(stack.getMetadata(), variants.length - 1));
        return "item." + Tags.MODID + "." + variants[meta];
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (!isInCreativeTab(tab)) {
            return;
        }
        for (int meta = 0; meta < variants.length; meta++) {
            items.add(new ItemStack(this, 1, meta));
        }
    }
}
