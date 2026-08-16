package com.drppp.drtech.common.drone.network;

import net.minecraft.util.math.BlockPos;

/** Immutable client/render snapshot for one source-to-target logistics line. */
public final class DroneEndpointWorldLink {
    private final int dimension;
    private final BlockPos sourcePosition;
    private final BlockPos targetPosition;
    private final long distance;

    DroneEndpointWorldLink(DroneEndpointRoute route) {
        this.dimension = route.getSource().getDimension();
        this.sourcePosition = route.getSource().getPosition();
        this.targetPosition = route.getTarget().getPosition();
        this.distance = route.getDistance();
    }

    public int getDimension() { return dimension; }
    public BlockPos getSourcePosition() { return sourcePosition; }
    public BlockPos getTargetPosition() { return targetPosition; }
    public long getDistance() { return distance; }
}
