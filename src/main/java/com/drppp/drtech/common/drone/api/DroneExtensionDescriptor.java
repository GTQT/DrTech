package com.drppp.drtech.common.drone.api;

import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Versioned, permission-aware declaration for a third-party drone extension. */
public final class DroneExtensionDescriptor {
    private final ResourceLocation id;
    private final int version;
    private final List<String> permissions;
    private final List<ResourceLocation> requiredModules;

    public DroneExtensionDescriptor(ResourceLocation id, int version, List<String> permissions,
            List<ResourceLocation> requiredModules) {
        if (id == null) throw new IllegalArgumentException("Extension id is required");
        this.id = id;
        this.version = Math.max(1, version);
        this.permissions = boundedStrings(permissions, 32, 64);
        this.requiredModules = boundedIds(requiredModules, 32);
    }
    public ResourceLocation getId() { return id; }
    public int getVersion() { return version; }
    public List<String> getPermissions() { return permissions; }
    public List<ResourceLocation> getRequiredModules() { return requiredModules; }

    private static List<String> boundedStrings(List<String> values, int max, int maxLength) {
        ArrayList<String> result = new ArrayList<>();
        if (values != null) for (String value : values) if (value != null && !value.trim().isEmpty() && result.size() < max) result.add(value.trim().substring(0, Math.min(maxLength, value.trim().length())));
        return Collections.unmodifiableList(result);
    }
    private static List<ResourceLocation> boundedIds(List<ResourceLocation> values, int max) {
        ArrayList<ResourceLocation> result = new ArrayList<>();
        if (values != null) for (ResourceLocation value : values) if (value != null && result.size() < max && !result.contains(value)) result.add(value);
        return Collections.unmodifiableList(result);
    }
}
