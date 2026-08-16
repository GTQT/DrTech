package com.drppp.drtech.common.drone.api;

import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Client-safe availability snapshot; missing implementations remain renderable as placeholders. */
public final class DroneExtensionAvailability {
    public enum Status { AVAILABLE, MISSING_MODULE, VERSION_INCOMPATIBLE }
    private final DroneExtensionDescriptor descriptor;
    private final Status status;
    private final List<ResourceLocation> missingModules;

    private DroneExtensionAvailability(DroneExtensionDescriptor descriptor, Status status, List<ResourceLocation> missingModules) {
        this.descriptor = descriptor;
        this.status = status;
        this.missingModules = Collections.unmodifiableList(new ArrayList<>(missingModules));
    }
    public static DroneExtensionAvailability resolve(DroneExtensionDescriptor descriptor, int supportedVersion,
            List<ResourceLocation> installedModules) {
        if (descriptor == null) throw new IllegalArgumentException("Descriptor is required");
        if (descriptor.getVersion() > Math.max(0, supportedVersion)) return new DroneExtensionAvailability(descriptor, Status.VERSION_INCOMPATIBLE, Collections.emptyList());
        ArrayList<ResourceLocation> missing = new ArrayList<>();
        for (ResourceLocation required : descriptor.getRequiredModules()) if (installedModules == null || !installedModules.contains(required)) missing.add(required);
        return new DroneExtensionAvailability(descriptor, missing.isEmpty() ? Status.AVAILABLE : Status.MISSING_MODULE, missing);
    }
    public DroneExtensionDescriptor getDescriptor() { return descriptor; }
    public Status getStatus() { return status; }
    public List<ResourceLocation> getMissingModules() { return missingModules; }
    public boolean isPlaceholder() { return status != Status.AVAILABLE; }
    public String getDisplayKey() { return isPlaceholder() ? "drtech.drone.extension.missing" : descriptor.getId().toString(); }
}
