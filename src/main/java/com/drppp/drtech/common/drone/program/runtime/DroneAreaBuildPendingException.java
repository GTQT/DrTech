package com.drppp.drtech.common.drone.program.runtime;

/** Internal control-flow signal: the current value chain used its per-tick area construction budget. */
final class DroneAreaBuildPendingException extends RuntimeException {
    static final DroneAreaBuildPendingException INSTANCE = new DroneAreaBuildPendingException();

    private DroneAreaBuildPendingException() {
        super(null, null, false, false);
    }
}
