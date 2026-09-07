package com.drppp.drtech.common.drone.program.edit;

import com.drppp.drtech.common.drone.program.model.DroneNodeDefinition;
import com.drppp.drtech.common.drone.program.model.DronePortDefinition;
import com.drppp.drtech.common.drone.program.model.DronePortType;
import com.drppp.drtech.common.drone.program.model.DroneProgramEdge;
import com.drppp.drtech.common.drone.program.model.DroneProgramGraph;
import com.drppp.drtech.common.drone.program.model.DroneProgramNode;
import com.drppp.drtech.common.drone.program.registry.DroneNodeRegistry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Pure presentation rules shared by the graph canvas and its regression tests. */
public final class DroneEdgePresentation {

    private DroneEdgePresentation() {}

    /** Data dependencies are painted first so the more important flow route remains visible on top. */
    public static List<DroneProgramEdge> dataThenFlow(DroneProgramGraph graph, DroneNodeRegistry registry) {
        List<DroneProgramEdge> ordered = new ArrayList<>(graph.getEdges().size());
        appendLayer(ordered, graph.getEdges(), graph, registry, false);
        appendLayer(ordered, graph.getEdges(), graph, registry, true);
        return ordered;
    }

    private static void appendLayer(List<DroneProgramEdge> target, Collection<DroneProgramEdge> edges,
            DroneProgramGraph graph, DroneNodeRegistry registry, boolean flowLayer) {
        for (DroneProgramEdge edge : edges) {
            if (isFlow(graph, edge, registry) == flowLayer) target.add(edge);
        }
    }

    public static boolean isFlow(DroneProgramGraph graph, DroneProgramEdge edge, DroneNodeRegistry registry) {
        DroneProgramNode source = graph.getNode(edge.getSourceNodeId());
        DroneNodeDefinition definition = source == null ? null : registry.get(source.getType());
        DronePortDefinition port = definition == null ? null : definition.getPort(edge.getSourcePortId());
        return port != null && port.getType() == DronePortType.FLOW;
    }

    public static boolean touchesSelection(DroneProgramEdge edge, Set<UUID> selection) {
        return selection == null || selection.isEmpty()
                || selection.contains(edge.getSourceNodeId())
                || selection.contains(edge.getTargetNodeId());
    }

    /** A stable small lane offset prevents coincident orthogonal routes from becoming one indistinguishable line. */
    public static int laneOffset(UUID edgeId) {
        if (edgeId == null) return 0;
        return (Math.floorMod(edgeId.hashCode(), 7) - 3) * 4;
    }

    public static boolean touchesPort(DroneProgramEdge edge, UUID nodeId, String portId) {
        if (edge == null || nodeId == null || portId == null) return false;
        return edge.getSourceNodeId().equals(nodeId) && edge.getSourcePortId().equals(portId)
                || edge.getTargetNodeId().equals(nodeId) && edge.getTargetPortId().equals(portId);
    }
}
