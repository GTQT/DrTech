package com.brachy84.mechtech.api.armor.modules;

import com.brachy84.mechtech.api.armor.IModule;
import com.brachy84.mechtech.api.armor.Modules;
import com.google.common.collect.Lists;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IElectricItem;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.common.items.MetaItems;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;

import java.util.Collection;

public class SolarGen implements IModule {

    private final long gen;
    private final int tier;

    public SolarGen(long gen, int tier) {
        this.gen = gen;
        this.tier = tier;
    }

    @Override
    public void onServerTick(World world, EntityPlayer player, ItemStack modularArmorPiece, NBTTagCompound armorData) {
        if (world.canSeeSky(new BlockPos(player.posX, player.posY + player.getEyeHeight(), player.posZ))) {
            float sunBrightness = world.getSunBrightness(Minecraft.getMinecraft().getRenderPartialTicks());
            sunBrightness -= 0.2f;
            sunBrightness /= 0.8f; // undo mc's trickery
            if (sunBrightness > 0.1f) {
                int generated = (int) (gen * sunBrightness);
                for (int i = 3; i >= 0; i--) { // charge from head to feet
                    ItemStack stack = player.inventory.armorInventory.get(i);
                    if (stack.isEmpty())
                        continue;
                    IElectricItem item = stack.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
                    if (item == null)
                        continue;
                    generated -= item.charge(generated, Integer.MAX_VALUE, false, false);
                    if (generated <= 0)
                        break;
                }
                player.inventoryContainer.detectAndSendChanges();
            }
        }
    }


    @Override
    public boolean canPlaceIn(EntityEquipmentSlot slot, ItemStack modularArmorPiece, IItemHandler modularSlots) {
        return slot == EntityEquipmentSlot.HEAD;
    }

    @Override
    public Collection<IModule> getIncompatibleModules() {
        return Lists.newArrayList(Modules.SOLAR_GEN_I, Modules.SOLAR_GEN_II, Modules.SOLAR_GEN_III);
    }

    @Override
    public String getModuleId() {
        return "solar_gen." + tier;
    }

    @Override
    public MetaItem<?>.MetaValueItem getMetaValueItem() {
        switch (tier) {
            case 1: return MetaItems.COVER_SOLAR_PANEL_LV;
            case 2: return MetaItems.COVER_SOLAR_PANEL_MV;
            case 3: return MetaItems.COVER_SOLAR_PANEL_HV;
            case 4: return MetaItems.COVER_SOLAR_PANEL_EV;
            case 5: return MetaItems.COVER_SOLAR_PANEL_IV;
            case 6: return MetaItems.COVER_SOLAR_PANEL_LUV;
            case 7: return MetaItems.COVER_SOLAR_PANEL_ZPM;
            case 8: return MetaItems.COVER_SOLAR_PANEL_UV;
        }
        throw new IllegalArgumentException("Invalid tier: " + tier);
    }
}
