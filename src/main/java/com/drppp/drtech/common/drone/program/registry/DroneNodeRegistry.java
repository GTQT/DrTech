package com.drppp.drtech.common.drone.program.registry;

import com.drppp.drtech.common.drone.program.model.DroneNodeDefinition;
import net.minecraft.util.ResourceLocation;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/** Dedicated registry; NBT stores ResourceLocation ids and never Java class names. */
public final class DroneNodeRegistry {

    private final Map<ResourceLocation, DroneNodeDefinition> definitions = new LinkedHashMap<>();
    private boolean frozen;

    public void register(DroneNodeDefinition definition) {
        if (frozen) {
            throw new IllegalStateException("Drone node registry is frozen");
        }
        Objects.requireNonNull(definition, "definition");
        if (definitions.putIfAbsent(definition.getId(), definition) != null) {
            throw new IllegalArgumentException("Duplicate drone node id " + definition.getId());
        }
    }

    public DroneNodeDefinition get(ResourceLocation id) {
        return definitions.get(id);
    }

    public Collection<DroneNodeDefinition> values() {
        return Collections.unmodifiableCollection(definitions.values());
    }

    public void freeze() {
        frozen = true;
    }

    public boolean isFrozen() {
        return frozen;
    }
}
