package com.drppp.drtech.drone.hardware;

import com.drppp.drtech.Tags;
import gregtech.api.GTValues;
import net.minecraft.util.ResourceLocation;

/** Native EU drone chassis progression. Metadata is stable because it is persisted on the item. */
public enum DroneChassisTier {
    HV(0, GTValues.HV, 4_000_000L, 9, 0.18D, 256, 20.0D, 4.0D),
    EV(1, GTValues.EV, 16_000_000L, 12, 0.21D, 320, 30.0D, 8.0D),
    IV(2, GTValues.IV, 64_000_000L, 15, 0.24D, 384, 40.0D, 12.0D);

    private final int metadata;
    private final ResourceLocation id;
    private final int voltageTier;
    private final long baseCapacity;
    private final int baseCargoSlots;
    private final double baseMovementSpeed;
    private final int baseWirelessRange;
    private final double maxHealth;
    private final double armor;

    DroneChassisTier(int metadata, int voltageTier, long baseCapacity, int baseCargoSlots,
            double baseMovementSpeed, int baseWirelessRange, double maxHealth, double armor) {
        this.metadata = metadata;
        this.id = new ResourceLocation(Tags.MODID, name().toLowerCase());
        this.voltageTier = voltageTier;
        this.baseCapacity = baseCapacity;
        this.baseCargoSlots = baseCargoSlots;
        this.baseMovementSpeed = baseMovementSpeed;
        this.baseWirelessRange = baseWirelessRange;
        this.maxHealth = maxHealth;
        this.armor = armor;
    }

    public int getMetadata() { return metadata; }
    public ResourceLocation getId() { return id; }
    public int getVoltageTier() { return voltageTier; }
    public long getBaseCapacity() { return baseCapacity; }
    public int getBaseCargoSlots() { return baseCargoSlots; }
    public double getBaseMovementSpeed() { return baseMovementSpeed; }
    public int getBaseWirelessRange() { return baseWirelessRange; }
    public double getMaxHealth() { return maxHealth; }
    public double getArmor() { return armor; }

    public static DroneChassisTier fromMetadata(int metadata) {
        for (DroneChassisTier tier : values()) {
            if (tier.metadata == metadata) return tier;
        }
        return HV;
    }

    public static DroneChassisTier fromId(String id, DroneChassisTier fallback) {
        if (id == null || id.isEmpty()) return fallback;
        for (DroneChassisTier tier : values()) {
            if (tier.id.toString().equals(id)) return tier;
        }
        return fallback;
    }
}
