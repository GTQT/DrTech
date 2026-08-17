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
            String resourceId, boolean providing, long currentAmount, long worldTime, long leaseTicks) {
        DroneJob job = getJobForOwner(requesterId, jobId).orElse(null);
        if (job == null || job.getState() != DroneJob.State.RUNNING || endpoint == null
                || !requesterId.equals(endpoint.getOwnerId())) return Optional.empty();
        Optional<DroneReservation> reservation = reservations.tryReserve(jobId, endpoint, resourceId,
                providing, currentAmount, worldTime, leaseTicks);
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
        if (!getJobForOwner(requesterId, jobId).isPresent() || !jobs.fail(jobId, worldTime, reason)) return false;
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

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        lastWorldTime = compound.getLong("SavedWorldTime");
        jobs.readFromNbt(compound.getTagList("Jobs", 10), lastWorldTime);
        reservations.readFromNbt(compound.getTagList("Reservations", 10), lastWorldTime);
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
