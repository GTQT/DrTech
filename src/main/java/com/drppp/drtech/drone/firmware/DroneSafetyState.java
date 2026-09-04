package com.drppp.drtech.drone.firmware;

/** Program-independent safety state. Safety handling always has priority over graph execution. */
public enum DroneSafetyState {
    PROGRAM,
    RETURNING,
    CHARGING,
    RECOVERING,
    SAFE_IDLE,
    EMERGENCY_LAND
}
