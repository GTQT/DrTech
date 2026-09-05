package com.brachy84.mechtech.api.armor.modules;

import com.brachy84.mechtech.api.armor.AbstractModule;
import com.brachy84.mechtech.api.armor.IModule;
import com.brachy84.mechtech.api.armor.ModularArmor;
import com.brachy84.mechtech.common.items.MTMetaItems;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IElectricItem;
import gregtech.api.items.metaitem.MetaItem;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.items.IItemHandler;

import java.util.List;

/**
 * Revealing Goggles Module — Thaumcraft 6 Goggles of Revealing.
 * Slot: Helmet
 *
 * Drains EU per tick to maintain the revealing effect.
 * The actual IGoggles interface is on MTArmorItem, which delegates to isActive().
 */
public class RevealingGoggles extends AbstractModule {

    private static final int ENERGY_PER_TICK = 8;

    public RevealingGoggles() {
        super("revealing_goggles");
    }

    @Override
    public boolean canPlaceIn(EntityEquipmentSlot slot, ItemStack modularArmorPiece, IItemHandler modularSlots) {
        return slot == EntityEquipmentSlot.HEAD;
    }

    @Override
    public void onServerTick(World world, EntityPlayer player, ItemStack modularArmorPiece, NBTTagCompound armorData) {
        if (!Loader.isModLoaded("thaumcraft")) {
            return;
        }

        IElectricItem electricItem = modularArmorPiece.getCapability(
                GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
        if (electricItem != null && electricItem.canUse(ENERGY_PER_TICK)) {
            electricItem.discharge(ENERGY_PER_TICK, Integer.MAX_VALUE, false, false, false);
            armorData.setBoolean("goggles_active", true);
        } else {
            armorData.setBoolean("goggles_active", false);
        }
    }

    /**
     * Called by MTArmorItem's IGoggles.showIngamePopups() delegation.
     * Checks BOTH the NBT flag AND that the module is still installed.
     */
    public static boolean isActive(ItemStack armorPiece) {
        // First verify the module is actually still installed in this armor piece
        List<IModule> modules = ModularArmor.getModulesOf(armorPiece);
        boolean hasModule = false;
        for (IModule module : modules) {
            if (module instanceof RevealingGoggles) {
                hasModule = true;
                break;
            }
        }
        if (!hasModule) {
            return false;
        }
        NBTTagCompound armorData = ModularArmor.getArmorData(armorPiece);
        return armorData.getBoolean("goggles_active");
    }

    @Override
    public void onUnequip(World world, EntityLivingBase entity, ItemStack modularArmorPiece, ItemStack newStack) {
        if (!world.isRemote) {
            NBTTagCompound armorData = ModularArmor.getArmorData(modularArmorPiece);
            armorData.setBoolean("goggles_active", false);
            ModularArmor.setArmorData(modularArmorPiece, armorData);
        }
    }

    @Override
    public void addHUDInfo(ItemStack armorPiece, NBTTagCompound armorData, List<String> hudStrings) {
        if (Loader.isModLoaded("thaumcraft")) {
            boolean active = armorData.getBoolean("goggles_active");
            String status = active
                    ? I18n.format("metaarmor.hud.status.enabled")
                    : I18n.format("metaarmor.hud.status.disabled");
            hudStrings.add(I18n.format("mechtech.revealing_goggles.mode", status));
        }
    }

    @Override
    public void addInformation(ItemStack itemStack, List<String> lines) {
        if (Loader.isModLoaded("thaumcraft")) {
            lines.add(I18n.format("mechtech.revealing_goggles.tooltip.1"));
            lines.add(I18n.format("mechtech.revealing_goggles.tooltip.2", ENERGY_PER_TICK));
        } else {
            lines.add(I18n.format("mechtech.revealing_goggles.tooltip.disabled"));
        }
        lines.add(I18n.format("mechtech.modular_armor.usable"));
    }

    @Override
    public MetaItem<?>.MetaValueItem getMetaValueItem() {
        return MTMetaItems.REVEALING_GOGGLES;
    }
}
