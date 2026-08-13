package com.drppp.drtech.common.drone.program.codec;

import com.drppp.drtech.common.drone.program.compile.DroneProgramCompiler;
import com.drppp.drtech.common.drone.program.model.DroneProgramEdge;
import com.drppp.drtech.common.drone.program.model.DroneProgramGraph;
import com.drppp.drtech.common.drone.program.model.DroneProgramNode;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class DroneProgramNbtCodec {

    public static final int SCHEMA_VERSION = DroneProgramMigrator.CURRENT_SCHEMA;
    public static final int MAX_NAME_LENGTH = 64;
    public static final int MAX_CONFIGURATION_KEYS = 64;
    private static final int TAG_LIST = 9;
    private static final int TAG_COMPOUND = 10;

    private DroneProgramNbtCodec() {}

    public static NBTTagCompound write(DroneProgramGraph graph) {
        NBTTagCompound root = new NBTTagCompound();
        root.setInteger("Schema", SCHEMA_VERSION);
        root.setString("ProgramId", graph.getProgramId().toString());
        root.setString("Name", trim(graph.getName(), MAX_NAME_LENGTH));
        root.setLong("Revision", graph.getRevision());

        NBTTagList nodes = new NBTTagList();
        for (DroneProgramNode node : graph.getNodes()) {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setString("Id", node.getId().toString());
            tag.setString("Type", node.getType().toString());
            tag.setInteger("X", node.getX());
            tag.setInteger("Y", node.getY());
            tag.setTag("Config", node.getConfiguration());
            nodes.appendTag(tag);
        }
        root.setTag("Nodes", nodes);

        NBTTagList edges = new NBTTagList();
        for (DroneProgramEdge edge : graph.getEdges()) {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setString("Id", edge.getId().toString());
            tag.setString("FromNode", edge.getSourceNodeId().toString());
            tag.setString("FromPort", edge.getSourcePortId());
            tag.setString("ToNode", edge.getTargetNodeId().toString());
            tag.setString("ToPort", edge.getTargetPortId());
            edges.appendTag(tag);
        }
        root.setTag("Edges", edges);
        return root;
    }

    public static DroneProgramGraph read(NBTTagCompound root) throws DroneProgramFormatException {
        root = DroneProgramMigrator.migrate(root);
        if (!root.hasKey("Nodes", TAG_LIST) || !root.hasKey("Edges", TAG_LIST)) {
            throw new DroneProgramFormatException("Program node or edge list is missing");
        }

        NBTTagList nodeTags = root.getTagList("Nodes", TAG_COMPOUND);
        NBTTagList edgeTags = root.getTagList("Edges", TAG_COMPOUND);
        if (nodeTags.tagCount() > DroneProgramCompiler.MAX_NODES) {
            throw new DroneProgramFormatException("Program exceeds the node limit");
        }
        if (edgeTags.tagCount() > DroneProgramCompiler.MAX_EDGES) {
            throw new DroneProgramFormatException("Program exceeds the edge limit");
        }

        List<DroneProgramNode> nodes = new ArrayList<>(nodeTags.tagCount());
        for (int index = 0; index < nodeTags.tagCount(); index++) {
            NBTTagCompound tag = nodeTags.getCompoundTagAt(index);
            UUID id = readUuid(tag, "Id");
            ResourceLocation type = readResourceLocation(tag, "Type");
            NBTTagCompound configuration = tag.hasKey("Config", TAG_COMPOUND)
                    ? tag.getCompoundTag("Config") : new NBTTagCompound();
            if (configuration.getKeySet().size() > MAX_CONFIGURATION_KEYS) {
                throw new DroneProgramFormatException("Node " + id + " configuration has too many keys");
            }
            String nbtViolation = DroneNbtLimits.violation(configuration);
            if (nbtViolation != null) {
                throw new DroneProgramFormatException("Node " + id + " configuration violates " + nbtViolation);
            }
            nodes.add(new DroneProgramNode(id, type, tag.getInteger("X"), tag.getInteger("Y"), configuration));
        }

        List<DroneProgramEdge> edges = new ArrayList<>(edgeTags.tagCount());
        for (int index = 0; index < edgeTags.tagCount(); index++) {
            NBTTagCompound tag = edgeTags.getCompoundTagAt(index);
            String sourcePort = readBoundedString(tag, "FromPort", 64);
            String targetPort = readBoundedString(tag, "ToPort", 64);
            edges.add(new DroneProgramEdge(readUuid(tag, "Id"), readUuid(tag, "FromNode"), sourcePort,
                    readUuid(tag, "ToNode"), targetPort));
        }

        try {
            return new DroneProgramGraph(readUuid(root, "ProgramId"), trim(root.getString("Name"), MAX_NAME_LENGTH),
                    root.getLong("Revision"), nodes, edges);
        } catch (IllegalArgumentException exception) {
            throw new DroneProgramFormatException("Program contains duplicate ids", exception);
        }
    }

    private static UUID readUuid(NBTTagCompound tag, String key) throws DroneProgramFormatException {
        String value = readBoundedString(tag, key, 36);
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException exception) {
            throw new DroneProgramFormatException("Invalid UUID in " + key, exception);
        }
    }

    private static ResourceLocation readResourceLocation(NBTTagCompound tag, String key)
            throws DroneProgramFormatException {
        String value = readBoundedString(tag, key, 128);
        try {
            return new ResourceLocation(value);
        } catch (RuntimeException exception) {
            throw new DroneProgramFormatException("Invalid resource location in " + key, exception);
        }
    }

    private static String readBoundedString(NBTTagCompound tag, String key, int maxLength)
            throws DroneProgramFormatException {
        String value = tag.getString(key);
        if (value.isEmpty() || value.length() > maxLength) {
            throw new DroneProgramFormatException("Invalid string length in " + key);
        }
        return value;
    }

    private static String trim(String value, int maxLength) {
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }
}
