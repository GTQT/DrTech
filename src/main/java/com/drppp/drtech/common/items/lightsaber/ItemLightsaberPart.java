package com.drppp.drtech.common.Items.lightsaber;

import com.drppp.drtech.DrTechMain;
import com.drppp.drtech.Tags;
import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class ItemLightsaberPart extends Item {
    private final LightsaberPartType partType;

    public ItemLightsaberPart(String registryName, LightsaberPartType partType) {
        this.partType = partType;
        setRegistryName(Tags.MODID, registryName);
        setTranslationKey(Tags.MODID + "." + registryName);
        setCreativeTab(DrTechMain.DrTechTab);
        setHasSubtypes(true);
        setMaxDamage(0);
        setMaxStackSize(16);
    }

    public LightsaberPartType getPartType() {
        return partType;
    }

    public LightsaberHilt getHilt(ItemStack stack) {
        return LightsaberHilt.byMetadata(stack.getMetadata());
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (isInCreativeTab(tab)) {
            for (LightsaberHilt hilt : LightsaberHilt.values()) {
                items.add(new ItemStack(this, 1, hilt.getMetadata()));
            }
        }
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip,
                               net.minecraft.client.util.ITooltipFlag flag) {
        tooltip.add(I18n.format("tooltip.drtech.lightsaber.hilt", getHilt(stack).getDisplayName()));
    }
}
