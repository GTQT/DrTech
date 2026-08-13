package com.drppp.drtech.common.drone.hardware;

import net.minecraftforge.items.IItemHandler;

/** Single source of truth for every attribute affected by a chassis or installed module. */
public final class DroneHardwareStats {

    public static final int MAX_CARGO_SLOTS = 18;
    public static final int UPGRADE_SLOTS = DroneUpgradeType.values().length;

    private DroneHardwareStats() {}

    public static boolean hasUpgrade(IItemHandler upgrades, DroneUpgradeType type) {
        if (upgrades == null || type.getMetadata() >= upgrades.getSlots()) return false;
        return ItemDroneUpgradeModule.getType(upgrades.getStackInSlot(type.getMetadata())) == type;
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
        return wirelessRange(chassis, hasUpgrade(upgrades, DroneUpgradeType.WIRELESS));
    }

    static int wirelessRange(DroneChassisTier chassis, boolean wireless) {
        return chassis.getBaseWirelessRange() + (wireless ? 128 : 0);
    }

    public static long energyCost(long baseCost, IItemHandler upgrades) {
        return energyCost(baseCost, hasUpgrade(upgrades, DroneUpgradeType.EFFICIENCY));
    }

    static long energyCost(long baseCost, boolean efficiency) {
        if (baseCost <= 0L) return 0L;
        return efficiency ? Math.max(1L, (baseCost * 3L + 3L) / 4L) : baseCost;
    }

    public static int transferLimit(IItemHandler upgrades) {
        return transferLimit(hasUpgrade(upgrades, DroneUpgradeType.EFFICIENCY));
    }

    static int transferLimit(boolean efficiency) { return efficiency ? 128 : 64; }

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
