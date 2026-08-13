package com.drppp.drtech.common.drone.program.runtime;

/** Stable semantic result exposed to future status ports and failure branches. */
public enum DroneActionStatus {
    RUNNING,
    PAUSED,
    SUCCESS,
    FAILURE,
    NOT_FOUND,
    NO_SPACE,
    NO_RESOURCE,
    NO_ENERGY,
    OUT_OF_RANGE,
    UNLOADED,
    DENIED,
    UNREACHABLE,
    INVALID_TARGET,
    ERROR;

    public boolean isFailure() {
        return this != RUNNING && this != PAUSED && this != SUCCESS;
    }
}
