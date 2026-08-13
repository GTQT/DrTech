package com.drppp.drtech.common.drone.program;

import com.drppp.drtech.common.drone.program.compile.DroneCompileResult;
import com.drppp.drtech.common.drone.program.compile.DroneDiagnosticCode;
import com.drppp.drtech.common.drone.program.compile.DroneDiagnosticSeverity;
import com.drppp.drtech.common.drone.program.compile.DroneProgramDiagnostic;
import com.drppp.drtech.common.drone.program.compile.DroneProgramCompiler;
import com.drppp.drtech.common.drone.program.model.DroneProgramEdge;
import com.drppp.drtech.common.drone.program.model.DroneProgramGraph;
import com.drppp.drtech.common.drone.program.model.DroneProgramNode;
import com.drppp.drtech.common.drone.program.registry.DrTechDroneNodes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DroneProgramCompilerTest {

    private final DroneProgramCompiler compiler = new DroneProgramCompiler(DrTechDroneNodes.createDefaultRegistry());

    @Test
    void compilesLinearProgramIntoImmutableSnapshot() {
        DroneProgramGraph graph = new DroneProgramGraph("linear");
        DroneProgramNode start = node(DrTechDroneNodes.START);
        DroneProgramNode wait = node(DrTechDroneNodes.WAIT);
        DroneProgramNode end = node(DrTechDroneNodes.END);
        graph.addNode(start);
        graph.addNode(wait);
        graph.addNode(end);
        graph.addEdge(DroneProgramEdge.create(start.getId(), "next", wait.getId(), "in"));
        graph.addEdge(DroneProgramEdge.create(wait.getId(), "next", end.getId(), "in"));

        DroneCompileResult result = compiler.compile(graph);

        assertFalse(result.hasErrors());
        assertTrue(result.getProgram().isPresent());
        assertTrue(result.getProgram().get().getEntryNodeId().equals(start.getId()));
        assertTrue(result.getProgram().get().getOutgoing(start.getId(), "next").size() == 1);
    }

    @Test
    void rejectsMismatchedTypedConnection() {
        DroneProgramGraph graph = new DroneProgramGraph("bad type");
        DroneProgramNode start = node(DrTechDroneNodes.START);
        DroneProgramNode branch = node(DrTechDroneNodes.BRANCH);
        DroneProgramNode number = node(DrTechDroneNodes.NUMBER);
        DroneProgramNode trueEnd = node(DrTechDroneNodes.END);
        DroneProgramNode falseEnd = node(DrTechDroneNodes.END);
        graph.addNode(start);
        graph.addNode(branch);
        graph.addNode(number);
        graph.addNode(trueEnd);
        graph.addNode(falseEnd);
        graph.addEdge(DroneProgramEdge.create(start.getId(), "next", branch.getId(), "in"));
        graph.addEdge(DroneProgramEdge.create(number.getId(), "value", branch.getId(), "condition"));
        graph.addEdge(DroneProgramEdge.create(branch.getId(), "true", trueEnd.getId(), "in"));
        graph.addEdge(DroneProgramEdge.create(branch.getId(), "false", falseEnd.getId(), "in"));

        DroneCompileResult result = compiler.compile(graph);

        assertTrue(result.hasErrors());
        assertTrue(hasDiagnostic(result, DroneDiagnosticCode.INCOMPATIBLE_PORT_TYPES));
        assertTrue(hasDiagnostic(result, DroneDiagnosticCode.REQUIRED_PORT_NOT_CONNECTED));
    }

    @Test
    void diagnosticsKeepNodeAndPortForEditorNavigation() {
        DroneProgramGraph graph = new DroneProgramGraph("diagnostic navigation");
        DroneProgramNode start = node(DrTechDroneNodes.START);
        DroneProgramNode branch = node(DrTechDroneNodes.BRANCH);
        DroneProgramNode end = node(DrTechDroneNodes.END);
        graph.addNode(start);
        graph.addNode(branch);
        graph.addNode(end);
        graph.addEdge(DroneProgramEdge.create(start.getId(), "next", branch.getId(), "in"));
        graph.addEdge(DroneProgramEdge.create(branch.getId(), "true", end.getId(), "in"));

        DroneCompileResult result = compiler.compile(graph);
        DroneProgramDiagnostic diagnostic = result.getDiagnostics().stream()
                .filter(value -> value.getCode() == DroneDiagnosticCode.REQUIRED_PORT_NOT_CONNECTED)
                .filter(value -> branch.getId().equals(value.getNodeId()))
                .findFirst()
                .orElseThrow(AssertionError::new);

        assertEquals(branch.getId(), diagnostic.getNodeId());
        assertNotNull(diagnostic.getPortId());
        assertFalse(diagnostic.getPortId().isEmpty());
    }

    @Test
    void reportsUnknownNodeTypeWithoutCrashing() {
        DroneProgramGraph graph = new DroneProgramGraph("unknown");
        graph.addNode(new DroneProgramNode(UUID.randomUUID(), new ResourceLocation("example", "missing"), 0, 0,
                new NBTTagCompound()));

        DroneCompileResult result = compiler.compile(graph);

        assertTrue(result.hasErrors());
        assertTrue(hasDiagnostic(result, DroneDiagnosticCode.UNKNOWN_NODE_TYPE));
        assertTrue(hasDiagnostic(result, DroneDiagnosticCode.NO_ENTRY_NODE));
    }

    @Test
    void warnsAboutDisconnectedNodeButStillCompiles() {
        DroneProgramGraph graph = new DroneProgramGraph("warning");
        DroneProgramNode start = node(DrTechDroneNodes.START);
        DroneProgramNode end = node(DrTechDroneNodes.END);
        DroneProgramNode unusedNumber = node(DrTechDroneNodes.NUMBER);
        graph.addNode(start);
        graph.addNode(end);
        graph.addNode(unusedNumber);
        graph.addEdge(DroneProgramEdge.create(start.getId(), "next", end.getId(), "in"));

        DroneCompileResult result = compiler.compile(graph);

        assertFalse(result.hasErrors());
        assertTrue(result.getProgram().isPresent());
        assertTrue(hasDiagnostic(result, DroneDiagnosticCode.UNREACHABLE_NODE));
    }

    @Test
    void commentNodePersistsAsEditorContentWithoutRuntimeReachabilityWarning() {
        DroneProgramGraph graph = new DroneProgramGraph("documented");
        DroneProgramNode start = node(DrTechDroneNodes.START);
        DroneProgramNode end = node(DrTechDroneNodes.END);
        NBTTagCompound configuration = new NBTTagCompound();
        configuration.setString("Text", "Harvest route: keep the failed branch connected to return-to-dock.");
        DroneProgramNode comment = node(DrTechDroneNodes.COMMENT).withConfiguration(configuration);
        graph.addNode(start);
        graph.addNode(end);
        graph.addNode(comment);
        graph.addEdge(DroneProgramEdge.create(start.getId(), "next", end.getId(), "in"));

        DroneCompileResult result = compiler.compile(graph);

        assertFalse(result.hasErrors());
        assertTrue(result.getProgram().isPresent());
        assertFalse(result.getDiagnostics().stream().anyMatch(value -> value.getNodeId() != null
                && value.getNodeId().equals(comment.getId())
                && value.getCode() == DroneDiagnosticCode.UNREACHABLE_NODE));
    }

    @Test
    void rejectsAreaThatExceedsRuntimeLimits() {
        DroneProgramGraph graph = new DroneProgramGraph("oversized area");
        DroneProgramNode start = node(DrTechDroneNodes.START);
        DroneProgramNode mine = node(DrTechDroneNodes.BREAK_BLOCK);
        DroneProgramNode end = node(DrTechDroneNodes.END);
        NBTTagCompound configuration = new NBTTagCompound();
        configuration.setInteger("X2", 32);
        DroneProgramNode area = node(DrTechDroneNodes.AREA).withConfiguration(configuration);
        graph.addNode(start);
        graph.addNode(mine);
        graph.addNode(end);
        graph.addNode(area);
        graph.addEdge(DroneProgramEdge.create(start.getId(), "next", mine.getId(), "in"));
        graph.addEdge(DroneProgramEdge.create(mine.getId(), "next", end.getId(), "in"));
        graph.addEdge(DroneProgramEdge.create(area.getId(), "value", mine.getId(), "area"));

        DroneCompileResult result = compiler.compile(graph);

        assertTrue(result.hasErrors());
        DroneProgramDiagnostic diagnostic = diagnostic(result, DroneDiagnosticCode.AREA_LIMIT_EXCEEDED);
        assertEquals(area.getId(), diagnostic.getNodeId());
        assertEquals("X2", diagnostic.getPropertyId());
    }

    @Test
    void rejectsReachableFlowCycleWithoutAnyExit() {
        DroneProgramGraph graph = new DroneProgramGraph("closed while");
        DroneProgramNode start = node(DrTechDroneNodes.START);
        DroneProgramNode loop = node(DrTechDroneNodes.WHILE);
        DroneProgramNode body = node(DrTechDroneNodes.WAIT);
        DroneProgramNode condition = node(DrTechDroneNodes.BOOLEAN);
        graph.addNode(start);
        graph.addNode(loop);
        graph.addNode(body);
        graph.addNode(condition);
        graph.addEdge(DroneProgramEdge.create(start.getId(), "next", loop.getId(), "in"));
        graph.addEdge(DroneProgramEdge.create(condition.getId(), "value", loop.getId(), "condition"));
        graph.addEdge(DroneProgramEdge.create(loop.getId(), "body", body.getId(), "in"));
        graph.addEdge(DroneProgramEdge.create(body.getId(), "next", loop.getId(), "in"));
        graph.addEdge(DroneProgramEdge.create(loop.getId(), "done", loop.getId(), "in"));

        DroneCompileResult result = compiler.compile(graph);

        assertTrue(result.hasErrors());
        DroneProgramDiagnostic diagnostic = diagnostic(result, DroneDiagnosticCode.NO_EXIT_LOOP);
        assertEquals(loop.getId(), diagnostic.getNodeId());
        assertEquals(DroneDiagnosticSeverity.ERROR, diagnostic.getSeverity());
    }

    @Test
    void warnsWhenConditionalCycleHasAnExitButMayNeverTakeIt() {
        DroneProgramGraph graph = new DroneProgramGraph("open while");
        DroneProgramNode start = node(DrTechDroneNodes.START);
        DroneProgramNode loop = node(DrTechDroneNodes.WHILE);
        DroneProgramNode body = node(DrTechDroneNodes.WAIT);
        DroneProgramNode condition = node(DrTechDroneNodes.BOOLEAN);
        DroneProgramNode end = node(DrTechDroneNodes.END);
        graph.addNode(start);
        graph.addNode(loop);
        graph.addNode(body);
        graph.addNode(condition);
        graph.addNode(end);
        graph.addEdge(DroneProgramEdge.create(start.getId(), "next", loop.getId(), "in"));
        graph.addEdge(DroneProgramEdge.create(condition.getId(), "value", loop.getId(), "condition"));
        graph.addEdge(DroneProgramEdge.create(loop.getId(), "body", body.getId(), "in"));
        graph.addEdge(DroneProgramEdge.create(body.getId(), "next", loop.getId(), "in"));
        graph.addEdge(DroneProgramEdge.create(loop.getId(), "done", end.getId(), "in"));

        DroneCompileResult result = compiler.compile(graph);

        assertFalse(result.hasErrors());
        DroneProgramDiagnostic diagnostic = diagnostic(result, DroneDiagnosticCode.POSSIBLE_INFINITE_LOOP);
        assertEquals(loop.getId(), diagnostic.getNodeId());
        assertEquals(DroneDiagnosticSeverity.WARNING, diagnostic.getSeverity());
    }

    @Test
    void doesNotWarnForBoundedRepeatBackEdge() {
        DroneProgramGraph graph = new DroneProgramGraph("bounded repeat");
        DroneProgramNode start = node(DrTechDroneNodes.START);
        DroneProgramNode repeat = node(DrTechDroneNodes.REPEAT);
        DroneProgramNode body = node(DrTechDroneNodes.WAIT);
        DroneProgramNode end = node(DrTechDroneNodes.END);
        graph.addNode(start);
        graph.addNode(repeat);
        graph.addNode(body);
        graph.addNode(end);
        graph.addEdge(DroneProgramEdge.create(start.getId(), "next", repeat.getId(), "in"));
        graph.addEdge(DroneProgramEdge.create(repeat.getId(), "body", body.getId(), "in"));
        graph.addEdge(DroneProgramEdge.create(body.getId(), "next", repeat.getId(), "in"));
        graph.addEdge(DroneProgramEdge.create(repeat.getId(), "done", end.getId(), "in"));

        DroneCompileResult result = compiler.compile(graph);

        assertFalse(result.hasErrors());
        assertFalse(hasDiagnostic(result, DroneDiagnosticCode.POSSIBLE_INFINITE_LOOP));
        assertFalse(hasDiagnostic(result, DroneDiagnosticCode.NO_EXIT_LOOP));
    }

    @Test
    void rejectsInvalidVariableName() {
        DroneProgramGraph graph = new DroneProgramGraph("invalid variable");
        DroneProgramNode start = node(DrTechDroneNodes.START);
        DroneProgramNode end = node(DrTechDroneNodes.END);
        NBTTagCompound configuration = new NBTTagCompound();
        configuration.setString("Name", "not valid!");
        DroneProgramNode variable = node(DrTechDroneNodes.GET_NUMBER_VARIABLE).withConfiguration(configuration);
        graph.addNode(start);
        graph.addNode(end);
        graph.addNode(variable);
        graph.addEdge(DroneProgramEdge.create(start.getId(), "next", end.getId(), "in"));

        DroneCompileResult result = compiler.compile(graph);

        assertTrue(result.hasErrors());
        DroneProgramDiagnostic diagnostic = diagnostic(result, DroneDiagnosticCode.INVALID_NODE_CONFIGURATION);
        assertEquals(variable.getId(), diagnostic.getNodeId());
        assertEquals("Name", diagnostic.getPropertyId());
    }

    @Test
    void rejectsUnknownMathOperator() {
        DroneProgramGraph graph = new DroneProgramGraph("invalid math");
        DroneProgramNode start = node(DrTechDroneNodes.START);
        DroneProgramNode end = node(DrTechDroneNodes.END);
        NBTTagCompound configuration = new NBTTagCompound();
        configuration.setString("Operator", "pow");
        DroneProgramNode math = node(DrTechDroneNodes.NUMBER_MATH).withConfiguration(configuration);
        graph.addNode(start);
        graph.addNode(end);
        graph.addNode(math);
        graph.addEdge(DroneProgramEdge.create(start.getId(), "next", end.getId(), "in"));

        DroneCompileResult result = compiler.compile(graph);

        assertTrue(result.hasErrors());
        assertTrue(hasDiagnostic(result, DroneDiagnosticCode.INVALID_NODE_CONFIGURATION));
    }

    @Test
    void validatesDeclaredNodePropertyRanges() {
        DroneProgramGraph graph = new DroneProgramGraph("invalid wait");
        DroneProgramNode start = node(DrTechDroneNodes.START);
        DroneProgramNode end = node(DrTechDroneNodes.END);
        NBTTagCompound configuration = new NBTTagCompound();
        configuration.setInteger("Ticks", 0);
        DroneProgramNode wait = node(DrTechDroneNodes.WAIT).withConfiguration(configuration);
        graph.addNode(start);
        graph.addNode(wait);
        graph.addNode(end);
        graph.addEdge(DroneProgramEdge.create(start.getId(), "next", wait.getId(), "in"));
        graph.addEdge(DroneProgramEdge.create(wait.getId(), "next", end.getId(), "in"));

        DroneCompileResult result = compiler.compile(graph);

        assertTrue(result.hasErrors());
        DroneProgramDiagnostic diagnostic = diagnostic(result, DroneDiagnosticCode.INVALID_NODE_CONFIGURATION);
        assertEquals(wait.getId(), diagnostic.getNodeId());
        assertEquals("Ticks", diagnostic.getPropertyId());
    }

    @Test
    void requiresExactlyOneCoordinateOrAreaForItemTransfer() {
        DroneProgramGraph graph = new DroneProgramGraph("transfer target choice");
        DroneProgramNode start = node(DrTechDroneNodes.START);
        DroneProgramNode transfer = node(DrTechDroneNodes.IMPORT_ITEMS);
        DroneProgramNode end = node(DrTechDroneNodes.END);
        DroneProgramNode coordinate = node(DrTechDroneNodes.COORDINATE);
        DroneProgramNode area = node(DrTechDroneNodes.AREA);
        graph.addNode(start);
        graph.addNode(transfer);
        graph.addNode(end);
        graph.addNode(coordinate);
        graph.addNode(area);
        graph.addEdge(DroneProgramEdge.create(start.getId(), "next", transfer.getId(), "in"));
        graph.addEdge(DroneProgramEdge.create(transfer.getId(), "next", end.getId(), "in"));

        DroneCompileResult missing = compiler.compile(graph);
        assertTrue(missing.hasErrors());
        assertTrue(hasDiagnostic(missing, DroneDiagnosticCode.INVALID_NODE_CONFIGURATION));

        graph.addEdge(DroneProgramEdge.create(coordinate.getId(), "value", transfer.getId(), "target"));
        graph.addEdge(DroneProgramEdge.create(area.getId(), "value", transfer.getId(), "area"));
        DroneCompileResult duplicate = compiler.compile(graph);
        assertTrue(duplicate.hasErrors());
        assertTrue(hasDiagnostic(duplicate, DroneDiagnosticCode.INVALID_NODE_CONFIGURATION));
    }

    private static DroneProgramNode node(ResourceLocation type) {
        return DroneProgramNode.create(type, 0, 0);
    }

    private static boolean hasDiagnostic(DroneCompileResult result, DroneDiagnosticCode code) {
        return result.getDiagnostics().stream().anyMatch(diagnostic -> diagnostic.getCode() == code);
    }

    private static DroneProgramDiagnostic diagnostic(DroneCompileResult result, DroneDiagnosticCode code) {
        return result.getDiagnostics().stream()
                .filter(value -> value.getCode() == code)
                .findFirst()
                .orElseThrow(AssertionError::new);
    }
}
