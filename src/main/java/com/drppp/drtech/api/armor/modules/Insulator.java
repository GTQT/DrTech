package com.drppp.drtech.api.armor.modules;

import com.drppp.drtech.api.armor.AbsorbResult;
import com.drppp.drtech.api.armor.IModule;
import com.drppp.drtech.api.armor.ISpecialArmorModule;
import com.drppp.drtech.common.items.MTMetaItems;
import gregtech.api.damagesources.DamageSources;
import gregtech.api.items.metaitem.MetaItem;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraftforge.items.IItemHandler;

public class Insulator implements IModule, ISpecialArmorModule {

    @Override
    public boolean canPlaceIn(EntityEquipmentSlot slot, ItemStack modularArmorPiece, IItemHandler modularSlots) {
        return true;
    }

    @Override
    public String getModuleId() {
        return "insulator";
    }

    @Override
    public AbsorbResult getArmorProperties(EntityLivingBase entity, ItemStack modularArmorPiece, NBTTagCompound moduleData, DamageSource source, double damage, EntityEquipmentSlot slot) {
        if (source == DamageSources.getElectricDamage()) {
            float ratio = 0;
            switch (slot) {
                case HEAD: ratio = 0.92f; break;
                case CHEST: ratio = 0.99f; break;
                case LEGS: ratio = 0.97f; break;
                case FEET: ratio = 0.93f; break;
            }
            return new AbsorbResult(ratio, 60);
        }
        return AbsorbResult.ZERO;
    }

    @Override
    public MetaItem<?>.MetaValueItem getMetaValueItem() {
        return MTMetaItems.THICK_INSULATOR;
    }
}
