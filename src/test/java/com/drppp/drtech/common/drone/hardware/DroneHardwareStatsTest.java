package com.drppp.drtech.common.drone.hardware;

import gregtech.api.GTValues;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

class DroneHardwareStatsTest {

    @Test
    void chassisMetadataIsStableAndUnknownMetadataFallsBackToHv() {
        assertEquals(DroneChassisTier.HV, DroneChassisTier.fromMetadata(0));
        assertEquals(DroneChassisTier.EV, DroneChassisTier.fromMetadata(1));
        assertEquals(DroneChassisTier.IV, DroneChassisTier.fromMetadata(2));
        assertEquals(DroneChassisTier.HV, DroneChassisTier.fromMetadata(99));
        assertEquals(GTValues.HV, DroneChassisTier.HV.getVoltageTier());
        assertEquals(GTValues.IV, DroneChassisTier.IV.getVoltageTier());
        assertEquals(20.0D, DroneChassisTier.HV.getMaxHealth());
        assertEquals(40.0D, DroneChassisTier.IV.getMaxHealth());
        assertEquals(12.0D, DroneChassisTier.IV.getArmor());
    }

    @Test
    void batteryAddsHalfOfEachChassisBaseCapacity() {
        assertEquals(4_000_000L, DroneHardwareStats.capacity(DroneChassisTier.HV, false));
        assertEquals(6_000_000L, DroneHardwareStats.capacity(DroneChassisTier.HV, true));
        assertEquals(24_000_000L, DroneHardwareStats.capacity(DroneChassisTier.EV, true));
        assertEquals(96_000_000L, DroneHardwareStats.capacity(DroneChassisTier.IV, true));
    }

    @Test
    void cargoNeverExceedsPhysicalInventory() {
        assertEquals(9, DroneHardwareStats.cargoSlots(DroneChassisTier.HV, false));
        assertEquals(12, DroneHardwareStats.cargoSlots(DroneChassisTier.HV, true));
        assertEquals(18, DroneHardwareStats.cargoSlots(DroneChassisTier.IV, true));
    }

    @Test
    void propulsionAndWirelessBonusesScaleFromChassis() {
        assertEquals(0.18D, DroneHardwareStats.movementSpeed(DroneChassisTier.HV, false), 0.00001D);
        assertEquals(0.234D, DroneHardwareStats.movementSpeed(DroneChassisTier.HV, true), 0.00001D);
        assertEquals(512, DroneHardwareStats.wirelessRange(DroneChassisTier.IV, true));
    }

    @Test
    void efficiencyRoundsEuUpAndDoublesTransferLimit() {
        assertEquals(1L, DroneHardwareStats.energyCost(1L, true));
        assertEquals(3L, DroneHardwareStats.energyCost(4L, true));
        assertEquals(96L, DroneHardwareStats.energyCost(128L, true));
        assertEquals(128, DroneHardwareStats.transferLimit(true));
        assertEquals(64, DroneHardwareStats.transferLimit(false));
    }

    @Test
    void fluidCargoCapacityDependsOnChassisAndDedicatedModule() {
        assertEquals(0, DroneHardwareStats.fluidCapacity(DroneChassisTier.IV, false));
        assertEquals(16_000, DroneHardwareStats.fluidCapacity(DroneChassisTier.HV, true));
        assertEquals(64_000, DroneHardwareStats.fluidCapacity(DroneChassisTier.EV, true));
        assertEquals(256_000, DroneHardwareStats.fluidCapacity(DroneChassisTier.IV, true));
        assertEquals(5, DroneUpgradeType.FLUID_CARGO.getMetadata());
        assertEquals(6, DroneHardwareStats.UPGRADE_SLOTS);
    }

    @Test
    void emptyUpgradeHandlerProducesAnEmptyNetworkMask() {
        IItemHandler empty = new IItemHandler() {
            @Override public int getSlots() { return DroneHardwareStats.UPGRADE_SLOTS; }
            @Override public ItemStack getStackInSlot(int slot) { return ItemStack.EMPTY; }
            @Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) { return stack; }
            @Override public ItemStack extractItem(int slot, int amount, boolean simulate) { return ItemStack.EMPTY; }
            @Override public int getSlotLimit(int slot) { return 1; }
        };
        assertEquals(0, DroneHardwareStats.upgradeMask(empty));
    }
}
