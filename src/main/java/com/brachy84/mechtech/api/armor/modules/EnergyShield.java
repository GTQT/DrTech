package com.brachy84.mechtech.api.armor.modules;

import com.brachy84.mechtech.api.armor.AbsorbResult;
import com.brachy84.mechtech.api.armor.AbstractModule;
import com.brachy84.mechtech.api.armor.ISpecialArmorModule;
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
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;

import java.util.List;

/**
 * Energy Shield Unit — uses stored EU to absorb ALL incoming damage,
 * including unblockable damage sources (void, /kill excluded via game design).
 * Slot: Chestplate
 *
 * Conversion: 1 damage point = {@link #EU_PER_DAMAGE} EU.
 * The shield absorb ratio and max are computed dynamically based on
 * available charge so it never promises to absorb more than it can pay for.
 */
public class EnergyShield extends AbstractModule implements ISpecialArmorModule {

    /** EU consumed per half-heart of damage absorbed */
    private static final long EU_PER_DAMAGE = 1000;

    /** Maximum damage points absorbable in a single hit */
    private static final int MAX_ABSORB = 100;

    /** Priority — higher than normal armor modules so the shield is checked first */
    private static final int PRIORITY = 20;

    private byte toggleTimer = 0;

    public EnergyShield() {
        super("energy_shield");
    }

    @Override
    public boolean canPlaceIn(EntityEquipmentSlot slot, ItemStack modularArmorPiece, IItemHandler modularSlots) {
        return slot == EntityEquipmentSlot.CHEST;
    }

    @Override
    public void onClientTick(World world, EntityPlayer player, ItemStack modularArmorPiece, NBTTagCompound armorData) {
        if (toggleTimer == 0 && KeyBind.ARMOR_CHARGING.isKeyDown(player)) {
            toggleTimer = 5;
            NetworkHandler.sendToServer(new CModularArmorSwitchModuleMode(EntityEquipmentSlot.CHEST, "shield_enabled"));
        }

        if (toggleTimer > 0) {
            toggleTimer--;
        }
    }

    @Override
    public void onServerTick(World world, EntityPlayer player, ItemStack modularArmorPiece, NBTTagCompound armorData) {
        // No periodic tick logic needed — shield activates on damage via getArmorProperties
    }

    @Override
    public AbsorbResult getArmorProperties(EntityLivingBase entity, ItemStack modularArmorPiece, NBTTagCompound moduleData, DamageSource source, double damage, EntityEquipmentSlot slot) {
        // Check if the shield is enabled (default: on)
        if (entity instanceof EntityPlayer) {
            NBTTagCompound armorData = ModularArmor.getArmorData(modularArmorPiece);
            if (armorData.hasKey("shield_enabled") && !armorData.getBoolean("shield_enabled")) {
                return AbsorbResult.ZERO;
            }
        }

        // Don't try to block /kill (OUT_OF_WORLD with very high damage) or starvation
        if (source == DamageSource.OUT_OF_WORLD || source == DamageSource.STARVE) {
            return AbsorbResult.ZERO;
        }

        IElectricItem electricItem = modularArmorPiece.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
        if (electricItem == null || electricItem.getCharge() <= 0) {
            return AbsorbResult.ZERO;
        }

        // Calculate how much damage we can afford to absorb
        long availableCharge = electricItem.getCharge();
        int affordableDamage = (int) Math.min(MAX_ABSORB, availableCharge / EU_PER_DAMAGE);

        if (affordableDamage <= 0) {
            return AbsorbResult.ZERO;
        }

        // Absorb ratio: try to absorb all damage, capped by what we can afford
        double ratio = Math.min(1.0, (double) affordableDamage / damage);

        // Drain the energy for absorbed damage
        long actualDamageAbsorbed = (long) Math.min(affordableDamage, Math.ceil(damage * ratio));
        long energyCost = actualDamageAbsorbed * EU_PER_DAMAGE;
        electricItem.discharge(energyCost, Integer.MAX_VALUE, false, false, false);

        return new AbsorbResult(ratio, affordableDamage, PRIORITY);
    }

    @Override
    public void addHUDInfo(ItemStack armorPiece, NBTTagCompound armorData, List<String> hudStrings) {
        if (armorData != null) {
            String status = I18n.format("metaarmor.hud.status.enabled");
            if (armorData.hasKey("shield_enabled")) {
                status = armorData.getBoolean("shield_enabled")
                        ? I18n.format("metaarmor.hud.status.enabled")
                        : I18n.format("metaarmor.hud.status.disabled");
            }
            hudStrings.add(I18n.format("mechtech.energy_shield.mode", status));
        }
    }

    @Override
    public void addInformation(ItemStack itemStack, List<String> lines) {
        lines.add(I18n.format("mechtech.energy_shield.tooltip.1"));
        lines.add(I18n.format("mechtech.energy_shield.tooltip.2", EU_PER_DAMAGE));
        lines.add(I18n.format("mechtech.energy_shield.tooltip.3", MAX_ABSORB));
        lines.add(I18n.format("mechtech.modular_armor.usable"));
    }

    @Override
    public MetaItem<?>.MetaValueItem getMetaValueItem() {
        return MTMetaItems.ENERGY_SHIELD;
    }
}
