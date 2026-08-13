package com.drppp.drtech.common.drone.program;

import com.drppp.drtech.common.drone.program.edit.DroneGraphAutoLayout;
import com.drppp.drtech.common.drone.program.model.DroneProgramEdge;
import com.drppp.drtech.common.drone.program.model.DroneProgramGraph;
import com.drppp.drtech.common.drone.program.model.DroneProgramNode;
import com.drppp.drtech.common.drone.program.registry.DrTechDroneNodes;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DroneGraphAutoLayoutTest {

    @Test
    void laysOutFlowFromLeftToRightWithoutMutatingProgram() {
        DroneProgramGraph graph = new DroneProgramGraph("layout");
        DroneProgramNode start = DroneProgramNode.create(DrTechDroneNodes.START, 500, 300);
        DroneProgramNode wait = DroneProgramNode.create(DrTechDroneNodes.WAIT, 100, 700);
        DroneProgramNode end = DroneProgramNode.create(DrTechDroneNodes.END, 20, 0);
        graph.addNode(start);
        graph.addNode(wait);
        graph.addNode(end);
        graph.addEdge(DroneProgramEdge.create(start.getId(), "next", wait.getId(), "in"));
        graph.addEdge(DroneProgramEdge.create(wait.getId(), "next", end.getId(), "in"));
        long revision = graph.getRevision();

        Map<UUID, DroneGraphAutoLayout.Position> layout = DroneGraphAutoLayout.layout(graph,
                Collections.emptyList(), DrTechDroneNodes.createDefaultRegistry());

        assertEquals(20, layout.get(start.getId()).getX());
        assertEquals(20 + DroneGraphAutoLayout.COLUMN_SPACING, layout.get(wait.getId()).getX());
        assertEquals(20 + DroneGraphAutoLayout.COLUMN_SPACING * 2, layout.get(end.getId()).getX());
        assertEquals(0, layout.get(start.getId()).getY());
        assertEquals(0, layout.get(wait.getId()).getY());
        assertEquals(0, layout.get(end.getId()).getY());
        assertEquals(revision, graph.getRevision());
        assertEquals(500, graph.getNode(start.getId()).getX());
    }

    @Test
    void selectedLayoutIgnoresNodesOutsideSelection() {
        DroneProgramGraph graph = new DroneProgramGraph("selection");
        DroneProgramNode start = DroneProgramNode.create(DrTechDroneNodes.START, 10, 10);
        DroneProgramNode wait = DroneProgramNode.create(DrTechDroneNodes.WAIT, 300, 200);
        DroneProgramNode end = DroneProgramNode.create(DrTechDroneNodes.END, 900, 400);
        graph.addNode(start);
        graph.addNode(wait);
        graph.addNode(end);
        graph.addEdge(DroneProgramEdge.create(start.getId(), "next", wait.getId(), "in"));
        graph.addEdge(DroneProgramEdge.create(wait.getId(), "next", end.getId(), "in"));

        Map<UUID, DroneGraphAutoLayout.Position> layout = DroneGraphAutoLayout.layout(graph,
                Arrays.asList(wait.getId(), end.getId()), DrTechDroneNodes.createDefaultRegistry());

        assertEquals(2, layout.size());
        assertTrue(!layout.containsKey(start.getId()));
        assertEquals(300, layout.get(wait.getId()).getX());
        assertEquals(300 + DroneGraphAutoLayout.COLUMN_SPACING, layout.get(end.getId()).getX());
    }
}
