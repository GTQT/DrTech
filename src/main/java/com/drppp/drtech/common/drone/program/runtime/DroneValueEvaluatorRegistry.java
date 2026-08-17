package com.drppp.drtech.common.drone.program.runtime;

import net.minecraft.util.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Collections;

public final class DroneValueEvaluatorRegistry {
    private static final int MAX_ENTRIES = 4096;

    private final Map<ResourceLocation, DroneValueEvaluator> evaluators = new HashMap<>();
    private boolean frozen;

    public void register(ResourceLocation type, DroneValueEvaluator evaluator) {
        if (frozen) throw new IllegalStateException("Value evaluator registry is frozen");
        if (type == null || type.toString().length() > 128 || evaluators.size() >= MAX_ENTRIES) {
            throw new IllegalArgumentException("Value evaluator registry limit exceeded");
        }
        if (evaluators.putIfAbsent(Objects.requireNonNull(type), Objects.requireNonNull(evaluator)) != null) {
            throw new IllegalArgumentException("Duplicate value evaluator for " + type);
        }
    }

    public DroneValueEvaluator get(ResourceLocation type) {
        return evaluators.get(type);
    }

    public Map<ResourceLocation, DroneValueEvaluator> snapshot() {
        return Collections.unmodifiableMap(new HashMap<>(evaluators));
    }

    public void freeze() {
        frozen = true;
    }

    public boolean isFrozen() { return frozen; }
}
