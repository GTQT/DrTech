package com.drppp.drtech.drone.program.runtime;

import com.drppp.drtech.drone.program.model.DroneProgramNode;
import net.minecraft.nbt.NBTTagCompound;

public final class DroneNodeExecutionContext {

    private final DroneProgramNode node;
    private final NBTTagCompound state;
    private final DroneRuntimeEnvironment environment;
    private final InputResolver inputResolver;
    private final DroneRuntimeMemory memory;

    DroneNodeExecutionContext(DroneProgramNode node, NBTTagCompound state, DroneRuntimeEnvironment environment,
            DroneRuntimeMemory memory, InputResolver inputResolver) {
        this.node = node;
        this.state = state;
        this.environment = environment;
        this.memory = memory;
        this.inputResolver = inputResolver;
    }

    public DroneProgramNode getNode() {
        return node;
    }

    /** Mutable state belongs to this node activation and is persisted by the runtime. */
    public NBTTagCompound getState() {
        return state;
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

    public <T> T getOptionalInput(String portId, Class<T> type) {
        return inputResolver.resolve(portId, type);
    }

    public <T> T requireInput(String portId, Class<T> type) {
        T value = getOptionalInput(portId, type);
        if (value == null) throw new IllegalStateException("Required input is missing: " + portId);
        return value;
    }

    @FunctionalInterface
    interface InputResolver {
        <T> T resolve(String portId, Class<T> type);
    }
}
