package com.drppp.drtech.common.drone.network;

/** Deterministic route between two compatible logistics endpoints. */
public final class DroneEndpointRoute {
    private final DroneEndpoint source;
    private final DroneEndpoint target;
    private final long distance;

    DroneEndpointRoute(DroneEndpoint source, DroneEndpoint target, long distance) {
        this.source = source;
        this.target = target;
        this.distance = distance;
    }

    public DroneEndpoint getSource() { return source; }
    public DroneEndpoint getTarget() { return target; }
    public long getDistance() { return distance; }
}
