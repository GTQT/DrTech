package com.drppp.drtech.common.drone.api;

import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** Versioned, permission-aware declaration for a third-party drone extension. */
public final class DroneExtensionDescriptor {
    private final ResourceLocation id;
    private final int version;
    private final List<String> permissions;
    private final List<ResourceLocation> requiredModules;

    public DroneExtensionDescriptor(ResourceLocation id, int version, List<String> permissions,
            List<ResourceLocation> requiredModules) {
        if (id == null) throw new IllegalArgumentException("Extension id is required");
        if (id.toString().length() > 128) throw new IllegalArgumentException("Extension id is too long");
        this.id = id;
        if (version < 1 || version > 1_000_000) throw new IllegalArgumentException("Extension version is invalid");
        this.version = version;
        this.permissions = boundedStrings(permissions, 32, 64);
        this.requiredModules = boundedIds(requiredModules, 32);
    }
    public ResourceLocation getId() { return id; }
    public int getVersion() { return version; }
    public List<String> getPermissions() { return permissions; }
    public List<ResourceLocation> getRequiredModules() { return requiredModules; }

    private static List<String> boundedStrings(List<String> values, int max, int maxLength) {
        ArrayList<String> result = new ArrayList<>();
        if (values != null) for (String value : values) if (value != null && !value.trim().isEmpty()
                && result.size() < max) {
            String checked = value.trim().substring(0, Math.min(maxLength, value.trim().length()));
            if (!result.contains(checked)) result.add(checked);
        }
        return Collections.unmodifiableList(result);
    }
    private static List<ResourceLocation> boundedIds(List<ResourceLocation> values, int max) {
        ArrayList<ResourceLocation> result = new ArrayList<>();
        if (values != null) for (ResourceLocation value : values) if (value != null && result.size() < max && !result.contains(value)) result.add(value);
        return Collections.unmodifiableList(result);
    }

    @Override public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof DroneExtensionDescriptor)) return false;
        DroneExtensionDescriptor that = (DroneExtensionDescriptor) other;
        return version == that.version && id.equals(that.id) && permissions.equals(that.permissions)
                && requiredModules.equals(that.requiredModules);
    }
    @Override public int hashCode() { return Objects.hash(id, version, permissions, requiredModules); }
}
