package com.drppp.drtech.drone.api;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.eventhandler.Event;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Forge event fired once while third-party module declarations may be registered. */
public final class DroneModuleRegistryEvent extends Event {
    private final Map<ResourceLocation, DroneModuleDefinition> modules;

    DroneModuleRegistryEvent(Map<ResourceLocation, DroneModuleDefinition> modules) { this.modules = modules; }

    public void register(DroneModuleDefinition definition) {
        if (definition == null || modules.size() >= 1024
                || modules.putIfAbsent(definition.getId(), definition) != null) {
            throw new IllegalArgumentException("Duplicate or null drone module definition");
        }
    }

    public Map<ResourceLocation, DroneModuleDefinition> getModules() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(modules));
    }
}
