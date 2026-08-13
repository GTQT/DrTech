package com.drppp.drtech.common.drone.program.runtime;

import net.minecraft.util.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class DroneValueEvaluatorRegistry {

    private final Map<ResourceLocation, DroneValueEvaluator> evaluators = new HashMap<>();
    private boolean frozen;

    public void register(ResourceLocation type, DroneValueEvaluator evaluator) {
        if (frozen) throw new IllegalStateException("Value evaluator registry is frozen");
        if (evaluators.putIfAbsent(Objects.requireNonNull(type), Objects.requireNonNull(evaluator)) != null) {
            throw new IllegalArgumentException("Duplicate value evaluator for " + type);
        }
    }

    public DroneValueEvaluator get(ResourceLocation type) {
        return evaluators.get(type);
    }

    public void freeze() {
        frozen = true;
    }
}
