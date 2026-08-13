package com.drppp.drtech.common.drone.action;

import com.drppp.drtech.common.drone.filter.DroneItemFilterSpec;

import javax.annotation.Nullable;
import java.util.Objects;

/** Bounded request for moving items between the drone cargo and world item entities. */
public final class DroneItemWorldRequest {
    private final double radius;
    private final int maximumAmount;
    private final DroneItemFilterSpec filter;
    private final DroneFailurePolicy failurePolicy;

    public DroneItemWorldRequest(double radius, int maximumAmount, @Nullable DroneItemFilterSpec filter,
            DroneFailurePolicy failurePolicy) {
        if (!Double.isFinite(radius) || radius < 0.5D || radius > 32.0D) {
            throw new IllegalArgumentException("Radius must be between 0.5 and 32 blocks");
        }
        if (maximumAmount <= 0 || maximumAmount > 1_000_000) {
            throw new IllegalArgumentException("Maximum amount must be between 1 and 1000000");
        }
        this.radius = radius;
        this.maximumAmount = maximumAmount;
        this.filter = filter == null ? DroneItemFilterSpec.ANY : filter;
        this.failurePolicy = Objects.requireNonNull(failurePolicy, "failurePolicy");
    }

    public double getRadius() { return radius; }
    public int getMaximumAmount() { return maximumAmount; }
    public DroneItemFilterSpec getFilter() { return filter; }
    public DroneFailurePolicy getFailurePolicy() { return failurePolicy; }
}
