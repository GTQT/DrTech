package com.drppp.drtech.common.drone.program;

import com.drppp.drtech.common.drone.program.compile.DroneCompileResult;
import com.drppp.drtech.common.drone.program.compile.DroneDiagnosticCode;
import com.drppp.drtech.common.drone.program.compile.DroneProgramCompiler;
import com.drppp.drtech.common.drone.program.edit.DroneGroupLayout;
import com.drppp.drtech.common.drone.program.edit.DroneGraphAutoLayout;
import com.drppp.drtech.common.drone.program.model.DroneProgramEdge;
import com.drppp.drtech.common.drone.program.model.DroneProgramGraph;
import com.drppp.drtech.common.drone.program.model.DroneProgramNode;
import com.drppp.drtech.common.drone.program.registry.DrTechDroneNodes;
import net.minecraft.nbt.NBTTagCompound;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DroneGroupLayoutTest {

    @Test
    void derivesMembershipFromThePersistentFrameGeometry() {
        DroneProgramGraph graph = new DroneProgramGraph("groups");
        DroneProgramNode group = group(100, 100, 320, 200, false);
        DroneProgramNode inside = DroneProgramNode.create(DrTechDroneNodes.WAIT, 130, 140);
        DroneProgramNode outside = DroneProgramNode.create(DrTechDroneNodes.WAIT, 500, 140);
        graph.addNode(group);
        graph.addNode(inside);
        graph.addNode(outside);

        assertEquals(1, DroneGroupLayout.members(graph, group).size());
        assertTrue(DroneGroupLayout.members(graph, group).contains(inside.getId()));
        assertFalse(DroneGroupLayout.members(graph, group).contains(outside.getId()));
    }

    @Test
    void selectedAndCollapsedGroupsIncludeTheirContents() {
        DroneProgramGraph graph = new DroneProgramGraph("collapsed");
        DroneProgramNode group = group(0, 0, 320, 200, true);
        DroneProgramNode inside = DroneProgramNode.create(DrTechDroneNodes.COMMENT, 20, 30);
        graph.addNode(group);
        graph.addNode(inside);
        Set<UUID> selection = new LinkedHashSet<>();
        selection.add(group.getId());

        DroneGroupLayout.expandSelectedGroups(graph, selection);

        assertTrue(selection.contains(inside.getId()));
        assertTrue(DroneGroupLayout.hiddenByCollapsedGroups(graph).contains(inside.getId()));
    }

    @Test
    void groupIsEditorOnlyAndDoesNotCreateAnUnreachableWarning() {
        DroneProgramGraph graph = new DroneProgramGraph("compile");
        DroneProgramNode start = DroneProgramNode.create(DrTechDroneNodes.START, 0, 0);
        DroneProgramNode end = DroneProgramNode.create(DrTechDroneNodes.END, 150, 0);
        DroneProgramNode group = group(-20, -20, 320, 200, false);
        graph.addNode(start);
        graph.addNode(end);
        graph.addNode(group);
        graph.addEdge(DroneProgramEdge.create(start.getId(), "next", end.getId(), "in"));

        DroneCompileResult result = new DroneProgramCompiler(DrTechDroneNodes.createDefaultRegistry()).compile(graph);

        assertTrue(result.getProgram().isPresent());
        assertTrue(result.getDiagnostics().stream().noneMatch(diagnostic ->
                diagnostic.getCode() == DroneDiagnosticCode.UNREACHABLE_NODE
                        && group.getId().equals(diagnostic.getNodeId())));
    }

    @Test
    void automaticLayoutLeavesGroupFramesAndTheirContentsUntouched() {
        DroneProgramGraph graph = new DroneProgramGraph("layout");
        DroneProgramNode group = group(0, 0, 320, 200, false);
        DroneProgramNode inside = DroneProgramNode.create(DrTechDroneNodes.WAIT, 20, 30);
        DroneProgramNode outside = DroneProgramNode.create(DrTechDroneNodes.WAIT, 500, 300);
        graph.addNode(group);
        graph.addNode(inside);
        graph.addNode(outside);

        Map<UUID, DroneGraphAutoLayout.Position> positions = DroneGraphAutoLayout.layout(graph,
                Collections.emptyList(), DrTechDroneNodes.createDefaultRegistry());

        assertFalse(positions.containsKey(group.getId()));
        assertFalse(positions.containsKey(inside.getId()));
        assertTrue(positions.containsKey(outside.getId()));
    }

    @Test
    void surroundingFrameAddsHeaderAndPaddingAroundASelection() {
        DroneProgramGraph graph = new DroneProgramGraph("surround");
        DroneProgramNode first = DroneProgramNode.create(DrTechDroneNodes.WAIT, 100, 120);
        DroneProgramNode second = DroneProgramNode.create(DrTechDroneNodes.END, 360, 260);
        graph.addNode(first);
        graph.addNode(second);
        Set<UUID> selection = new LinkedHashSet<>();
        selection.add(first.getId());
        selection.add(second.getId());

        DroneGroupLayout.Frame frame = DroneGroupLayout.surroundingFrame(graph, selection);

        assertEquals(80, frame.getX());
        assertEquals(88, frame.getY());
        assertTrue(frame.getX() + frame.getWidth() > second.getX() + 96);
        assertTrue(frame.getY() + frame.getHeight() > second.getY() + 32);
    }

    private static DroneProgramNode group(int x, int y, int width, int height, boolean collapsed) {
        NBTTagCompound config = new NBTTagCompound();
        config.setString("Title", "Test");
        config.setInteger("Width", width);
        config.setInteger("Height", height);
        config.setString("Color", "BLUE");
        config.setBoolean("Collapsed", collapsed);
        return new DroneProgramNode(UUID.randomUUID(), DrTechDroneNodes.GROUP, x, y, config);
    }
}
