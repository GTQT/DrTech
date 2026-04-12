package com.brachy84.mechtech.api.armor.modules;

import com.brachy84.mechtech.api.armor.AbstractModule;
import com.brachy84.mechtech.common.items.MTMetaItems;
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
 * is implemented on MTArmorItem, which checks for this module.
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
            lines.add(I18n.format("mechtech.apiarist_shield.tooltip.1"));
        } else {
            lines.add(I18n.format("mechtech.apiarist_shield.tooltip.disabled"));
        }
        lines.add(I18n.format("mechtech.modular_armor.usable"));
    }

    @Override
    public MetaItem<?>.MetaValueItem getMetaValueItem() {
        return MTMetaItems.APIARIST_SHIELD;
    }
}
