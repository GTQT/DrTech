package com.drppp.drtech.drone.network;

import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nullable;

/** Immutable bounded resource snapshot published by one logistics endpoint heartbeat. */
public final class DroneEndpointResource {
    private final String resourceId;
    private final long amount;
    private final long capacity;

    public DroneEndpointResource(String resourceId, long amount, long capacity) {
        String checked = resourceId == null ? "" : resourceId.trim();
        if (checked.isEmpty()) throw new IllegalArgumentException("Resource id is required");
        this.resourceId = checked.substring(0, Math.min(128, checked.length()));
        this.amount = Math.max(0L, amount);
        this.capacity = Math.max(this.amount, capacity);
    }

    public String getResourceId() { return resourceId; }
    public long getAmount() { return amount; }
    public long getCapacity() { return capacity; }

    public NBTTagCompound writeToNbt() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("Resource", resourceId);
        tag.setLong("Amount", amount);
        tag.setLong("Capacity", capacity);
        return tag;
    }

    @Nullable
    public static DroneEndpointResource readFromNbt(NBTTagCompound tag) {
        if (tag == null || !tag.hasKey("Resource", 8) || tag.getString("Resource").trim().isEmpty()) return null;
        return new DroneEndpointResource(tag.getString("Resource"), tag.getLong("Amount"), tag.getLong("Capacity"));
    }
}
