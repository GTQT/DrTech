package com.drppp.drtech.drone.energy;

import net.minecraft.nbt.NBTTagCompound;

/** Long-based EU store owned by a deployed drone. Cable interaction remains the dock's responsibility. */
public final class DroneEnergyStorage {

    private final long capacity;
    private final int tier;
    private long stored;

    public DroneEnergyStorage(long capacity, int tier) {
        this(capacity, tier, 0L);
    }

    public DroneEnergyStorage(long capacity, int tier, long stored) {
        if (capacity <= 0L) {
            throw new IllegalArgumentException("capacity must be positive");
        }
        if (tier < 0) {
            throw new IllegalArgumentException("tier cannot be negative");
        }
        this.capacity = capacity;
        this.tier = tier;
        this.stored = clamp(stored, 0L, capacity);
    }

    public long insert(long amount, int sourceTier, boolean simulate) {
        if (amount <= 0L || sourceTier > tier) {
            return 0L;
        }
        long accepted = Math.min(amount, capacity - stored);
        if (!simulate) {
            stored += accepted;
        }
        return accepted;
    }

    public long extract(long amount, boolean simulate) {
        if (amount <= 0L) {
            return 0L;
        }
        long extracted = Math.min(amount, stored);
        if (!simulate) {
            stored -= extracted;
        }
        return extracted;
    }

    public boolean consume(long amount) {
        if (amount < 0L || stored < amount) {
            return false;
        }
        stored -= amount;
        return true;
    }

    public long getStored() {
        return stored;
    }

    public long getCapacity() {
        return capacity;
    }

    public int getTier() {
        return tier;
    }

    public NBTTagCompound writeToNbt() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setLong("StoredEU", stored);
        tag.setLong("Capacity", capacity);
        tag.setInteger("Tier", tier);
        return tag;
    }

    public static DroneEnergyStorage readFromNbt(NBTTagCompound tag, long fallbackCapacity, int fallbackTier) {
        long capacity = tag != null && tag.getLong("Capacity") > 0L ? tag.getLong("Capacity") : fallbackCapacity;
        int tier = tag != null && tag.hasKey("Tier") ? Math.max(0, tag.getInteger("Tier")) : fallbackTier;
        long stored = tag == null ? 0L : tag.getLong("StoredEU");
        return new DroneEnergyStorage(capacity, tier, stored);
    }

    private static long clamp(long value, long min, long max) {
        return Math.max(min, Math.min(max, value));
    }
}
