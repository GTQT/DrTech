package com.drppp.drtech.drone.api;

import com.drppp.drtech.drone.program.model.DroneNodeDefinition;
import com.drppp.drtech.drone.program.registry.DroneNodeRegistry;
import net.minecraftforge.fml.common.eventhandler.Event;

/** Forge event fired while third-party drone node definitions are registered. */
public class DroneNodeRegistryEvent extends Event {
    private final DroneNodeRegistry registry;
    private final java.util.Map<net.minecraft.util.ResourceLocation, DroneExtensionDescriptor> descriptors;
    private final java.util.Map<net.minecraft.util.ResourceLocation, net.minecraft.util.ResourceLocation> nodeExtensions;
    public DroneNodeRegistryEvent(DroneNodeRegistry registry) {
        this(registry, new java.util.LinkedHashMap<>(), new java.util.LinkedHashMap<>());
    }
    DroneNodeRegistryEvent(DroneNodeRegistry registry,
            java.util.Map<net.minecraft.util.ResourceLocation, DroneExtensionDescriptor> descriptors,
            java.util.Map<net.minecraft.util.ResourceLocation, net.minecraft.util.ResourceLocation> nodeExtensions) {
        this.registry = registry; this.descriptors = descriptors; this.nodeExtensions = nodeExtensions;
    }
    public DroneNodeRegistry getRegistry() { return registry; }
    public void register(DroneNodeDefinition definition) { registry.register(definition); }
    public void register(DroneNodeDefinition definition, DroneExtensionDescriptor descriptor) {
        registerDescriptorShared(descriptor); register(definition);
        if (nodeExtensions.size() >= 4096 || nodeExtensions.putIfAbsent(definition.getId(), descriptor.getId()) != null) {
            throw new IllegalArgumentException("Duplicate node extension binding " + definition.getId());
        }
    }
    public void registerDescriptor(DroneExtensionDescriptor descriptor) {
        if (descriptor == null || descriptors.size() >= 1024
                || descriptors.putIfAbsent(descriptor.getId(), descriptor) != null) {
            throw new IllegalArgumentException("Duplicate, null or excessive extension descriptor");
        }
    }
    private void registerDescriptorShared(DroneExtensionDescriptor descriptor) {
        if (descriptor == null) throw new IllegalArgumentException("Extension descriptor is required");
        DroneExtensionDescriptor existing = descriptors.get(descriptor.getId());
        if (existing == null) registerDescriptor(descriptor);
        else if (!existing.equals(descriptor)) throw new IllegalArgumentException(
                "Conflicting extension descriptor " + descriptor.getId());
    }
    public java.util.Map<net.minecraft.util.ResourceLocation, DroneExtensionDescriptor> getDescriptors() { return java.util.Collections.unmodifiableMap(descriptors); }
    public java.util.Map<net.minecraft.util.ResourceLocation, net.minecraft.util.ResourceLocation> getNodeExtensions() { return java.util.Collections.unmodifiableMap(nodeExtensions); }
}
