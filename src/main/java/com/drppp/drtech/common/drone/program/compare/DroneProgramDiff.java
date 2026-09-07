package com.drppp.drtech.common.drone.program.compare;

import com.drppp.drtech.common.drone.program.model.DroneProgramEdge;
import com.drppp.drtech.common.drone.program.model.DroneProgramGraph;
import com.drppp.drtech.common.drone.program.model.DroneProgramNode;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Deterministic structural comparison for editor previews and conflict diagnostics. */
public final class DroneProgramDiff {
    private DroneProgramDiff() {}

    public static DroneProgramDiffResult compare(DroneProgramGraph left, DroneProgramGraph right) {
        if (left == null || right == null) throw new IllegalArgumentException("graphs cannot be null");
        int addedNodes = 0, removedNodes = 0, changedNodes = 0;
        Map<UUID, DroneProgramNode> leftNodes = new HashMap<>();
        for (DroneProgramNode node : left.getNodes()) leftNodes.put(node.getId(), node);
        for (DroneProgramNode node : right.getNodes()) {
            DroneProgramNode previous = leftNodes.remove(node.getId());
            if (previous == null) addedNodes++;
            else if (!sameNode(previous, node)) changedNodes++;
        }
        removedNodes = leftNodes.size();

        int addedEdges = 0, removedEdges = 0, changedEdges = 0;
        Map<UUID, DroneProgramEdge> leftEdges = new HashMap<>();
        for (DroneProgramEdge edge : left.getEdges()) leftEdges.put(edge.getId(), edge);
        for (DroneProgramEdge edge : right.getEdges()) {
            DroneProgramEdge previous = leftEdges.remove(edge.getId());
            if (previous == null) addedEdges++;
            else if (!sameEdge(previous, edge)) changedEdges++;
        }
        removedEdges = leftEdges.size();
        boolean metadataChanged = !left.getName().equals(right.getName());
        return new DroneProgramDiffResult(metadataChanged, addedNodes, removedNodes, changedNodes,
                addedEdges, removedEdges, changedEdges);
    }

    private static boolean sameNode(DroneProgramNode left, DroneProgramNode right) {
        return left.getType().equals(right.getType()) && left.getX() == right.getX() && left.getY() == right.getY()
                && left.getConfiguration().toString().equals(right.getConfiguration().toString());
    }

    private static boolean sameEdge(DroneProgramEdge left, DroneProgramEdge right) {
        return left.getSourceNodeId().equals(right.getSourceNodeId())
                && left.getSourcePortId().equals(right.getSourcePortId())
                && left.getTargetNodeId().equals(right.getTargetNodeId())
                && left.getTargetPortId().equals(right.getTargetPortId());
    }
}
