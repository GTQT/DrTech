package com.drppp.drtech.drone.navigation;

import net.minecraft.util.math.BlockPos;

import java.util.Collections;
import java.util.List;

public final class DronePathResult {

    private final DronePathStatus status;
    private final List<BlockPos> path;
    private final int visitedNodes;

    DronePathResult(DronePathStatus status, List<BlockPos> path, int visitedNodes) {
        this.status = status;
        this.path = Collections.unmodifiableList(path);
        this.visitedNodes = visitedNodes;
    }

    public DronePathStatus getStatus() {
        return status;
    }

    public boolean isFound() {
        return status == DronePathStatus.FOUND;
    }

    /** Includes both start and goal. */
    public List<BlockPos> getPath() {
        return path;
    }

    public int getVisitedNodes() {
        return visitedNodes;
    }
}
