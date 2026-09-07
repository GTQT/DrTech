package com.drppp.drtech.common.drone.network;

import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nullable;
import java.util.UUID;

/** Mutable server-side lifecycle for a fleet job; persistence is owned by the future fleet controller. */
public final class DroneJob {
    public enum State { QUEUED, RUNNING, RETRY_WAIT, COMPLETED, FAILED, CANCELLED }
    public enum LogisticsStage { NONE, DISCOVER, RESERVED, TO_SOURCE, PICKUP, TO_TARGET, DELIVER, RETURNING, RECOVERED }

    private static final int MAX_ATTEMPTS = 16;
    private static final long MAX_BACKOFF_TICKS = 20L * 60L * 30L;
    private static final int MAX_FAILURE_LENGTH = 256;

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
    private DroneEndpoint.Kind resourceKind;
    private String resourceId = "";
    private long requestedAmount;
    private UUID sourceEndpointId;
    private UUID targetEndpointId;
    private UUID assignedDroneId;
    private long pickedAmount;
    private long deliveredAmount;
    private LogisticsStage logisticsStage = LogisticsStage.NONE;

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

    public static DroneJob logistics(UUID jobId, UUID ownerId, int priority, long submittedTick,
            long timeoutTicks, int maxAttempts, long baseBackoffTicks, DroneEndpoint.Kind resourceKind,
            String resourceId, long requestedAmount, UUID sourceEndpointId, UUID targetEndpointId) {
        if (resourceKind == null || sourceEndpointId == null || targetEndpointId == null
                || sourceEndpointId.equals(targetEndpointId)) {
            throw new IllegalArgumentException("Distinct source and target endpoints are required");
        }
        String checkedResource = normalizeResourceId(resourceId);
        if (checkedResource.isEmpty() || requestedAmount <= 0L) {
            throw new IllegalArgumentException("Resource id and positive amount are required");
        }
        DroneJob job = new DroneJob(jobId, ownerId, priority, submittedTick, timeoutTicks,
                maxAttempts, baseBackoffTicks);
        job.resourceKind = resourceKind;
        job.resourceId = checkedResource;
        job.requestedAmount = requestedAmount;
        job.sourceEndpointId = sourceEndpointId;
        job.targetEndpointId = targetEndpointId;
        job.logisticsStage = LogisticsStage.DISCOVER;
        return job;
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
    public boolean isLogisticsJob() { return logisticsStage != LogisticsStage.NONE; }
    @Nullable public DroneEndpoint.Kind getResourceKind() { return resourceKind; }
    public String getResourceId() { return resourceId; }
    public long getRequestedAmount() { return requestedAmount; }
    @Nullable public UUID getSourceEndpointId() { return sourceEndpointId; }
    @Nullable public UUID getTargetEndpointId() { return targetEndpointId; }
    @Nullable public UUID getAssignedDroneId() { return assignedDroneId; }
    public long getPickedAmount() { return pickedAmount; }
    public long getDeliveredAmount() { return deliveredAmount; }
    public LogisticsStage getLogisticsStage() { return logisticsStage; }

    boolean assignDrone(UUID droneId) {
        if (!isLogisticsJob() || droneId == null || assignedDroneId != null) return false;
        assignedDroneId = droneId;
        return true;
    }

    void clearAssignedDrone() { assignedDroneId = null; }

    boolean setLogisticsStage(LogisticsStage nextStage) {
        if (!isLogisticsJob() || nextStage == null || nextStage == LogisticsStage.NONE) return false;
        logisticsStage = nextStage;
        return true;
    }

    boolean recordPickup(long amount) {
        if (!isLogisticsJob() || amount <= 0L || pickedAmount >= requestedAmount) return false;
        pickedAmount = Math.min(requestedAmount, saturatingAdd(pickedAmount, amount));
        return true;
    }

    boolean recordDelivery(long amount) {
        if (!isLogisticsJob() || amount <= 0L || deliveredAmount >= pickedAmount) return false;
        deliveredAmount = Math.min(pickedAmount, saturatingAdd(deliveredAmount, amount));
        return true;
    }

    void limitRequestedAmount(long amount) {
        if (!isLogisticsJob() || amount <= 0L) return;
        requestedAmount = Math.min(requestedAmount, amount);
        pickedAmount = Math.min(pickedAmount, requestedAmount);
        deliveredAmount = Math.min(deliveredAmount, pickedAmount);
    }

    void prepareLogisticsRetry() {
        if (!isLogisticsJob()) {
            clearAssignedDrone();
            return;
        }
        if (pickedAmount > deliveredAmount && assignedDroneId != null) {
            logisticsStage = LogisticsStage.TO_TARGET;
        } else {
            clearAssignedDrone();
            logisticsStage = LogisticsStage.DISCOVER;
        }
    }

    boolean finishLogisticsRecovery() {
        if ((state != State.FAILED && state != State.CANCELLED) || logisticsStage != LogisticsStage.RETURNING
                || pickedAmount <= deliveredAmount || assignedDroneId == null) return false;
        logisticsStage = LogisticsStage.RECOVERED;
        clearAssignedDrone();
        return true;
    }

    public boolean isEligible(long worldTime) {
        return (state == State.QUEUED || state == State.RETRY_WAIT) && worldTime >= nextEligibleTick;
    }

    boolean start(long worldTime) {
        if (!isEligible(worldTime)) return false;
        state = State.RUNNING;
        runningSinceTick = worldTime;
        lastFailure = "";
        return true;
    }

    boolean touch(long worldTime) {
        if (state != State.RUNNING) return false;
        runningSinceTick = Math.max(runningSinceTick, worldTime);
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
        lastFailure = "";
    }

    void fail(long worldTime, @Nullable String reason) {
        if (state != State.RUNNING) return;
        attempts++;
        runningSinceTick = -1L;
        lastFailure = normalizeFailure(reason);
        if (attempts >= maxAttempts) {
            state = State.FAILED;
            if (isLogisticsJob() && pickedAmount > deliveredAmount && assignedDroneId != null) {
                logisticsStage = LogisticsStage.RETURNING;
            } else {
                prepareLogisticsRetry();
            }
            return;
        }
        state = State.RETRY_WAIT;
        nextEligibleTick = saturatingAdd(worldTime, backoffForAttempt(attempts));
        prepareLogisticsRetry();
    }

    /** Scheduling contention is not an execution attempt and must not exhaust the retry budget. */
    boolean defer(long worldTime, @Nullable String reason, long delayTicks) {
        if (state != State.RUNNING) return false;
        state = State.RETRY_WAIT;
        runningSinceTick = -1L;
        lastFailure = normalizeFailure(reason);
        nextEligibleTick = saturatingAdd(worldTime, Math.max(1L, delayTicks));
        prepareLogisticsRetry();
        return true;
    }

    boolean cancel() {
        if (state == State.COMPLETED || state == State.FAILED || state == State.CANCELLED) return false;
        state = State.CANCELLED;
        runningSinceTick = -1L;
        if (isLogisticsJob() && pickedAmount > deliveredAmount && assignedDroneId != null) {
            logisticsStage = LogisticsStage.RETURNING;
        } else {
            clearAssignedDrone();
        }
        return true;
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
        if (isLogisticsJob()) {
            tag.setString("ResourceKind", resourceKind.name());
            tag.setString("ResourceId", resourceId);
            tag.setLong("RequestedAmount", requestedAmount);
            tag.setString("SourceEndpoint", sourceEndpointId.toString());
            tag.setString("TargetEndpoint", targetEndpointId.toString());
            if (assignedDroneId != null) tag.setString("AssignedDrone", assignedDroneId.toString());
            tag.setLong("PickedAmount", pickedAmount);
            tag.setLong("DeliveredAmount", deliveredAmount);
            tag.setString("LogisticsStage", logisticsStage.name());
        }
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
        job.lastFailure = normalizeFailure(tag.getString("LastFailure"));
        readLogisticsPayload(job, tag);
        return job;
    }

    void recoverAfterLoad(long worldTime) {
        if (state == State.RUNNING) {
            fail(worldTime, "SERVER_RESTART");
        }
    }

    private long backoffForAttempt(int failedAttempt) {
        long multiplier = 1L << Math.min(30, Math.max(0, failedAttempt - 1));
        return multiplier > MAX_BACKOFF_TICKS / baseBackoffTicks
                ? MAX_BACKOFF_TICKS : Math.min(MAX_BACKOFF_TICKS, baseBackoffTicks * multiplier);
    }

    private static long saturatingAdd(long left, long right) {
        return right > Long.MAX_VALUE - left ? Long.MAX_VALUE : left + right;
    }

    private static void readLogisticsPayload(DroneJob job, NBTTagCompound tag) {
        if (!tag.hasKey("ResourceKind", 8)) return;
        DroneEndpoint.Kind kind;
        LogisticsStage stage;
        try {
            kind = DroneEndpoint.Kind.valueOf(tag.getString("ResourceKind"));
            stage = LogisticsStage.valueOf(tag.getString("LogisticsStage"));
        } catch (IllegalArgumentException ignored) { return; }
        String resource = normalizeResourceId(tag.getString("ResourceId"));
        UUID source = readUuid(tag, "SourceEndpoint");
        UUID target = readUuid(tag, "TargetEndpoint");
        long requested = Math.max(0L, tag.getLong("RequestedAmount"));
        if (resource.isEmpty() || source == null || target == null || source.equals(target) || requested <= 0L) return;
        job.resourceKind = kind;
        job.resourceId = resource;
        job.requestedAmount = requested;
        job.sourceEndpointId = source;
        job.targetEndpointId = target;
        job.assignedDroneId = readUuid(tag, "AssignedDrone");
        job.pickedAmount = Math.min(requested, Math.max(0L, tag.getLong("PickedAmount")));
        job.deliveredAmount = Math.min(job.pickedAmount, Math.max(0L, tag.getLong("DeliveredAmount")));
        job.logisticsStage = stage == LogisticsStage.NONE ? LogisticsStage.DISCOVER : stage;
    }

    private static String normalizeResourceId(String resourceId) {
        String checked = resourceId == null ? "" : resourceId.trim();
        return checked.substring(0, Math.min(128, checked.length()));
    }

    private static String normalizeFailure(@Nullable String failure) {
        String checked = failure == null ? "" : failure.replace('\n', ' ').replace('\r', ' ').trim();
        return checked.substring(0, Math.min(MAX_FAILURE_LENGTH, checked.length()));
    }

    @Nullable
    private static UUID readUuid(NBTTagCompound tag, String key) {
        try { return tag.hasKey(key, 8) ? UUID.fromString(tag.getString(key)) : null; }
        catch (IllegalArgumentException ignored) { return null; }
    }
}
