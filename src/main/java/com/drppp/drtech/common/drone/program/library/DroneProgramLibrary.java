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
import java.util.HashSet;
import java.util.Set;

/** Server-authoritative, owner-isolated directory of validated program snapshots. */
public final class DroneProgramLibrary extends WorldSavedData {

    public enum RegisterResult {
        REGISTERED,
        UNCHANGED,
        CONFLICT,
        OWNER_MISMATCH,
        CAPACITY;

        public boolean isAccepted() {
            return this == REGISTERED || this == UNCHANGED;
        }
    }

    public static final String DATA_NAME = "drtech_drone_program_library";
    private static final int MAX_RECORDS = 2_048;
    private static final int MAX_RECORDS_PER_OWNER = 128;

    private final Map<UUID, DroneProgramLibraryRecord> records = new LinkedHashMap<>();
    private final Map<UUID, List<UUID>> recentOpened = new LinkedHashMap<>();
    private final Map<UUID, List<DroneProgramRevision>> revisions = new LinkedHashMap<>();
    /** Explicit owner -> requester grants for external, revision-locked references. */
    private final Set<String> externalGrants = new HashSet<>();

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
        return registerChecked(ownerId, graph, worldTime).isAccepted();
    }

    /**
     * Registers a validated snapshot without allowing a stale card to replace a newer library revision.
     * Repeating the exact same snapshot is idempotent and does not add duplicate history.
     */
    public RegisterResult registerChecked(UUID ownerId, DroneProgramGraph graph, long worldTime) {
        if (ownerId == null || graph == null) return RegisterResult.OWNER_MISMATCH;
        boolean replacing = records.containsKey(graph.getProgramId());
        long owned = records.values().stream().filter(record -> ownerId.equals(record.getOwnerId())).count();
        if (!replacing && (records.size() >= MAX_RECORDS || owned >= MAX_RECORDS_PER_OWNER)) {
            return RegisterResult.CAPACITY;
        }
        DroneProgramLibraryRecord existing = records.get(graph.getProgramId());
        if (existing != null && !ownerId.equals(existing.getOwnerId())) return RegisterResult.OWNER_MISMATCH;

        DroneProgramLibraryRecord candidate = new DroneProgramLibraryRecord(ownerId, graph, worldTime);
        if (existing != null) {
            if (graph.getRevision() < existing.getRevision()) return RegisterResult.CONFLICT;
            if (graph.getRevision() == existing.getRevision()) {
                return candidate.getSignature().equals(existing.getSignature())
                        ? RegisterResult.UNCHANGED : RegisterResult.CONFLICT;
            }
        }

        records.put(graph.getProgramId(), candidate);
        List<DroneProgramRevision> history = revisions.computeIfAbsent(graph.getProgramId(), ignored -> new ArrayList<>());
        history.removeIf(entry -> entry.getRevision() == graph.getRevision());
        history.add(new DroneProgramRevision(graph.getRevision(), worldTime, candidate.getSignature()));
        while (history.size() > 32) history.remove(0);
        markDirty();
        return RegisterResult.REGISTERED;
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

    /** Lists owned and explicitly shared snapshots for a requester, without exposing other owners' records. */
    public List<DroneProgramLibraryRecord> listAccessibleForOwner(UUID requester) {
        if (requester == null) return Collections.emptyList();
        List<DroneProgramLibraryRecord> result = new ArrayList<>();
        for (DroneProgramLibraryRecord record : records.values()) {
            if (canResolveExternal(requester, record.getOwnerId(), record.getProgramId())) result.add(record);
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

    public boolean grantExternalAccess(UUID programOwner, UUID requester, UUID programId) {
        DroneProgramLibraryRecord record = records.get(programId);
        if (programOwner == null || requester == null || programId == null || record == null
                || !programOwner.equals(record.getOwnerId()) || programOwner.equals(requester)) return false;
        externalGrants.add(grantKey(programOwner, requester, programId));
        markDirty();
        return true;
    }

    public boolean revokeExternalAccess(UUID programOwner, UUID requester, UUID programId) {
        boolean removed = externalGrants.remove(grantKey(programOwner, requester, programId));
        if (removed) markDirty();
        return removed;
    }

    public boolean canResolveExternal(UUID requester, UUID programOwner, UUID programId) {
        return requester != null && programOwner != null && programId != null
                && (requester.equals(programOwner) || externalGrants.contains(grantKey(programOwner, requester, programId)));
    }

    public Optional<DroneProgramGraph> resolve(UUID requester, com.drppp.drtech.common.drone.program.model.DroneProgramReference reference) {
        if (requester == null || reference == null) return Optional.empty();
        UUID programOwner = reference.getOwnerId() == null ? requester : reference.getOwnerId();
        if (!canResolveExternal(requester, programOwner, reference.getProgramId())) return Optional.empty();
        return resolve(programOwner, reference.getProgramId(), reference.getRevision());
    }

    public int size() { return records.size(); }

    /** Records an actual editor open without changing the program revision. */
    public void recordOpened(UUID ownerId, UUID programId) {
        DroneProgramLibraryRecord record = records.get(programId);
        if (ownerId == null || record == null || !ownerId.equals(record.getOwnerId())) return;
        List<UUID> history = recentOpened.computeIfAbsent(ownerId, ignored -> new ArrayList<>());
        history.remove(programId);
        history.add(0, programId);
        while (history.size() > 32) history.remove(history.size() - 1);
        markDirty();
    }

    public List<UUID> listRecentlyOpened(UUID ownerId) {
        List<UUID> history = recentOpened.get(ownerId);
        if (history == null) return Collections.emptyList();
        List<UUID> result = new ArrayList<>();
        for (UUID programId : history) {
            DroneProgramLibraryRecord record = records.get(programId);
            if (record != null && ownerId.equals(record.getOwnerId())) result.add(programId);
        }
        return Collections.unmodifiableList(result);
    }

    public List<DroneProgramRevision> listRevisions(UUID ownerId, UUID programId) {
        DroneProgramLibraryRecord record = records.get(programId);
        if (ownerId == null || record == null || !ownerId.equals(record.getOwnerId())) return Collections.emptyList();
        return Collections.unmodifiableList(new ArrayList<>(revisions.getOrDefault(programId, Collections.emptyList())));
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        records.clear();
        externalGrants.clear();
        recentOpened.clear();
        revisions.clear();
        NBTTagList list = nbt.getTagList("Programs", 10);
        for (int index = 0; index < Math.min(MAX_RECORDS, list.tagCount()); index++) {
            DroneProgramLibraryRecord record = DroneProgramLibraryRecord.readFromNbt(list.getCompoundTagAt(index));
            if (record != null && !records.containsKey(record.getProgramId())
                    && listForOwner(record.getOwnerId()).size() < MAX_RECORDS_PER_OWNER) {
                records.put(record.getProgramId(), record);
                revisions.computeIfAbsent(record.getProgramId(), ignored -> new ArrayList<>())
                        .add(new DroneProgramRevision(record.getRevision(), record.getUpdatedAt(), record.getSignature()));
            }
        }
        NBTTagList grants = nbt.getTagList("ExternalGrants", 8);
        for (int index = 0; index < grants.tagCount() && externalGrants.size() < MAX_RECORDS * 4; index++) {
            String grant = grants.getStringTagAt(index);
            if (grant.length() <= 128 && grant.split("/", -1).length == 3) externalGrants.add(grant);
        }
        NBTTagList recent = nbt.getTagList("RecentOpened", 10);
        for (int index = 0; index < recent.tagCount() && recentOpened.size() < MAX_RECORDS_PER_OWNER; index++) {
            NBTTagCompound ownerTag = recent.getCompoundTagAt(index);
            try {
                UUID owner = UUID.fromString(ownerTag.getString("Owner"));
                List<UUID> history = new ArrayList<>();
                NBTTagList programs = ownerTag.getTagList("Programs", 8);
                for (int item = 0; item < programs.tagCount() && history.size() < 32; item++) {
                    UUID program = UUID.fromString(programs.getStringTagAt(item));
                    DroneProgramLibraryRecord record = records.get(program);
                    if (record != null && owner.equals(record.getOwnerId()) && !history.contains(program)) {
                        history.add(program);
                    }
                }
                if (!history.isEmpty()) recentOpened.put(owner, history);
            } catch (IllegalArgumentException ignored) { }
        }
        NBTTagList revisionTags = nbt.getTagList("RevisionHistory", 10);
        for (int index = 0; index < revisionTags.tagCount() && revisions.size() < MAX_RECORDS; index++) {
            NBTTagCompound program = revisionTags.getCompoundTagAt(index);
            try {
                UUID programId = UUID.fromString(program.getString("ProgramId"));
                DroneProgramLibraryRecord record = records.get(programId);
                if (record == null || !record.getOwnerId().toString().equals(program.getString("Owner"))) continue;
                List<DroneProgramRevision> history = revisions.computeIfAbsent(programId, ignored -> new ArrayList<>());
                history.clear();
                NBTTagList entries = program.getTagList("History", 10);
                for (int item = 0; item < entries.tagCount() && history.size() < 32; item++) {
                    NBTTagCompound entry = entries.getCompoundTagAt(item);
                    history.add(new DroneProgramRevision(entry.getLong("Revision"), entry.getLong("UpdatedAt"),
                            entry.getString("Signature")));
                }
            } catch (IllegalArgumentException ignored) { }
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        NBTTagList list = new NBTTagList();
        List<DroneProgramLibraryRecord> stable = new ArrayList<>(records.values());
        stable.sort(Comparator.comparing(record -> record.getProgramId().toString()));
        for (DroneProgramLibraryRecord record : stable) list.appendTag(record.writeToNbt());
        compound.setTag("Programs", list);
        NBTTagList grants = new NBTTagList();
        for (String grant : externalGrants) grants.appendTag(new net.minecraft.nbt.NBTTagString(grant));
        compound.setTag("ExternalGrants", grants);
        NBTTagList recent = new NBTTagList();
        for (Map.Entry<UUID, List<UUID>> entry : recentOpened.entrySet()) {
            NBTTagCompound ownerTag = new NBTTagCompound();
            ownerTag.setString("Owner", entry.getKey().toString());
            NBTTagList programs = new NBTTagList();
            for (UUID program : entry.getValue()) programs.appendTag(new net.minecraft.nbt.NBTTagString(program.toString()));
            ownerTag.setTag("Programs", programs);
            recent.appendTag(ownerTag);
        }
        compound.setTag("RecentOpened", recent);
        NBTTagList revisionTags = new NBTTagList();
        for (Map.Entry<UUID, List<DroneProgramRevision>> entry : revisions.entrySet()) {
            DroneProgramLibraryRecord record = records.get(entry.getKey());
            if (record == null) continue;
            NBTTagCompound program = new NBTTagCompound();
            program.setString("ProgramId", entry.getKey().toString());
            program.setString("Owner", record.getOwnerId().toString());
            NBTTagList history = new NBTTagList();
            for (DroneProgramRevision revision : entry.getValue()) {
                NBTTagCompound item = new NBTTagCompound();
                item.setLong("Revision", revision.getRevision());
                item.setLong("UpdatedAt", revision.getUpdatedAt());
                item.setString("Signature", revision.getSignature());
                history.appendTag(item);
            }
            program.setTag("History", history);
            revisionTags.appendTag(program);
        }
        compound.setTag("RevisionHistory", revisionTags);
        return compound;
    }

    private static String grantKey(UUID owner, UUID requester, UUID programId) {
        return owner + "/" + requester + "/" + programId;
    }
}
