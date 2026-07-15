package com.drppp.drtech.common.Items.lightsaber;

import com.drppp.drtech.DrTechMain;
import com.drppp.drtech.Tags;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

public class ItemFocusingCrystal extends Item {
    public ItemFocusingCrystal() {
        setRegistryName(Tags.MODID, "focusing_crystal");
        setTranslationKey(Tags.MODID + ".focusing_crystal");
        setCreativeTab(DrTechMain.DrTechTab);
        setHasSubtypes(true);
        setMaxDamage(0);
    }

    @Override
    public String getTranslationKey(ItemStack stack) {
        return super.getTranslationKey() + "." + FocusingCrystal.byMetadata(stack.getMetadata()).getSerializedName();
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (isInCreativeTab(tab)) {
            for (FocusingCrystal crystal : FocusingCrystal.values()) {
                items.add(new ItemStack(this, 1, crystal.getMetadata()));
            }
        }
    }
}
