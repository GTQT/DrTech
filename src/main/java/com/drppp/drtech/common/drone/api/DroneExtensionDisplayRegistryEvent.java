package com.drppp.drtech.common.drone.api;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.eventhandler.Event;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Registration event for client-safe extension title, description and icon metadata. */
public final class DroneExtensionDisplayRegistryEvent extends Event {
    private final Map<ResourceLocation, DroneExtensionDisplay> displays;

    DroneExtensionDisplayRegistryEvent(Map<ResourceLocation, DroneExtensionDisplay> displays) {
        this.displays = displays;
    }

    public void register(DroneExtensionDisplay display) {
        if (display == null || displays.size() >= 1024
                || displays.putIfAbsent(display.getExtensionId(), display) != null) {
            throw new IllegalArgumentException("Duplicate or null extension display");
        }
    }

    public Map<ResourceLocation, DroneExtensionDisplay> getDisplays() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(displays));
    }
}
