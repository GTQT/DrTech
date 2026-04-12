package com.brachy84.mechtech.api.armor.modules;

import com.brachy84.mechtech.api.armor.AbstractModule;
import com.brachy84.mechtech.api.armor.ModularArmor;
import com.brachy84.mechtech.common.items.MTMetaItems;
import com.brachy84.mechtech.network.NetworkHandler;
import com.brachy84.mechtech.network.packets.CModularArmorSwitchModuleMode;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IElectricItem;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.util.input.KeyBind;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;

import java.util.List;
import java.util.UUID;

/**
 * Sprint Module — doubles the player's sprint speed and flight speed.
 * Slot: Leggings
 * Drains EU while the player is sprinting or flying.
 */
public class SprintModule extends AbstractModule {

    private static final UUID SPRINT_MODIFIER_UUID = UUID.fromString("d6103cbc-b90b-4c4a-b3ee-1e89effcf5c8");
    private static final String SPRINT_MODIFIER_NAME = "mechtech.sprint_module";

    /** EU drained per tick while sprinting */
    private static final int ENERGY_PER_TICK_SPRINT = 64;
    /** EU drained per tick while flying */
    private static final int ENERGY_PER_TICK_FLY = 128;

    /** Speed multiplier: 1.0 = +100% = doubled speed */
    private static final double SPEED_MULTIPLIER = 1.0;

    private byte toggleTimer = 0;

    public SprintModule() {
        super("sprint_module");
    }

    @Override
    public boolean canPlaceIn(EntityEquipmentSlot slot, ItemStack modularArmorPiece, IItemHandler modularSlots) {
        return slot == EntityEquipmentSlot.LEGS;
    }

    @Override
    public void onClientTick(World world, EntityPlayer player, ItemStack modularArmorPiece, NBTTagCompound armorData) {
        if (toggleTimer == 0 && KeyBind.ARMOR_MODE_SWITCH.isKeyDown(player)) {
            toggleTimer = 5;
            NetworkHandler.sendToServer(new CModularArmorSwitchModuleMode(EntityEquipmentSlot.LEGS, "sprint_enabled"));
        }

        if (toggleTimer > 0) {
            toggleTimer--;
        }
    }

    @Override
    public void onServerTick(World world, EntityPlayer player, ItemStack modularArmorPiece, NBTTagCompound armorData) {
        boolean enabled = true;
        if (armorData.hasKey("sprint_enabled")) {
            enabled = armorData.getBoolean("sprint_enabled");
        }

        IElectricItem electricItem = modularArmorPiece.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
        boolean hasEnergy = electricItem != null && electricItem.getCharge() > 0;

        if (enabled && hasEnergy) {
            boolean isMovingFast = player.isSprinting() || player.capabilities.isFlying;

            if (isMovingFast) {
                // Drain energy
                int cost = player.capabilities.isFlying ? ENERGY_PER_TICK_FLY : ENERGY_PER_TICK_SPRINT;
                electricItem.discharge(cost, Integer.MAX_VALUE, false, false, false);

                // Apply speed boost
                applySpeedBoost(player);

                // Boost flight speed
                if (player.capabilities.isFlying) {
                    player.capabilities.setFlySpeed(0.05f * 2.0f); // default 0.05, doubled
                    player.sendPlayerAbilities();
                }
            } else {
                removeSpeedBoost(player);
                resetFlySpeed(player);
            }
        } else {
            removeSpeedBoost(player);
            resetFlySpeed(player);
        }

        player.inventoryContainer.detectAndSendChanges();
    }

    @Override
    public void onUnequip(World world, EntityLivingBase entity, ItemStack modularArmorPiece, ItemStack newStack) {
        if (!world.isRemote && entity instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) entity;
            removeSpeedBoost(player);
            resetFlySpeed(player);
        }
    }

    private void applySpeedBoost(EntityPlayer player) {
        IAttributeInstance attribute = player.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
        if (attribute.getModifier(SPRINT_MODIFIER_UUID) == null) {
            // Operation 1 = multiply base by (1 + amount), so amount=1.0 doubles the speed
            AttributeModifier modifier = new AttributeModifier(
                    SPRINT_MODIFIER_UUID, SPRINT_MODIFIER_NAME, SPEED_MULTIPLIER, 1
            );
            attribute.applyModifier(modifier);
        }
    }

    private void removeSpeedBoost(EntityPlayer player) {
        IAttributeInstance attribute = player.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
        if (attribute.getModifier(SPRINT_MODIFIER_UUID) != null) {
            attribute.removeModifier(SPRINT_MODIFIER_UUID);
        }
    }

    private void resetFlySpeed(EntityPlayer player) {
        if (player.capabilities.getFlySpeed() != 0.05f) {
            player.capabilities.setFlySpeed(0.05f);
            player.sendPlayerAbilities();
        }
    }

    @Override
    public void addHUDInfo(ItemStack armorPiece, NBTTagCompound armorData, List<String> hudStrings) {
        if (armorData != null) {
            String status = I18n.format("metaarmor.hud.status.enabled");
            if (armorData.hasKey("sprint_enabled")) {
                status = armorData.getBoolean("sprint_enabled")
                        ? I18n.format("metaarmor.hud.status.enabled")
                        : I18n.format("metaarmor.hud.status.disabled");
            }
            hudStrings.add(I18n.format("mechtech.sprint_module.mode", status));
        }
    }

    @Override
    public MetaItem<?>.MetaValueItem getMetaValueItem() {
        return MTMetaItems.SPRINT_MODULE;
    }
}
