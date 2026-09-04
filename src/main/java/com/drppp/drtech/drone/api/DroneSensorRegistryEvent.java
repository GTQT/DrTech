package com.drppp.drtech.drone.api;

import com.drppp.drtech.drone.program.runtime.DroneValueEvaluator;
import com.drppp.drtech.drone.program.runtime.DroneValueEvaluatorRegistry;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.eventhandler.Event;

import java.util.Map;

/** Forge event fired once while third-party sensor/value evaluators may be registered. */
public final class DroneSensorRegistryEvent extends Event {
    private final DroneValueEvaluatorRegistry registry;

    DroneSensorRegistryEvent(DroneValueEvaluatorRegistry registry) { this.registry = registry; }

    public void register(ResourceLocation id, DroneValueEvaluator evaluator) { registry.register(id, evaluator); }
    public Map<ResourceLocation, DroneValueEvaluator> getSensors() { return registry.snapshot(); }
}
