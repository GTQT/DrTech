package com.drppp.drtech.common.drone.program.library;

import com.drppp.drtech.common.drone.program.model.DroneProgramGraph;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/** Server-authoritative, owner-isolated directory of validated program snapshots. */
public final class DroneProgramLibrary extends WorldSavedData {

    public static final String DATA_NAME = "drtech_drone_program_library";
    private static final int MAX_RECORDS = 2_048;
    private static final int MAX_RECORDS_PER_OWNER = 128;

    private final Map<UUID, DroneProgramLibraryRecord> records = new LinkedHashMap<>();

    public DroneProgramLibrary() { this(DATA_NAME); }
    public DroneProgramLibrary(String name) { super(name); }

    public static DroneProgramLibrary get(World world) {
        MapStorage storage = world.getPerWorldStorage();
        DroneProgramLibrary library = (DroneProgramLibrary) storage.getOrLoadData(
                DroneProgramLibrary.class, DATA_NAME);
        if (library == null) {
            library = new DroneProgramLibrary();
            storage.setData(DATA_NAME, library);
        }
        return library;
    }

    /** Registers only a graph that the caller has already compiled and validated. */
    public boolean register(UUID ownerId, DroneProgramGraph graph, long worldTime) {
        if (ownerId == null || graph == null) return false;
        boolean replacing = records.containsKey(graph.getProgramId());
        long owned = records.values().stream().filter(record -> ownerId.equals(record.getOwnerId())).count();
        if (!replacing && (records.size() >= MAX_RECORDS || owned >= MAX_RECORDS_PER_OWNER)) return false;
        DroneProgramLibraryRecord existing = records.get(graph.getProgramId());
        if (existing != null && !ownerId.equals(existing.getOwnerId())) return false;
        records.put(graph.getProgramId(), new DroneProgramLibraryRecord(ownerId, graph, worldTime));
        markDirty();
        return true;
    }

    public List<DroneProgramLibraryRecord> listForOwner(UUID ownerId) {
        if (ownerId == null) return Collections.emptyList();
        List<DroneProgramLibraryRecord> result = new ArrayList<>();
        for (DroneProgramLibraryRecord record : records.values()) {
            if (ownerId.equals(record.getOwnerId())) result.add(record);
        }
        result.sort(Comparator.comparingLong(DroneProgramLibraryRecord::getUpdatedAt).reversed()
                .thenComparing(DroneProgramLibraryRecord::getName, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(record -> record.getProgramId().toString()));
        return Collections.unmodifiableList(result);
    }

    /** Resolves only an exact revision, preventing silently changed subprogram behavior. */
    public Optional<DroneProgramGraph> resolve(UUID ownerId, UUID programId, long revision) {
        DroneProgramLibraryRecord record = records.get(programId);
        if (record == null || !record.getOwnerId().equals(ownerId) || record.getRevision() != revision) {
            return Optional.empty();
        }
        return Optional.of(record.getGraph());
    }

    public int size() { return records.size(); }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        records.clear();
        NBTTagList list = nbt.getTagList("Programs", 10);
        for (int index = 0; index < Math.min(MAX_RECORDS, list.tagCount()); index++) {
            DroneProgramLibraryRecord record = DroneProgramLibraryRecord.readFromNbt(list.getCompoundTagAt(index));
            if (record != null && !records.containsKey(record.getProgramId())
                    && listForOwner(record.getOwnerId()).size() < MAX_RECORDS_PER_OWNER) {
                records.put(record.getProgramId(), record);
            }
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        NBTTagList list = new NBTTagList();
        List<DroneProgramLibraryRecord> stable = new ArrayList<>(records.values());
        stable.sort(Comparator.comparing(record -> record.getProgramId().toString()));
        for (DroneProgramLibraryRecord record : stable) list.appendTag(record.writeToNbt());
        compound.setTag("Programs", list);
        return compound;
    }
}
