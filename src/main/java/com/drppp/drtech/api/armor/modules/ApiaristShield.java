package com.drppp.drtech.api.armor.modules;

import com.drppp.drtech.api.armor.AbstractModule;
import com.drppp.drtech.common.items.metaItems.DrMetaItems;
import gregtech.api.items.metaitem.MetaItem;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.items.IItemHandler;

import java.util.List;

/**
 * Apiarist Shield Module — provides Forestry apiarist armor protection.
 * Slot: Chestplate
 *
 * This is a marker module. The actual IArmorApiarist interface
 * is implemented on DrArmorItem, which checks for this module.
 */
public class ApiaristShield extends AbstractModule {

    public ApiaristShield() {
        super("apiarist_shield");
    }

    @Override
    public boolean canPlaceIn(EntityEquipmentSlot slot, ItemStack modularArmorPiece, IItemHandler modularSlots) {
        return slot == EntityEquipmentSlot.CHEST;
    }

    @Override
    public void addInformation(ItemStack itemStack, List<String> lines) {
        if (Loader.isModLoaded("forestry")) {
            lines.add(I18n.format("drtech.apiarist_shield.tooltip.1"));
        } else {
            lines.add(I18n.format("drtech.apiarist_shield.tooltip.disabled"));
        }
        lines.add(I18n.format("drtech.modular_armor.usable"));
    }

    @Override
    public MetaItem<?>.MetaValueItem getMetaValueItem() {
        return DrMetaItems.APIARIST_SHIELD;
    }
}
