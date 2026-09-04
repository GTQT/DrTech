package com.drppp.drtech.drone.program.runtime;

@FunctionalInterface
public interface DroneNodeExecutor {

    DroneExecutionResult tick(DroneNodeExecutionContext context);
}
