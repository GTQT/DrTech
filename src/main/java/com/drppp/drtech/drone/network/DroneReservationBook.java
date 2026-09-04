package com.drppp.drtech.drone.network;

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
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/** Server-thread atomic reservation ledger for target inventory quantities. */
public final class DroneReservationBook {
    private final Map<UUID, DroneReservation> byReservationId = new LinkedHashMap<>();
    private final Map<UUID, UUID> reservationByJob = new HashMap<>();
    private final Map<UUID, Long> reservedByEndpoint = new HashMap<>();
    /** Outgoing claims are resource-specific; unrelated stacks in the same endpoint remain available. */
    private final Map<UUID, Map<String, Long>> reservedProviding = new HashMap<>();
    /** Incoming claims share the endpoint's physical capacity, regardless of resource id. */
    private final Map<UUID, Long> reservedReceiving = new HashMap<>();

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
        addReceiving(endpointId, amount);
        return Optional.of(reservation);
    }

    /** Atomically claims both withdrawal and insertion capacity for one logistics movement. */
    public Optional<DroneReservation> tryReservePair(UUID jobId, DroneEndpoint source, DroneEndpoint target,
            String resourceId, long requestedAmount, long worldTime, long leaseTicks) {
        if (jobId == null || source == null || target == null || resourceId == null || requestedAmount <= 0L
                || leaseTicks <= 0L || source.getEndpointId().equals(target.getEndpointId())
                || source.getKind() != target.getKind() || reservationByJob.containsKey(jobId)
                || !source.matchesResource(resourceId) || !target.canStoreResource(resourceId)) return Optional.empty();
        DroneEndpointResource sourceResource = source.getResource(resourceId);
        DroneEndpointResource targetResource = target.getResource(resourceId);
        long sourceAmount = sourceResource == null ? 0L : sourceResource.getAmount();
        long targetAmount = targetResource == null ? 0L : targetResource.getAmount();
        long sourceReserved = getReservedProvidingAmount(source.getEndpointId(), resourceId);
        long targetReserved = getReservedReceivingAmount(target.getEndpointId());
        long sourceTotal = getReservedAmount(source.getEndpointId());
        long targetTotal = getReservedAmount(target.getEndpointId());
        long available = source.availableToProvide(sourceAmount, sourceReserved);
        long capacity = target.requestCapacity(targetAmount, targetReserved);
        long amount = Math.min(requestedAmount, Math.min(available, capacity));
        if (amount <= 0L || amount > Long.MAX_VALUE - sourceReserved || amount > Long.MAX_VALUE - targetReserved
                || amount > Long.MAX_VALUE - sourceTotal || amount > Long.MAX_VALUE - targetTotal) {
            return Optional.empty();
        }
        DroneReservation reservation = new DroneReservation(UUID.randomUUID(), jobId, source.getEndpointId(),
                target.getEndpointId(), resourceId, amount, saturatingAdd(worldTime, leaseTicks));
        byReservationId.put(reservation.getReservationId(), reservation);
        reservationByJob.put(jobId, reservation.getReservationId());
        reservedByEndpoint.put(source.getEndpointId(), sourceTotal + amount);
        reservedByEndpoint.put(target.getEndpointId(), targetTotal + amount);
        addProviding(source.getEndpointId(), resourceId, amount);
        addReceiving(target.getEndpointId(), amount);
        return Optional.of(reservation);
    }

    /** Atomically reserves an endpoint resource after applying whitelist and direction-specific limits. */
    public Optional<DroneReservation> tryReserve(UUID jobId, DroneEndpoint endpoint, String resourceId,
            boolean providing, long currentAmount, long maximumAmount, long worldTime, long leaseTicks) {
        if (endpoint == null || providing && !endpoint.matchesResource(resourceId)
                || !providing && !endpoint.canStoreResource(resourceId) || maximumAmount <= 0L
                || jobId == null || leaseTicks <= 0L || reservationByJob.containsKey(jobId)) return Optional.empty();
        long reserved = providing
                ? getReservedProvidingAmount(endpoint.getEndpointId(), resourceId)
                : getReservedReceivingAmount(endpoint.getEndpointId());
        long capacity = providing ? endpoint.availableToProvide(currentAmount, reserved)
                : endpoint.requestCapacity(currentAmount, reserved);
        long amount = Math.min(capacity, maximumAmount);
        if (amount <= 0L) return Optional.empty();
        long total = getReservedAmount(endpoint.getEndpointId());
        if (amount > Long.MAX_VALUE - reserved || amount > Long.MAX_VALUE - total) return Optional.empty();
        DroneReservation reservation = new DroneReservation(UUID.randomUUID(), jobId, endpoint.getEndpointId(),
                null, providing ? resourceId : "", amount, saturatingAdd(worldTime, leaseTicks));
        byReservationId.put(reservation.getReservationId(), reservation);
        reservationByJob.put(jobId, reservation.getReservationId());
        reservedByEndpoint.put(endpoint.getEndpointId(), total + amount);
        if (providing) addProviding(endpoint.getEndpointId(), resourceId, amount);
        else addReceiving(endpoint.getEndpointId(), amount);
        return Optional.of(reservation);
    }

    public boolean release(UUID reservationId) {
        DroneReservation reservation = byReservationId.remove(reservationId);
        if (reservation == null) return false;
        reservationByJob.remove(reservation.getJobId());
        long remaining = reservedByEndpoint.getOrDefault(reservation.getEndpointId(), 0L) - reservation.getAmount();
        if (remaining <= 0L) reservedByEndpoint.remove(reservation.getEndpointId());
        else reservedByEndpoint.put(reservation.getEndpointId(), remaining);
        releaseEndpointAmount(reservation.getTargetEndpointId(), reservation.getAmount());
        if (reservation.isEndpointPair()) {
            removeProviding(reservation.getSourceEndpointId(), reservation.getResourceId(), reservation.getAmount());
            removeReceiving(reservation.getTargetEndpointId(), reservation.getAmount());
        } else if (reservation.isProvidingOnly()) {
            removeProviding(reservation.getEndpointId(), reservation.getResourceId(), reservation.getAmount());
        } else {
            removeReceiving(reservation.getEndpointId(), reservation.getAmount());
        }
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

    /** Truncated or damaged saves must not leave capacity claims with no owning job. */
    public void releaseOrphanedJobs(Collection<DroneJob> jobs) {
        Set<UUID> liveJobIds = new HashSet<>();
        if (jobs != null) for (DroneJob job : jobs) {
            if (job != null) liveJobIds.add(job.getJobId());
        }
        List<UUID> orphaned = new ArrayList<>();
        for (DroneReservation reservation : byReservationId.values()) {
            if (!liveJobIds.contains(reservation.getJobId())) orphaned.add(reservation.getReservationId());
        }
        for (UUID reservationId : orphaned) release(reservationId);
    }

    public long getReservedAmount(UUID endpointId) { return reservedByEndpoint.getOrDefault(endpointId, 0L); }

    public long getReservedProvidingAmount(UUID endpointId, String resourceId) {
        Map<String, Long> resources = reservedProviding.get(endpointId);
        return resources == null || resourceId == null ? 0L : resources.getOrDefault(resourceId, 0L);
    }

    public long getReservedReceivingAmount(UUID endpointId) {
        return reservedReceiving.getOrDefault(endpointId, 0L);
    }

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
            if (reservation.getTargetEndpointId() != null) {
                tag.setString("TargetEndpointId", reservation.getTargetEndpointId().toString());
            }
            if (!reservation.getResourceId().isEmpty()) tag.setString("ResourceId", reservation.getResourceId());
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
        reservedProviding.clear();
        reservedReceiving.clear();
        for (int index = 0; index < Math.min(2048, list.tagCount()); index++) {
            NBTTagCompound tag = list.getCompoundTagAt(index);
            UUID reservationId = readUuid(tag, "ReservationId");
            UUID jobId = readUuid(tag, "JobId");
            UUID endpointId = readUuid(tag, "EndpointId");
            UUID targetEndpointId = readUuid(tag, "TargetEndpointId");
            long amount = tag.getLong("Amount");
            long expiresAt = tag.getLong("ExpiresAt");
            if (reservationId == null || jobId == null || endpointId == null || amount <= 0L
                    || expiresAt <= worldTime || byReservationId.containsKey(reservationId)
                    || reservationByJob.containsKey(jobId)) continue;
            long reserved = reservedByEndpoint.getOrDefault(endpointId, 0L);
            long targetReserved = targetEndpointId == null ? 0L
                    : reservedByEndpoint.getOrDefault(targetEndpointId, 0L);
            if (amount > Long.MAX_VALUE - reserved || targetEndpointId != null
                    && (targetEndpointId.equals(endpointId) || amount > Long.MAX_VALUE - targetReserved)) continue;
            DroneReservation reservation = new DroneReservation(reservationId, jobId, endpointId,
                    targetEndpointId, tag.getString("ResourceId"), amount, expiresAt);
            if (reservation.isEndpointPair() && reservation.getResourceId().isEmpty()) continue;
            byReservationId.put(reservationId, reservation);
            reservationByJob.put(jobId, reservationId);
            reservedByEndpoint.put(endpointId, reserved + amount);
            if (targetEndpointId != null) reservedByEndpoint.put(targetEndpointId, targetReserved + amount);
            if (targetEndpointId != null) {
                addProviding(endpointId, reservation.getResourceId(), amount);
                addReceiving(targetEndpointId, amount);
            } else if (reservation.isProvidingOnly()) {
                addProviding(endpointId, reservation.getResourceId(), amount);
            } else {
                addReceiving(endpointId, amount);
            }
        }
    }

    private void addProviding(UUID endpointId, String resourceId, long amount) {
        Map<String, Long> resources = reservedProviding.computeIfAbsent(endpointId, ignored -> new HashMap<>());
        resources.put(resourceId, resources.getOrDefault(resourceId, 0L) + amount);
    }

    private void removeProviding(UUID endpointId, String resourceId, long amount) {
        Map<String, Long> resources = reservedProviding.get(endpointId);
        if (resources == null) return;
        long remaining = resources.getOrDefault(resourceId, 0L) - amount;
        if (remaining <= 0L) resources.remove(resourceId);
        else resources.put(resourceId, remaining);
        if (resources.isEmpty()) reservedProviding.remove(endpointId);
    }

    private void addReceiving(UUID endpointId, long amount) {
        reservedReceiving.put(endpointId, reservedReceiving.getOrDefault(endpointId, 0L) + amount);
    }

    private void removeReceiving(UUID endpointId, long amount) {
        if (endpointId == null) return;
        long remaining = reservedReceiving.getOrDefault(endpointId, 0L) - amount;
        if (remaining <= 0L) reservedReceiving.remove(endpointId);
        else reservedReceiving.put(endpointId, remaining);
    }

    private void releaseEndpointAmount(UUID endpointId, long amount) {
        if (endpointId == null) return;
        long remaining = reservedByEndpoint.getOrDefault(endpointId, 0L) - amount;
        if (remaining <= 0L) reservedByEndpoint.remove(endpointId);
        else reservedByEndpoint.put(endpointId, remaining);
    }

    private static long saturatingAdd(long left, long right) {
        return right > Long.MAX_VALUE - left ? Long.MAX_VALUE : left + right;
    }

    private static UUID readUuid(NBTTagCompound tag, String key) {
        try { return tag.hasKey(key, 8) ? UUID.fromString(tag.getString(key)) : null; }
        catch (IllegalArgumentException ignored) { return null; }
    }
}
