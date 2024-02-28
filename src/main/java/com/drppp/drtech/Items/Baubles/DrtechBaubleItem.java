package com.drppp.drtech.Items.Baubles;

import baubles.api.BaubleType;
import baubles.api.IBauble;
import com.drppp.drtech.Tags;
import gregtech.api.GTValues;
import gregtech.api.items.toolitem.IGTTool;
import gregtech.api.items.toolitem.IGTToolDefinition;
import gregtech.api.items.toolitem.behavior.IToolBehavior;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

public class DrtechBaubleItem extends Item implements IBauble, IGTTool  {
    @Override
    public BaubleType getBaubleType(ItemStack itemStack) {
        return BaubleType.RING;
    }

    @Override
    public String getDomain() {
        return Tags.MODID;
    }
    @Override
    public int getMaterialHarvestLevel(ItemStack stack) {
        return 0;
    }

    @Override
    public int getTotalHarvestLevel(ItemStack stack) {
        return 0;
    }
    @Override
    public String getToolId() {
        return null;
    }

    @Override
    public boolean isElectric() {
        return true;
    }

    @Override
    public int getElectricTier() {
        return GTValues.HV;
    }

    @Override
    public IGTToolDefinition getToolStats() {
        return null;
    }

    @Override
    public @Nullable SoundEvent getSound() {
        return null;
    }

    @Override
    public boolean playSoundOnBlockDestroy() {
        return false;
    }

    @Override
    public @Nullable String getOreDictName() {
        return null;
    }

    @Override
    public @NotNull List<String> getSecondaryOreDicts() {
        return null;
    }

    @Override
    public @Nullable Supplier<ItemStack> getMarkerItem() {
        return null;
    }
}
