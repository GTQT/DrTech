package com.brachy84.mechtech.api.armor.modules;

import com.brachy84.mechtech.MechTech;
import com.brachy84.mechtech.api.armor.AbstractModule;
import com.brachy84.mechtech.api.armor.ModularArmor;
import com.brachy84.mechtech.common.items.MTMetaItems;
import com.brachy84.mechtech.network.NetworkHandler;
import com.brachy84.mechtech.network.packets.CModularArmorSwitchModuleMode;
import gregtech.api.GTValues;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IElectricItem;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.util.input.KeyBind;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;

import java.util.List;

/**
 * Anti-Gravity Module — grants creative flight to the player.
 * Slot: Chestplate
 * Continuously drains EU while flight is active.
 *
 * Key fix notes:
 * - Uses a dedicated keybind (MechTech.keys) to avoid conflicts with jetpack toggle.
 * - Tracks the previous flight state in NBT ("ag_was_flying") to only call
 *   sendPlayerAbilities() on actual state transitions, avoiding client-server
 *   ability sync storms that cause the "fly then immediately fall" bug.
 * - Default state is OFF (enabled=false) so the module doesn't interfere until
 *   the player explicitly activates it.
 */
public class AntiGravity extends AbstractModule {

    /** EU drained per tick while the player is actively flying */
    private static final int ENERGY_PER_TICK = 256;

    private byte toggleTimer = 0;

    public AntiGravity() {
        super("anti_gravity");
    }

    @Override
    public boolean canPlaceIn(EntityEquipmentSlot slot, ItemStack modularArmorPiece, IItemHandler modularSlots) {
        return slot == EntityEquipmentSlot.CHEST;
    }

    @Override
    public void onClientTick(World world, EntityPlayer player, ItemStack modularArmorPiece, NBTTagCompound armorData) {
        // Use a dedicated keybind to avoid conflict with jetpack hover toggle
        if (toggleTimer == 0 && KeyBind.ARMOR_MODE_SWITCH.isKeyDown(player)) {
            toggleTimer = 5;
            NetworkHandler.sendToServer(
                    new CModularArmorSwitchModuleMode(EntityEquipmentSlot.CHEST, "anti_gravity_enabled"));
        }

        if (toggleTimer > 0) {
            toggleTimer--;
        }
    }

    @Override
    public void onServerTick(World world, EntityPlayer player, ItemStack modularArmorPiece, NBTTagCompound armorData) {
        // Skip creative/spectator players — they already have flight
        if (player.isCreative() || player.isSpectator()) {
            return;
        }

        boolean enabled = armorData.getBoolean("anti_gravity_enabled");
        boolean wasFlying = armorData.getBoolean("ag_was_flying");

        if (enabled) {
            IElectricItem electricItem = modularArmorPiece.getCapability(
                    GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
            boolean hasEnergy = electricItem != null && electricItem.getCharge() > 0;

            if (hasEnergy) {
                // --- Enable flight (only sync on state change) ---
                if (!player.capabilities.allowFlying) {
                    player.capabilities.allowFlying = true;
                    player.sendPlayerAbilities();
                }

                // Drain energy only while actually flying
                if (player.capabilities.isFlying) {
                    long drained = electricItem.discharge(
                            ENERGY_PER_TICK, GTValues.HV, false, false, false);
                    // If we couldn't drain enough, energy just ran out
                    if (drained < ENERGY_PER_TICK && electricItem.getCharge() <= 0) {
                        setFlightDisabled(player, armorData);
                    }
                }
            } else {
                // Energy depleted while module is enabled
                setFlightDisabled(player, armorData);
            }
        } else {
            // Module is toggled off — only disable if we were the ones who enabled it
            if (wasFlying) {
                setFlightDisabled(player, armorData);
            }
        }

        // Track state for transition detection
        boolean isNowFlying = player.capabilities.allowFlying;
        if (isNowFlying != wasFlying) {
            armorData.setBoolean("ag_was_flying", isNowFlying);
            player.inventoryContainer.detectAndSendChanges();
        }
    }

    @Override
    public void onUnequip(World world, EntityLivingBase entity, ItemStack modularArmorPiece, ItemStack newStack) {
        if (!world.isRemote && entity instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) entity;
            if (!player.isCreative() && !player.isSpectator()) {
                setFlightDisabled(player, null);
            }
        }
    }

    /**
     * Disables flight and syncs abilities to the client exactly once.
     * Only calls sendPlayerAbilities() if allowFlying was actually true,
     * preventing redundant network packets.
     */
    private void setFlightDisabled(EntityPlayer player, NBTTagCompound armorData) {
        if (player.capabilities.allowFlying) {
            player.capabilities.allowFlying = false;
            player.capabilities.isFlying = false;
            player.sendPlayerAbilities();
        }
        if (armorData != null) {
            armorData.setBoolean("ag_was_flying", false);
        }
    }

    @Override
    public void addHUDInfo(ItemStack armorPiece, NBTTagCompound armorData, List<String> hudStrings) {
        if (armorData != null) {
            String status;
            if (armorData.getBoolean("anti_gravity_enabled")) {
                status = I18n.format("metaarmor.hud.status.enabled");
            } else {
                status = I18n.format("metaarmor.hud.status.disabled");
            }
            hudStrings.add(I18n.format("mechtech.anti_gravity.mode", status));
        }
    }

    @Override
    public void addInformation(ItemStack itemStack, List<String> lines) {
        lines.add(I18n.format("mechtech.anti_gravity.tooltip.1"));
        lines.add(I18n.format("mechtech.anti_gravity.tooltip.2", ENERGY_PER_TICK));
        lines.add(I18n.format("mechtech.modular_armor.usable"));
    }

    @Override
    public MetaItem<?>.MetaValueItem getMetaValueItem() {
        return MTMetaItems.ANTI_GRAVITY;
    }
}