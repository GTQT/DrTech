package com.drppp.drtech.common.drone.network;

import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nullable;
import java.util.UUID;

/** Mutable server-side lifecycle for a fleet job; persistence is owned by the future fleet controller. */
public final class DroneJob {
    public enum State { QUEUED, RUNNING, RETRY_WAIT, COMPLETED, FAILED, CANCELLED }

    private static final int MAX_ATTEMPTS = 16;
    private static final long MAX_BACKOFF_TICKS = 20L * 60L * 30L;

    private final UUID jobId;
    private final UUID ownerId;
    private final int priority;
    private final long submittedTick;
    private final long timeoutTicks;
    private final int maxAttempts;
    private final long baseBackoffTicks;
    private State state = State.QUEUED;
    private int attempts;
    private long nextEligibleTick;
    private long runningSinceTick = -1L;
    private String lastFailure = "";

    public DroneJob(UUID jobId, UUID ownerId, int priority, long submittedTick, long timeoutTicks,
            int maxAttempts, long baseBackoffTicks) {
        if (jobId == null || ownerId == null) throw new IllegalArgumentException("Job and owner ids are required");
        this.jobId = jobId;
        this.ownerId = ownerId;
        this.priority = priority;
        this.submittedTick = Math.max(0L, submittedTick);
        this.timeoutTicks = Math.max(1L, timeoutTicks);
        this.maxAttempts = Math.max(1, Math.min(MAX_ATTEMPTS, maxAttempts));
        this.baseBackoffTicks = Math.max(1L, Math.min(MAX_BACKOFF_TICKS, baseBackoffTicks));
        this.nextEligibleTick = this.submittedTick;
    }

    public UUID getJobId() { return jobId; }
    public UUID getOwnerId() { return ownerId; }
    public int getPriority() { return priority; }
    public long getSubmittedTick() { return submittedTick; }
    public long getTimeoutTicks() { return timeoutTicks; }
    public int getMaxAttempts() { return maxAttempts; }
    public int getAttempts() { return attempts; }
    public long getNextEligibleTick() { return nextEligibleTick; }
    public long getRunningSinceTick() { return runningSinceTick; }
    public State getState() { return state; }
    public String getLastFailure() { return lastFailure; }

    public boolean isEligible(long worldTime) {
        return (state == State.QUEUED || state == State.RETRY_WAIT) && worldTime >= nextEligibleTick;
    }

    boolean start(long worldTime) {
        if (!isEligible(worldTime)) return false;
        state = State.RUNNING;
        runningSinceTick = worldTime;
        return true;
    }

    boolean hasTimedOut(long worldTime) {
        return state == State.RUNNING && worldTime >= runningSinceTick
                && worldTime - runningSinceTick >= timeoutTicks;
    }

    void complete() {
        if (state != State.RUNNING) return;
        state = State.COMPLETED;
        runningSinceTick = -1L;
    }

    void fail(long worldTime, @Nullable String reason) {
        if (state != State.RUNNING) return;
        attempts++;
        runningSinceTick = -1L;
        lastFailure = reason == null ? "" : reason;
        if (attempts >= maxAttempts) {
            state = State.FAILED;
            return;
        }
        state = State.RETRY_WAIT;
        nextEligibleTick = saturatingAdd(worldTime, backoffForAttempt(attempts));
    }

    void cancel() {
        if (state == State.COMPLETED || state == State.FAILED) return;
        state = State.CANCELLED;
        runningSinceTick = -1L;
    }

    NBTTagCompound writeToNbt() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("JobId", jobId.toString());
        tag.setString("Owner", ownerId.toString());
        tag.setInteger("Priority", priority);
        tag.setLong("Submitted", submittedTick);
        tag.setLong("Timeout", timeoutTicks);
        tag.setInteger("MaxAttempts", maxAttempts);
        tag.setLong("BaseBackoff", baseBackoffTicks);
        tag.setString("State", state.name());
        tag.setInteger("Attempts", attempts);
        tag.setLong("NextEligible", nextEligibleTick);
        tag.setLong("RunningSince", runningSinceTick);
        tag.setString("LastFailure", lastFailure);
        return tag;
    }

    @Nullable
    static DroneJob readFromNbt(NBTTagCompound tag) {
        UUID jobId = readUuid(tag, "JobId");
        UUID ownerId = readUuid(tag, "Owner");
        if (jobId == null || ownerId == null) return null;
        DroneJob job = new DroneJob(jobId, ownerId, tag.getInteger("Priority"), tag.getLong("Submitted"),
                tag.getLong("Timeout"), tag.getInteger("MaxAttempts"), tag.getLong("BaseBackoff"));
        try { job.state = State.valueOf(tag.getString("State")); }
        catch (IllegalArgumentException ignored) { job.state = State.QUEUED; }
        job.attempts = Math.max(0, Math.min(job.maxAttempts, tag.getInteger("Attempts")));
        job.nextEligibleTick = Math.max(job.submittedTick, tag.getLong("NextEligible"));
        job.runningSinceTick = tag.getLong("RunningSince");
        job.lastFailure = tag.getString("LastFailure");
        return job;
    }

    void recoverAfterLoad(long worldTime) {
        if (state == State.RUNNING) fail(worldTime, "SERVER_RESTART");
    }

    private long backoffForAttempt(int failedAttempt) {
        long multiplier = 1L << Math.min(30, Math.max(0, failedAttempt - 1));
        return multiplier > MAX_BACKOFF_TICKS / baseBackoffTicks
                ? MAX_BACKOFF_TICKS : Math.min(MAX_BACKOFF_TICKS, baseBackoffTicks * multiplier);
    }

    private static long saturatingAdd(long left, long right) {
        return right > Long.MAX_VALUE - left ? Long.MAX_VALUE : left + right;
    }

    @Nullable
    private static UUID readUuid(NBTTagCompound tag, String key) {
        try { return tag.hasKey(key, 8) ? UUID.fromString(tag.getString(key)) : null; }
        catch (IllegalArgumentException ignored) { return null; }
    }
}
