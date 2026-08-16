package com.drppp.drtech.common.drone.api;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.eventhandler.Event;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Forge event for third-party sensor handlers. */
public class DroneSensorRegistryEvent extends Event {
    private final Map<ResourceLocation, Object> sensors = new LinkedHashMap<>();
    public void register(ResourceLocation id, Object handler) { if (id == null || handler == null) throw new IllegalArgumentException("Sensor id and handler are required"); if (sensors.putIfAbsent(id, handler) != null) throw new IllegalArgumentException("Duplicate sensor id " + id); }
    public Map<ResourceLocation, Object> getSensors() { return Collections.unmodifiableMap(sensors); }
}
