package com.drppp.drtech.common.drone.network;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/** Persistent fleet scheduling state. Running jobs recover through the normal retry path after a restart. */
public final class DroneFleetState extends WorldSavedData {
    public static final String DATA_NAME = "drtech_drone_fleet";
    private final DroneJobQueue jobs = new DroneJobQueue();
    private final DroneReservationBook reservations = new DroneReservationBook();
    private long lastWorldTime;
    private long lastDispatchTick = Long.MIN_VALUE;
    private long lastEndpointPruneTick = Long.MIN_VALUE;

    public DroneFleetState() { this(DATA_NAME); }
    public DroneFleetState(String name) { super(name); }

    public static DroneFleetState get(World world) {
        MapStorage storage = world.getPerWorldStorage();
        DroneFleetState state = (DroneFleetState) storage.getOrLoadData(DroneFleetState.class, DATA_NAME);
        if (state == null) {
            state = new DroneFleetState();
            storage.setData(DATA_NAME, state);
        }
        return state;
    }

    DroneJobQueue getJobs() { return jobs; }
    DroneReservationBook getReservations() { return reservations; }

    public boolean submitJob(UUID requesterId, DroneJob job) {
        if (requesterId == null || job == null || !requesterId.equals(job.getOwnerId())) return false;
        boolean submitted = jobs.submit(job);
        if (submitted) markDirty();
        return submitted;
    }

    public Optional<DroneJob> getJobForOwner(UUID requesterId, UUID jobId) {
        if (requesterId == null || jobId == null) return Optional.empty();
        return jobs.get(jobId).filter(job -> requesterId.equals(job.getOwnerId()));
    }

    public Collection<DroneJob> getJobsForOwner(UUID requesterId) {
        if (requesterId == null) return java.util.Collections.emptyList();
        List<DroneJob> owned = jobs.snapshot().stream()
                .filter(job -> requesterId.equals(job.getOwnerId()))
                .collect(Collectors.toList());
        return java.util.Collections.unmodifiableList(owned);
    }

    public Optional<DroneReservation> reserveTarget(UUID requesterId, UUID jobId, UUID endpointId, long amount,
            long availableAmount, long worldTime, long leaseTicks) {
        DroneJob job = getJobForOwner(requesterId, jobId).orElse(null);
        if (job == null || job.getState() != DroneJob.State.RUNNING) return Optional.empty();
        Optional<DroneReservation> reservation = reservations.tryReserve(jobId, endpointId, amount,
                availableAmount, worldTime, leaseTicks);
        if (reservation.isPresent()) markDirty();
        return reservation;
    }

    /** Owner-scoped endpoint reservation that applies resource and inventory policy atomically. */
    public Optional<DroneReservation> reserveTarget(UUID requesterId, UUID jobId, DroneEndpoint endpoint,
            String resourceId, boolean providing, long currentAmount, long maximumAmount,
            long worldTime, long leaseTicks) {
        DroneJob job = getJobForOwner(requesterId, jobId).orElse(null);
        if (job == null || job.getState() != DroneJob.State.RUNNING || endpoint == null
                || !requesterId.equals(endpoint.getOwnerId())) return Optional.empty();
        Optional<DroneReservation> reservation = reservations.tryReserve(jobId, endpoint, resourceId,
                providing, currentAmount, maximumAmount, worldTime, leaseTicks);
        if (reservation.isPresent()) markDirty();
        return reservation;
    }

    public Optional<DroneReservation> reserveLogistics(UUID requesterId, UUID jobId, DroneEndpoint source,
            DroneEndpoint target, String resourceId, long requestedAmount, long worldTime, long leaseTicks) {
        DroneJob job = getJobForOwner(requesterId, jobId).orElse(null);
        if (job == null || job.getState() != DroneJob.State.RUNNING || !job.isLogisticsJob()
                || source == null || target == null || !requesterId.equals(source.getOwnerId())
                || !requesterId.equals(target.getOwnerId())) return Optional.empty();
        Optional<DroneReservation> reservation = reservations.tryReservePair(jobId, source, target,
                resourceId, requestedAmount, worldTime, leaseTicks);
        if (reservation.isPresent()) markDirty();
        return reservation;
    }

    public boolean completeJob(UUID requesterId, UUID jobId) {
        if (!getJobForOwner(requesterId, jobId).isPresent() || !jobs.complete(jobId)) return false;
        reservations.releaseForJob(jobId);
        markDirty();
        return true;
    }

    public boolean failJob(UUID requesterId, UUID jobId, long worldTime, String reason) {
        DroneJob job = getJobForOwner(requesterId, jobId).orElse(null);
        if (job == null || !jobs.fail(jobId, worldTime, reason)) return false;
        reservations.releaseForJob(jobId);
        markDirty();
        return true;
    }

    /** Defers resource contention without counting it as a failed execution attempt. */
    public boolean deferJob(UUID requesterId, UUID jobId, long worldTime, String reason, long delayTicks) {
        if (!getJobForOwner(requesterId, jobId).isPresent()
                || !jobs.defer(jobId, worldTime, reason, delayTicks)) return false;
        reservations.releaseForJob(jobId);
        markDirty();
        return true;
    }

    public boolean cancelJob(UUID requesterId, UUID jobId) {
        if (!getJobForOwner(requesterId, jobId).isPresent() || !jobs.cancel(jobId)) return false;
        reservations.releaseForJob(jobId);
        markDirty();
        return true;
    }

