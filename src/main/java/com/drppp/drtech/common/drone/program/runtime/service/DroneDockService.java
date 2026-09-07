package com.drppp.drtech.common.drone.program.runtime.service;

import com.drppp.drtech.common.drone.program.runtime.DroneExecutionResult;
import net.minecraft.util.math.BlockPos;

public interface DroneDockService {
    default BlockPos findNearestDock() {
        return null;
    }

    default DroneExecutionResult bindDock(BlockPos target) {
        return DroneExecutionResult.error("Dock binding is unavailable in this runtime environment");
    }

    default DroneExecutionResult unbindDock() {
        return DroneExecutionResult.error("Dock binding is unavailable in this runtime environment");
    }

    default DroneExecutionResult returnToDock(BlockPos target) {
        return DroneExecutionResult.error("Dock navigation is unavailable in this runtime environment");
    }

    default DroneExecutionResult chargeUntil(double percent) {
        return DroneExecutionResult.error("Dock charging is unavailable in this runtime environment");
    }
}
