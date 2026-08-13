package com.drppp.drtech.common.drone.program;

import com.drppp.drtech.common.drone.program.edit.DroneEdgePresentation;
import com.drppp.drtech.common.drone.program.model.DroneProgramEdge;
import com.drppp.drtech.common.drone.program.model.DroneProgramGraph;
import com.drppp.drtech.common.drone.program.model.DroneProgramNode;
import com.drppp.drtech.common.drone.program.registry.DrTechDroneNodes;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DroneEdgePresentationTest {

    @Test
    void paintsDataBeforeFlowWhilePreservingOrderInsideEachLayer() {
        DroneProgramGraph graph = new DroneProgramGraph("layers");
        DroneProgramNode start = DroneProgramNode.create(DrTechDroneNodes.START, 0, 0);
        DroneProgramNode branch = DroneProgramNode.create(DrTechDroneNodes.BRANCH, 100, 0);
        DroneProgramNode value = DroneProgramNode.create(DrTechDroneNodes.BOOLEAN, 0, 100);
        graph.addNode(start);
        graph.addNode(branch);
        graph.addNode(value);
        DroneProgramEdge flow = DroneProgramEdge.create(start.getId(), "next", branch.getId(), "in");
        DroneProgramEdge data = DroneProgramEdge.create(value.getId(), "value", branch.getId(), "condition");
        graph.addEdge(flow);
        graph.addEdge(data);

        List<DroneProgramEdge> ordered = DroneEdgePresentation.dataThenFlow(graph,
                DrTechDroneNodes.createDefaultRegistry());

        assertEquals(data.getId(), ordered.get(0).getId());
        assertEquals(flow.getId(), ordered.get(1).getId());
    }

    @Test
    void selectionFocusKeepsOnlyAdjacentEdgesProminent() {
        DroneProgramGraph graph = new DroneProgramGraph("focus");
        DroneProgramNode start = DroneProgramNode.create(DrTechDroneNodes.START, 0, 0);
        DroneProgramNode wait = DroneProgramNode.create(DrTechDroneNodes.WAIT, 100, 0);
        DroneProgramNode end = DroneProgramNode.create(DrTechDroneNodes.END, 200, 0);
        graph.addNode(start);
        graph.addNode(wait);
        graph.addNode(end);
        DroneProgramEdge first = DroneProgramEdge.create(start.getId(), "next", wait.getId(), "in");
        DroneProgramEdge second = DroneProgramEdge.create(wait.getId(), "next", end.getId(), "in");

        assertTrue(DroneEdgePresentation.touchesSelection(first, Collections.singleton(start.getId())));
        assertFalse(DroneEdgePresentation.touchesSelection(second, Collections.singleton(start.getId())));
        assertTrue(DroneEdgePresentation.touchesSelection(second, Collections.emptySet()));
    }

    @Test
    void portFocusAndLaneOffsetRemainStableAfterNodesMove() {
        DroneProgramNode source = DroneProgramNode.create(DrTechDroneNodes.START, 0, 0);
        DroneProgramNode target = DroneProgramNode.create(DrTechDroneNodes.WAIT, 100, 0);
        DroneProgramEdge edge = DroneProgramEdge.create(source.getId(), "next", target.getId(), "in");

        assertTrue(DroneEdgePresentation.touchesPort(edge, source.getId(), "next"));
        assertTrue(DroneEdgePresentation.touchesPort(edge, target.getId(), "in"));
        assertFalse(DroneEdgePresentation.touchesPort(edge, target.getId(), "duration"));
        assertEquals(DroneEdgePresentation.laneOffset(edge.getId()),
                DroneEdgePresentation.laneOffset(edge.getId()));
        assertTrue(Math.abs(DroneEdgePresentation.laneOffset(edge.getId())) <= 12);
    }
}