    public void tick(long worldTime) {
        lastWorldTime = Math.max(lastWorldTime, worldTime);
        jobs.tick(worldTime);
        reservations.releaseExpired(worldTime);
        reservations.releaseRetryWaitingJobs(jobs.snapshot());
        reservations.releaseTerminalJobs(jobs.snapshot());
        markDirty();
    }

    public void tick(World world) {
        if (world == null || world.isRemote) return;
        long worldTime = world.getTotalWorldTime();
        tick(worldTime);
        if (lastEndpointPruneTick == Long.MIN_VALUE || worldTime < lastEndpointPruneTick
                || worldTime - lastEndpointPruneTick >= 1_200L) {
            DroneEndpointNetwork.get(world).prune(worldTime);
            lastEndpointPruneTick = worldTime;
        }
        if (lastDispatchTick == worldTime) return;
        lastDispatchTick = worldTime;
        DroneLogisticsDispatcher.tick(world, this, worldTime);
    }

    public Optional<DroneJob> findAssignedLogisticsJob(UUID ownerId, UUID droneId) {
        if (ownerId == null || droneId == null) return Optional.empty();
        return jobs.snapshot().stream().filter(DroneJob::isLogisticsJob)
                .filter(job -> job.getState() == DroneJob.State.RUNNING)
                .filter(job -> ownerId.equals(job.getOwnerId()) && droneId.equals(job.getAssignedDroneId()))
                .findFirst();
    }

    public Optional<DroneJob> findAssignedLogisticsRecovery(UUID ownerId, UUID droneId) {
        if (ownerId == null || droneId == null) return Optional.empty();
        return jobs.snapshot().stream().filter(DroneJob::isLogisticsJob)
                .filter(job -> (job.getState() == DroneJob.State.FAILED
                                || job.getState() == DroneJob.State.CANCELLED)
                        && job.getLogisticsStage() == DroneJob.LogisticsStage.RETURNING)
                .filter(job -> ownerId.equals(job.getOwnerId()) && droneId.equals(job.getAssignedDroneId()))
                .findFirst();
    }

    /** A carrying retry keeps the physical payload drone pinned while waiting for its backoff to expire. */
    public Optional<DroneJob> findRetainedLogisticsJob(UUID ownerId, UUID droneId) {
        if (ownerId == null || droneId == null) return Optional.empty();
        return jobs.snapshot().stream().filter(DroneJob::isLogisticsJob)
                .filter(job -> job.getState() == DroneJob.State.RETRY_WAIT
                        && job.getPickedAmount() > job.getDeliveredAmount())
                .filter(job -> ownerId.equals(job.getOwnerId()) && droneId.equals(job.getAssignedDroneId()))
                .findFirst();
    }

    public boolean finishLogisticsRecovery(UUID ownerId, UUID jobId) {
        DroneJob job = getJobForOwner(ownerId, jobId).orElse(null);
        if (job == null || !job.finishLogisticsRecovery()) return false;
        markDirty();
        return true;
    }

    public Optional<DroneReservation> getReservationForJob(UUID ownerId, UUID jobId) {
        return getJobForOwner(ownerId, jobId).isPresent() ? reservations.getForJob(jobId) : Optional.empty();
    }

    public boolean setLogisticsStage(UUID ownerId, UUID jobId, DroneJob.LogisticsStage stage) {
        DroneJob job = getJobForOwner(ownerId, jobId).orElse(null);
        if (job == null || job.getState() != DroneJob.State.RUNNING || !job.setLogisticsStage(stage)) return false;
        markDirty();
        return true;
    }

    /** Loaded assigned drones heartbeat their task so timeout means loss of progress/executor, not long work. */
    public boolean touchLogisticsJob(UUID ownerId, UUID jobId, long worldTime) {
        DroneJob job = getJobForOwner(ownerId, jobId).orElse(null);
        if (job == null || !job.isLogisticsJob() || !job.touch(worldTime)) return false;
        markDirty();
        return true;
    }

    public boolean recordLogisticsPickup(UUID ownerId, UUID jobId, long amount) {
        DroneJob job = getJobForOwner(ownerId, jobId).orElse(null);
        if (job == null || job.getState() != DroneJob.State.RUNNING || !job.recordPickup(amount)) return false;
        markDirty();
        return true;
    }

    public boolean recordLogisticsDelivery(UUID ownerId, UUID jobId, long amount) {
        DroneJob job = getJobForOwner(ownerId, jobId).orElse(null);
        if (job == null || job.getState() != DroneJob.State.RUNNING || !job.recordDelivery(amount)) return false;
        markDirty();
        return true;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        lastWorldTime = compound.getLong("SavedWorldTime");
        jobs.readFromNbt(compound.getTagList("Jobs", 10), lastWorldTime);
        reservations.readFromNbt(compound.getTagList("Reservations", 10), lastWorldTime);
        reservations.releaseOrphanedJobs(jobs.snapshot());
        reservations.releaseRetryWaitingJobs(jobs.snapshot());
        reservations.releaseTerminalJobs(jobs.snapshot());
        reservations.releaseRestartedJobs(jobs.snapshot());
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setLong("SavedWorldTime", lastWorldTime);
        compound.setTag("Jobs", jobs.writeToNbt());
        compound.setTag("Reservations", reservations.writeToNbt());
        return compound;
    }

}
