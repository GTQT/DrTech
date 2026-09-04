package com.drppp.drtech.drone.api;

import com.drppp.drtech.drone.program.runtime.DroneExecutorRegistry;
import com.drppp.drtech.drone.program.runtime.DroneNodeExecutor;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.eventhandler.Event;

import java.util.Map;

/** Forge event fired once while third-party action executors may be registered. */
public final class DroneActionRegistryEvent extends Event {
    private final DroneExecutorRegistry registry;

    DroneActionRegistryEvent(DroneExecutorRegistry registry) { this.registry = registry; }

    public void register(ResourceLocation id, DroneNodeExecutor executor) { registry.register(id, executor); }
    public Map<ResourceLocation, DroneNodeExecutor> getActions() { return registry.snapshot(); }
}
