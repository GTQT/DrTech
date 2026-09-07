package com.drppp.drtech.common.drone.program.library;

import com.drppp.drtech.common.drone.program.model.DroneProgramGraph;
import net.minecraft.util.ResourceLocation;

import java.util.Objects;
import java.util.function.Supplier;

/** Immutable metadata for a server-owned program template. */
public final class DroneOfficialProgramTemplate {
    private final ResourceLocation id;
    private final String nameKey;
    private final String descriptionKey;
    private final String requiredHardwareKey;
    private final Supplier<DroneProgramGraph> factory;

    public DroneOfficialProgramTemplate(ResourceLocation id, String nameKey, String descriptionKey,
            String requiredHardwareKey, Supplier<DroneProgramGraph> factory) {
        this.id = Objects.requireNonNull(id, "id");
        this.nameKey = Objects.requireNonNull(nameKey, "nameKey");
        this.descriptionKey = Objects.requireNonNull(descriptionKey, "descriptionKey");
        this.requiredHardwareKey = Objects.requireNonNull(requiredHardwareKey, "requiredHardwareKey");
        this.factory = Objects.requireNonNull(factory, "factory");
    }

    public ResourceLocation getId() { return id; }
    public String getNameKey() { return nameKey; }
    public String getDescriptionKey() { return descriptionKey; }
    public String getRequiredHardwareKey() { return requiredHardwareKey; }

    /** A fresh graph is returned on every call. */
    public DroneProgramGraph createGraph() { return factory.get(); }
}
