package com.drppp.drtech.common.drone.api;

import com.drppp.drtech.common.drone.program.model.DroneNodeDefinition;
import com.drppp.drtech.common.drone.program.registry.DroneNodeRegistry;
import net.minecraftforge.fml.common.eventhandler.Event;

/** Forge event fired while third-party drone node definitions are registered. */
public class DroneNodeRegistryEvent extends Event {
    private final DroneNodeRegistry registry;
    private final java.util.Map<net.minecraft.util.ResourceLocation, DroneExtensionDescriptor> descriptors = new java.util.LinkedHashMap<>();
    public DroneNodeRegistryEvent(DroneNodeRegistry registry) { this.registry = registry; }
    public DroneNodeRegistry getRegistry() { return registry; }
    public void register(DroneNodeDefinition definition) { registry.register(definition); }
    public void registerDescriptor(DroneExtensionDescriptor descriptor) { if (descriptor == null || descriptors.putIfAbsent(descriptor.getId(), descriptor) != null) throw new IllegalArgumentException("Duplicate extension descriptor"); }
    public java.util.Map<net.minecraft.util.ResourceLocation, DroneExtensionDescriptor> getDescriptors() { return java.util.Collections.unmodifiableMap(descriptors); }
}
