package com.drppp.drtech.drone.program.runtime;

import net.minecraft.util.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Collections;

public final class DroneExecutorRegistry {
    private static final int MAX_ENTRIES = 4096;

    private final Map<ResourceLocation, DroneNodeExecutor> executors = new HashMap<>();
    private boolean frozen;

    public void register(ResourceLocation nodeType, DroneNodeExecutor executor) {
        if (frozen) throw new IllegalStateException("Executor registry is frozen");
        if (nodeType == null || nodeType.toString().length() > 128 || executors.size() >= MAX_ENTRIES) {
            throw new IllegalArgumentException("Executor registry limit exceeded");
        }
        if (executors.putIfAbsent(Objects.requireNonNull(nodeType), Objects.requireNonNull(executor)) != null) {
            throw new IllegalArgumentException("Duplicate executor for " + nodeType);
        }
    }

    public DroneNodeExecutor get(ResourceLocation nodeType) {
        return executors.get(nodeType);
    }

    public Map<ResourceLocation, DroneNodeExecutor> snapshot() {
        return Collections.unmodifiableMap(new HashMap<>(executors));
    }

    public void freeze() {
        frozen = true;
    }

    public boolean isFrozen() { return frozen; }
}
