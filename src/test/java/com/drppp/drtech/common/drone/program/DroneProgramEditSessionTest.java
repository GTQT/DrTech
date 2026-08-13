package com.drppp.drtech.common.drone.program;

import com.drppp.drtech.common.drone.program.edit.DroneGraphCommandCodec;
import com.drppp.drtech.common.drone.program.edit.DroneGraphEditCommand;
import com.drppp.drtech.common.drone.program.edit.DroneGraphEditResult;
import com.drppp.drtech.common.drone.program.edit.DroneGraphEditStatus;
import com.drppp.drtech.common.drone.program.edit.DroneProgramEditSession;
import com.drppp.drtech.common.drone.program.model.DroneProgramEdge;
import com.drppp.drtech.common.drone.program.model.DroneProgramGraph;
import com.drppp.drtech.common.drone.program.model.DroneProgramNode;
import com.drppp.drtech.common.drone.program.registry.DrTechDroneNodes;
import net.minecraft.nbt.NBTTagCompound;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DroneProgramEditSessionTest {

    @Test
    void commandCodecRoundTripsAddEdgePayload() {
        UUID edge = UUID.randomUUID();
        UUID source = UUID.randomUUID();
        UUID target = UUID.randomUUID();
        DroneGraphEditCommand sourceCommand = DroneGraphEditCommand.addEdge(9L, edge, source, "next", target, "in");

        DroneGraphEditCommand decoded = DroneGraphCommandCodec.read(DroneGraphCommandCodec.write(sourceCommand));

        assertEquals(9L, decoded.getExpectedRevision());
        assertEquals(edge, decoded.getObjectId());
        assertEquals(source, decoded.getSourceNodeId());
        assertEquals(target, decoded.getTargetNodeId());
        assertEquals("next", decoded.getSourcePort());
        assertEquals("in", decoded.getTargetPort());
    }

    @Test
    void renamesProgramThroughRevisionCheckedUndoableCommand() {
        DroneProgramEditSession session = new DroneProgramEditSession(defaultGraph(),
                DrTechDroneNodes.createDefaultRegistry());
        long initialRevision = session.getGraph().getRevision();

        DroneGraphEditResult renamed = session.apply(DroneGraphEditCommand.rename(initialRevision, "温室收割"));
        assertTrue(renamed.isAccepted());
        assertEquals("温室收割", session.getGraph().getName());

        DroneGraphEditResult undone = session.apply(DroneGraphEditCommand.undo(renamed.getRevision()));
        assertTrue(undone.isAccepted());
        assertEquals("New Drone Program", session.getGraph().getName());
    }

    @Test
    void acceptsAtomicMoveAndRejectsStaleRevision() {
        DroneProgramGraph graph = defaultGraph();
        DroneProgramNode end = graph.getNodes().stream()
                .filter(node -> node.getType().equals(DrTechDroneNodes.END)).findFirst().orElseThrow(AssertionError::new);
        DroneProgramEditSession session = new DroneProgramEditSession(graph, DrTechDroneNodes.createDefaultRegistry());
        long initialRevision = session.getGraph().getRevision();

        DroneGraphEditResult accepted = session.apply(
                DroneGraphEditCommand.moveNode(initialRevision, end.getId(), 300, 120));
        DroneGraphEditResult conflict = session.apply(
                DroneGraphEditCommand.moveNode(initialRevision, end.getId(), 400, 120));

        assertTrue(accepted.isAccepted());
        assertEquals(initialRevision + 1L, accepted.getRevision());
        assertEquals(300, session.getGraph().getNode(end.getId()).getX());
        assertEquals(DroneGraphEditStatus.REVISION_CONFLICT, conflict.getStatus());
        assertEquals(300, session.getGraph().getNode(end.getId()).getX());
    }

    @Test
    void rejectsUnknownNodeTypeWithoutPartiallyMutatingGraph() {
        DroneProgramGraph graph = defaultGraph();
        DroneProgramEditSession session = new DroneProgramEditSession(graph, DrTechDroneNodes.createDefaultRegistry());
        int nodeCount = session.getGraph().getNodes().size();
        long revision = session.getGraph().getRevision();

        DroneGraphEditResult result = session.apply(DroneGraphEditCommand.addNode(revision, UUID.randomUUID(),
                new net.minecraft.util.ResourceLocation("example", "unregistered"), 0, 0, new NBTTagCompound()));

        assertFalse(result.isAccepted());
        assertEquals(DroneGraphEditStatus.INVALID_COMMAND, result.getStatus());
        assertEquals(revision, session.getGraph().getRevision());
        assertEquals(nodeCount, session.getGraph().getNodes().size());
    }

    @Test
    void acceptsTemporarilyInvalidGraphAndReturnsDiagnostics() {
        DroneProgramGraph graph = defaultGraph();
        DroneProgramEditSession session = new DroneProgramEditSession(graph, DrTechDroneNodes.createDefaultRegistry());
        long revision = session.getGraph().getRevision();

        DroneGraphEditResult result = session.apply(DroneGraphEditCommand.addNode(revision, UUID.randomUUID(),
                DrTechDroneNodes.BRANCH, 100, 100, new NBTTagCompound()));

        assertTrue(result.isAccepted());
        assertFalse(result.getDiagnostics().isEmpty());
        assertNotNull(session.getLastCompileResult());
        assertTrue(session.getLastCompileResult().hasErrors());
    }

    @Test
    void undoAndRedoRestoreContentWithMonotonicRevisions() {
        DroneProgramEditSession session = new DroneProgramEditSession(defaultGraph(),
                DrTechDroneNodes.createDefaultRegistry());
        DroneProgramNode end = session.getGraph().getNodes().stream()
                .filter(node -> node.getType().equals(DrTechDroneNodes.END)).findFirst().orElseThrow(AssertionError::new);
        long initial = session.getGraph().getRevision();
        session.apply(DroneGraphEditCommand.moveNode(initial, end.getId(), 500, 200));
        long movedRevision = session.getGraph().getRevision();

        DroneGraphEditResult undo = session.apply(DroneGraphEditCommand.undo(movedRevision));
        int undoX = session.getGraph().getNode(end.getId()).getX();
        DroneGraphEditResult redo = session.apply(DroneGraphEditCommand.redo(undo.getRevision()));

        assertEquals(initial + 2L, undo.getRevision());
        assertEquals(220, undoX);
        assertEquals(initial + 3L, redo.getRevision());
        assertEquals(500, session.getGraph().getNode(end.getId()).getX());
        assertTrue(session.canUndo());
        assertFalse(session.canRedo());
    }

    private static DroneProgramGraph defaultGraph() {
        DroneProgramGraph graph = new DroneProgramGraph("New Drone Program");
        DroneProgramNode start = DroneProgramNode.create(DrTechDroneNodes.START, 20, 60);
        DroneProgramNode end = DroneProgramNode.create(DrTechDroneNodes.END, 220, 60);
        graph.addNode(start);
        graph.addNode(end);
        graph.addEdge(DroneProgramEdge.create(start.getId(), "next", end.getId(), "in"));
        return graph;
    }
}
