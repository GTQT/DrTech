package com.drppp.drtech.common.drone.program.edit;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;

import java.util.UUID;

public final class DroneGraphCommandCodec {

    public static final int SCHEMA_VERSION = 1;

    private DroneGraphCommandCodec() {}

    public static NBTTagCompound write(DroneGraphEditCommand command) {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("Schema", SCHEMA_VERSION);
        tag.setString("Type", command.getType().name());
        tag.setLong("Revision", command.getExpectedRevision());
        setUuid(tag, "ObjectId", command.getObjectId());
        setUuid(tag, "SourceNode", command.getSourceNodeId());
        setUuid(tag, "TargetNode", command.getTargetNodeId());
        if (command.getNodeType() != null) tag.setString("NodeType", command.getNodeType().toString());
        if (command.getSourcePort() != null) tag.setString("SourcePort", command.getSourcePort());
        if (command.getTargetPort() != null) tag.setString("TargetPort", command.getTargetPort());
        if (command.getText() != null) tag.setString("Text", command.getText());
        tag.setInteger("X", command.getX());
        tag.setInteger("Y", command.getY());
        if (command.getConfiguration() != null) tag.setTag("Config", command.getConfiguration());
        if (command.getType() == DroneGraphCommandType.BATCH) {
            NBTTagList commands = new NBTTagList();
            for (DroneGraphEditCommand child : command.getCommands()) commands.appendTag(write(child));
            tag.setTag("Commands", commands);
        }
        return tag;
    }

    public static DroneGraphEditCommand read(NBTTagCompound tag) {
        if (tag == null || tag.getInteger("Schema") != SCHEMA_VERSION) {
            throw new IllegalArgumentException("Unsupported graph command schema");
        }
        return read(tag, true);
    }

    private static DroneGraphEditCommand read(NBTTagCompound tag, boolean allowBatch) {
        if (tag == null || tag.getInteger("Schema") != SCHEMA_VERSION) {
            throw new IllegalArgumentException("Unsupported graph command schema");
        }
        DroneGraphCommandType type;
        try {
            type = DroneGraphCommandType.valueOf(readText(tag, "Type", 32));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Unknown graph command type", exception);
        }
        long revision = tag.getLong("Revision");
        return switch (type) {
            case ADD_NODE -> DroneGraphEditCommand.addNode(revision, readUuid(tag, "ObjectId"),
                    new ResourceLocation(readText(tag, "NodeType", 128)), tag.getInteger("X"), tag.getInteger("Y"),
                    tag.hasKey("Config", 10) ? tag.getCompoundTag("Config") : new NBTTagCompound());
            case REMOVE_NODE -> DroneGraphEditCommand.removeNode(revision, readUuid(tag, "ObjectId"));
            case MOVE_NODE -> DroneGraphEditCommand.moveNode(revision, readUuid(tag, "ObjectId"),
                    tag.getInteger("X"), tag.getInteger("Y"));
            case CONFIGURE_NODE -> DroneGraphEditCommand.configureNode(revision, readUuid(tag, "ObjectId"),
                    tag.hasKey("Config", 10) ? tag.getCompoundTag("Config") : new NBTTagCompound());
            case ADD_EDGE -> DroneGraphEditCommand.addEdge(revision, readUuid(tag, "ObjectId"),
                    readUuid(tag, "SourceNode"), readText(tag, "SourcePort", 64),
                    readUuid(tag, "TargetNode"), readText(tag, "TargetPort", 64));
            case REMOVE_EDGE -> DroneGraphEditCommand.removeEdge(revision, readUuid(tag, "ObjectId"));
            case RENAME_PROGRAM -> DroneGraphEditCommand.rename(revision, bounded(tag.getString("Text"), 64, "Text"));
            case BATCH -> readBatch(tag, revision, allowBatch);
            case UNDO -> DroneGraphEditCommand.undo(revision);
            case REDO -> DroneGraphEditCommand.redo(revision);
        };
    }

    private static DroneGraphEditCommand readBatch(NBTTagCompound tag, long revision, boolean allowBatch) {
        if (!allowBatch) throw new IllegalArgumentException("Nested batch commands are not supported");
        NBTTagList list = tag.getTagList("Commands", 10);
        int size = list.tagCount();
        if (size == 0 || size > DroneGraphEditCommand.MAX_BATCH_COMMANDS) {
            throw new IllegalArgumentException("Invalid batch command size");
        }
        java.util.List<DroneGraphEditCommand> commands = new java.util.ArrayList<>(size);
        for (int i = 0; i < size; i++) commands.add(read(list.getCompoundTagAt(i), false));
        return DroneGraphEditCommand.batch(revision, commands);
    }

    private static void setUuid(NBTTagCompound tag, String key, UUID value) {
        if (value != null) tag.setString(key, value.toString());
    }

    private static UUID readUuid(NBTTagCompound tag, String key) {
        try {
            return UUID.fromString(readText(tag, key, 36));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Invalid UUID in " + key, exception);
        }
    }

    private static String readText(NBTTagCompound tag, String key, int maxLength) {
        String value = tag.getString(key);
        if (value.isEmpty()) throw new IllegalArgumentException("Missing " + key);
        return bounded(value, maxLength, key);
    }

    private static String bounded(String value, int maxLength, String key) {
        if (value.length() > maxLength) throw new IllegalArgumentException(key + " is too long");
        return value;
    }
}
