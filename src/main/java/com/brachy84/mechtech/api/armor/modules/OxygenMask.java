package com.brachy84.mechtech.api.armor.modules;

import com.brachy84.mechtech.api.armor.AbstractModule;
import com.brachy84.mechtech.api.armor.ModularArmor;
import com.brachy84.mechtech.common.items.MTMetaItems;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.unification.material.Materials;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.IItemHandler;

public class OxygenMask extends AbstractModule {

    private static final int maxAir = 300;
    private static final float ratio = 1.7f;

    public OxygenMask() {
        super("oxygen_mask");
    }

    @Override
    public boolean canPlaceIn(EntityEquipmentSlot slot, ItemStack modularArmorPiece, IItemHandler modularSlots) {
        return slot == EntityEquipmentSlot.HEAD;
    }

    @Override
    public void onServerTick(World world, EntityPlayer player, ItemStack modularArmorPiece, NBTTagCompound armorData) {
        if (world.getTotalWorldTime() % 40 == 0) {
            int air = player.getAir();
            if (air < maxAir) {
                int oxygen = (int) (Math.min(40, maxAir - air) / ratio);
                if (oxygen <= 2) return;
                oxygen = drainOxygen(player, oxygen);
                player.setAir(air + (int) Math.min(300, Math.ceil(oxygen * ratio)));
            }
        }
    }

    public int drainOxygen(EntityPlayer player, int amount) {
        FluidStack toDrain = Materials.Oxygen.getFluid(amount);
        for (int i = 5; i > 1; i--) {
            EntityEquipmentSlot slot = EntityEquipmentSlot.values()[i];
            ItemStack armor = player.getItemStackFromSlot(slot);
            if (ModularArmor.get(armor) != null) {
                toDrain.amount -= ModularArmor.drainFluid(armor, toDrain, false);
                if (toDrain.amount <= 0) return amount;
            }
        }
        return amount - toDrain.amount;
    }

    @Override
    public MetaItem<?>.MetaValueItem getMetaValueItem() {
        return MTMetaItems.OXYGEN_MASK;
    }
}
