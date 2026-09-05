package com.drppp.drtech.common.drone.program.runtime.service;

import com.drppp.drtech.common.drone.program.runtime.DroneExecutionResult;
import com.drppp.drtech.common.drone.program.model.DroneArea;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;

public interface DroneMovementService {
    DroneExecutionResult moveTo(BlockPos target);

    /** Returns the nearest stable passable coordinate in an area, or null when none can be used. */
    @Nullable
    default BlockPos findNearestPassablePosition(DroneArea area) {
        return findNearestPassablePosition(area, new long[0]);
    }

    /** Returns the nearest passable coordinate excluding previously rejected packed positions. */
    @Nullable
    default BlockPos findNearestPassablePosition(DroneArea area, long[] excludedPositions) {
        return null;
    }
}
