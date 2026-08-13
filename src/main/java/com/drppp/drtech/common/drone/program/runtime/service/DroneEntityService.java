package com.drppp.drtech.common.drone.program.runtime.service;

import com.drppp.drtech.common.drone.program.runtime.DroneExecutionResult;
import net.minecraft.util.math.BlockPos;

public interface DroneEntityService {
    default DroneExecutionResult interactWithNearestEntity(BlockPos target) {
        return DroneExecutionResult.error("Entity interaction is unavailable in this runtime environment");
    }
}
