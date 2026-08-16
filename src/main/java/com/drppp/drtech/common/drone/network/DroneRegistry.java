package com.drppp.drtech.common.drone.network;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/** World-level owner-isolated drone directory for fleet and logistics services. */
public final class DroneRegistry extends WorldSavedData {
    public static final String DATA_NAME = "drtech_drone_registry";
    public static final long ONLINE_TIMEOUT_TICKS = 100L;
    public static final long PRUNE_AFTER_TICKS = 24_000L;
    private static final int MAX_RECORDS = 2_048;
    private final Map<UUID, DroneRegistryRecord> records = new LinkedHashMap<>();

    public DroneRegistry() { this(DATA_NAME); }
    public DroneRegistry(String name) { super(name); }

    public static DroneRegistry get(World world) {
        MapStorage storage = world.getPerWorldStorage();
        DroneRegistry registry = (DroneRegistry) storage.getOrLoadData(DroneRegistry.class, DATA_NAME);
        if (registry == null) { registry = new DroneRegistry(); storage.setData(DATA_NAME, registry); }
        return registry;
    }

    public void heartbeat(DroneRegistryRecord record) {
        if (record == null) return;
        if (!records.containsKey(record.getDroneId()) && records.size() >= MAX_RECORDS) prune(record.getLastHeartbeat());
        if (!records.containsKey(record.getDroneId()) && records.size() >= MAX_RECORDS) return;
        records.put(record.getDroneId(), record);
        markDirty();
    }

    public Optional<DroneRegistryRecord> getRecord(UUID droneId) { return Optional.ofNullable(records.get(droneId)); }

    public List<DroneRegistryRecord> listForOwner(@Nullable UUID ownerId, int dimension) {
        List<DroneRegistryRecord> result = new ArrayList<>();
        for (DroneRegistryRecord record : records.values()) {
            if (record.getDimension() == dimension && Objects.equals(ownerId, record.getOwnerId())) result.add(record);
        }
        result.sort(Comparator.comparing(DroneRegistryRecord::getStatus)
                .thenComparing(DroneRegistryRecord::getChassis)
                .thenComparing(record -> record.getDroneId().toString()));
        return Collections.unmodifiableList(result);
    }

    public List<DroneRegistryRecord> listForOwner(@Nullable UUID ownerId) {
        List<DroneRegistryRecord> result = new ArrayList<>();
        for (DroneRegistryRecord record : records.values()) {
            if (Objects.equals(ownerId, record.getOwnerId())) result.add(record);
        }
        result.sort(Comparator.comparingInt(DroneRegistryRecord::getDimension)
                .thenComparing(DroneRegistryRecord::getStatus)
                .thenComparing(record -> record.getDroneId().toString()));
        return Collections.unmodifiableList(result);
    }

    public List<DroneRegistryRecord> listAll(int dimension, long worldTime) {
        List<DroneRegistryRecord> result = new ArrayList<>();
        for (DroneRegistryRecord record : records.values()) {
            if (record.getDimension() == dimension) result.add(withLoadedState(record, worldTime));
        }
        result.sort(Comparator.comparing(record -> record.getDroneId().toString()));
        return Collections.unmodifiableList(result);
    }

    public static boolean isOnline(DroneRegistryRecord record, long worldTime) {
        return record != null && record.isLoaded() && worldTime >= record.getLastHeartbeat()
                && worldTime - record.getLastHeartbeat() <= ONLINE_TIMEOUT_TICKS;
    }

    public void prune(long worldTime) {
        boolean removed = records.values().removeIf(record -> worldTime > record.getLastHeartbeat()
                && worldTime - record.getLastHeartbeat() > PRUNE_AFTER_TICKS);
        if (removed) markDirty();
    }

    private static DroneRegistryRecord withLoadedState(DroneRegistryRecord record, long worldTime) {
        return new DroneRegistryRecord(record.getDroneId(), record.getDimension(), record.getPosition(),
                record.getOwnerId(), record.getChassis(), record.getEnergyStored(), record.getEnergyCapacity(),
                isOnline(record, worldTime) ? record.getStatus() : "OFFLINE", record.getProgramId(),
                record.getCargoOccupiedSlots(), record.getCargoCapacitySlots(), record.getProgramRevision(),
                record.getLastHeartbeat(), isOnline(record, worldTime),
                record.getBoundDock(), record.getUpgradeMask());
    }

    @Override public void readFromNBT(NBTTagCompound nbt) {
        records.clear();
        NBTTagList list = nbt.getTagList("Drones", 10);
        for (int i = 0; i < Math.min(MAX_RECORDS, list.tagCount()); i++) {
            DroneRegistryRecord record = DroneRegistryRecord.readFromNbt(list.getCompoundTagAt(i));
            if (record != null) records.put(record.getDroneId(), new DroneRegistryRecord(record.getDroneId(),
                    record.getDimension(), record.getPosition(), record.getOwnerId(), record.getChassis(),
                    record.getEnergyStored(), record.getEnergyCapacity(), record.getStatus(), record.getProgramId(),
                    record.getCargoOccupiedSlots(), record.getCargoCapacitySlots(), record.getProgramRevision(),
                    record.getLastHeartbeat(), false, record.getBoundDock(), record.getUpgradeMask()));
        }
    }

    @Override public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        NBTTagList list = new NBTTagList();
        List<DroneRegistryRecord> stable = new ArrayList<>(records.values());
        stable.sort(Comparator.comparing(record -> record.getDroneId().toString()));
        for (DroneRegistryRecord record : stable) list.appendTag(record.writeToNbt());
        compound.setTag("Drones", list);
        return compound;
    }
}
