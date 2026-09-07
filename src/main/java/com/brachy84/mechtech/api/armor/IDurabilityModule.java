package com.brachy84.mechtech.api.armor;

import gregtech.api.items.metaitem.stats.IItemDurabilityManager;
import gregtech.api.util.GradientUtil;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import org.apache.commons.lang3.tuple.Pair;

import java.awt.*;

public interface IDurabilityModule {

    default IItemDurabilityManager getDurabilityManager(int durability) {
        return new IItemDurabilityManager() {

            @Override
            public boolean showEmptyBar(ItemStack itemStack) {
                return false;
            }

            @Override
            public boolean showFullBar(ItemStack itemStack) {
                return true;
            }

            @Override
            public Pair<Color, Color> getDurabilityColorsForDisplay(ItemStack itemStack) {
                return GradientUtil.getGradient(0x0dfc25, 20);
            }

            @Override
            public double getDurabilityForDisplay(ItemStack itemStack) {
                NBTTagCompound nbt = itemStack.getTagCompound();
                return nbt == null ? 1 : (durability - getDamage(nbt)) / ((double) durability);
            }
        };
    }

    /**
     * Called when the armor is damaged
     *
     * @param entityLivingBase    wearing entity
     * @param modularArmorPiece   armor piece
     * @param damageSource        damage source
     * @param damage              amount
     * @param entityEquipmentSlot current slot
     * @return the damage applied to this module
     */
     default double damage(EntityLivingBase entityLivingBase, ItemStack modularArmorPiece, NBTTagCompound moduleData, DamageSource damageSource, float damage, EntityEquipmentSlot entityEquipmentSlot) {
         damage *= 100;
         int durability = getMaxDurability(moduleData) * 100;
         if (!moduleData.hasKey("Dmg")) {
             damage = Math.min(damage, durability);
             moduleData.setInteger("Dmg", (int) (damage));
         } else {
             int dmg = moduleData.getInteger("Dmg");
             damage = Math.min(damage, durability - dmg);
             moduleData.setInteger("Dmg", (int) (dmg + damage));
         }
         if(getDamage(moduleData) >= durability)
             moduleData.setBoolean("Destroyed", true);
         return damage / 100;
     }

     default double getDamage(NBTTagCompound moduleData) {
         return moduleData.getInteger("Dmg") / 100.0;
     }

     int getMaxDurability(NBTTagCompound moduleData);
}
