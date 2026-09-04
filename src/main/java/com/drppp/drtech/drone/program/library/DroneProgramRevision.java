package com.drppp.drtech.drone.program.library;

/** Immutable revision metadata retained for program history displays. */
public final class DroneProgramRevision {
    private final long revision;
    private final long updatedAt;
    private final String signature;

    public DroneProgramRevision(long revision, long updatedAt, String signature) {
        this.revision = Math.max(0L, revision);
        this.updatedAt = Math.max(0L, updatedAt);
        this.signature = signature == null ? "" : signature;
    }
    public long getRevision() { return revision; }
    public long getUpdatedAt() { return updatedAt; }
    public String getSignature() { return signature; }
}
