package com.brachy84.mechtech.api.armor.modules;

import com.brachy84.mechtech.api.armor.AbstractModule;
import com.brachy84.mechtech.api.armor.IModule;
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
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;

import java.util.List;

/**
 * Healing Module — applies Regeneration to the player.
 * Slot: HEAD, CHEST, LEGS, FEET (all armor slots)
 *
 * Multiple healing modules stack the regeneration amplifier:
 *   1 module  → Regeneration I
 *   2 modules → Regeneration II
 *   3 modules → Regeneration III
 *   4 modules → Regeneration IV
 *
 * Each module drains EU independently.
 */
public class HealingModule extends AbstractModule {

    /** EU drained every application cycle (once per second) */
    private static final int ENERGY_PER_CYCLE = 128;

    /** Potion re-application interval in ticks (20 ticks = 1 second) */
    private static final int APPLY_INTERVAL = 20;

    /** Duration of the potion effect in ticks (slightly longer than interval to avoid flicker) */
    private static final int EFFECT_DURATION = 45;

    public HealingModule() {
        super("healing_module");
    }

    @Override
    public int maxModules() {
        // Only 1 per armor piece, but can have one in each of the 4 armor slots
        return 1;
    }

    @Override
    public boolean canPlaceIn(EntityEquipmentSlot slot, ItemStack modularArmorPiece, IItemHandler modularSlots) {
        // Allowed in all armor slots
        return slot == EntityEquipmentSlot.HEAD
                || slot == EntityEquipmentSlot.CHEST
                || slot == EntityEquipmentSlot.LEGS
                || slot == EntityEquipmentSlot.FEET;
    }

    @Override
    public void onServerTick(World world, EntityPlayer player, ItemStack modularArmorPiece, NBTTagCompound armorData) {
        if (world.getTotalWorldTime() % APPLY_INTERVAL != 0) {
            return;
        }

        // Only the helmet piece drives the combined regen effect to avoid redundant application
        ModularArmor modularArmor = ModularArmor.get(modularArmorPiece);
        if (modularArmor == null) return;
        EntityEquipmentSlot thisSlot = modularArmor.getSlot();

        // Let the "lowest" slot drive the combined effect to avoid re-applying 4 times
        EntityEquipmentSlot driverSlot = findLowestHealingSlot(player);
        if (driverSlot != thisSlot) {
            return;
        }

        // Count how many healing modules the player is wearing across all armor pieces
        int amplifier = countHealingModules(player) - 1; // amplifier 0 = Regen I
        if (amplifier < 0) return;

        // Check if we can drain energy from this armor piece
        IElectricItem electricItem = modularArmorPiece.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
        if (electricItem == null || !electricItem.canUse(ENERGY_PER_CYCLE)) {
            return;
        }
        electricItem.discharge(ENERGY_PER_CYCLE, Integer.MAX_VALUE, false, false, false);

        // Apply regeneration
        player.addPotionEffect(new PotionEffect(MobEffects.REGENERATION, EFFECT_DURATION, amplifier, true, false));
        player.inventoryContainer.detectAndSendChanges();
    }

    @Override
    public void onUnequip(World world, EntityLivingBase entity, ItemStack modularArmorPiece, ItemStack newStack) {
        if (!world.isRemote && entity instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) entity;
            // Recount after this piece is removed; if no healing modules remain, clear the effect
            int remaining = countHealingModules(player) - 1; // -1 because this piece hasn't been removed yet in the inventory
            if (remaining <= 0) {
                player.removePotionEffect(MobEffects.REGENERATION);
            }
        }
    }

    /**
     * Counts the total number of healing modules across all worn modular armor pieces.
     */
    private int countHealingModules(EntityPlayer player) {
        int count = 0;
        for (int i = 0; i < 4; i++) {
            ItemStack armorStack = player.inventory.armorInventory.get(i);
            if (armorStack.isEmpty()) continue;
            List<IModule> modules = ModularArmor.getModulesOf(armorStack);
            for (IModule module : modules) {
                if (module instanceof HealingModule) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * Finds the lowest equipment slot index that contains a healing module.
     * This slot will be the "driver" that applies the combined regeneration effect.
     */
    private EntityEquipmentSlot findLowestHealingSlot(EntityPlayer player) {
        EntityEquipmentSlot[] slots = {
                EntityEquipmentSlot.FEET,   // index 0
                EntityEquipmentSlot.LEGS,   // index 1
                EntityEquipmentSlot.CHEST,  // index 2
                EntityEquipmentSlot.HEAD    // index 3
        };
        for (EntityEquipmentSlot slot : slots) {
            ItemStack armorStack = player.getItemStackFromSlot(slot);
            if (armorStack.isEmpty()) continue;
            List<IModule> modules = ModularArmor.getModulesOf(armorStack);
            for (IModule module : modules) {
                if (module instanceof HealingModule) {
                    return slot;
                }
            }
        }
        return EntityEquipmentSlot.HEAD; // fallback, should not happen
    }

    @Override
    public void addHUDInfo(ItemStack armorPiece, NBTTagCompound armorData, List<String> hudStrings) {
        hudStrings.add(I18n.format("mechtech.healing_module.active"));
    }

    @Override
    public void addInformation(ItemStack itemStack, List<String> lines) {
        lines.add(I18n.format("mechtech.healing_module.tooltip.1"));
        lines.add(I18n.format("mechtech.healing_module.tooltip.2", ENERGY_PER_CYCLE));
        lines.add(I18n.format("mechtech.healing_module.tooltip.3"));
        lines.add(I18n.format("mechtech.modular_armor.usable"));
    }

    @Override
    public MetaItem<?>.MetaValueItem getMetaValueItem() {
        return MTMetaItems.HEALING_MODULE;
    }
}
