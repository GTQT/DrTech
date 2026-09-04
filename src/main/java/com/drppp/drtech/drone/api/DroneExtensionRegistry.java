package com.drppp.drtech.drone.api;

import com.drppp.drtech.drone.program.registry.DrTechDroneNodes;
import com.drppp.drtech.drone.program.registry.DroneNodeRegistry;
import com.drppp.drtech.drone.program.runtime.DrTechDroneExecutors;
import com.drppp.drtech.drone.program.runtime.DrTechDroneValueEvaluators;
import com.drppp.drtech.drone.program.runtime.DroneExecutorRegistry;
import com.drppp.drtech.drone.program.runtime.DroneValueEvaluatorRegistry;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** One authoritative registry set shared by editor compilation and entity runtime. */
public final class DroneExtensionRegistry {
    public static final int API_VERSION = 1;
    private static DroneNodeRegistry nodes;
    private static DroneExecutorRegistry actions;
    private static DroneValueEvaluatorRegistry sensors;
    private static Map<ResourceLocation, DroneModuleDefinition> modules;
    private static Map<ResourceLocation, DroneExtensionDescriptor> descriptors;
    private static Map<ResourceLocation, DroneExtensionDisplay> displays;
    private static Map<ResourceLocation, ResourceLocation> nodeExtensions;
    private static boolean bootstrapped;
    private static boolean bootstrapping;

    private DroneExtensionRegistry() { }

    public static synchronized void bootstrap() {
        if (bootstrapped || bootstrapping) return;
        bootstrapping = true;
        DroneNodeRegistry nodeRegistry = DrTechDroneNodes.createExtensibleRegistry();
        DroneExecutorRegistry actionRegistry = DrTechDroneExecutors.createExtensibleRegistry();
        DroneValueEvaluatorRegistry sensorRegistry = DrTechDroneValueEvaluators.createExtensibleRegistry();
        Map<ResourceLocation, DroneModuleDefinition> moduleRegistry = new LinkedHashMap<>();
        Map<ResourceLocation, DroneExtensionDisplay> displayRegistry = new LinkedHashMap<>();
        Map<ResourceLocation, DroneExtensionDescriptor> descriptorRegistry = new LinkedHashMap<>();
        Map<ResourceLocation, ResourceLocation> nodeExtensionRegistry = new LinkedHashMap<>();
        nodes = nodeRegistry; actions = actionRegistry; sensors = sensorRegistry;
        modules = moduleRegistry; displays = displayRegistry;
        descriptors = descriptorRegistry; nodeExtensions = nodeExtensionRegistry;

        DroneNodeRegistryEvent nodeEvent = new DroneNodeRegistryEvent(
                nodeRegistry, descriptorRegistry, nodeExtensionRegistry);
        MinecraftForge.EVENT_BUS.post(nodeEvent);
        MinecraftForge.EVENT_BUS.post(new DroneActionRegistryEvent(actionRegistry));
        MinecraftForge.EVENT_BUS.post(new DroneSensorRegistryEvent(sensorRegistry));
        MinecraftForge.EVENT_BUS.post(new DroneModuleRegistryEvent(moduleRegistry));
        MinecraftForge.EVENT_BUS.post(new DroneExtensionDisplayRegistryEvent(displayRegistry));

        nodeRegistry.freeze(); actionRegistry.freeze(); sensorRegistry.freeze();
        nodes = nodeRegistry; actions = actionRegistry; sensors = sensorRegistry;
        modules = immutable(moduleRegistry); descriptors = immutable(nodeEvent.getDescriptors());
        displays = immutable(displayRegistry); nodeExtensions = immutable(nodeExtensionRegistry);
        bootstrapped = true; bootstrapping = false;
    }

    public static DroneNodeRegistry nodes() { bootstrap(); return nodes; }
    public static DroneExecutorRegistry actions() { bootstrap(); return actions; }
    public static DroneValueEvaluatorRegistry sensors() { bootstrap(); return sensors; }
    public static Map<ResourceLocation, DroneModuleDefinition> modules() { bootstrap(); return modules; }
    public static Map<ResourceLocation, DroneExtensionDescriptor> descriptors() { bootstrap(); return descriptors; }
    public static Map<ResourceLocation, DroneExtensionDisplay> displays() { bootstrap(); return displays; }
    public static Map<ResourceLocation, ResourceLocation> nodeExtensions() { bootstrap(); return nodeExtensions; }

    public static DroneExtensionAvailability availabilityForNode(ResourceLocation nodeId) {
        bootstrap(); ResourceLocation extensionId = nodeExtensions.get(nodeId);
        DroneExtensionDescriptor descriptor = extensionId == null ? null : descriptors.get(extensionId);
        return descriptor == null ? null : DroneExtensionAvailability.resolve(descriptor);
    }

    private static <T> Map<ResourceLocation, T> immutable(Map<ResourceLocation, T> values) {
        return Collections.unmodifiableMap(new LinkedHashMap<>(values));
    }
}
