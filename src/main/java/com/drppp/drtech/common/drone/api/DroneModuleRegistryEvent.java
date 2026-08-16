package com.drppp.drtech.common.drone.api;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.eventhandler.Event;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Forge event for third-party drone module descriptors. */
public class DroneModuleRegistryEvent extends Event {
    private final Map<ResourceLocation, Object> modules = new LinkedHashMap<>();
    public void register(ResourceLocation id, Object descriptor) { if (id == null || descriptor == null) throw new IllegalArgumentException("Module id and descriptor are required"); if (modules.putIfAbsent(id, descriptor) != null) throw new IllegalArgumentException("Duplicate module id " + id); }
    public Map<ResourceLocation, Object> getModules() { return Collections.unmodifiableMap(modules); }
}
