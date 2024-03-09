package com.drppp.drtech.Items.Behavior;

import gregtech.api.GTValues;
import gregtech.api.capability.FeCompat;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IElectricItem;
import gregtech.api.capability.impl.ElectricItem;
import gregtech.api.items.metaitem.ElectricStats;
import gregtech.api.items.metaitem.stats.*;
import gregtech.api.util.Mods;
import gregtech.common.ConfigHolder;
import gregtech.integration.baubles.BaublesModule;
import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

public class ElectricStatsNuclear implements IItemComponent, IItemCapabilityProvider, IItemMaxStackSizeProvider, IItemBehaviour, ISubItemHandler {
    public static final ElectricStatsNuclear EMPTY = new ElectricStatsNuclear(0L, 0L, false, false);
    public final long maxCharge;
    public final int tier;
    public final boolean chargeable;
    public final boolean dischargeable;

    public ElectricStatsNuclear(long maxCharge, long tier, boolean chargeable, boolean dischargeable) {
        this.maxCharge = maxCharge;
        this.tier = (int)tier;
        this.chargeable = chargeable;
        this.dischargeable = dischargeable;
    }

    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack itemStack = player.getHeldItem(hand);
        IElectricItem electricItem = (IElectricItem)itemStack.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, (EnumFacing)null);
        if (electricItem != null && electricItem.canProvideChargeExternally() && player.isSneaking()) {
            if (!world.isRemote) {
                boolean isInDischargeMode = isInDischargeMode(itemStack);
                String locale = "metaitem.electric.discharge_mode." + (isInDischargeMode ? "disabled" : "enabled");
                player.sendStatusMessage(new TextComponentTranslation(locale, new Object[0]), true);
                setInDischargeMode(itemStack, !isInDischargeMode);
            }

            return ActionResult.newResult(EnumActionResult.SUCCESS, itemStack);
        } else {
            return ActionResult.newResult(EnumActionResult.PASS, itemStack);
        }
    }

    public void onUpdate(ItemStack itemStack, Entity entity) {
        IElectricItem electricItem = (IElectricItem)itemStack.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, (EnumFacing)null);
        long amount = (long)((double)GTValues.V[this.tier] * 0.01d);
        amount = Math.max(amount,1);
        electricItem.charge(amount,this.tier,false,false);
        if (!entity.world.isRemote && entity instanceof EntityPlayer) {
            EntityPlayer entityPlayer = (EntityPlayer)entity;
            if (electricItem != null && electricItem.canProvideChargeExternally() && isInDischargeMode(itemStack) && electricItem.getCharge() > 0L) {
                IInventory inventoryPlayer = entityPlayer.inventory;
                long transferLimit = electricItem.getTransferLimit();
                if (Mods.Baubles.isModLoaded()) {
                    inventoryPlayer = BaublesModule.getBaublesWrappedInventory(entityPlayer);
                }

                for(int i = 0; i < ((IInventory)inventoryPlayer).getSizeInventory(); ++i) {
                    ItemStack itemInSlot = ((IInventory)inventoryPlayer).getStackInSlot(i);
                    IElectricItem slotElectricItem = (IElectricItem)itemInSlot.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, (EnumFacing)null);
                    IEnergyStorage feEnergyItem = (IEnergyStorage)itemInSlot.getCapability(CapabilityEnergy.ENERGY, (EnumFacing)null);
                    if (slotElectricItem != null && !slotElectricItem.canProvideChargeExternally()) {
                        long chargedAmount = chargeElectricItem(transferLimit, electricItem, slotElectricItem);
                        if (chargedAmount > 0L) {
                            transferLimit -= chargedAmount;
                            if (transferLimit == 0L) {
                                break;
                            }
                        }
                    } else if (ConfigHolder.compat.energy.nativeEUToFE && feEnergyItem != null && feEnergyItem.getEnergyStored() < feEnergyItem.getMaxEnergyStored()) {
                        int energyMissing = feEnergyItem.getMaxEnergyStored() - feEnergyItem.getEnergyStored();
                        long euToCharge = FeCompat.toEu((long)energyMissing, ConfigHolder.compat.energy.feToEuRatio);
                        long energyToTransfer = Math.min(euToCharge, transferLimit);
                        long maxDischargeAmount = Math.min(energyToTransfer, electricItem.discharge(energyToTransfer, electricItem.getTier(), false, true, true));
                        FeCompat.insertEu(feEnergyItem, maxDischargeAmount);
                        electricItem.discharge(maxDischargeAmount, electricItem.getTier(), false, true, false);
                    }
                }
            }
        }

    }

    private static long chargeElectricItem(long maxDischargeAmount, IElectricItem source, IElectricItem target) {
        long maxDischarged = source.discharge(maxDischargeAmount, source.getTier(), false, false, true);
        long maxReceived = target.charge(maxDischarged, source.getTier(), false, true);
        if (maxReceived > 0L) {
            long resultDischarged = source.discharge(maxReceived, source.getTier(), false, true, false);
            target.charge(resultDischarged, source.getTier(), false, false);
            return resultDischarged;
        } else {
            return 0L;
        }
    }

    private static void setInDischargeMode(ItemStack itemStack, boolean isDischargeMode) {
        NBTTagCompound tagCompound = itemStack.getTagCompound();
        if (isDischargeMode) {
            if (tagCompound == null) {
                tagCompound = new NBTTagCompound();
                itemStack.setTagCompound(tagCompound);
            }

            tagCompound.setBoolean("DischargeMode", true);
        } else if (tagCompound != null) {
            tagCompound.removeTag("DischargeMode");
            if (tagCompound.isEmpty()) {
                itemStack.setTagCompound((NBTTagCompound)null);
            }
        }

    }

    public void addInformation(ItemStack itemStack, List<String> lines) {
        IElectricItem electricItem = (IElectricItem)itemStack.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, (EnumFacing)null);
        if (electricItem != null && electricItem.canProvideChargeExternally()) {
            addTotalChargeTooltip(lines, electricItem.getMaxCharge(), electricItem.getTier());
            if (isInDischargeMode(itemStack)) {
                lines.add(I18n.format("metaitem.electric.discharge_mode.enabled", new Object[0]));
            } else {
                lines.add(I18n.format("metaitem.electric.discharge_mode.disabled", new Object[0]));
            }

            lines.add(I18n.format("metaitem.electric.discharge_mode.tooltip", new Object[0]));
        }

    }

    private static void addTotalChargeTooltip(List<String> tooltip, long maxCharge, int tier) {
        Instant start = Instant.now();
        Instant end = Instant.now().plusSeconds((long)((double)maxCharge * 1.0 / (double)GTValues.V[tier] / 20.0));
        Duration duration = Duration.between(start, end);
        long chargeTime;
        String unit;
        if (duration.getSeconds() <= 180L) {
            chargeTime = duration.getSeconds();
            unit = I18n.format("metaitem.battery.charge_unit.second", new Object[0]);
        } else if (duration.toMinutes() <= 180L) {
            chargeTime = duration.toMinutes();
            unit = I18n.format("metaitem.battery.charge_unit.minute", new Object[0]);
        } else {
            chargeTime = duration.toHours();
            unit = I18n.format("metaitem.battery.charge_unit.hour", new Object[0]);
        }

        tooltip.add(I18n.format("metaitem.battery.charge_time", new Object[]{chargeTime, unit, GTValues.VNF[tier]}));
    }

    private static boolean isInDischargeMode(ItemStack itemStack) {
        NBTTagCompound tagCompound = itemStack.getTagCompound();
        return tagCompound != null && tagCompound.getBoolean("DischargeMode");
    }

    public int getMaxStackSize(ItemStack itemStack, int defaultValue) {
        ElectricItem electricItem = (ElectricItem)itemStack.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, (EnumFacing)null);
        return electricItem != null && electricItem.getCharge() != 0L ? 1 : defaultValue;
    }

    public String getItemSubType(ItemStack itemStack) {
        return "";
    }

    public void getSubItems(ItemStack itemStack, CreativeTabs creativeTab, NonNullList<ItemStack> subItems) {
        ItemStack copy = itemStack.copy();
        IElectricItem electricItem = (IElectricItem)copy.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, (EnumFacing)null);
        if (electricItem != null) {
            electricItem.charge(electricItem.getMaxCharge(), electricItem.getTier(), true, false);
            subItems.add(copy);
        } else {
            subItems.add(itemStack);
        }

    }

    public ICapabilityProvider createProvider(ItemStack itemStack) {
        return new ElectricItem(itemStack, this.maxCharge, this.tier, this.chargeable, this.dischargeable);
    }

    public static ElectricStatsNuclear createBattery(long maxCharge, int tier, boolean rechargeable) {
        return new ElectricStatsNuclear(maxCharge, (long)tier, rechargeable, true);
    }
}
