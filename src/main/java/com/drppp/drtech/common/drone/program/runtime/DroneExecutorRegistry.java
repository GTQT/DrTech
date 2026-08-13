package com.drppp.drtech.common.drone.program.runtime;

import net.minecraft.util.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class DroneExecutorRegistry {

    private final Map<ResourceLocation, DroneNodeExecutor> executors = new HashMap<>();
    private boolean frozen;

    public void register(ResourceLocation nodeType, DroneNodeExecutor executor) {
        if (frozen) throw new IllegalStateException("Executor registry is frozen");
        if (executors.putIfAbsent(Objects.requireNonNull(nodeType), Objects.requireNonNull(executor)) != null) {
            throw new IllegalArgumentException("Duplicate executor for " + nodeType);
        }
    }

    public DroneNodeExecutor get(ResourceLocation nodeType) {
        return executors.get(nodeType);
    }

    public void freeze() {
        frozen = true;
    }
}
