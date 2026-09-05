package com.drppp.drtech.common.drone.program.model;

import java.util.Objects;
import java.util.UUID;

public final class DroneProgramEdge {

    private final UUID id;
    private final UUID sourceNodeId;
    private final String sourcePortId;
    private final UUID targetNodeId;
    private final String targetPortId;

    public DroneProgramEdge(UUID id, UUID sourceNodeId, String sourcePortId, UUID targetNodeId, String targetPortId) {
        this.id = Objects.requireNonNull(id, "id");
        this.sourceNodeId = Objects.requireNonNull(sourceNodeId, "sourceNodeId");
        this.sourcePortId = requirePortId(sourcePortId, "sourcePortId");
        this.targetNodeId = Objects.requireNonNull(targetNodeId, "targetNodeId");
        this.targetPortId = requirePortId(targetPortId, "targetPortId");
    }

    public static DroneProgramEdge create(UUID sourceNodeId, String sourcePortId, UUID targetNodeId,
            String targetPortId) {
        return new DroneProgramEdge(UUID.randomUUID(), sourceNodeId, sourcePortId, targetNodeId, targetPortId);
    }

    private static String requirePortId(String value, String name) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException(name + " cannot be empty");
        }
        return value;
    }

    public UUID getId() {
        return id;
    }

    public UUID getSourceNodeId() {
        return sourceNodeId;
    }

    public String getSourcePortId() {
        return sourcePortId;
    }

    public UUID getTargetNodeId() {
        return targetNodeId;
    }

    public String getTargetPortId() {
        return targetPortId;
    }
}
