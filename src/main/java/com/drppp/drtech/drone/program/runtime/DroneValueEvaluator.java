package com.drppp.drtech.drone.program.runtime;

@FunctionalInterface
public interface DroneValueEvaluator {

    Object evaluate(DroneValueEvaluationContext context, String outputPort);
}
