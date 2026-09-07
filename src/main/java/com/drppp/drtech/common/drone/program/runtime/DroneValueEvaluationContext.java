package com.drppp.drtech.common.drone.program.runtime;

import com.drppp.drtech.common.drone.program.model.DroneProgramNode;
import net.minecraft.nbt.NBTTagCompound;

public final class DroneValueEvaluationContext {

    private final DroneProgramNode node;
    private final DroneRuntimeEnvironment environment;
    private final DroneNodeExecutionContext.InputResolver inputResolver;
    private final DroneRuntimeMemory memory;

    DroneValueEvaluationContext(DroneProgramNode node, DroneRuntimeEnvironment environment,
            DroneRuntimeMemory memory, DroneNodeExecutionContext.InputResolver inputResolver) {
        this.node = node;
        this.environment = environment;
        this.memory = memory;
        this.inputResolver = inputResolver;
    }

    public DroneProgramNode getNode() {
        return node;
    }

    public NBTTagCompound getConfiguration() {
        return node.getConfiguration();
    }

    public DroneRuntimeEnvironment getEnvironment() {
        return environment;
    }

    public DroneRuntimeMemory getMemory() {
        return memory;
    }

    public <T> T requireInput(String portId, Class<T> type) {
        T value = inputResolver.resolve(portId, type);
        if (value == null) throw new IllegalStateException("Required value input is missing: " + portId);
        return value;
    }

    public <T> T getOptionalInput(String portId, Class<T> type) {
        return inputResolver.resolve(portId, type);
    }
}
