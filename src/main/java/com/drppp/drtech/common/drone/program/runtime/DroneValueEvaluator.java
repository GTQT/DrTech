package com.drppp.drtech.common.drone.program.runtime;

@FunctionalInterface
public interface DroneValueEvaluator {

    Object evaluate(DroneValueEvaluationContext context, String outputPort);
}
