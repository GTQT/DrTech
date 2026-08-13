package com.drppp.drtech.common.drone.program.compile;

import com.drppp.drtech.common.drone.program.model.DroneNodeDefinition;
import com.drppp.drtech.common.drone.program.model.DroneNodePropertyDefinition;
import com.drppp.drtech.common.drone.program.codec.DroneNbtLimits;
import com.drppp.drtech.common.drone.program.model.DroneArea;
import com.drppp.drtech.common.drone.program.model.DronePortDefinition;
import com.drppp.drtech.common.drone.program.model.DronePortDirection;
import com.drppp.drtech.common.drone.program.model.DronePortType;
import com.drppp.drtech.common.drone.program.model.DroneProgramEdge;
import com.drppp.drtech.common.drone.program.model.DroneProgramGraph;
import com.drppp.drtech.common.drone.program.model.DroneProgramNode;
import com.drppp.drtech.common.drone.program.registry.DroneNodeRegistry;
import com.drppp.drtech.common.drone.program.registry.DrTechDroneNodes;
import com.drppp.drtech.common.drone.program.runtime.DroneRuntimeMemory;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class DroneProgramCompiler {

    public static final int MAX_NODES = 256;
    public static final int MAX_EDGES = 512;

    private final DroneNodeRegistry registry;

    public DroneProgramCompiler(DroneNodeRegistry registry) {
        this.registry = registry;
    }

    public DroneCompileResult compile(DroneProgramGraph graph) {
        List<DroneProgramDiagnostic> diagnostics = new ArrayList<>();
        if (graph.getNodes().size() > MAX_NODES) {
            error(diagnostics, DroneDiagnosticCode.TOO_MANY_NODES, null, null,
                    Integer.toString(graph.getNodes().size()), Integer.toString(MAX_NODES));
        }
        if (graph.getEdges().size() > MAX_EDGES) {
            error(diagnostics, DroneDiagnosticCode.TOO_MANY_EDGES, null, null,
                    Integer.toString(graph.getEdges().size()), Integer.toString(MAX_EDGES));
        }

        Map<UUID, DroneNodeDefinition> definitions = resolveDefinitions(graph, diagnostics);
        validateNodeConfigurations(graph, definitions, diagnostics);
        UUID entryNodeId = validateEntryNode(graph, definitions, diagnostics);
        Map<PortKey, Integer> connectionCounts = new HashMap<>();
        Set<ConnectionKey> connections = new HashSet<>();
        validateEdges(graph, definitions, connectionCounts, connections, diagnostics);
        validateRequiredPorts(graph, definitions, connectionCounts, diagnostics);
        validateTransferTargets(graph, connectionCounts, diagnostics);
        if (entryNodeId != null) {
            validateReachability(graph, definitions, entryNodeId, diagnostics);
            validateControlFlowCycles(graph, definitions, entryNodeId, diagnostics);
        }

        boolean hasErrors = diagnostics.stream()
                .anyMatch(diagnostic -> diagnostic.getSeverity() == DroneDiagnosticSeverity.ERROR);
        return new DroneCompileResult(hasErrors ? null : new CompiledDroneProgram(graph, entryNodeId), diagnostics);
    }

    private Map<UUID, DroneNodeDefinition> resolveDefinitions(DroneProgramGraph graph,
            List<DroneProgramDiagnostic> diagnostics) {
        Map<UUID, DroneNodeDefinition> definitions = new HashMap<>();
        for (DroneProgramNode node : graph.getNodes()) {
            DroneNodeDefinition definition = registry.get(node.getType());
            if (definition == null) {
                error(diagnostics, DroneDiagnosticCode.UNKNOWN_NODE_TYPE, node.getId(), null, node.getType().toString());
            } else {
                definitions.put(node.getId(), definition);
            }
        }
        return definitions;
    }

    private void validateNodeConfigurations(DroneProgramGraph graph, Map<UUID, DroneNodeDefinition> definitions,
            List<DroneProgramDiagnostic> diagnostics) {
        for (DroneProgramNode node : graph.getNodes()) {
            DroneNodeDefinition definition = definitions.get(node.getId());
            if (definition == null) continue;
            NBTTagCompound config = node.getConfiguration();
            String nbtViolation = DroneNbtLimits.violation(config);
            if (nbtViolation != null) {
                error(diagnostics, DroneDiagnosticCode.INVALID_NODE_CONFIGURATION, node.getId(), null, nbtViolation);
                continue;
            }
            for (DroneNodePropertyDefinition property : definition.getProperties()) {
                String reason = property.validate(config);
                if (reason != null) {
                    propertyError(diagnostics, DroneDiagnosticCode.INVALID_NODE_CONFIGURATION, node.getId(),
                            property.getId(), reason, property.getType().name());
                }
            }
            if (node.getType().equals(DrTechDroneNodes.AREA)) {
                DroneArea area = DroneArea.between(
                        new BlockPos(config.getInteger("X1"), config.getInteger("Y1"), config.getInteger("Z1")),
                        new BlockPos(config.getInteger("X2"), config.getInteger("Y2"), config.getInteger("Z2")));
                if (!area.isWithinRuntimeLimits()) {
                    propertyError(diagnostics, DroneDiagnosticCode.AREA_LIMIT_EXCEEDED, node.getId(),
                            areaFocusProperty(area), Integer.toString(area.getSizeX()),
                            Integer.toString(area.getSizeY()), Integer.toString(area.getSizeZ()),
                            Long.toString(area.getVolume()), Integer.toString(DroneArea.MAX_AXIS_LENGTH),
                            Integer.toString(DroneArea.MAX_BLOCKS));
                }
            }
            if (isVariableNode(node)) {
                String name = config.getString("Name");
                if (name.isEmpty()) name = "value";
                if (!DroneRuntimeMemory.isValidName(name)) {
                    propertyError(diagnostics, DroneDiagnosticCode.INVALID_NODE_CONFIGURATION, node.getId(),
                            "Name", "invalid_variable_name", name);
                }
            }
        }
    }

    private static boolean isVariableNode(DroneProgramNode node) {
        return node.getType().equals(DrTechDroneNodes.GET_NUMBER_VARIABLE)
                || node.getType().equals(DrTechDroneNodes.SET_NUMBER_VARIABLE)
                || node.getType().equals(DrTechDroneNodes.ADD_NUMBER_VARIABLE);
    }

    private static String areaFocusProperty(DroneArea area) {
        if (area.getSizeX() > DroneArea.MAX_AXIS_LENGTH) return "X2";
        if (area.getSizeY() > DroneArea.MAX_AXIS_LENGTH) return "Y2";
        if (area.getSizeZ() > DroneArea.MAX_AXIS_LENGTH) return "Z2";
        if (area.getSizeX() >= area.getSizeY() && area.getSizeX() >= area.getSizeZ()) return "X2";
        return area.getSizeY() >= area.getSizeZ() ? "Y2" : "Z2";
    }

    private UUID validateEntryNode(DroneProgramGraph graph, Map<UUID, DroneNodeDefinition> definitions,
            List<DroneProgramDiagnostic> diagnostics) {
        List<UUID> entries = new ArrayList<>();
        for (DroneProgramNode node : graph.getNodes()) {
            DroneNodeDefinition definition = definitions.get(node.getId());
            if (definition != null && definition.getFlowRole() == DroneNodeDefinition.FlowRole.ENTRY) {
                entries.add(node.getId());
            }
        }
        if (entries.isEmpty()) {
            error(diagnostics, DroneDiagnosticCode.NO_ENTRY_NODE, null, null);
            return null;
        }
        if (entries.size() > 1) {
            for (UUID entry : entries) {
                error(diagnostics, DroneDiagnosticCode.MULTIPLE_ENTRY_NODES, entry, null,
                        Integer.toString(entries.size()));
            }
            return null;
        }
        return entries.get(0);
    }

    private void validateEdges(DroneProgramGraph graph, Map<UUID, DroneNodeDefinition> definitions,
            Map<PortKey, Integer> connectionCounts, Set<ConnectionKey> connections,
            List<DroneProgramDiagnostic> diagnostics) {
        for (DroneProgramEdge edge : graph.getEdges()) {
            DroneProgramNode sourceNode = graph.getNode(edge.getSourceNodeId());
            DroneProgramNode targetNode = graph.getNode(edge.getTargetNodeId());
            if (sourceNode == null) {
                error(diagnostics, DroneDiagnosticCode.UNKNOWN_SOURCE_NODE, edge.getSourceNodeId(), edge.getSourcePortId());
                continue;
            }
            if (targetNode == null) {
                error(diagnostics, DroneDiagnosticCode.UNKNOWN_TARGET_NODE, edge.getTargetNodeId(), edge.getTargetPortId());
                continue;
            }
            DroneNodeDefinition sourceDefinition = definitions.get(sourceNode.getId());
            DroneNodeDefinition targetDefinition = definitions.get(targetNode.getId());
            if (sourceDefinition == null || targetDefinition == null) {
                continue;
            }
            DronePortDefinition sourcePort = sourceDefinition.getPort(edge.getSourcePortId());
            DronePortDefinition targetPort = targetDefinition.getPort(edge.getTargetPortId());
            if (sourcePort == null) {
                error(diagnostics, DroneDiagnosticCode.UNKNOWN_SOURCE_PORT, sourceNode.getId(), edge.getSourcePortId(),
                        sourceDefinition.getId().toString());
                continue;
            }
            if (targetPort == null) {
                error(diagnostics, DroneDiagnosticCode.UNKNOWN_TARGET_PORT, targetNode.getId(), edge.getTargetPortId(),
                        targetDefinition.getId().toString());
                continue;
            }
            if (sourcePort.getDirection() != DronePortDirection.OUTPUT
                    || targetPort.getDirection() != DronePortDirection.INPUT) {
                error(diagnostics, DroneDiagnosticCode.INVALID_EDGE_DIRECTION, sourceNode.getId(), sourcePort.getId());
                continue;
            }
            if (!targetPort.getType().accepts(sourcePort.getType())) {
                error(diagnostics, DroneDiagnosticCode.INCOMPATIBLE_PORT_TYPES, targetNode.getId(), targetPort.getId(),
                        sourcePort.getType().name(), targetPort.getType().name());
                continue;
            }

            ConnectionKey connection = new ConnectionKey(edge.getSourceNodeId(), edge.getSourcePortId(),
                    edge.getTargetNodeId(), edge.getTargetPortId());
            if (!connections.add(connection)) {
                error(diagnostics, DroneDiagnosticCode.DUPLICATE_CONNECTION, targetNode.getId(), targetPort.getId());
                continue;
            }
            incrementAndValidate(connectionCounts, new PortKey(sourceNode.getId(), sourcePort.getId()), sourcePort,
                    diagnostics, sourceNode.getId());
            incrementAndValidate(connectionCounts, new PortKey(targetNode.getId(), targetPort.getId()), targetPort,
                    diagnostics, targetNode.getId());
        }
    }

    private void incrementAndValidate(Map<PortKey, Integer> counts, PortKey key, DronePortDefinition port,
            List<DroneProgramDiagnostic> diagnostics, UUID nodeId) {
        int count = counts.merge(key, 1, Integer::sum);
        if (count > 1 && !port.allowsMultipleConnections()) {
            error(diagnostics, DroneDiagnosticCode.TOO_MANY_PORT_CONNECTIONS, nodeId, port.getId(),
                    Integer.toString(count));
        }
    }

    private void validateRequiredPorts(DroneProgramGraph graph, Map<UUID, DroneNodeDefinition> definitions,
            Map<PortKey, Integer> connectionCounts, List<DroneProgramDiagnostic> diagnostics) {
        for (DroneProgramNode node : graph.getNodes()) {
            DroneNodeDefinition definition = definitions.get(node.getId());
            if (definition == null) continue;
            for (DronePortDefinition port : definition.getPorts()) {
                if (port.isRequired() && connectionCounts.getOrDefault(new PortKey(node.getId(), port.getId()), 0) == 0) {
                    error(diagnostics, DroneDiagnosticCode.REQUIRED_PORT_NOT_CONNECTED, node.getId(), port.getId());
                }
            }
        }
    }

    private void validateTransferTargets(DroneProgramGraph graph, Map<PortKey, Integer> connectionCounts,
            List<DroneProgramDiagnostic> diagnostics) {
        for (DroneProgramNode node : graph.getNodes()) {
            if (!node.getType().equals(DrTechDroneNodes.IMPORT_ITEMS)
                    && !node.getType().equals(DrTechDroneNodes.EXPORT_ITEMS)) continue;
            int targets = connectionCounts.getOrDefault(new PortKey(node.getId(), "target"), 0)
                    + connectionCounts.getOrDefault(new PortKey(node.getId(), "area"), 0);
            if (targets != 1) {
                error(diagnostics, DroneDiagnosticCode.INVALID_NODE_CONFIGURATION, node.getId(), "target",
                        "exactly_one_transfer_target", Integer.toString(targets));
            }
        }
    }

    private void validateReachability(DroneProgramGraph graph, Map<UUID, DroneNodeDefinition> definitions,
            UUID entryNodeId, List<DroneProgramDiagnostic> diagnostics) {
        Set<UUID> reachable = new HashSet<>();
        Deque<UUID> queue = new ArrayDeque<>();
        reachable.add(entryNodeId);
        queue.add(entryNodeId);
        while (!queue.isEmpty()) {
            UUID current = queue.removeFirst();
            for (DroneProgramEdge edge : graph.getEdges()) {
                if (!edge.getSourceNodeId().equals(current)) continue;
                DroneNodeDefinition sourceDefinition = definitions.get(edge.getSourceNodeId());
                DroneNodeDefinition targetDefinition = definitions.get(edge.getTargetNodeId());
                if (sourceDefinition == null || targetDefinition == null) continue;
                DronePortDefinition sourcePort = sourceDefinition.getPort(edge.getSourcePortId());
                DronePortDefinition targetPort = targetDefinition.getPort(edge.getTargetPortId());
                if (sourcePort != null && targetPort != null && sourcePort.getType() == DronePortType.FLOW
                        && targetPort.getType() == DronePortType.FLOW && reachable.add(edge.getTargetNodeId())) {
                    queue.addLast(edge.getTargetNodeId());
                }
            }
        }

        // Data-producing nodes are reachable when they feed an already reachable executable/value dependency.
        boolean changed;
        do {
            changed = false;
            for (DroneProgramEdge edge : graph.getEdges()) {
                if (!reachable.contains(edge.getTargetNodeId())) continue;
                DroneNodeDefinition sourceDefinition = definitions.get(edge.getSourceNodeId());
                if (sourceDefinition == null) continue;
                DronePortDefinition sourcePort = sourceDefinition.getPort(edge.getSourcePortId());
                if (sourcePort != null && sourcePort.getType() != DronePortType.FLOW
                        && reachable.add(edge.getSourceNodeId())) {
                    changed = true;
                }
            }
        } while (changed);

        for (DroneProgramNode node : graph.getNodes()) {
            if (definitions.containsKey(node.getId()) && !DrTechDroneNodes.isEditorOnly(node.getType())
                    && !reachable.contains(node.getId())) {
                diagnostics.add(new DroneProgramDiagnostic(DroneDiagnosticSeverity.WARNING,
                        DroneDiagnosticCode.UNREACHABLE_NODE, node.getId(), null));
            }
        }
    }

    /**
     * Finds reachable strongly connected flow components. A closed component can never finish and is an error;
     * an open, non-bounded component can finish only through a runtime condition and is therefore a warning.
     */
    private void validateControlFlowCycles(DroneProgramGraph graph, Map<UUID, DroneNodeDefinition> definitions,
            UUID entryNodeId, List<DroneProgramDiagnostic> diagnostics) {
        Map<UUID, Set<UUID>> outgoing = new HashMap<>();
        Map<UUID, Set<UUID>> incoming = new HashMap<>();
        for (UUID nodeId : definitions.keySet()) {
            outgoing.put(nodeId, new HashSet<>());
            incoming.put(nodeId, new HashSet<>());
        }
        for (DroneProgramEdge edge : graph.getEdges()) {
            DroneNodeDefinition source = definitions.get(edge.getSourceNodeId());
            DroneNodeDefinition target = definitions.get(edge.getTargetNodeId());
            if (source == null || target == null) continue;
            DronePortDefinition sourcePort = source.getPort(edge.getSourcePortId());
            DronePortDefinition targetPort = target.getPort(edge.getTargetPortId());
            if (sourcePort == null || targetPort == null || sourcePort.getType() != DronePortType.FLOW
                    || targetPort.getType() != DronePortType.FLOW
                    || sourcePort.getDirection() != DronePortDirection.OUTPUT
                    || targetPort.getDirection() != DronePortDirection.INPUT) continue;
            outgoing.get(edge.getSourceNodeId()).add(edge.getTargetNodeId());
            incoming.get(edge.getTargetNodeId()).add(edge.getSourceNodeId());
        }

        Set<UUID> reachable = traverse(entryNodeId, outgoing);
        Set<UUID> remaining = new HashSet<>(reachable);
        while (!remaining.isEmpty()) {
            UUID seed = remaining.iterator().next();
            Set<UUID> forward = traverse(seed, outgoing);
            Set<UUID> backward = traverse(seed, incoming);
            forward.retainAll(backward);
            forward.retainAll(reachable);
            Set<UUID> component = forward;
            remaining.removeAll(component);
            boolean cyclic = component.size() > 1 || outgoing.getOrDefault(seed, java.util.Collections.emptySet())
                    .contains(seed);
            if (!cyclic) continue;

            boolean hasExit = false;
            for (UUID nodeId : component) {
                for (UUID target : outgoing.getOrDefault(nodeId, java.util.Collections.emptySet())) {
                    if (!component.contains(target)) {
                        hasExit = true;
                        break;
                    }
                }
                if (hasExit) break;
            }
            UUID focus = cycleFocusNode(graph, component);
            if (!hasExit) {
                diagnostics.add(new DroneProgramDiagnostic(DroneDiagnosticSeverity.ERROR,
                        DroneDiagnosticCode.NO_EXIT_LOOP, focus, null, Integer.toString(component.size())));
            } else if (!isBoundedLoopComponent(graph, component)) {
                diagnostics.add(new DroneProgramDiagnostic(DroneDiagnosticSeverity.WARNING,
                        DroneDiagnosticCode.POSSIBLE_INFINITE_LOOP, focus, null,
                        Integer.toString(component.size())));
            }
        }
    }

    private static Set<UUID> traverse(UUID start, Map<UUID, Set<UUID>> edges) {
        Set<UUID> visited = new HashSet<>();
        Deque<UUID> queue = new ArrayDeque<>();
        if (start != null && edges.containsKey(start)) {
            visited.add(start);
            queue.add(start);
        }
        while (!queue.isEmpty()) {
            for (UUID target : edges.getOrDefault(queue.removeFirst(), java.util.Collections.emptySet())) {
                if (visited.add(target)) queue.addLast(target);
            }
        }
        return visited;
    }

    private static UUID cycleFocusNode(DroneProgramGraph graph, Set<UUID> component) {
        for (UUID nodeId : component) {
            DroneProgramNode node = graph.getNode(nodeId);
            if (node != null && node.getType().equals(DrTechDroneNodes.WHILE)) return nodeId;
        }
        return component.iterator().next();
    }

    private static boolean isBoundedLoopComponent(DroneProgramGraph graph, Set<UUID> component) {
        for (UUID nodeId : component) {
            DroneProgramNode node = graph.getNode(nodeId);
            if (node != null && (node.getType().equals(DrTechDroneNodes.REPEAT)
                    || node.getType().equals(DrTechDroneNodes.FOR_EACH_COORDINATE))) return true;
        }
        return false;
    }

    private static void error(List<DroneProgramDiagnostic> diagnostics, DroneDiagnosticCode code, UUID nodeId,
            String portId, String... arguments) {
        diagnostics.add(new DroneProgramDiagnostic(DroneDiagnosticSeverity.ERROR, code, nodeId, portId, arguments));
    }

    private static void propertyError(List<DroneProgramDiagnostic> diagnostics, DroneDiagnosticCode code, UUID nodeId,
            String propertyId, String... arguments) {
        diagnostics.add(DroneProgramDiagnostic.withProperty(DroneDiagnosticSeverity.ERROR, code, nodeId, null,
                propertyId, arguments));
    }

    private static final class PortKey {
        private final UUID nodeId;
        private final String portId;

        private PortKey(UUID nodeId, String portId) {
            this.nodeId = nodeId;
            this.portId = portId;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) return true;
            if (!(other instanceof PortKey)) return false;
            PortKey that = (PortKey) other;
            return nodeId.equals(that.nodeId) && portId.equals(that.portId);
        }

        @Override
        public int hashCode() {
            return 31 * nodeId.hashCode() + portId.hashCode();
        }
    }

    private static final class ConnectionKey {
        private final UUID sourceNode;
        private final String sourcePort;
        private final UUID targetNode;
        private final String targetPort;

        private ConnectionKey(UUID sourceNode, String sourcePort, UUID targetNode, String targetPort) {
            this.sourceNode = sourceNode;
            this.sourcePort = sourcePort;
            this.targetNode = targetNode;
            this.targetPort = targetPort;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) return true;
            if (!(other instanceof ConnectionKey)) return false;
            ConnectionKey that = (ConnectionKey) other;
            return sourceNode.equals(that.sourceNode) && sourcePort.equals(that.sourcePort)
                    && targetNode.equals(that.targetNode) && targetPort.equals(that.targetPort);
        }

        @Override
        public int hashCode() {
            int result = sourceNode.hashCode();
            result = 31 * result + sourcePort.hashCode();
            result = 31 * result + targetNode.hashCode();
            return 31 * result + targetPort.hashCode();
        }
    }
}
