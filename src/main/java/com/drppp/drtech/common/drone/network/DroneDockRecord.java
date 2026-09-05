package com.drppp.drtech.common.drone.network;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.UUID;

/** Immutable persisted summary of one dock. Live machine objects are never stored here. */
public final class DroneDockRecord {

    private final UUID dockId;
    private final int dimension;
    private final BlockPos position;
    private final UUID ownerId;
    private final String name;
    private final int tier;
    private final int priority;
    private final long lastHeartbeat;
    private final boolean loaded;
    private final int currentLoad;
    private final long availableEu;
    private final boolean enabled;
    private final boolean canAcceptDrone;
    private final String occupancyState;

    public DroneDockRecord(UUID dockId, int dimension, BlockPos position, @Nullable UUID ownerId, String name,
            int tier, int priority, long lastHeartbeat, boolean loaded, int currentLoad, long availableEu,
            boolean enabled, boolean canAcceptDrone) {
        this(dockId, dimension, position, ownerId, name, tier, priority, lastHeartbeat, loaded,
                currentLoad, availableEu, enabled, canAcceptDrone, currentLoad > 0 ? "STORED" : "FREE");
    }

    public DroneDockRecord(UUID dockId, int dimension, BlockPos position, @Nullable UUID ownerId, String name,
            int tier, int priority, long lastHeartbeat, boolean loaded, int currentLoad, long availableEu,
            boolean enabled, boolean canAcceptDrone, String occupancyState) {
        if (dockId == null || position == null) throw new IllegalArgumentException("Dock id and position are required");
        this.dockId = dockId;
        this.dimension = dimension;
        this.position = position.toImmutable();
        this.ownerId = ownerId;
        this.name = name == null || name.isEmpty() ? "Drone Dock" : name;
        this.tier = Math.max(0, tier);
        this.priority = Math.max(-100, Math.min(100, priority));
        this.lastHeartbeat = Math.max(0L, lastHeartbeat);
        this.loaded = loaded;
        this.currentLoad = Math.max(0, currentLoad);
        this.availableEu = Math.max(0L, availableEu);
        this.enabled = enabled;
        this.canAcceptDrone = canAcceptDrone;
        this.occupancyState = "RESERVED".equals(occupancyState) || "STORED".equals(occupancyState)
                ? occupancyState : "FREE";
    }

    public UUID getDockId() { return dockId; }
    public int getDimension() { return dimension; }
    public BlockPos getPosition() { return position; }
    @Nullable public UUID getOwnerId() { return ownerId; }
    public String getName() { return name; }
    public int getTier() { return tier; }
    public int getPriority() { return priority; }
    public long getLastHeartbeat() { return lastHeartbeat; }
    public boolean isLoaded() { return loaded; }
    public int getCurrentLoad() { return currentLoad; }
    public long getAvailableEu() { return availableEu; }
    public boolean isEnabled() { return enabled; }
    public boolean canAcceptDrone() { return canAcceptDrone; }
    public String getOccupancyState() { return occupancyState; }

    public NBTTagCompound writeToNbt() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("DockId", dockId.toString());
        tag.setInteger("Dimension", dimension);
        tag.setLong("Position", position.toLong());
        if (ownerId != null) tag.setString("Owner", ownerId.toString());
        tag.setString("Name", name);
        tag.setInteger("Tier", tier);
        tag.setInteger("Priority", priority);
        tag.setLong("LastHeartbeat", lastHeartbeat);
        tag.setBoolean("Loaded", loaded);
        tag.setInteger("CurrentLoad", currentLoad);
        tag.setLong("AvailableEU", availableEu);
        tag.setBoolean("Enabled", enabled);
        tag.setBoolean("CanAcceptDrone", canAcceptDrone);
        tag.setString("OccupancyState", occupancyState);
        return tag;
    }

    @Nullable
    public static DroneDockRecord readFromNbt(NBTTagCompound tag) {
        UUID id = readUuid(tag, "DockId");
        if (id == null || !tag.hasKey("Position", 4)) return null;
        String occupancy = tag.hasKey("OccupancyState", 8) ? tag.getString("OccupancyState")
                : tag.getInteger("CurrentLoad") > 0 ? "STORED" : "FREE";
        return new DroneDockRecord(id, tag.getInteger("Dimension"), BlockPos.fromLong(tag.getLong("Position")),
                readUuid(tag, "Owner"), tag.getString("Name"), tag.getInteger("Tier"),
                tag.getInteger("Priority"), tag.getLong("LastHeartbeat"), tag.getBoolean("Loaded"),
                tag.getInteger("CurrentLoad"), tag.getLong("AvailableEU"), tag.getBoolean("Enabled"),
                tag.getBoolean("CanAcceptDrone"), occupancy);
    }

    @Nullable
    private static UUID readUuid(NBTTagCompound tag, String key) {
        if (!tag.hasKey(key, 8)) return null;
        try {
            return UUID.fromString(tag.getString(key));
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }
}
