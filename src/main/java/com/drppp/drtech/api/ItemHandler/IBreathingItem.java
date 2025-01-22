package com.drppp.drtech.api.ItemHandler;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import java.util.List;

public interface IBreathingItem {
    boolean isValid(ItemStack stack, EntityPlayer player);

    double getDamageAbsorbed(ItemStack stack, EntityPlayer player);
}