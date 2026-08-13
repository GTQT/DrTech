package com.drppp.drtech.common.drone.program.edit;

import com.drppp.drtech.common.drone.program.model.DroneProgramEdge;
import com.drppp.drtech.common.drone.program.model.DroneProgramGraph;
import com.drppp.drtech.common.drone.program.model.DroneProgramNode;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import java.util.Objects;
import java.util.UUID;

/** One atomic editor mutation. Commands carry the client's expected source revision. */
public final class DroneGraphEditCommand {

    private final DroneGraphCommandType type;
    private final long expectedRevision;
    private final UUID objectId;
    private final UUID sourceNodeId;
    private final UUID targetNodeId;
    private final ResourceLocation nodeType;
    private final String sourcePort;
    private final String targetPort;
    private final String text;
    private final int x;
    private final int y;
    private final NBTTagCompound configuration;

    private DroneGraphEditCommand(DroneGraphCommandType type, long expectedRevision, UUID objectId,
            UUID sourceNodeId, UUID targetNodeId, ResourceLocation nodeType, String sourcePort, String targetPort,
            String text, int x, int y, NBTTagCompound configuration) {
        this.type = Objects.requireNonNull(type, "type");
        this.expectedRevision = expectedRevision;
        this.objectId = objectId;
        this.sourceNodeId = sourceNodeId;
        this.targetNodeId = targetNodeId;
        this.nodeType = nodeType;
        this.sourcePort = sourcePort;
        this.targetPort = targetPort;
        this.text = text;
        this.x = x;
        this.y = y;
        this.configuration = configuration == null ? null : configuration.copy();
    }

    public static DroneGraphEditCommand addNode(long revision, UUID nodeId, ResourceLocation nodeType, int x, int y,
            NBTTagCompound configuration) {
        return new DroneGraphEditCommand(DroneGraphCommandType.ADD_NODE, revision, nodeId, null, null, nodeType,
                null, null, null, x, y, configuration);
    }

    public static DroneGraphEditCommand removeNode(long revision, UUID nodeId) {
        return simple(DroneGraphCommandType.REMOVE_NODE, revision, nodeId);
    }

    public static DroneGraphEditCommand moveNode(long revision, UUID nodeId, int x, int y) {
        return new DroneGraphEditCommand(DroneGraphCommandType.MOVE_NODE, revision, nodeId, null, null, null,
                null, null, null, x, y, null);
    }

    public static DroneGraphEditCommand configureNode(long revision, UUID nodeId, NBTTagCompound configuration) {
        return new DroneGraphEditCommand(DroneGraphCommandType.CONFIGURE_NODE, revision, nodeId, null, null, null,
                null, null, null, 0, 0, configuration);
    }

    public static DroneGraphEditCommand addEdge(long revision, UUID edgeId, UUID sourceNodeId, String sourcePort,
            UUID targetNodeId, String targetPort) {
        return new DroneGraphEditCommand(DroneGraphCommandType.ADD_EDGE, revision, edgeId, sourceNodeId, targetNodeId,
                null, sourcePort, targetPort, null, 0, 0, null);
    }

    public static DroneGraphEditCommand removeEdge(long revision, UUID edgeId) {
        return simple(DroneGraphCommandType.REMOVE_EDGE, revision, edgeId);
    }

    public static DroneGraphEditCommand rename(long revision, String name) {
        return new DroneGraphEditCommand(DroneGraphCommandType.RENAME_PROGRAM, revision, null, null, null, null,
                null, null, name, 0, 0, null);
    }

    public static DroneGraphEditCommand undo(long revision) {
        return simple(DroneGraphCommandType.UNDO, revision, null);
    }

    public static DroneGraphEditCommand redo(long revision) {
        return simple(DroneGraphCommandType.REDO, revision, null);
    }

    private static DroneGraphEditCommand simple(DroneGraphCommandType type, long revision, UUID objectId) {
        return new DroneGraphEditCommand(type, revision, objectId, null, null, null, null, null, null, 0, 0, null);
    }

    public void apply(DroneProgramGraph graph) {
        switch (type) {
            case ADD_NODE -> graph.addNode(new DroneProgramNode(require(objectId, "node id"),
                    require(nodeType, "node type"), x, y, configuration));
            case REMOVE_NODE -> graph.removeNode(require(objectId, "node id"));
            case MOVE_NODE -> graph.moveNode(require(objectId, "node id"), x, y);
            case CONFIGURE_NODE -> graph.configureNode(require(objectId, "node id"),
                    configuration == null ? new NBTTagCompound() : configuration);
            case ADD_EDGE -> graph.addEdge(new DroneProgramEdge(require(objectId, "edge id"),
                    require(sourceNodeId, "source node"), requireText(sourcePort, "source port"),
                    require(targetNodeId, "target node"), requireText(targetPort, "target port")));
            case REMOVE_EDGE -> graph.removeEdge(require(objectId, "edge id"));
            case RENAME_PROGRAM -> graph.rename(text == null ? "" : text);
            case UNDO, REDO -> throw new IllegalStateException(type + " must be handled by the edit session");
        }
    }

    private static <T> T require(T value, String name) {
        if (value == null) throw new IllegalArgumentException("Missing " + name);
        return value;
    }

    private static String requireText(String value, String name) {
        if (value == null || value.isEmpty()) throw new IllegalArgumentException("Missing " + name);
        return value;
    }

    public DroneGraphCommandType getType() {
        return type;
    }

    public long getExpectedRevision() {
        return expectedRevision;
    }

    public UUID getObjectId() {
        return objectId;
    }

    public ResourceLocation getNodeType() {
        return nodeType;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public NBTTagCompound getConfiguration() {
        return configuration == null ? null : configuration.copy();
    }

    public UUID getSourceNodeId() {
        return sourceNodeId;
    }

    public UUID getTargetNodeId() {
        return targetNodeId;
    }

    public String getSourcePort() {
        return sourcePort;
    }

    public String getTargetPort() {
        return targetPort;
    }

    public String getText() {
        return text;
    }
}
