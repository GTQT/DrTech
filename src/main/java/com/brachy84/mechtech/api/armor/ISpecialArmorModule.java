package com.brachy84.mechtech.api.armor;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;

@FunctionalInterface
public interface ISpecialArmorModule {

    /**
     * Modifies armor values when damaged.
     * !NOTE! Armor and toughness is completely ignored
     *
     * @param entity            wearing entity
     * @param modularArmorPiece armor piece
     * @param source            damage source
     * @param damage            damage amount
     * @param slot              slot of the armor piece
     * @return properties with a action result
     */
    AbsorbResult getArmorProperties(EntityLivingBase entity, ItemStack modularArmorPiece, NBTTagCompound moduleData, DamageSource source, double damage, EntityEquipmentSlot slot);
}
