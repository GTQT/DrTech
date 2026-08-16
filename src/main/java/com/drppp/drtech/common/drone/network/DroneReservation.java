package com.drppp.drtech.common.drone.network;

import java.util.UUID;

/** A bounded claim against one target endpoint, owned by exactly one fleet job. */
public final class DroneReservation {
    private final UUID reservationId;
    private final UUID jobId;
    private final UUID endpointId;
    private final long amount;
    private final long expiresAtTick;

    DroneReservation(UUID reservationId, UUID jobId, UUID endpointId, long amount, long expiresAtTick) {
        this.reservationId = reservationId;
        this.jobId = jobId;
        this.endpointId = endpointId;
        this.amount = amount;
        this.expiresAtTick = expiresAtTick;
    }

    public UUID getReservationId() { return reservationId; }
    public UUID getJobId() { return jobId; }
    public UUID getEndpointId() { return endpointId; }
    public long getAmount() { return amount; }
    public long getExpiresAtTick() { return expiresAtTick; }
}
