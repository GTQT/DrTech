package com.drppp.drtech.common.drone.network;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.UUID;

/** Persisted, bounded summary of a programmable drone; live entity state is never stored here. */
public final class DroneRegistryRecord {
    private final UUID droneId;
    private final int dimension;
    private final BlockPos position;
    private final UUID ownerId;
    private final String chassis;
    private final long energyStored;
    private final long energyCapacity;
    private final int cargoOccupiedSlots;
    private final int cargoCapacitySlots;
    private final int upgradeMask;
    private final String status;
    private final String programId;
    private final long programRevision;
    private final long lastHeartbeat;
    private final boolean loaded;
    private final BlockPos boundDock;

    public DroneRegistryRecord(UUID droneId, int dimension, BlockPos position, @Nullable UUID ownerId,
            String chassis, long energyStored, long energyCapacity, String status, String programId,
            long programRevision, long lastHeartbeat, boolean loaded) {
        this(droneId, dimension, position, ownerId, chassis, energyStored, energyCapacity, status, programId,
                0, 0, programRevision, lastHeartbeat, loaded, null, 0);
    }

    public DroneRegistryRecord(UUID droneId, int dimension, BlockPos position, @Nullable UUID ownerId,
            String chassis, long energyStored, long energyCapacity, String status, String programId,
            long programRevision, long lastHeartbeat, boolean loaded, @Nullable BlockPos boundDock) {
        this(droneId, dimension, position, ownerId, chassis, energyStored, energyCapacity, status, programId,
                0, 0, programRevision, lastHeartbeat, loaded, boundDock, 0);
    }

    public DroneRegistryRecord(UUID droneId, int dimension, BlockPos position, @Nullable UUID ownerId,
            String chassis, long energyStored, long energyCapacity, String status, String programId,
            int cargoOccupiedSlots, int cargoCapacitySlots, long programRevision, long lastHeartbeat,
            boolean loaded, @Nullable BlockPos boundDock) {
        this(droneId, dimension, position, ownerId, chassis, energyStored, energyCapacity, status, programId,
                cargoOccupiedSlots, cargoCapacitySlots, programRevision, lastHeartbeat, loaded, boundDock, 0);
    }

    public DroneRegistryRecord(UUID droneId, int dimension, BlockPos position, @Nullable UUID ownerId,
            String chassis, long energyStored, long energyCapacity, String status, String programId,
            int cargoOccupiedSlots, int cargoCapacitySlots, long programRevision, long lastHeartbeat,
            boolean loaded, @Nullable BlockPos boundDock, int upgradeMask) {
        if (droneId == null || position == null) throw new IllegalArgumentException("Drone id and position are required");
        this.droneId = droneId;
        this.dimension = dimension;
        this.position = position.toImmutable();
        this.ownerId = ownerId;
        this.chassis = chassis == null ? "HV" : chassis;
        this.energyCapacity = Math.max(0L, energyCapacity);
        this.energyStored = Math.max(0L, Math.min(this.energyCapacity, energyStored));
        this.cargoCapacitySlots = Math.max(0, cargoCapacitySlots);
        this.cargoOccupiedSlots = Math.max(0, Math.min(this.cargoCapacitySlots, cargoOccupiedSlots));
        this.upgradeMask = upgradeMask;
        this.status = status == null ? "UNKNOWN" : status;
        this.programId = programId == null ? "" : programId;
        this.programRevision = Math.max(0L, programRevision);
        this.lastHeartbeat = Math.max(0L, lastHeartbeat);
        this.loaded = loaded;
        this.boundDock = boundDock == null ? null : boundDock.toImmutable();
    }

    public UUID getDroneId() { return droneId; }
    public int getDimension() { return dimension; }
    public BlockPos getPosition() { return position; }
    @Nullable public UUID getOwnerId() { return ownerId; }
    public String getChassis() { return chassis; }
    public long getEnergyStored() { return energyStored; }
    public long getEnergyCapacity() { return energyCapacity; }
    public int getCargoOccupiedSlots() { return cargoOccupiedSlots; }
    public int getCargoCapacitySlots() { return cargoCapacitySlots; }
    public int getUpgradeMask() { return upgradeMask; }
    public String getStatus() { return status; }
    public String getProgramId() { return programId; }
    public long getProgramRevision() { return programRevision; }
    public long getLastHeartbeat() { return lastHeartbeat; }
    public boolean isLoaded() { return loaded; }
    @Nullable public BlockPos getBoundDock() { return boundDock; }

    public NBTTagCompound writeToNbt() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("DroneId", droneId.toString());
        tag.setInteger("Dimension", dimension);
        tag.setLong("Position", position.toLong());
        if (ownerId != null) tag.setString("Owner", ownerId.toString());
        tag.setString("Chassis", chassis);
        tag.setLong("EnergyStored", energyStored);
        tag.setLong("EnergyCapacity", energyCapacity);
        tag.setInteger("CargoOccupiedSlots", cargoOccupiedSlots);
        tag.setInteger("CargoCapacitySlots", cargoCapacitySlots);
        tag.setInteger("UpgradeMask", upgradeMask);
        tag.setString("Status", status);
        if (!programId.isEmpty()) tag.setString("ProgramId", programId);
        tag.setLong("ProgramRevision", programRevision);
        tag.setLong("LastHeartbeat", lastHeartbeat);
        tag.setBoolean("Loaded", loaded);
        if (boundDock != null) tag.setLong("BoundDock", boundDock.toLong());
        return tag;
    }

    @Nullable
    public static DroneRegistryRecord readFromNbt(NBTTagCompound tag) {
        UUID droneId = readUuid(tag, "DroneId");
        if (droneId == null || !tag.hasKey("Position", 4)) return null;
        BlockPos boundDock = tag.hasKey("BoundDock", 4) ? BlockPos.fromLong(tag.getLong("BoundDock")) : null;
        return new DroneRegistryRecord(droneId, tag.getInteger("Dimension"),
                BlockPos.fromLong(tag.getLong("Position")), readUuid(tag, "Owner"), tag.getString("Chassis"),
                tag.getLong("EnergyStored"), tag.getLong("EnergyCapacity"), tag.getString("Status"),
                tag.getString("ProgramId"), tag.getInteger("CargoOccupiedSlots"),
                tag.getInteger("CargoCapacitySlots"), tag.getLong("ProgramRevision"), tag.getLong("LastHeartbeat"),
                tag.getBoolean("Loaded"), boundDock, tag.getInteger("UpgradeMask"));
    }

    @Nullable
    private static UUID readUuid(NBTTagCompound tag, String key) {
        if (!tag.hasKey(key, 8)) return null;
        try { return UUID.fromString(tag.getString(key)); }
        catch (IllegalArgumentException ignored) { return null; }
    }
}
