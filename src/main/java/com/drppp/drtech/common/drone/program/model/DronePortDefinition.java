package com.drppp.drtech.common.drone.program.model;

import java.util.Objects;

public final class DronePortDefinition {

    private final String id;
    private final DronePortDirection direction;
    private final DronePortType type;
    private final boolean required;
    private final boolean multipleConnections;

    public DronePortDefinition(String id, DronePortDirection direction, DronePortType type, boolean required,
            boolean multipleConnections) {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("Port id cannot be empty");
        }
        this.id = id;
        this.direction = Objects.requireNonNull(direction, "direction");
        this.type = Objects.requireNonNull(type, "type");
        this.required = required;
        this.multipleConnections = multipleConnections;
    }

    public static DronePortDefinition input(String id, DronePortType type, boolean required) {
        return new DronePortDefinition(id, DronePortDirection.INPUT, type, required, false);
    }

    public static DronePortDefinition output(String id, DronePortType type, boolean required) {
        return new DronePortDefinition(id, DronePortDirection.OUTPUT, type, required, false);
    }

    public static DronePortDefinition multiInput(String id, DronePortType type, boolean required) {
        return new DronePortDefinition(id, DronePortDirection.INPUT, type, required, true);
    }

    public static DronePortDefinition multiOutput(String id, DronePortType type, boolean required) {
        return new DronePortDefinition(id, DronePortDirection.OUTPUT, type, required, true);
    }

    public String getId() {
        return id;
    }

    public DronePortDirection getDirection() {
        return direction;
    }

    public DronePortType getType() {
        return type;
    }

    public boolean isRequired() {
        return required;
    }

    public boolean allowsMultipleConnections() {
        return multipleConnections;
    }
}
