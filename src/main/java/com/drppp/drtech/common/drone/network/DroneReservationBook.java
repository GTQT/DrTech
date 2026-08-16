package com.drppp.drtech.common.drone.network;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/** Server-thread atomic reservation ledger for target inventory quantities. */
public final class DroneReservationBook {
    private final Map<UUID, DroneReservation> byReservationId = new LinkedHashMap<>();
    private final Map<UUID, UUID> reservationByJob = new HashMap<>();
    private final Map<UUID, Long> reservedByEndpoint = new HashMap<>();

    public Optional<DroneReservation> tryReserve(UUID jobId, UUID endpointId, long amount,
            long availableAmount, long worldTime, long leaseTicks) {
        if (jobId == null || endpointId == null || amount <= 0L || availableAmount < 0L || leaseTicks <= 0L
                || reservationByJob.containsKey(jobId)) return Optional.empty();
        long reserved = reservedByEndpoint.getOrDefault(endpointId, 0L);
        if (reserved > availableAmount || amount > availableAmount - reserved) return Optional.empty();
        DroneReservation reservation = new DroneReservation(UUID.randomUUID(), jobId, endpointId, amount,
                saturatingAdd(worldTime, leaseTicks));
        byReservationId.put(reservation.getReservationId(), reservation);
        reservationByJob.put(jobId, reservation.getReservationId());
        if (amount > Long.MAX_VALUE - reserved) {
            byReservationId.remove(reservation.getReservationId());
            reservationByJob.remove(jobId);
            return Optional.empty();
        }
        reservedByEndpoint.put(endpointId, reserved + amount);
        return Optional.of(reservation);
    }

    /** Atomically reserves an endpoint resource after applying whitelist and direction-specific limits. */
    public Optional<DroneReservation> tryReserve(UUID jobId, DroneEndpoint endpoint, String resourceId,
            boolean providing, long currentAmount, long worldTime, long leaseTicks) {
        if (endpoint == null || !endpoint.matchesResource(resourceId)) return Optional.empty();
        long reserved = getReservedAmount(endpoint.getEndpointId());
        long capacity = providing ? endpoint.availableToProvide(currentAmount, reserved)
                : endpoint.requestCapacity(currentAmount, reserved);
        if (capacity <= 0L) return Optional.empty();
        return tryReserve(jobId, endpoint.getEndpointId(), capacity, capacity, worldTime, leaseTicks);
    }

    public boolean release(UUID reservationId) {
        DroneReservation reservation = byReservationId.remove(reservationId);
        if (reservation == null) return false;
        reservationByJob.remove(reservation.getJobId());
        long remaining = reservedByEndpoint.getOrDefault(reservation.getEndpointId(), 0L) - reservation.getAmount();
        if (remaining <= 0L) reservedByEndpoint.remove(reservation.getEndpointId());
        else reservedByEndpoint.put(reservation.getEndpointId(), remaining);
        return true;
    }

    public boolean releaseForJob(UUID jobId) {
        UUID reservationId = reservationByJob.get(jobId);
        return reservationId != null && release(reservationId);
    }

    public void releaseExpired(long worldTime) {
        List<UUID> expired = new ArrayList<>();
        for (DroneReservation reservation : byReservationId.values()) {
            if (worldTime >= reservation.getExpiresAtTick()) expired.add(reservation.getReservationId());
        }
        for (UUID reservationId : expired) release(reservationId);
    }

    public void releaseTerminalJobs(Collection<DroneJob> jobs) {
        if (jobs == null) return;
        for (DroneJob job : jobs) {
            if (job == null) continue;
            DroneJob.State state = job.getState();
            if (state == DroneJob.State.COMPLETED || state == DroneJob.State.FAILED || state == DroneJob.State.CANCELLED) {
                releaseForJob(job.getJobId());
            }
        }
    }

    /** Failed attempts must release capacity before a later retry re-reserves it. */
    public void releaseRetryWaitingJobs(Collection<DroneJob> jobs) {
        if (jobs == null) return;
        for (DroneJob job : jobs) {
            if (job != null && job.getState() == DroneJob.State.RETRY_WAIT) releaseForJob(job.getJobId());
        }
    }

    /** A restarted running job must reserve again when it is rescheduled, rather than retaining stale capacity. */
    public void releaseRestartedJobs(Collection<DroneJob> jobs) {
        if (jobs == null) return;
        for (DroneJob job : jobs) {
            if (job != null && job.getState() == DroneJob.State.RETRY_WAIT
                    && "SERVER_RESTART".equals(job.getLastFailure())) releaseForJob(job.getJobId());
        }
    }

    public long getReservedAmount(UUID endpointId) { return reservedByEndpoint.getOrDefault(endpointId, 0L); }

    public Optional<DroneReservation> getForJob(UUID jobId) {
        UUID reservationId = reservationByJob.get(jobId);
        return reservationId == null ? Optional.empty() : Optional.ofNullable(byReservationId.get(reservationId));
    }

    public Collection<DroneReservation> snapshot() {
        return Collections.unmodifiableList(new ArrayList<>(byReservationId.values()));
    }

    NBTTagList writeToNbt() {
        NBTTagList list = new NBTTagList();
        for (DroneReservation reservation : byReservationId.values()) {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setString("ReservationId", reservation.getReservationId().toString());
            tag.setString("JobId", reservation.getJobId().toString());
            tag.setString("EndpointId", reservation.getEndpointId().toString());
            tag.setLong("Amount", reservation.getAmount());
            tag.setLong("ExpiresAt", reservation.getExpiresAtTick());
            list.appendTag(tag);
        }
        return list;
    }

    void readFromNbt(NBTTagList list, long worldTime) {
        byReservationId.clear();
        reservationByJob.clear();
        reservedByEndpoint.clear();
        for (int index = 0; index < Math.min(2048, list.tagCount()); index++) {
            NBTTagCompound tag = list.getCompoundTagAt(index);
            UUID reservationId = readUuid(tag, "ReservationId");
            UUID jobId = readUuid(tag, "JobId");
            UUID endpointId = readUuid(tag, "EndpointId");
            long amount = tag.getLong("Amount");
            long expiresAt = tag.getLong("ExpiresAt");
            if (reservationId == null || jobId == null || endpointId == null || amount <= 0L
                    || expiresAt <= worldTime || byReservationId.containsKey(reservationId)
                    || reservationByJob.containsKey(jobId)) continue;
            long reserved = reservedByEndpoint.getOrDefault(endpointId, 0L);
            if (amount > Long.MAX_VALUE - reserved) continue;
            DroneReservation reservation = new DroneReservation(reservationId, jobId, endpointId, amount, expiresAt);
            byReservationId.put(reservationId, reservation);
            reservationByJob.put(jobId, reservationId);
            reservedByEndpoint.put(endpointId, reserved + amount);
        }
    }

    private static long saturatingAdd(long left, long right) {
        return right > Long.MAX_VALUE - left ? Long.MAX_VALUE : left + right;
    }

    private static UUID readUuid(NBTTagCompound tag, String key) {
        try { return tag.hasKey(key, 8) ? UUID.fromString(tag.getString(key)) : null; }
        catch (IllegalArgumentException ignored) { return null; }
    }
}
