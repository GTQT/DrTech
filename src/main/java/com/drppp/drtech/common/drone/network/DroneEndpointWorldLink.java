package com.drppp.drtech.common.drone.network;

import net.minecraft.util.math.BlockPos;

/** Immutable client/render snapshot for one source-to-target logistics line. */
public final class DroneEndpointWorldLink {
    private final int dimension;
    private final BlockPos sourcePosition;
    private final BlockPos targetPosition;
    private final long distance;

    DroneEndpointWorldLink(DroneEndpointRoute route) {
        this(route.getSource().getDimension(), route.getSource().getPosition(),
                route.getTarget().getPosition(), route.getDistance());
    }

    public DroneEndpointWorldLink(int dimension, BlockPos sourcePosition, BlockPos targetPosition, long distance) {
        if (sourcePosition == null || targetPosition == null) throw new IllegalArgumentException("Link positions are required");
        this.dimension = dimension;
        this.sourcePosition = sourcePosition.toImmutable();
        this.targetPosition = targetPosition.toImmutable();
        this.distance = Math.max(0L, distance);
    }

    public int getDimension() { return dimension; }
    public BlockPos getSourcePosition() { return sourcePosition; }
    public BlockPos getTargetPosition() { return targetPosition; }
    public long getDistance() { return distance; }
}
