package com.drppp.drtech.drone.program.compile;

import com.drppp.drtech.drone.program.model.DroneProgramEdge;
import com.drppp.drtech.drone.program.model.DroneProgramGraph;
import com.drppp.drtech.drone.program.model.DroneProgramNode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import com.drppp.drtech.drone.program.registry.DrTechDroneNodes;

/** Immutable lookup snapshot consumed by the future server-side runtime. */
public final class CompiledDroneProgram {

    private final UUID programId;
    private final String name;
    private final long sourceRevision;
    private final UUID entryNodeId;
    private final Map<UUID, DroneProgramNode> nodes;
    private final Map<PortAddress, List<DroneProgramEdge>> outgoing;
    private final Map<PortAddress, List<DroneProgramEdge>> incoming;

    CompiledDroneProgram(DroneProgramGraph graph, UUID entryNodeId) {
        this.programId = graph.getProgramId();
        this.name = graph.getName();
        this.sourceRevision = graph.getRevision();
        this.entryNodeId = entryNodeId;
        Map<UUID, DroneProgramNode> nodeMap = new HashMap<>();
        for (DroneProgramNode node : graph.getNodes()) {
            if (!DrTechDroneNodes.isEditorOnly(node.getType())) nodeMap.put(node.getId(), node);
        }
        this.nodes = Collections.unmodifiableMap(nodeMap);
        List<DroneProgramEdge> executableEdges = new ArrayList<>();
        for (DroneProgramEdge edge : graph.getEdges()) {
            if (nodeMap.containsKey(edge.getSourceNodeId()) && nodeMap.containsKey(edge.getTargetNodeId())) {
                executableEdges.add(edge);
            }
        }
        this.outgoing = indexEdges(executableEdges, true);
        this.incoming = indexEdges(executableEdges, false);
    }

    private static Map<PortAddress, List<DroneProgramEdge>> indexEdges(Collection<DroneProgramEdge> edges,
            boolean bySource) {
        Map<PortAddress, List<DroneProgramEdge>> mutable = new HashMap<>();
        for (DroneProgramEdge edge : edges) {
            PortAddress key = bySource
                    ? new PortAddress(edge.getSourceNodeId(), edge.getSourcePortId())
                    : new PortAddress(edge.getTargetNodeId(), edge.getTargetPortId());
            mutable.computeIfAbsent(key, ignored -> new ArrayList<>()).add(edge);
        }
        Map<PortAddress, List<DroneProgramEdge>> result = new HashMap<>();
        for (Map.Entry<PortAddress, List<DroneProgramEdge>> entry : mutable.entrySet()) {
            result.put(entry.getKey(), Collections.unmodifiableList(entry.getValue()));
        }
        return Collections.unmodifiableMap(result);
    }

    public UUID getProgramId() {
        return programId;
    }

    public String getName() {
        return name;
    }

    public long getSourceRevision() {
        return sourceRevision;
    }

    public UUID getEntryNodeId() {
        return entryNodeId;
    }

    public DroneProgramNode getNode(UUID nodeId) {
        return nodes.get(nodeId);
    }

    public List<DroneProgramEdge> getOutgoing(UUID nodeId, String portId) {
        return outgoing.getOrDefault(new PortAddress(nodeId, portId), Collections.emptyList());
    }

    public List<DroneProgramEdge> getIncoming(UUID nodeId, String portId) {
        return incoming.getOrDefault(new PortAddress(nodeId, portId), Collections.emptyList());
    }

    private static final class PortAddress {

        private final UUID nodeId;
        private final String portId;

        private PortAddress(UUID nodeId, String portId) {
            this.nodeId = nodeId;
            this.portId = portId;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) return true;
            if (!(other instanceof PortAddress)) return false;
            PortAddress that = (PortAddress) other;
            return nodeId.equals(that.nodeId) && portId.equals(that.portId);
        }

        @Override
        public int hashCode() {
            return 31 * nodeId.hashCode() + portId.hashCode();
        }
    }
}
