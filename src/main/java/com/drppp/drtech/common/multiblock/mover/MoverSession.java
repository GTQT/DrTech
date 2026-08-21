package com.drppp.drtech.common.multiblock.mover;

import java.util.UUID;

public final class MoverSession {
    private final UUID id;
    private final UUID playerId;
    private final long createdAt;
    private final long captureNanos;
    private final MultiblockSnapshot snapshot;
    private boolean committing;
    private MoverRotation rotation = MoverRotation.NONE;
    private long lastRotationTick = Long.MIN_VALUE;

    public MoverSession(UUID id, UUID playerId, MultiblockSnapshot snapshot, long captureNanos) {
        this.id = id;
        this.playerId = playerId;
        this.snapshot = snapshot;
        this.createdAt = System.currentTimeMillis();
        this.captureNanos = Math.max(0L, captureNanos);
    }

    public UUID getId() {
        return id;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public MultiblockSnapshot getSnapshot() {
        return snapshot;
    }

    public long getCaptureNanos() {
        return captureNanos;
    }

    public boolean isCommitting() {
        return committing;
    }

    public void setCommitting(boolean committing) {
        this.committing = committing;
    }

    public MoverRotation getRotation() {
        return rotation;
    }

    public void setRotation(MoverRotation rotation) {
        this.rotation = rotation == null ? MoverRotation.NONE : rotation;
    }

    public boolean canRotateAt(long worldTick) {
        return lastRotationTick == Long.MIN_VALUE || worldTick - lastRotationTick >= 3L;
    }

    public void markRotatedAt(long worldTick) {
        lastRotationTick = worldTick;
    }
}
