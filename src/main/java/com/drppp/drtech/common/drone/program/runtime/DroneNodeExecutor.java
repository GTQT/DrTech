package com.drppp.drtech.common.drone.program.runtime;

@FunctionalInterface
public interface DroneNodeExecutor {

    DroneExecutionResult tick(DroneNodeExecutionContext context);
}
