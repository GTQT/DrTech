package com.drppp.drtech.api.armor.modules;

import com.drppp.drtech.api.armor.IModule;
import com.drppp.drtech.common.items.MTMetaItems;
import gregtech.api.items.metaitem.MetaItem;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

public class ReceiverModule implements IModule {
    @Override
    public boolean canPlaceIn(EntityEquipmentSlot slot, ItemStack modularArmorPiece, IItemHandler modularSlots) {
        return slot == EntityEquipmentSlot.CHEST;
    }

    @Override
    public MetaItem<?>.MetaValueItem getMetaValueItem() {
        return MTMetaItems.WIRELESS_RECEIVER;
    }

    @Override
    public String getModuleId() {
        return "wireless_receiver";
    }
}
