package com.drppp.drtech.common.drone.network;

import java.util.UUID;

/** A bounded claim against one target endpoint, owned by exactly one fleet job. */
public final class DroneReservation {
    private final UUID reservationId;
    private final UUID jobId;
    private final UUID endpointId;
    private final UUID secondaryEndpointId;
    private final String resourceId;
    private final long amount;
    private final long expiresAtTick;

    DroneReservation(UUID reservationId, UUID jobId, UUID endpointId, long amount, long expiresAtTick) {
        this(reservationId, jobId, endpointId, null, "", amount, expiresAtTick);
    }

    DroneReservation(UUID reservationId, UUID jobId, UUID endpointId, UUID secondaryEndpointId,
            String resourceId, long amount, long expiresAtTick) {
        this.reservationId = reservationId;
        this.jobId = jobId;
        this.endpointId = endpointId;
        this.secondaryEndpointId = secondaryEndpointId;
        this.resourceId = resourceId == null ? "" : resourceId;
        this.amount = amount;
        this.expiresAtTick = expiresAtTick;
    }

    public UUID getReservationId() { return reservationId; }
    public UUID getJobId() { return jobId; }
    public UUID getEndpointId() { return endpointId; }
    public UUID getSourceEndpointId() { return endpointId; }
    public UUID getTargetEndpointId() { return secondaryEndpointId; }
    public String getResourceId() { return resourceId; }
    public boolean isEndpointPair() { return secondaryEndpointId != null; }
    public long getAmount() { return amount; }
    public long getExpiresAtTick() { return expiresAtTick; }
}
