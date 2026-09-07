package com.drppp.drtech.common.Items.lightsaber;

import com.drppp.drtech.DrTechMain;
import com.drppp.drtech.Tags;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

public class ItemLightsaberCrystal extends Item {
    public ItemLightsaberCrystal() {
        setRegistryName(Tags.MODID, "lightsaber_crystal");
        setTranslationKey(Tags.MODID + ".lightsaber_crystal");
        setCreativeTab(DrTechMain.DrTechTab);
        setHasSubtypes(true);
        setMaxDamage(0);
    }

    @Override
    public String getTranslationKey(ItemStack stack) {
        return super.getTranslationKey() + "." + LightsaberColor.byMetadata(stack.getMetadata()).getSerializedName();
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (isInCreativeTab(tab)) {
            for (LightsaberColor color : LightsaberColor.values()) {
                items.add(new ItemStack(this, 1, color.getMetadata()));
            }
        }
    }
}
