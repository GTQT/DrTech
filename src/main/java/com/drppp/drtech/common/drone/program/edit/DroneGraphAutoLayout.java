package com.drppp.drtech.common.drone.program.edit;

import com.drppp.drtech.common.drone.program.model.DroneNodeDefinition;
import com.drppp.drtech.common.drone.program.model.DronePortDefinition;
import com.drppp.drtech.common.drone.program.model.DronePortType;
import com.drppp.drtech.common.drone.program.model.DroneProgramEdge;
import com.drppp.drtech.common.drone.program.model.DroneProgramGraph;
import com.drppp.drtech.common.drone.program.model.DroneProgramNode;
import com.drppp.drtech.common.drone.program.registry.DroneNodeRegistry;
import com.drppp.drtech.common.drone.program.registry.DrTechDroneNodes;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Deterministic layered layout for editor nodes. Runtime and program semantics are never changed. */
public final class DroneGraphAutoLayout {

    public static final int COLUMN_SPACING = 140;
    public static final int ROW_SPACING = 70;

    private DroneGraphAutoLayout() {}

    public static Map<UUID, Position> layout(DroneProgramGraph graph, Collection<UUID> selection,
            DroneNodeRegistry registry) {
        Map<UUID, DroneProgramNode> nodes = selectedNodes(graph, selection);
        if (nodes.isEmpty()) return java.util.Collections.emptyMap();

        int originX = nodes.values().stream().mapToInt(DroneProgramNode::getX).min().orElse(0);
        int originY = nodes.values().stream().mapToInt(DroneProgramNode::getY).min().orElse(0);
        Map<UUID, List<UUID>> flow = new LinkedHashMap<>();
        Map<UUID, Integer> incoming = new HashMap<>();
        for (UUID nodeId : nodes.keySet()) {
            flow.put(nodeId, new ArrayList<>());
            incoming.put(nodeId, 0);
        }
        for (DroneProgramEdge edge : graph.getEdges()) {
            if (!nodes.containsKey(edge.getSourceNodeId()) || !nodes.containsKey(edge.getTargetNodeId())) continue;
            DroneProgramNode source = nodes.get(edge.getSourceNodeId());
            DroneNodeDefinition definition = registry.get(source.getType());
            DronePortDefinition port = definition == null ? null : definition.getPort(edge.getSourcePortId());
            if (port == null || port.getType() != DronePortType.FLOW) continue;
            flow.get(edge.getSourceNodeId()).add(edge.getTargetNodeId());
            incoming.put(edge.getTargetNodeId(), incoming.get(edge.getTargetNodeId()) + 1);
        }

        Comparator<UUID> visualOrder = Comparator
                .comparingInt((UUID id) -> nodes.get(id).getY())
                .thenComparingInt(id -> nodes.get(id).getX())
                .thenComparing(UUID::toString);
        for (List<UUID> targets : flow.values()) targets.sort(visualOrder);

        Map<UUID, Integer> ranks = new HashMap<>();
        List<UUID> roots = new ArrayList<>();
        for (UUID nodeId : nodes.keySet()) if (incoming.get(nodeId) == 0) roots.add(nodeId);
        roots.sort(visualOrder);
        for (UUID root : roots) assignRanks(root, flow, ranks);
        // Closed loop components have no zero-indegree root. Seed them in their existing visual order.
        List<UUID> remaining = new ArrayList<>(nodes.keySet());
        remaining.sort(visualOrder);
        for (UUID nodeId : remaining) if (!ranks.containsKey(nodeId)) assignRanks(nodeId, flow, ranks);

        Map<Integer, List<UUID>> layers = new java.util.TreeMap<>();
        for (UUID nodeId : nodes.keySet()) layers.computeIfAbsent(ranks.getOrDefault(nodeId, 0), key -> new ArrayList<>())
                .add(nodeId);
        Map<UUID, Position> result = new LinkedHashMap<>();
        for (Map.Entry<Integer, List<UUID>> layer : layers.entrySet()) {
            layer.getValue().sort(visualOrder);
            for (int row = 0; row < layer.getValue().size(); row++) {
                result.put(layer.getValue().get(row), new Position(originX + layer.getKey() * COLUMN_SPACING,
                        originY + row * ROW_SPACING));
            }
        }
        return result;
    }

    private static void assignRanks(UUID root, Map<UUID, List<UUID>> flow, Map<UUID, Integer> ranks) {
        Deque<UUID> queue = new ArrayDeque<>();
        Set<UUID> expanded = new HashSet<>();
        ranks.putIfAbsent(root, 0);
        queue.add(root);
        while (!queue.isEmpty()) {
            UUID source = queue.removeFirst();
            if (!expanded.add(source)) continue;
            int nextRank = ranks.get(source) + 1;
            for (UUID target : flow.get(source)) {
                Integer oldRank = ranks.get(target);
                if (oldRank == null || nextRank < oldRank) ranks.put(target, nextRank);
                if (!expanded.contains(target)) queue.addLast(target);
            }
        }
    }

    private static Map<UUID, DroneProgramNode> selectedNodes(DroneProgramGraph graph, Collection<UUID> selection) {
        Set<UUID> selected = selection == null ? java.util.Collections.emptySet() : new HashSet<>(selection);
        Set<UUID> grouped = DroneGroupLayout.membersOfAllGroups(graph);
        boolean all = selected.isEmpty();
        Map<UUID, DroneProgramNode> nodes = new LinkedHashMap<>();
        for (DroneProgramNode node : graph.getNodes()) {
            if (!DrTechDroneNodes.isEditorOnly(node.getType()) && !grouped.contains(node.getId())
                    && (all || selected.contains(node.getId()))) {
                nodes.put(node.getId(), node);
            }
        }
        return nodes;
    }

    public static final class Position {
        private final int x;
        private final int y;

        private Position(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public int getX() { return x; }

        public int getY() { return y; }
    }
}
