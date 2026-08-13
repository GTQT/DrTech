package com.drppp.drtech.common.drone.network;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/** Dimension-aware persistent dock directory, refreshed by loaded dock heartbeats. */
public final class DroneDockNetwork extends WorldSavedData {

    public static final String DATA_NAME = "drtech_drone_docks";
    public static final long ONLINE_TIMEOUT_TICKS = 100L;
    public static final long PRUNE_AFTER_TICKS = 24_000L;
    private static final int MAX_RECORDS = 2_048;

    private final Map<UUID, DroneDockRecord> records = new LinkedHashMap<>();

    public DroneDockNetwork() {
        this(DATA_NAME);
    }

    public DroneDockNetwork(String name) {
        super(name);
    }

    public static DroneDockNetwork get(World world) {
        MapStorage storage = world.getPerWorldStorage();
        DroneDockNetwork network = (DroneDockNetwork) storage.getOrLoadData(DroneDockNetwork.class, DATA_NAME);
        if (network == null) {
            network = new DroneDockNetwork();
            storage.setData(DATA_NAME, network);
        }
        return network;
    }

    public void heartbeat(DroneDockRecord record) {
        if (record == null) return;
        if (!records.containsKey(record.getDockId()) && records.size() >= MAX_RECORDS) prune(record.getLastHeartbeat());
        if (!records.containsKey(record.getDockId()) && records.size() >= MAX_RECORDS) return;
        records.put(record.getDockId(), record);
        markDirty();
    }

    public Optional<DroneDockRecord> findNearest(int dimension, BlockPos origin, @Nullable UUID ownerId,
            int maximumSourceTier, long worldTime, boolean requireAccepting, @Nullable UUID excludedDockId) {
        if (origin == null) return Optional.empty();
        Comparator<DroneDockRecord> order = Comparator
                .comparingInt(DroneDockRecord::getPriority).reversed()
                .thenComparingDouble(record -> record.getPosition().distanceSq(origin))
                .thenComparing(record -> record.getDockId().toString());
        return records.values().stream()
                .filter(record -> record.getDimension() == dimension)
                .filter(record -> excludedDockId == null || !excludedDockId.equals(record.getDockId()))
                .filter(record -> Objects.equals(ownerId, record.getOwnerId()))
                .filter(record -> record.getTier() <= maximumSourceTier)
                .filter(record -> isOnline(record, worldTime))
                .filter(DroneDockRecord::isEnabled)
                .filter(record -> record.getAvailableEu() > 0L)
                .filter(record -> !requireAccepting || record.canAcceptDrone())
                .min(order);
    }

    public Optional<DroneDockRecord> getRecord(UUID dockId) {
        return Optional.ofNullable(records.get(dockId));
    }

    public int size() {
        return records.size();
    }

    public void prune(long worldTime) {
        boolean removed = records.values().removeIf(record -> worldTime > record.getLastHeartbeat()
                && worldTime - record.getLastHeartbeat() > PRUNE_AFTER_TICKS);
        if (removed) markDirty();
    }

    private static boolean isOnline(DroneDockRecord record, long worldTime) {
        return record.isLoaded() && worldTime >= record.getLastHeartbeat()
                && worldTime - record.getLastHeartbeat() <= ONLINE_TIMEOUT_TICKS;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        records.clear();
        NBTTagList list = nbt.getTagList("Docks", 10);
        int count = Math.min(MAX_RECORDS, list.tagCount());
        for (int index = 0; index < count; index++) {
            DroneDockRecord record = DroneDockRecord.readFromNbt(list.getCompoundTagAt(index));
            if (record != null) {
                DroneDockRecord offline = new DroneDockRecord(record.getDockId(), record.getDimension(),
                        record.getPosition(), record.getOwnerId(), record.getName(), record.getTier(),
                        record.getPriority(), record.getLastHeartbeat(), false, record.getCurrentLoad(),
                        record.getAvailableEu(), record.isEnabled(), record.canAcceptDrone());
                records.put(offline.getDockId(), offline);
            }
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        NBTTagList list = new NBTTagList();
        List<DroneDockRecord> stable = new ArrayList<>(records.values());
        stable.sort(Comparator.comparing(record -> record.getDockId().toString()));
        for (DroneDockRecord record : stable) list.appendTag(record.writeToNbt());
        compound.setTag("Docks", list);
        return compound;
    }
}
