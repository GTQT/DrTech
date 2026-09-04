package com.drppp.drtech.drone.program.runtime.service;

import com.drppp.drtech.drone.program.runtime.DroneExecutionResult;

public interface DroneSafetyService {
    default DroneExecutionResult configureSafety(int returnAtPercent, int resumeAtPercent) {
        return DroneExecutionResult.error("Safety firmware configuration is unavailable in this runtime environment");
    }
}
