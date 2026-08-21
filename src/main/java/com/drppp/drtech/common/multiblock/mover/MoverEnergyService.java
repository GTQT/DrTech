package com.drppp.drtech.common.multiblock.mover;

import com.drppp.drtech.DrtConfig;
import gregtech.api.GTValues;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IElectricItem;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;

public final class MoverEnergyService {
    public static final long CAPACITY = 64_000_000L;
    public static final int TIER = GTValues.IV;

    private MoverEnergyService() {
    }

    public static long calculateCost(MultiblockSnapshot snapshot) {
        return calculateCost(snapshot, MoverRotation.NONE);
    }

    public static long calculateCost(MultiblockSnapshot snapshot, MoverRotation rotation) {
        long base = Math.max(0, DrtConfig.MultiblockMover.baseEnergyCost);
        long blockCost = (long) Math.max(0, DrtConfig.MultiblockMover.energyPerBlock)
                * snapshot.getBlockCount();
        long tileCost = (long) Math.max(0, DrtConfig.MultiblockMover.energyPerTileEntity)
                * snapshot.getTileEntityCount();
        long rotationCost = (long) Math.max(0,
                DrtConfig.MultiblockMover.rotationEnergyPerQuarterTurn)
                * rotation.getChargedQuarterTurns();
        return base + blockCost + tileCost + rotationCost;
    }

    public static boolean canConsume(EntityPlayerMP player, ItemStack mover, long amount) {
        if (player.capabilities.isCreativeMode || amount <= 0) return true;
        IElectricItem electricItem = mover.getCapability(
                GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
        return electricItem != null && electricItem.canUse(amount);
    }

    public static boolean consume(EntityPlayerMP player, ItemStack mover, long amount) {
        if (player.capabilities.isCreativeMode || amount <= 0) return true;
        IElectricItem electricItem = mover.getCapability(
                GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
        if (electricItem == null || !electricItem.canUse(amount)) return false;
        long discharged = electricItem.discharge(amount, Integer.MAX_VALUE,
                true, false, false);
        if (discharged == amount) return true;
        if (discharged > 0) {
            electricItem.charge(discharged, Integer.MAX_VALUE, true, false);
        }
        return false;
    }

    public static boolean refund(EntityPlayerMP player, ItemStack mover, long amount) {
        if (player.capabilities.isCreativeMode || amount <= 0) return true;
        IElectricItem electricItem = mover.getCapability(
                GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
        return electricItem != null
                && electricItem.charge(amount, Integer.MAX_VALUE, true, false) == amount;
    }
}
