package com.brachy84.mechtech.api.armor.modules;

import com.brachy84.mechtech.api.armor.IModule;
import com.brachy84.mechtech.api.armor.ModularArmor;
import com.brachy84.mechtech.api.armor.Modules;
import com.brachy84.mechtech.network.NetworkHandler;
import com.brachy84.mechtech.network.packets.CModularArmorDrainEnergy;
import com.brachy84.mechtech.network.packets.CModularArmorSwitchModuleMode;
import com.google.common.collect.Lists;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IElectricItem;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.util.input.KeyBind;
import gregtech.common.items.MetaItems;
import gregtech.common.items.armor.IJetpack;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;

public class JetpackModule implements IJetpack, IModule {

    private byte toggleTimer = 0;

    @Override
    public Collection<IModule> getIncompatibleModules() {
        return Lists.newArrayList(Modules.ADVANCED_JETPACK);
    }

    @Override
    public boolean canPlaceIn(EntityEquipmentSlot slot, ItemStack modularArmorPiece, IItemHandler modularSlots) {
        return slot == EntityEquipmentSlot.CHEST;
    }

    @Override
    public void onClientTick(World world, EntityPlayer player, ItemStack modularArmorPiece, NBTTagCompound armorData) {
        boolean hover = false;
        boolean cancelInertia = false;

        if (armorData.hasKey("hover")) {
            hover = armorData.getBoolean("hover");
        }

        ItemStack stack = player.getItemStackFromSlot(EntityEquipmentSlot.FEET);
        if (stack != null && ModularArmor.get(stack) != null) {
            NBTTagCompound nbt = ModularArmor.getArmorData(stack);
            if (nbt.hasKey("cancel_inertia")) {
                cancelInertia = nbt.getBoolean("cancel_inertia");
            }
        }

        if (toggleTimer == 0 && KeyBind.ARMOR_HOVER.isKeyDown(player)) {
            toggleTimer = 5;
            NetworkHandler.sendToServer(new CModularArmorSwitchModuleMode(EntityEquipmentSlot.CHEST, "hover"));
        }

        if (toggleTimer > 0) {
            --toggleTimer;
        }

        this.performFlying(player, hover, cancelInertia, modularArmorPiece);
    }

    @Override
    public int getEnergyPerUse() {
        return 32;
    }

    @Override
    public boolean canUseEnergy(@Nonnull ItemStack stack, int amount) {
        IElectricItem container = this.getIElectricItem(stack);
        return container != null && container.canUse(amount);
    }

    @Override
    public void drainEnergy(@Nonnull ItemStack stack, int amount) {
        NetworkHandler.sendToServer(new CModularArmorDrainEnergy(ModularArmor.get(stack).getSlot(), amount));
    }

    @Override
    public boolean hasEnergy(@Nonnull ItemStack stack) {
        IElectricItem container = this.getIElectricItem(stack);
        if (container == null) {
            return false;
        } else {
            return container.getCharge() > 0L;
        }
    }

    private IElectricItem getIElectricItem(@Nonnull ItemStack stack) {
        return stack.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
    }

    @Override
    public void addHUDInfo(ItemStack item, NBTTagCompound armorData, List<String> hudStrings) {
        if (armorData != null) {
            String status = I18n.format("metaarmor.hud.status.disabled");
            if (armorData.hasKey("hover")) {
                 status = (armorData.getBoolean("hover") ? I18n.format("metaarmor.hud.status.enabled") :
                        I18n.format("metaarmor.hud.status.disabled"));
            }
            String result = I18n.format("metaarmor.hud.hover_mode", status);
            hudStrings.add(result);
        }
    }

    @Override
    public double getVerticalHoverSpeed() {
        return 0.18D;
    }

    @Override
    public double getVerticalHoverSlowSpeed() {
        return 0.1D;
    }

    @Override
    public double getVerticalAcceleration() {
        return 0.12D;
    }

    @Override
    public double getVerticalSpeed() {
        return 0.3D;
    }

    @Override
    public double getSidewaysSpeed() {
        return 0.08D;
    }

    @Override
    public EnumParticleTypes getParticle() {
        return EnumParticleTypes.SMOKE_NORMAL;
    }

    @Override
    public String getModuleId() {
        return "jetpack";
    }

    @Override
    public MetaItem<?>.MetaValueItem getMetaValueItem() {
        return MetaItems.ELECTRIC_JETPACK;
    }
}
