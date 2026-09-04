package com.drppp.drtech.drone.action;

import com.drppp.drtech.drone.filter.DroneBlockFilterSpec;
import com.drppp.drtech.drone.program.model.DroneArea;

import javax.annotation.Nullable;
import java.util.Objects;

public final class DroneAreaTraversalRequest {
    private final DroneArea area;
    private final DroneTraversalOrder order;
    private final DroneBlockFilterSpec blockFilter;
    private final boolean skipAir;
    private final long randomSeed;

    public DroneAreaTraversalRequest(DroneArea area, DroneTraversalOrder order,
            @Nullable DroneBlockFilterSpec blockFilter, boolean skipAir, long randomSeed) {
        this.area = Objects.requireNonNull(area, "area");
        this.order = Objects.requireNonNull(order, "order");
        this.blockFilter = blockFilter;
        this.skipAir = skipAir;
        this.randomSeed = randomSeed;
    }

    public DroneArea getArea() { return area; }
    public DroneTraversalOrder getOrder() { return order; }
    @Nullable public DroneBlockFilterSpec getBlockFilter() { return blockFilter; }
    public boolean isSkipAir() { return skipAir; }
    public long getRandomSeed() { return randomSeed; }
}
