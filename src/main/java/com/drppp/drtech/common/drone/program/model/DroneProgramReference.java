package com.drppp.drtech.common.drone.program.model;

import java.util.Objects;
import java.util.UUID;
import javax.annotation.Nullable;

/** Immutable, revision-locked handle to a server-owned program library entry. */
public final class DroneProgramReference {

    private final UUID programId;
    @Nullable
    private final UUID ownerId;
    private final long revision;
    private final String name;

    public DroneProgramReference(UUID programId, long revision, String name) {
        this(programId, null, revision, name);
    }

    public DroneProgramReference(UUID programId, @Nullable UUID ownerId, long revision, String name) {
        this.programId = Objects.requireNonNull(programId, "programId");
        this.ownerId = ownerId;
        this.revision = Math.max(0L, revision);
        this.name = name == null ? "" : name;
    }

    public UUID getProgramId() { return programId; }
    @Nullable public UUID getOwnerId() { return ownerId; }
    public long getRevision() { return revision; }
    public String getName() { return name; }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof DroneProgramReference other)) return false;
        return revision == other.revision && programId.equals(other.programId)
                && Objects.equals(ownerId, other.ownerId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(programId, ownerId, revision);
    }
}
