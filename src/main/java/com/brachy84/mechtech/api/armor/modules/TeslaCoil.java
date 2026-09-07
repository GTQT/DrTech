package com.brachy84.mechtech.api.armor.modules;

import baubles.api.BaublesApi;
import baubles.api.cap.IBaublesItemHandler;
import com.brachy84.mechtech.MechTech;
import com.brachy84.mechtech.api.armor.IModule;
import com.brachy84.mechtech.api.armor.ModularArmor;
import com.brachy84.mechtech.common.MTConfig;
import com.brachy84.mechtech.common.items.MTMetaItems;
import com.brachy84.mechtech.network.NetworkHandler;
import com.brachy84.mechtech.network.packets.CModularArmorSwitchModuleMode;
import com.brachy84.mechtech.network.packets.STeslaCoilEffect;
import com.brachy84.mechtech.network.packets.CTeslaCoilModeSwitch;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IElectricItem;
import gregtech.api.damagesources.DamageSources;
import gregtech.api.items.armor.ArmorUtils;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.util.Mods;
import gregtech.api.util.input.KeyBind;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.items.IItemHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TeslaCoil implements IModule {

    private byte toggleTimer = 0;

    @Override
    public boolean canPlaceIn(EntityEquipmentSlot slot, ItemStack modularArmorPiece, IItemHandler modularSlots) {
        return slot == EntityEquipmentSlot.HEAD || slot == EntityEquipmentSlot.CHEST;
    }

    @Override
    public void onClientTick(World world, EntityPlayer player, ItemStack modularArmorPiece, NBTTagCompound armorData) {

        EntityEquipmentSlot slot = ModularArmor.get(modularArmorPiece).getEquipmentSlot(modularArmorPiece);
        if (toggleTimer == 0 && MechTech.keys.TESLA_COIL_MODE_SWITCH.isKeyDown(player) && MechTech.keys.TESLA_COIL_MODE_SWITCH.isPressed(player)) {
            toggleTimer = 5;
            NetworkHandler.sendToServer(new CTeslaCoilModeSwitch(slot == EntityEquipmentSlot.HEAD)); // Workaround since it seems like we cannot send NBT changes Client -> Server
        }
        if (toggleTimer == 0 && KeyBind.ARMOR_CHARGING.isKeyDown(player)) { // Not quite sure why but down here we only need to check isKeyDown and isPressed is false
            toggleTimer = 5;
            NetworkHandler.sendToServer(new CModularArmorSwitchModuleMode(slot, "charge_mode"));
        }

        if (toggleTimer > 0) {
            toggleTimer--;
        }
    }

    @Override
    public void onServerTick(World world, EntityPlayer player, ItemStack modularArmorPiece, NBTTagCompound armorData) {
        byte mode = 1;
        boolean chargeItems = true;
        if (armorData.hasKey("tesla_mode")) {
            mode = armorData.getByte("tesla_mode");
        }
        if (armorData.hasKey("charge_mode")) {
            chargeItems = armorData.getBoolean("charge_mode");
        }

        if (world.getTotalWorldTime() % 4 == 0) {
            if (!modularArmorPiece.hasCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null)) {
                return;
            }

            // Item Charging
            if (chargeItems) {
                List<ItemStack> inventories = new ArrayList<>();
                inventories.addAll(player.inventory.mainInventory);
                inventories.addAll(player.inventory.offHandInventory);
                inventories.addAll(player.inventory.armorInventory);

                for (ItemStack stack : inventories) {
                    chargeItem(modularArmorPiece, stack);
                }

                // Thanks Forge/Baubles for forcing me to make a second iterator
                if (Mods.Baubles.isModLoaded()) {
                    IBaublesItemHandler baublesItemHandler = BaublesApi.getBaublesHandler(player);
                    for (int i = 0; i < baublesItemHandler.getSlots(); i++) {
                        chargeItem(modularArmorPiece, baublesItemHandler.getStackInSlot(i));
                    }
                }
            }

            // Attack Mobs
            if (mode != 0) {
                IElectricItem electricItem = modularArmorPiece.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
                double range = MTConfig.modularArmor.modules.teslaCoilRange;
                double damage = MTConfig.modularArmor.modules.teslaCoilDamage;
                double maxEntities = MTConfig.modularArmor.modules.teslaCoilMaxEntitiesPerSecond;
                double edRatio = MTConfig.modularArmor.modules.teslaCoilDamageEnergyRatio;

                if (electricItem.getMaxCharge() < edRatio)
                    return;
                AxisAlignedBB box = new AxisAlignedBB(player.getPosition()).grow(range);
                List<Entity> livings = world.getEntitiesInAABBexcluding(player, box, entity -> entity instanceof EntityLivingBase && entity.isEntityAlive() && !(entity instanceof EntityPlayer));
                Collections.shuffle(livings);
                int count = 0;
                for (Entity entity : livings) {
                    if (mode == 1 && !(entity instanceof EntityMob)) {
                        continue;
                    } else if (mode == 2 && !(entity instanceof EntityAnimal)) {
                        continue;
                    }

                    EntityLivingBase living = (EntityLivingBase) entity;
                    float dmg = (float) Math.min(damage, living.getHealth());
                    long energy = (long) (dmg * edRatio);

                    electricItem.discharge(energy, Integer.MAX_VALUE, false, false, true);

                    if (living.attackEntityFrom(DamageSources.getElectricDamage(), (float) damage)) {
                        playEffects(player, living);
                        if (++count == maxEntities)
                            break;
                    }
                }
            }
        }
    }

    private void chargeItem(ItemStack armor, ItemStack stack) {
        if (armor == stack) return;
        IElectricItem armorElectricItem = armor.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
        if (stack.hasCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null)) {
            IElectricItem electricItem = stack.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
            if (electricItem.chargeable()) {
                long transferLimit = Math.min(electricItem.getTransferLimit(), armorElectricItem.getTransferLimit());
                long chargeToTake = electricItem.charge(transferLimit, Integer.MAX_VALUE, true, true);
                if (chargeToTake > 0 && armorElectricItem.getCharge() > 0) {
                    long chargeTaken = armorElectricItem.discharge(chargeToTake, Integer.MAX_VALUE, false, false, false);
                    electricItem.charge(chargeTaken, Integer.MAX_VALUE, true, false);
                }
            }
        } else if (stack.hasCapability(CapabilityEnergy.ENERGY, null)) {
            IEnergyStorage energyStorage = stack.getCapability(CapabilityEnergy.ENERGY, null);
            if (energyStorage.canReceive() && energyStorage.getEnergyStored() != energyStorage.getMaxEnergyStored()) {
                if (armorElectricItem.getCharge() > 0) {
                    long chargeToTake = Math.min(armorElectricItem.getTransferLimit(), energyStorage.getMaxEnergyStored() - energyStorage.getEnergyStored());
                    if (chargeToTake > 0) {
                        int chargeTaken = (int) armorElectricItem.discharge(armorElectricItem.getTransferLimit(), Integer.MAX_VALUE, false, false, false);
                        energyStorage.receiveEnergy(chargeTaken, false);
                    }
                }
            }
        }
    }

    @Override
    public String getModuleId() {
        return "tesla_coil";
    }

    @Override
    public MetaItem<?>.MetaValueItem getMetaValueItem() {
        return MTMetaItems.TESLA_COIL;
    }

    private void playEffects(EntityPlayer source, Entity target) {
        double targetY = target.posY + target.height / 2.0;
        double sourceY = source.posY + 2.2;
        STeslaCoilEffect packet = new STeslaCoilEffect(new Vec3d(source.posX, sourceY, source.posZ), new Vec3d(target.posX, targetY, target.posZ));
        NetworkRegistry.TargetPoint targetPoint = new NetworkRegistry.TargetPoint(source.dimension, source.posX, source.posY, source.posZ, 20);
        NetworkHandler.sendToAllAround(packet, targetPoint);
    }

    @Override
    public void addHUDInfo(ItemStack armorPiece, NBTTagCompound armorData, List<String> hudStrings) {
        if (armorData != null) {
            if (armorData.hasKey("tesla_mode")) {
                byte teslaMode = armorData.getByte("tesla_mode");
                switch (teslaMode) {
                    case 0 -> hudStrings.add(I18n.format("mechtech.tesla_coil.attack_mode_switch", I18n.format("metaarmor.hud.status.disabled")));
                    case 1 -> hudStrings.add(I18n.format("mechtech.tesla_coil.attack_mode_switch", I18n.format("mechtech.tesla_coil.attack_mode_switch.monsters")));
                    case 2 -> hudStrings.add(I18n.format("mechtech.tesla_coil.attack_mode_switch", I18n.format("mechtech.tesla_coil.attack_mode_switch.animals")));
                    case 3 -> hudStrings.add(I18n.format("mechtech.tesla_coil.attack_mode_switch", I18n.format("mechtech.tesla_coil.attack_mode_switch.animals_and_monsters")));
                }
            }

            String status = I18n.format("metaarmor.hud.status.disabled");
            if (armorData.hasKey("charge_mode")) {
                status = (armorData.getBoolean("charge_mode") ? I18n.format("metaarmor.hud.status.enabled") :
                        I18n.format("metaarmor.hud.status.disabled"));
            }
            String result = I18n.format("mataarmor.hud.supply_mode", status);
            hudStrings.add(result);
        }
    }
}
