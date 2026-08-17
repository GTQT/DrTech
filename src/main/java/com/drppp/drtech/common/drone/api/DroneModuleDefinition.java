package com.drppp.drtech.common.drone.api;

import net.minecraft.util.ResourceLocation;

import java.util.Objects;

/** Client-safe declaration for a third-party hardware module id. */
public final class DroneModuleDefinition {
    private final ResourceLocation id;
    private final String displayKey;

    public DroneModuleDefinition(ResourceLocation id, String displayKey) {
        this.id = Objects.requireNonNull(id, "Module id is required");
        if (id.toString().length() > 128) throw new IllegalArgumentException("Module id is too long");
        String key = displayKey == null ? "" : displayKey.trim();
        if (key.isEmpty() || key.length() > 128) throw new IllegalArgumentException("Invalid module display key");
        this.displayKey = key;
    }

    public ResourceLocation getId() { return id; }
    public String getDisplayKey() { return displayKey; }
}
