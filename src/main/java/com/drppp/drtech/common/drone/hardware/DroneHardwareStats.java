package com.drppp.drtech.common.drone.hardware;

import net.minecraftforge.items.IItemHandler;

/** Single source of truth for every attribute affected by a chassis or installed module. */
public final class DroneHardwareStats {

    public static final int MAX_CARGO_SLOTS = 18;
    public static final int UPGRADE_SLOTS = DroneUpgradeType.values().length;

    private DroneHardwareStats() {}

    public static boolean hasUpgrade(IItemHandler upgrades, DroneUpgradeType type) {
        return findUpgradeSlot(upgrades, type, -1) >= 0;
    }

    /** Finds a module regardless of its visual slot; excludedSlot is used while validating slot moves. */
    public static int findUpgradeSlot(IItemHandler upgrades, DroneUpgradeType type, int excludedSlot) {
        if (upgrades == null || type == null) return -1;
        for (int slot = 0; slot < upgrades.getSlots(); slot++) {
            if (slot != excludedSlot && ItemDroneUpgradeModule.getType(upgrades.getStackInSlot(slot)) == type) {
                return slot;
            }
        }
        return -1;
    }

    public static long capacity(DroneChassisTier chassis, IItemHandler upgrades) {
        return capacity(chassis, hasUpgrade(upgrades, DroneUpgradeType.BATTERY));
    }

    static long capacity(DroneChassisTier chassis, boolean battery) {
        long base = chassis.getBaseCapacity();
        return battery ? base + base / 2L : base;
    }

    public static int cargoSlots(DroneChassisTier chassis, IItemHandler upgrades) {
        return cargoSlots(chassis, hasUpgrade(upgrades, DroneUpgradeType.CARGO));
    }

    static int cargoSlots(DroneChassisTier chassis, boolean cargo) {
        return Math.min(MAX_CARGO_SLOTS, chassis.getBaseCargoSlots() + (cargo ? 3 : 0));
    }

    public static double movementSpeed(DroneChassisTier chassis, IItemHandler upgrades) {
        return movementSpeed(chassis, hasUpgrade(upgrades, DroneUpgradeType.PROPULSION));
    }

    static double movementSpeed(DroneChassisTier chassis, boolean propulsion) {
        double multiplier = propulsion ? 1.30D : 1.0D;
        return chassis.getBaseMovementSpeed() * multiplier;
    }

    public static int wirelessRange(DroneChassisTier chassis, IItemHandler upgrades) {
        return wirelessRange(chassis, hasUpgrade(upgrades, DroneUpgradeType.WIRELESS),
                hasUpgrade(upgrades, DroneUpgradeType.FLEET_COMMUNICATION));
    }

    static int wirelessRange(DroneChassisTier chassis, boolean wireless, boolean fleetCommunication) {
        return chassis.getBaseWirelessRange() + (wireless ? 128 : 0) + (fleetCommunication ? 256 : 0);
    }

    public static long energyCost(long baseCost, IItemHandler upgrades) {
        return energyCost(baseCost, hasUpgrade(upgrades, DroneUpgradeType.EFFICIENCY));
    }

    static long energyCost(long baseCost, boolean efficiency) {
        if (baseCost <= 0L) return 0L;
        return efficiency ? Math.max(1L, (baseCost * 3L + 3L) / 4L) : baseCost;
    }

    public static int transferLimit(IItemHandler upgrades) {
        return transferLimit(hasUpgrade(upgrades, DroneUpgradeType.EFFICIENCY),
                hasUpgrade(upgrades, DroneUpgradeType.ADVANCED_ITEM_HANDLING));
    }

    static int transferLimit(boolean efficiency, boolean advancedItemHandling) {
        return advancedItemHandling ? 256 : efficiency ? 128 : 64;
    }

    public static int navigationRange(IItemHandler upgrades) {
        return navigationRange(hasUpgrade(upgrades, DroneUpgradeType.ADVANCED_NAVIGATION));
    }

    static int navigationRange(boolean advancedNavigation) {
        return advancedNavigation ? 128 : 64;
    }

    public static int navigationNodeBudget(IItemHandler upgrades) {
        return navigationNodeBudget(hasUpgrade(upgrades, DroneUpgradeType.ADVANCED_NAVIGATION));
    }

    static int navigationNodeBudget(boolean advancedNavigation) {
        return advancedNavigation ? 16_384 : 4_096;
    }

    /** Maximum number of coordinates a runtime area may expose for each chassis tier. */
    public static int areaBlockLimit(DroneChassisTier chassis) {
        if (chassis == null) return 1_024;
        return switch (chassis) {
            case HV -> 1_024;
            case EV -> 2_048;
            case IV -> 4_096;
        };
    }

    public static int euTransferAmperage(IItemHandler upgrades) {
        return euTransferAmperage(hasUpgrade(upgrades, DroneUpgradeType.EU_INTERFACE));
    }

    static int euTransferAmperage(boolean euInterface) {
        return euInterface ? 4 : 1;
    }

    /** Fluid storage exists only while the dedicated module is installed. Values are in mB. */
    public static int fluidCapacity(DroneChassisTier chassis, IItemHandler upgrades) {
        return fluidCapacity(chassis, hasUpgrade(upgrades, DroneUpgradeType.FLUID_CARGO));
    }

    static int fluidCapacity(DroneChassisTier chassis, boolean fluidCargo) {
        if (!fluidCargo) return 0;
        return switch (chassis) {
            case HV -> 16_000;
            case EV -> 64_000;
            case IV -> 256_000;
        };
    }

    public static int upgradeMask(IItemHandler upgrades) {
        int mask = 0;
        for (DroneUpgradeType type : DroneUpgradeType.values()) {
            if (hasUpgrade(upgrades, type)) mask |= 1 << type.getMetadata();
        }
        return mask;
    }
}
