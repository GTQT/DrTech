package com.drppp.drtech.common.drone.program.model;

import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class DroneNodeDefinition {

    public enum FlowRole {
        ENTRY,
        NORMAL,
        TERMINAL,
        VALUE
    }

    private final ResourceLocation id;
    private final String translationKey;
    private final String category;
    private final FlowRole flowRole;
    private final Map<String, DronePortDefinition> ports;
    private final Map<String, DroneNodePropertyDefinition> properties;

    private DroneNodeDefinition(ResourceLocation id, String translationKey, String category, FlowRole flowRole,
            Collection<DronePortDefinition> ports, Collection<DroneNodePropertyDefinition> properties) {
        this.id = Objects.requireNonNull(id, "id");
        this.translationKey = Objects.requireNonNull(translationKey, "translationKey");
        this.category = Objects.requireNonNull(category, "category");
        this.flowRole = Objects.requireNonNull(flowRole, "flowRole");
        LinkedHashMap<String, DronePortDefinition> portMap = new LinkedHashMap<>();
        for (DronePortDefinition port : ports) {
            if (portMap.put(port.getId(), port) != null) {
                throw new IllegalArgumentException("Duplicate port '" + port.getId() + "' in node " + id);
            }
        }
        this.ports = Collections.unmodifiableMap(portMap);
        LinkedHashMap<String, DroneNodePropertyDefinition> propertyMap = new LinkedHashMap<>();
        for (DroneNodePropertyDefinition property : properties) {
            if (propertyMap.put(property.getId(), property) != null) {
                throw new IllegalArgumentException("Duplicate property '" + property.getId() + "' in node " + id);
            }
        }
        this.properties = Collections.unmodifiableMap(propertyMap);
    }

    public static Builder builder(ResourceLocation id, FlowRole flowRole) {
        return new Builder(id, flowRole);
    }

    public ResourceLocation getId() {
        return id;
    }

    public String getTranslationKey() {
        return translationKey;
    }

    public String getCategory() {
        return category;
    }

    public FlowRole getFlowRole() {
        return flowRole;
    }

    public DronePortDefinition getPort(String id) {
        return ports.get(id);
    }

    public Collection<DronePortDefinition> getPorts() {
        return ports.values();
    }

    public DroneNodePropertyDefinition getProperty(String id) { return properties.get(id); }

    public Collection<DroneNodePropertyDefinition> getProperties() { return properties.values(); }

    public static final class Builder {

        private final ResourceLocation id;
        private final FlowRole flowRole;
        private final Collection<DronePortDefinition> ports = new ArrayList<>();
        private final Collection<DroneNodePropertyDefinition> properties = new ArrayList<>();
        private String category = "misc";
        private String translationKey;

        private Builder(ResourceLocation id, FlowRole flowRole) {
            this.id = Objects.requireNonNull(id, "id");
            this.flowRole = Objects.requireNonNull(flowRole, "flowRole");
            this.translationKey = "drtech.drone.node." + id.getPath();
        }

        public Builder category(String category) {
            this.category = category;
            return this;
        }

        public Builder translationKey(String translationKey) {
            this.translationKey = translationKey;
            return this;
        }

        public Builder port(DronePortDefinition port) {
            this.ports.add(port);
            return this;
        }

        public Builder property(DroneNodePropertyDefinition property) {
            this.properties.add(property);
            return this;
        }

        public DroneNodeDefinition build() {
            return new DroneNodeDefinition(id, translationKey, category, flowRole, ports, properties);
        }
    }
}
