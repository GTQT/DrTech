package com.drppp.drtech.common.drone.program.model;

import net.minecraft.nbt.NBTTagCompound;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/** Mutable source graph used by editor commands. Runtime code consumes a compiled snapshot instead. */
public final class DroneProgramGraph {

    private final UUID programId;
    private final Map<UUID, DroneProgramNode> nodes = new LinkedHashMap<>();
    private final Map<UUID, DroneProgramEdge> edges = new LinkedHashMap<>();
    private String name;
    private long revision;

    public DroneProgramGraph(String name) {
        this(UUID.randomUUID(), name, 0L, Collections.emptyList(), Collections.emptyList());
    }

    public DroneProgramGraph(UUID programId, String name, long revision, Collection<DroneProgramNode> nodes,
            Collection<DroneProgramEdge> edges) {
        this.programId = Objects.requireNonNull(programId, "programId");
        this.name = name == null ? "" : name;
        this.revision = Math.max(0L, revision);
        for (DroneProgramNode node : nodes) {
            if (this.nodes.put(node.getId(), node) != null) {
                throw new IllegalArgumentException("Duplicate node id " + node.getId());
            }
        }
        for (DroneProgramEdge edge : edges) {
            if (this.edges.put(edge.getId(), edge) != null) {
                throw new IllegalArgumentException("Duplicate edge id " + edge.getId());
            }
        }
    }

    public UUID getProgramId() {
        return programId;
    }

    public String getName() {
        return name;
    }

    public long getRevision() {
        return revision;
    }

    public Collection<DroneProgramNode> getNodes() {
        return Collections.unmodifiableCollection(nodes.values());
    }

    public Collection<DroneProgramEdge> getEdges() {
        return Collections.unmodifiableCollection(edges.values());
    }

    public DroneProgramNode getNode(UUID id) {
        return nodes.get(id);
    }

    public DroneProgramEdge getEdge(UUID id) {
        return edges.get(id);
    }

    public DroneProgramGraph copy() {
        return new DroneProgramGraph(programId, name, revision, nodes.values(), edges.values());
    }

    public DroneProgramGraph copyWithRevision(long revision) {
        return new DroneProgramGraph(programId, name, revision, nodes.values(), edges.values());
    }

    /**
     * Creates an independent program suitable for "Save As".  Node and edge ids
     * remain stable inside the copied graph, while the program identity and
     * revision are reset so existing revision-locked references cannot silently
     * begin targeting the new card.
     */
    public DroneProgramGraph copyAsNewProgram(String newName) {
        return new DroneProgramGraph(UUID.randomUUID(), newName == null ? name : newName,
                0L, nodes.values(), edges.values());
    }

    public void rename(String name) {
        this.name = name == null ? "" : name;
        revision++;
    }

    public void addNode(DroneProgramNode node) {
        Objects.requireNonNull(node, "node");
        if (nodes.putIfAbsent(node.getId(), node) != null) {
            throw new IllegalArgumentException("Duplicate node id " + node.getId());
        }
        revision++;
    }

    public void removeNode(UUID nodeId) {
        if (nodes.remove(nodeId) == null) {
            return;
        }
        edges.values().removeIf(edge -> edge.getSourceNodeId().equals(nodeId) || edge.getTargetNodeId().equals(nodeId));
        revision++;
    }

    public void moveNode(UUID nodeId, int x, int y) {
        DroneProgramNode node = requireNode(nodeId);
        nodes.put(nodeId, node.movedTo(x, y));
        revision++;
    }

    public void configureNode(UUID nodeId, NBTTagCompound configuration) {
        DroneProgramNode node = requireNode(nodeId);
        nodes.put(nodeId, node.withConfiguration(configuration));
        revision++;
    }

    public void addEdge(DroneProgramEdge edge) {
        Objects.requireNonNull(edge, "edge");
        requireNode(edge.getSourceNodeId());
        requireNode(edge.getTargetNodeId());
        if (edges.putIfAbsent(edge.getId(), edge) != null) {
            throw new IllegalArgumentException("Duplicate edge id " + edge.getId());
        }
        revision++;
    }

    public void removeEdge(UUID edgeId) {
        if (edges.remove(edgeId) != null) {
            revision++;
        }
    }

    private DroneProgramNode requireNode(UUID nodeId) {
        DroneProgramNode node = nodes.get(nodeId);
        if (node == null) {
            throw new IllegalArgumentException("Unknown node id " + nodeId);
        }
        return node;
    }
}
