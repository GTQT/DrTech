package com.drppp.drtech.common.drone.program;

import com.drppp.drtech.common.drone.action.DroneTransferRequest;
import com.drppp.drtech.common.drone.action.DroneInteractionRequest;
import com.drppp.drtech.common.drone.action.DroneItemWorldRequest;
import com.drppp.drtech.common.drone.action.DroneSearchMode;
import com.drppp.drtech.common.drone.inventory.DroneItemFilter;
import com.drppp.drtech.common.drone.program.compile.CompiledDroneProgram;
import com.drppp.drtech.common.drone.program.compile.DroneProgramCompiler;
import com.drppp.drtech.common.drone.program.model.DroneProgramEdge;
import com.drppp.drtech.common.drone.program.model.DroneProgramGraph;
import com.drppp.drtech.common.drone.program.model.DroneProgramNode;
import com.drppp.drtech.common.drone.program.registry.DrTechDroneNodes;
import com.drppp.drtech.common.drone.program.runtime.DrTechDroneExecutors;
import com.drppp.drtech.common.drone.program.runtime.DroneProgramRuntime;
import com.drppp.drtech.common.drone.program.runtime.DroneExecutionResult;
import com.drppp.drtech.common.drone.program.runtime.DroneRuntimeEnvironment;
import com.drppp.drtech.common.drone.program.runtime.DroneRuntimeStatus;
import com.drppp.drtech.common.drone.program.runtime.DroneActionStatus;
import com.drppp.drtech.common.drone.program.runtime.DrTechDroneValueEvaluators;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DroneProgramRuntimeTest {

    @Test
    void executesStartWaitEndAcrossTicks() {
        ProgramFixture fixture = waitProgram(3);
        DroneProgramRuntime runtime = fixture.runtime;

        runtime.tick();
        assertEquals(DroneRuntimeStatus.RUNNING, runtime.getStatus());
        assertEquals(fixture.waitNode.getId(), runtime.getCurrentNodeId());
        runtime.tick();
        assertEquals(DroneRuntimeStatus.RUNNING, runtime.getStatus());
        runtime.tick();
        assertEquals(DroneRuntimeStatus.COMPLETED, runtime.getStatus());
        assertEquals(fixture.endNode.getId(), runtime.getCurrentNodeId());
    }

    @Test
    void pauseResumeStopAndRestartControlTheLifecycle() {
        ProgramFixture fixture = waitProgram(4);
        DroneProgramRuntime runtime = fixture.runtime;

        runtime.tick();
        runtime.pause();
        assertEquals(DroneRuntimeStatus.PAUSED, runtime.getStatus());
        assertEquals(fixture.waitNode.getId(), runtime.getCurrentNodeId());
        runtime.tick();
        assertEquals(DroneRuntimeStatus.PAUSED, runtime.getStatus());

        runtime.resume();
        assertEquals(DroneRuntimeStatus.RUNNING, runtime.getStatus());
        runtime.stop();
        assertEquals(DroneRuntimeStatus.READY, runtime.getStatus());
        assertEquals(fixture.program.getEntryNodeId(), runtime.getCurrentNodeId());

        runtime.restart();
        assertEquals(DroneRuntimeStatus.RUNNING, runtime.getStatus());
        assertEquals(fixture.program.getEntryNodeId(), runtime.getCurrentNodeId());
        assertEquals("wait", runtime.getProgramName());
        assertEquals("start", runtime.getCurrentNodeType());
    }

    @Test
    void resumesNodeLocalStateAfterNbtRoundTrip() {
        ProgramFixture fixture = waitProgram(3);
        fixture.runtime.tick();
        NBTTagCompound saved = fixture.runtime.writeToNbt();
        DroneProgramRuntime restored = new DroneProgramRuntime(fixture.program, DrTechDroneExecutors.createDefaultRegistry());
        restored.readFromNbt(saved);

        restored.tick();
        assertEquals(DroneRuntimeStatus.RUNNING, restored.getStatus());
        restored.tick();
        assertEquals(DroneRuntimeStatus.COMPLETED, restored.getStatus());
    }

    @Test
    void pausedRecallStateSurvivesItemRoundTripAndDockLaunchResumesIt() {
        ProgramFixture fixture = waitProgram(4);
        fixture.runtime.tick();
        fixture.runtime.pause();

        DroneProgramRuntime restored = new DroneProgramRuntime(
                fixture.program, DrTechDroneExecutors.createDefaultRegistry());
        restored.readFromNbt(fixture.runtime.writeToNbt());

        assertEquals(DroneRuntimeStatus.PAUSED, restored.getStatus());
        assertEquals(fixture.waitNode.getId(), restored.getCurrentNodeId());
        restored.tick();
        assertEquals(DroneRuntimeStatus.PAUSED, restored.getStatus());
        assertEquals(true, restored.activateForDockLaunch());
        assertEquals(DroneRuntimeStatus.RUNNING, restored.getStatus());
        assertEquals(fixture.waitNode.getId(), restored.getCurrentNodeId());
    }

    @Test
    void dockLaunchStartsReadyAndRestartsCompletedPrograms() {
        ProgramFixture readyFixture = waitProgram(1);
        assertEquals(DroneRuntimeStatus.READY, readyFixture.runtime.getStatus());
        assertEquals(true, readyFixture.runtime.activateForDockLaunch());
        assertEquals(DroneRuntimeStatus.RUNNING, readyFixture.runtime.getStatus());

        ProgramFixture completedFixture = waitProgram(1);
        completedFixture.runtime.tick();
        completedFixture.runtime.tick();
        assertEquals(DroneRuntimeStatus.COMPLETED, completedFixture.runtime.getStatus());
        assertEquals(true, completedFixture.runtime.activateForDockLaunch());
        assertEquals(DroneRuntimeStatus.RUNNING, completedFixture.runtime.getStatus());
        assertEquals(completedFixture.program.getEntryNodeId(), completedFixture.runtime.getCurrentNodeId());
    }

    @Test
    void evaluatesTypedBooleanInputAndSelectsBranchPort() {
        DroneProgramGraph graph = new DroneProgramGraph("branch");
        DroneProgramNode start = DroneProgramNode.create(DrTechDroneNodes.START, 0, 0);
        DroneProgramNode branch = DroneProgramNode.create(DrTechDroneNodes.BRANCH, 100, 0);
        NBTTagCompound valueConfig = new NBTTagCompound();
        valueConfig.setBoolean("Value", true);
        DroneProgramNode value = DroneProgramNode.create(DrTechDroneNodes.BOOLEAN, 100, 80)
                .withConfiguration(valueConfig);
        DroneProgramNode trueEnd = DroneProgramNode.create(DrTechDroneNodes.END, 220, 0);
        DroneProgramNode falseEnd = DroneProgramNode.create(DrTechDroneNodes.END, 220, 80);
        graph.addNode(start);
        graph.addNode(branch);
        graph.addNode(value);
        graph.addNode(trueEnd);
        graph.addNode(falseEnd);
        graph.addEdge(DroneProgramEdge.create(start.getId(), "next", branch.getId(), "in"));
        graph.addEdge(DroneProgramEdge.create(value.getId(), "value", branch.getId(), "condition"));
        graph.addEdge(DroneProgramEdge.create(branch.getId(), "true", trueEnd.getId(), "in"));
        graph.addEdge(DroneProgramEdge.create(branch.getId(), "false", falseEnd.getId(), "in"));
        CompiledDroneProgram compiled = new DroneProgramCompiler(DrTechDroneNodes.createDefaultRegistry())
                .compile(graph).getProgram().orElseThrow(AssertionError::new);
        DroneProgramRuntime runtime = new DroneProgramRuntime(compiled, DrTechDroneExecutors.createDefaultRegistry());

        runtime.tick();

        assertEquals(DroneRuntimeStatus.COMPLETED, runtime.getStatus());
        assertEquals(trueEnd.getId(), runtime.getCurrentNodeId());
    }

    @Test
    void dispatchesCoordinateBlockActionsToRuntimeEnvironment() {
        DroneProgramGraph graph = new DroneProgramGraph("block actions");
        DroneProgramNode start = DroneProgramNode.create(DrTechDroneNodes.START, 0, 0);
        DroneProgramNode breakAt = DroneProgramNode.create(DrTechDroneNodes.BREAK_BLOCK_AT, 100, 0);
        DroneProgramNode placeAt = DroneProgramNode.create(DrTechDroneNodes.PLACE_BLOCK, 200, 0);
        DroneProgramNode end = DroneProgramNode.create(DrTechDroneNodes.END, 300, 0);
        DroneProgramNode breakTarget = coordinate(4, 5, 6);
        DroneProgramNode placeTarget = coordinate(7, 8, 9);
        graph.addNode(start);
        graph.addNode(breakAt);
        graph.addNode(placeAt);
        graph.addNode(end);
        graph.addNode(breakTarget);
        graph.addNode(placeTarget);
        graph.addEdge(DroneProgramEdge.create(start.getId(), "next", breakAt.getId(), "in"));
        graph.addEdge(DroneProgramEdge.create(breakAt.getId(), "next", placeAt.getId(), "in"));
        graph.addEdge(DroneProgramEdge.create(placeAt.getId(), "next", end.getId(), "in"));
        graph.addEdge(DroneProgramEdge.create(breakTarget.getId(), "value", breakAt.getId(), "target"));
        graph.addEdge(DroneProgramEdge.create(placeTarget.getId(), "value", placeAt.getId(), "target"));
        CompiledDroneProgram compiled = new DroneProgramCompiler(DrTechDroneNodes.createDefaultRegistry())
                .compile(graph).getProgram().orElseThrow(AssertionError::new);
        RecordingEnvironment environment = new RecordingEnvironment();
        DroneProgramRuntime runtime = new DroneProgramRuntime(compiled, DrTechDroneExecutors.createDefaultRegistry(),
                DrTechDroneValueEvaluators.createDefaultRegistry(), environment);

        runtime.tick();

        assertEquals(DroneRuntimeStatus.COMPLETED, runtime.getStatus());
        assertEquals(new BlockPos(4, 5, 6), environment.broken);
        assertEquals(new BlockPos(7, 8, 9), environment.placed);
    }

    @Test
    void returnsToDockThenWaitsForConfiguredChargeLevel() {
        DroneProgramGraph graph = new DroneProgramGraph("dock cycle");
        DroneProgramNode start = DroneProgramNode.create(DrTechDroneNodes.START, 0, 0);
        DroneProgramNode returnToDock = DroneProgramNode.create(DrTechDroneNodes.RETURN_TO_DOCK, 100, 0);
        NBTTagCompound chargeConfiguration = new NBTTagCompound();
        chargeConfiguration.setDouble("Percent", 75.0D);
        DroneProgramNode charge = DroneProgramNode.create(DrTechDroneNodes.CHARGE_UNTIL, 200, 0)
                .withConfiguration(chargeConfiguration);
        DroneProgramNode end = DroneProgramNode.create(DrTechDroneNodes.END, 300, 0);
        DroneProgramNode dockTarget = coordinate(12, 64, -3);
        graph.addNode(start);
        graph.addNode(returnToDock);
        graph.addNode(charge);
        graph.addNode(end);
        graph.addNode(dockTarget);
        graph.addEdge(DroneProgramEdge.create(start.getId(), "next", returnToDock.getId(), "in"));
        graph.addEdge(DroneProgramEdge.create(returnToDock.getId(), "next", charge.getId(), "in"));
        graph.addEdge(DroneProgramEdge.create(charge.getId(), "next", end.getId(), "in"));
        graph.addEdge(DroneProgramEdge.create(dockTarget.getId(), "value", returnToDock.getId(), "target"));
        CompiledDroneProgram compiled = new DroneProgramCompiler(DrTechDroneNodes.createDefaultRegistry())
                .compile(graph).getProgram().orElseThrow(AssertionError::new);
        RecordingEnvironment environment = new RecordingEnvironment();
        DroneProgramRuntime runtime = new DroneProgramRuntime(compiled, DrTechDroneExecutors.createDefaultRegistry(),
                DrTechDroneValueEvaluators.createDefaultRegistry(), environment);

        runtime.tick();
        assertEquals(DroneRuntimeStatus.RUNNING, runtime.getStatus());
        assertEquals(new BlockPos(12, 64, -3), environment.dock);
        runtime.tick();

        assertEquals(DroneRuntimeStatus.COMPLETED, runtime.getStatus());
        assertEquals(75.0D, environment.requestedChargePercent);
    }

    @Test
    void bindsConfiguresAndUnbindsDockThroughVisualActions() {
        DroneProgramGraph graph = new DroneProgramGraph("dock controls");
        DroneProgramNode start = DroneProgramNode.create(DrTechDroneNodes.START, 0, 0);
        DroneProgramNode bind = DroneProgramNode.create(DrTechDroneNodes.BIND_DOCK, 100, 0);
        NBTTagCompound firmwareConfig = new NBTTagCompound();
        firmwareConfig.setInteger("ReturnPercent", 30);
        firmwareConfig.setInteger("ResumePercent", 85);
        DroneProgramNode configure = DroneProgramNode.create(DrTechDroneNodes.CONFIGURE_SAFETY, 200, 0)
                .withConfiguration(firmwareConfig);
        DroneProgramNode unbind = DroneProgramNode.create(DrTechDroneNodes.UNBIND_DOCK, 300, 0);
        DroneProgramNode end = DroneProgramNode.create(DrTechDroneNodes.END, 400, 0);
        DroneProgramNode target = coordinate(4, 70, -2);
        for (DroneProgramNode node : new DroneProgramNode[] { start, bind, configure, unbind, end, target }) {
            graph.addNode(node);
        }
        graph.addEdge(DroneProgramEdge.create(start.getId(), "next", bind.getId(), "in"));
        graph.addEdge(DroneProgramEdge.create(bind.getId(), "next", configure.getId(), "in"));
        graph.addEdge(DroneProgramEdge.create(configure.getId(), "next", unbind.getId(), "in"));
        graph.addEdge(DroneProgramEdge.create(unbind.getId(), "next", end.getId(), "in"));
        graph.addEdge(DroneProgramEdge.create(target.getId(), "value", bind.getId(), "target"));
        CompiledDroneProgram compiled = new DroneProgramCompiler(DrTechDroneNodes.createDefaultRegistry())
                .compile(graph).getProgram().orElseThrow(AssertionError::new);
        RecordingEnvironment environment = new RecordingEnvironment();
        DroneProgramRuntime runtime = new DroneProgramRuntime(compiled, DrTechDroneExecutors.createDefaultRegistry(),
                DrTechDroneValueEvaluators.createDefaultRegistry(), environment);

        for (int tick = 0; tick < 5 && runtime.getStatus() != DroneRuntimeStatus.COMPLETED; tick++) runtime.tick();

        assertEquals(DroneRuntimeStatus.COMPLETED, runtime.getStatus());
        assertEquals(new BlockPos(4, 70, -2), environment.boundDock);
        assertEquals(true, environment.unboundDock);
        assertEquals(30, environment.returnAtPercent);
        assertEquals(85, environment.resumeAtPercent);
    }

    @Test
    void transfersEuAndChargesTargetPercentThroughVisualActions() {
        DroneProgramGraph graph = new DroneProgramGraph("eu logistics");
        DroneProgramNode start = DroneProgramNode.create(DrTechDroneNodes.START, 0, 0);
        NBTTagCompound euConfig = new NBTTagCompound();
        euConfig.setInteger("MaxEU", 128);
        DroneProgramNode importEu = DroneProgramNode.create(DrTechDroneNodes.IMPORT_EU, 100, 0)
                .withConfiguration(euConfig);
        DroneProgramNode exportEu = DroneProgramNode.create(DrTechDroneNodes.EXPORT_EU, 200, 0)
                .withConfiguration(euConfig);
        NBTTagCompound chargeConfig = new NBTTagCompound();
        chargeConfig.setInteger("Percent", 75);
        chargeConfig.setInteger("MaxEU", 128);
        DroneProgramNode charge = DroneProgramNode.create(DrTechDroneNodes.CHARGE_TARGET_PERCENT, 300, 0)
                .withConfiguration(chargeConfig);
        DroneProgramNode end = DroneProgramNode.create(DrTechDroneNodes.END, 400, 0);
        DroneProgramNode importTarget = coordinate(5, 64, 5);
        DroneProgramNode exportTarget = coordinate(5, 64, 5);
        DroneProgramNode chargeTarget = coordinate(5, 64, 5);
        for (DroneProgramNode node : new DroneProgramNode[] {
                start, importEu, exportEu, charge, end, importTarget, exportTarget, chargeTarget }) {
            graph.addNode(node);
        }
        graph.addEdge(DroneProgramEdge.create(start.getId(), "next", importEu.getId(), "in"));
        graph.addEdge(DroneProgramEdge.create(importEu.getId(), "next", exportEu.getId(), "in"));
        graph.addEdge(DroneProgramEdge.create(exportEu.getId(), "next", charge.getId(), "in"));
        graph.addEdge(DroneProgramEdge.create(charge.getId(), "next", end.getId(), "in"));
        graph.addEdge(DroneProgramEdge.create(importTarget.getId(), "value", importEu.getId(), "target"));
        graph.addEdge(DroneProgramEdge.create(exportTarget.getId(), "value", exportEu.getId(), "target"));
        graph.addEdge(DroneProgramEdge.create(chargeTarget.getId(), "value", charge.getId(), "target"));
        CompiledDroneProgram compiled = new DroneProgramCompiler(DrTechDroneNodes.createDefaultRegistry())
                .compile(graph).getProgram().orElseThrow(AssertionError::new);
        RecordingEnvironment environment = new RecordingEnvironment();
        DroneProgramRuntime runtime = new DroneProgramRuntime(compiled, DrTechDroneExecutors.createDefaultRegistry(),
                DrTechDroneValueEvaluators.createDefaultRegistry(), environment);

        for (int tick = 0; tick < 10 && runtime.getStatus() != DroneRuntimeStatus.COMPLETED; tick++) runtime.tick();

        assertEquals(DroneRuntimeStatus.COMPLETED, runtime.getStatus());
        assertEquals(new BlockPos(5, 64, 5), environment.importEuTarget);
        assertEquals(new BlockPos(5, 64, 5), environment.exportEuTarget);
        assertEquals(75.0D, environment.targetChargePercent);
        assertEquals(128L, environment.importedEu);
        assertEquals(64L, environment.exportedEu);
        assertEquals(64L, runtime.getActionAmount(charge.getId()));
    }

    @Test
    void dispatchesFilteredInventoryImportAndExport() {
        DroneProgramGraph graph = new DroneProgramGraph("item logistics");
        DroneProgramNode start = DroneProgramNode.create(DrTechDroneNodes.START, 0, 0);
        NBTTagCompound importConfiguration = new NBTTagCompound();
        importConfiguration.setString("Direction", "DOWN");
        importConfiguration.setInteger("MaxAmount", 32);
        importConfiguration.setInteger("BatchSize", 8);
        DroneProgramNode importItems = DroneProgramNode.create(DrTechDroneNodes.IMPORT_ITEMS, 100, 0)
                .withConfiguration(importConfiguration);
        NBTTagCompound exportConfiguration = new NBTTagCompound();
        exportConfiguration.setString("Direction", "UP");
        exportConfiguration.setInteger("MaxAmount", 16);
        exportConfiguration.setInteger("BatchSize", 4);
        DroneProgramNode exportItems = DroneProgramNode.create(DrTechDroneNodes.EXPORT_ITEMS, 200, 0)
                .withConfiguration(exportConfiguration);
        DroneProgramNode end = DroneProgramNode.create(DrTechDroneNodes.END, 300, 0);
        DroneProgramNode importTarget = coordinate(2, 3, 4);
        DroneProgramNode exportTarget = coordinate(8, 9, 10);
        NBTTagCompound filterConfiguration = new NBTTagCompound();
        filterConfiguration.setString("Item", "minecraft:stone");
        filterConfiguration.setInteger("Meta", 2);
        DroneProgramNode importFilter = DroneProgramNode.create(DrTechDroneNodes.ITEM_FILTER, 0, 120)
                .withConfiguration(filterConfiguration);
        DroneProgramNode exportFilter = DroneProgramNode.create(DrTechDroneNodes.ITEM_FILTER, 100, 120)
                .withConfiguration(filterConfiguration);
        graph.addNode(start);
        graph.addNode(importItems);
        graph.addNode(exportItems);
        graph.addNode(end);
        graph.addNode(importTarget);
        graph.addNode(exportTarget);
        graph.addNode(importFilter);
        graph.addNode(exportFilter);
        graph.addEdge(DroneProgramEdge.create(start.getId(), "next", importItems.getId(), "in"));
        graph.addEdge(DroneProgramEdge.create(importItems.getId(), "next", exportItems.getId(), "in"));
        graph.addEdge(DroneProgramEdge.create(exportItems.getId(), "next", end.getId(), "in"));
        graph.addEdge(DroneProgramEdge.create(importTarget.getId(), "value", importItems.getId(), "target"));
        graph.addEdge(DroneProgramEdge.create(exportTarget.getId(), "value", exportItems.getId(), "target"));
        graph.addEdge(DroneProgramEdge.create(importFilter.getId(), "filter", importItems.getId(), "filter"));
        graph.addEdge(DroneProgramEdge.create(exportFilter.getId(), "filter", exportItems.getId(), "filter"));
        CompiledDroneProgram compiled = new DroneProgramCompiler(DrTechDroneNodes.createDefaultRegistry())
                .compile(graph).getProgram().orElseThrow(AssertionError::new);
        RecordingEnvironment environment = new RecordingEnvironment();
        DroneProgramRuntime runtime = new DroneProgramRuntime(compiled, DrTechDroneExecutors.createDefaultRegistry(),
                DrTechDroneValueEvaluators.createDefaultRegistry(), environment);

        runtime.tick();

        assertEquals(DroneRuntimeStatus.COMPLETED, runtime.getStatus());
        assertEquals(new BlockPos(2, 3, 4), environment.importTarget);
        assertEquals(new BlockPos(8, 9, 10), environment.exportTarget);
        assertEquals("minecraft:stone", environment.itemFilter.getItemId().toString());
        assertEquals(2, environment.itemFilter.getMetadata());
        assertEquals(EnumFacing.DOWN, environment.importRequest.getSide());
        assertEquals(32, environment.importRequest.getMaximumAmount());
        assertEquals(8, environment.importRequest.getBatchSize());
        assertEquals(EnumFacing.UP, environment.exportRequest.getSide());
        assertEquals(16, environment.exportRequest.getMaximumAmount());
        assertEquals(4, environment.exportRequest.getBatchSize());
    }

    @Test
    void minesAndFillsAreasOneTargetAtATime() {
        DroneProgramGraph graph = new DroneProgramGraph("area work");
        DroneProgramNode start = DroneProgramNode.create(DrTechDroneNodes.START, 0, 0);
        DroneProgramNode mine = DroneProgramNode.create(DrTechDroneNodes.BREAK_BLOCK, 100, 0);
        DroneProgramNode fill = DroneProgramNode.create(DrTechDroneNodes.PLACE_AREA, 200, 0);
        DroneProgramNode end = DroneProgramNode.create(DrTechDroneNodes.END, 300, 0);
        DroneProgramNode mineArea = area(0, 5, 0, 2, 5, 0);
        DroneProgramNode fillArea = area(10, 5, 0, 12, 5, 0);
        graph.addNode(start);
        graph.addNode(mine);
        graph.addNode(fill);
        graph.addNode(end);
        graph.addNode(mineArea);
        graph.addNode(fillArea);
        graph.addEdge(DroneProgramEdge.create(start.getId(), "next", mine.getId(), "in"));
        graph.addEdge(DroneProgramEdge.create(mine.getId(), "next", fill.getId(), "in"));
        graph.addEdge(DroneProgramEdge.create(fill.getId(), "next", end.getId(), "in"));
        graph.addEdge(DroneProgramEdge.create(mineArea.getId(), "value", mine.getId(), "area"));
        graph.addEdge(DroneProgramEdge.create(fillArea.getId(), "value", fill.getId(), "area"));
        CompiledDroneProgram compiled = new DroneProgramCompiler(DrTechDroneNodes.createDefaultRegistry())
                .compile(graph).getProgram().orElseThrow(AssertionError::new);
        RecordingEnvironment environment = new RecordingEnvironment();
        DroneProgramRuntime runtime = new DroneProgramRuntime(compiled, DrTechDroneExecutors.createDefaultRegistry(),
                DrTechDroneValueEvaluators.createDefaultRegistry(), environment);

        for (int tick = 0; tick < 10 && runtime.getStatus() != DroneRuntimeStatus.COMPLETED; tick++) runtime.tick();

        assertEquals(DroneRuntimeStatus.COMPLETED, runtime.getStatus());
        assertEquals(Arrays.asList(new BlockPos(0, 5, 0), new BlockPos(1, 5, 0), new BlockPos(2, 5, 0)),
                environment.brokenTargets);
        assertEquals(Arrays.asList(new BlockPos(10, 5, 0), new BlockPos(11, 5, 0), new BlockPos(12, 5, 0)),
                environment.placedTargets);
    }

    @Test
    void persistsVariablesAndRunsFiniteLoopBackEdges() {
        DroneProgramGraph graph = new DroneProgramGraph("variables and repeat");
        DroneProgramNode start = DroneProgramNode.create(DrTechDroneNodes.START, 0, 0);
        DroneProgramNode set = configured(DrTechDroneNodes.SET_NUMBER_VARIABLE, "Name", "total");
        NBTTagCompound repeatConfig = new NBTTagCompound();
        repeatConfig.setInteger("Count", 3);
        DroneProgramNode repeat = DroneProgramNode.create(DrTechDroneNodes.REPEAT, 200, 0)
                .withConfiguration(repeatConfig);
        DroneProgramNode add = configured(DrTechDroneNodes.ADD_NUMBER_VARIABLE, "Name", "total");
        DroneProgramNode end = DroneProgramNode.create(DrTechDroneNodes.END, 400, 0);
        DroneProgramNode zero = number(0.0D);
        DroneProgramNode two = number(2.0D);
        graph.addNode(start);
        graph.addNode(set);
        graph.addNode(repeat);
        graph.addNode(add);
        graph.addNode(end);
        graph.addNode(zero);
        graph.addNode(two);
        graph.addEdge(DroneProgramEdge.create(start.getId(), "next", set.getId(), "in"));
        graph.addEdge(DroneProgramEdge.create(set.getId(), "next", repeat.getId(), "in"));
        graph.addEdge(DroneProgramEdge.create(repeat.getId(), "body", add.getId(), "in"));
        graph.addEdge(DroneProgramEdge.create(add.getId(), "next", repeat.getId(), "in"));
        graph.addEdge(DroneProgramEdge.create(repeat.getId(), "done", end.getId(), "in"));
        graph.addEdge(DroneProgramEdge.create(zero.getId(), "value", set.getId(), "value"));
        graph.addEdge(DroneProgramEdge.create(two.getId(), "value", add.getId(), "amount"));
        CompiledDroneProgram compiled = new DroneProgramCompiler(DrTechDroneNodes.createDefaultRegistry())
                .compile(graph).getProgram().orElseThrow(AssertionError::new);
        DroneProgramRuntime runtime = new DroneProgramRuntime(compiled, DrTechDroneExecutors.createDefaultRegistry());

        for (int tick = 0; tick < 10 && runtime.getStatus() != DroneRuntimeStatus.COMPLETED; tick++) runtime.tick();

        assertEquals(DroneRuntimeStatus.COMPLETED, runtime.getStatus());
        assertEquals(6.0D, runtime.getNumberVariable("total"));
        assertTrue(runtime.getVariableSummary(3).contains("total=6"));
        assertTrue(runtime.getTraceSummary(24).contains("repeat -> done"));

        DroneProgramRuntime restored = new DroneProgramRuntime(compiled, DrTechDroneExecutors.createDefaultRegistry());
        restored.readFromNbt(runtime.writeToNbt());
        assertEquals(6.0D, restored.getNumberVariable("total"));
        assertTrue(restored.getTraceSummary(24).contains("add_number_variable"));
    }

    @Test
    void breakpointPausesAndSingleStepAdvancesExactlyOneNode() {
        DroneProgramGraph graph = new DroneProgramGraph("debug");
        DroneProgramNode start = DroneProgramNode.create(DrTechDroneNodes.START, 0, 0);
        NBTTagCompound waitConfig = new NBTTagCompound();
        waitConfig.setInteger("Ticks", 1);
        waitConfig.setBoolean("Breakpoint", true);
        DroneProgramNode wait = DroneProgramNode.create(DrTechDroneNodes.WAIT, 100, 0).withConfiguration(waitConfig);
        DroneProgramNode end = DroneProgramNode.create(DrTechDroneNodes.END, 200, 0);
        graph.addNode(start);
        graph.addNode(wait);
        graph.addNode(end);
        graph.addEdge(DroneProgramEdge.create(start.getId(), "next", wait.getId(), "in"));
        graph.addEdge(DroneProgramEdge.create(wait.getId(), "next", end.getId(), "in"));
        CompiledDroneProgram compiled = new DroneProgramCompiler(DrTechDroneNodes.createDefaultRegistry())
                .compile(graph).getProgram().orElseThrow(AssertionError::new);
        DroneProgramRuntime runtime = new DroneProgramRuntime(compiled, DrTechDroneExecutors.createDefaultRegistry());

        runtime.tick();
        assertEquals(DroneRuntimeStatus.PAUSED, runtime.getStatus());
        assertEquals(wait.getId(), runtime.getCurrentNodeId());
        assertTrue(runtime.getTraceSummary(4).contains("BREAK wait"));

        runtime.requestSingleStep();
        runtime.tick();
        assertEquals(DroneRuntimeStatus.PAUSED, runtime.getStatus());
        assertEquals(end.getId(), runtime.getCurrentNodeId());

        runtime.resume();
        runtime.tick();
        assertEquals(DroneRuntimeStatus.COMPLETED, runtime.getStatus());
    }

    @Test
    void eventGatesWaitForRedstoneThenOwnerProximity() {
        DroneProgramGraph graph = new DroneProgramGraph("events");
        DroneProgramNode start = DroneProgramNode.create(DrTechDroneNodes.START, 0, 0);
        DroneProgramNode redstone = DroneProgramNode.create(DrTechDroneNodes.WAIT_FOR_REDSTONE, 100, 0);
        NBTTagCompound ownerConfig = new NBTTagCompound();
        ownerConfig.setDouble("Radius", 24.0D);
        DroneProgramNode owner = DroneProgramNode.create(DrTechDroneNodes.WAIT_FOR_OWNER, 200, 0)
                .withConfiguration(ownerConfig);
        DroneProgramNode end = DroneProgramNode.create(DrTechDroneNodes.END, 300, 0);
        graph.addNode(start);
        graph.addNode(redstone);
        graph.addNode(owner);
        graph.addNode(end);
        graph.addEdge(DroneProgramEdge.create(start.getId(), "next", redstone.getId(), "in"));
        graph.addEdge(DroneProgramEdge.create(redstone.getId(), "next", owner.getId(), "in"));
        graph.addEdge(DroneProgramEdge.create(owner.getId(), "next", end.getId(), "in"));
        CompiledDroneProgram compiled = new DroneProgramCompiler(DrTechDroneNodes.createDefaultRegistry())
                .compile(graph).getProgram().orElseThrow(AssertionError::new);
        RecordingEnvironment environment = new RecordingEnvironment();
        DroneProgramRuntime runtime = new DroneProgramRuntime(compiled, DrTechDroneExecutors.createDefaultRegistry(),
                DrTechDroneValueEvaluators.createDefaultRegistry(), environment);

        runtime.tick();
        assertEquals(redstone.getId(), runtime.getCurrentNodeId());
        environment.redstonePowered = true;
        runtime.tick();
        assertEquals(owner.getId(), runtime.getCurrentNodeId());
        assertEquals(24.0D, environment.ownerRadius);
        environment.ownerNearby = true;
        runtime.tick();

        assertEquals(DroneRuntimeStatus.COMPLETED, runtime.getStatus());
        assertEquals(end.getId(), runtime.getCurrentNodeId());
    }

    @Test
    void whileLoopReevaluatesMathBooleanLogicAndVariablesEachIteration() {
        DroneProgramGraph graph = new DroneProgramGraph("computed while");
        DroneProgramNode start = DroneProgramNode.create(DrTechDroneNodes.START, 0, 0);
        DroneProgramNode set = configured(DrTechDroneNodes.SET_NUMBER_VARIABLE, "Name", "counter");
        DroneProgramNode loop = DroneProgramNode.create(DrTechDroneNodes.WHILE, 200, 0);
        DroneProgramNode add = configured(DrTechDroneNodes.ADD_NUMBER_VARIABLE, "Name", "counter");
        DroneProgramNode end = DroneProgramNode.create(DrTechDroneNodes.END, 400, 0);
        DroneProgramNode zero = number(0.0D);
        DroneProgramNode four = number(4.0D);
        DroneProgramNode two = number(2.0D);
        DroneProgramNode six = number(6.0D);
        DroneProgramNode falsity = bool(false);
        DroneProgramNode not = DroneProgramNode.create(DrTechDroneNodes.BOOLEAN_NOT, 0, 100);
        DroneProgramNode variable = configured(DrTechDroneNodes.GET_NUMBER_VARIABLE, "Name", "counter");
        DroneProgramNode math = configured(DrTechDroneNodes.NUMBER_MATH, "Operator", "/");
        DroneProgramNode compare = configured(DrTechDroneNodes.COMPARE_NUMBER, "Operator", "<");
        DroneProgramNode logic = configured(DrTechDroneNodes.BOOLEAN_LOGIC, "Operator", "AND");
        for (DroneProgramNode node : Arrays.asList(start, set, loop, add, end, zero, four, two, six, falsity, not,
                variable, math, compare, logic)) graph.addNode(node);
        graph.addEdge(DroneProgramEdge.create(start.getId(), "next", set.getId(), "in"));
        graph.addEdge(DroneProgramEdge.create(set.getId(), "next", loop.getId(), "in"));
        graph.addEdge(DroneProgramEdge.create(loop.getId(), "body", add.getId(), "in"));
        graph.addEdge(DroneProgramEdge.create(add.getId(), "next", loop.getId(), "in"));
        graph.addEdge(DroneProgramEdge.create(loop.getId(), "done", end.getId(), "in"));
        graph.addEdge(DroneProgramEdge.create(zero.getId(), "value", set.getId(), "value"));
        graph.addEdge(DroneProgramEdge.create(four.getId(), "value", math.getId(), "left"));
        graph.addEdge(DroneProgramEdge.create(two.getId(), "value", math.getId(), "right"));
        graph.addEdge(DroneProgramEdge.create(math.getId(), "result", add.getId(), "amount"));
        graph.addEdge(DroneProgramEdge.create(variable.getId(), "value", compare.getId(), "left"));
        graph.addEdge(DroneProgramEdge.create(six.getId(), "value", compare.getId(), "right"));
        graph.addEdge(DroneProgramEdge.create(compare.getId(), "result", logic.getId(), "left"));
        graph.addEdge(DroneProgramEdge.create(falsity.getId(), "value", not.getId(), "value"));
        graph.addEdge(DroneProgramEdge.create(not.getId(), "result", logic.getId(), "right"));
        graph.addEdge(DroneProgramEdge.create(logic.getId(), "result", loop.getId(), "condition"));
        CompiledDroneProgram compiled = new DroneProgramCompiler(DrTechDroneNodes.createDefaultRegistry())
                .compile(graph).getProgram().orElseThrow(AssertionError::new);
        DroneProgramRuntime runtime = new DroneProgramRuntime(compiled, DrTechDroneExecutors.createDefaultRegistry());

        for (int tick = 0; tick < 10 && runtime.getStatus() != DroneRuntimeStatus.COMPLETED; tick++) runtime.tick();

        assertEquals(DroneRuntimeStatus.COMPLETED, runtime.getStatus());
        assertEquals(6.0D, runtime.getNumberVariable("counter"));
    }

    @Test
    void buildsDynamicAreaFromCoordinateOffset() {
        DroneProgramGraph graph = new DroneProgramGraph("dynamic area");
        DroneProgramNode start = DroneProgramNode.create(DrTechDroneNodes.START, 0, 0);
        DroneProgramNode mine = DroneProgramNode.create(DrTechDroneNodes.BREAK_BLOCK, 100, 0);
        DroneProgramNode end = DroneProgramNode.create(DrTechDroneNodes.END, 200, 0);
        DroneProgramNode first = coordinate(5, 10, 5);
        DroneProgramNode base = coordinate(5, 10, 5);
        NBTTagCompound offsetConfig = new NBTTagCompound();
        offsetConfig.setInteger("X", 1);
        DroneProgramNode offset = DroneProgramNode.create(DrTechDroneNodes.COORDINATE_OFFSET, 0, 100)
                .withConfiguration(offsetConfig);
        DroneProgramNode area = DroneProgramNode.create(DrTechDroneNodes.AREA_FROM_CORNERS, 100, 100);
        for (DroneProgramNode node : Arrays.asList(start, mine, end, first, base, offset, area)) graph.addNode(node);
        graph.addEdge(DroneProgramEdge.create(start.getId(), "next", mine.getId(), "in"));
        graph.addEdge(DroneProgramEdge.create(mine.getId(), "next", end.getId(), "in"));
        graph.addEdge(DroneProgramEdge.create(base.getId(), "value", offset.getId(), "base"));
        graph.addEdge(DroneProgramEdge.create(first.getId(), "value", area.getId(), "first"));
        graph.addEdge(DroneProgramEdge.create(offset.getId(), "value", area.getId(), "second"));
        graph.addEdge(DroneProgramEdge.create(area.getId(), "value", mine.getId(), "area"));
        CompiledDroneProgram compiled = new DroneProgramCompiler(DrTechDroneNodes.createDefaultRegistry())
                .compile(graph).getProgram().orElseThrow(AssertionError::new);
        RecordingEnvironment environment = new RecordingEnvironment();
        DroneProgramRuntime runtime = new DroneProgramRuntime(compiled, DrTechDroneExecutors.createDefaultRegistry(),
                DrTechDroneValueEvaluators.createDefaultRegistry(), environment);

        for (int tick = 0; tick < 5 && runtime.getStatus() != DroneRuntimeStatus.COMPLETED; tick++) runtime.tick();

        assertEquals(Arrays.asList(new BlockPos(5, 10, 5), new BlockPos(6, 10, 5)), environment.brokenTargets);
        assertEquals(DroneRuntimeStatus.COMPLETED, runtime.getStatus());
    }

    @Test
    void forEachCoordinateDrivesArbitraryCoordinateActionAndPersistsIndex() {
        DroneProgramGraph graph = new DroneProgramGraph("for each coordinate");
        DroneProgramNode start = DroneProgramNode.create(DrTechDroneNodes.START, 0, 0);
        DroneProgramNode loop = DroneProgramNode.create(DrTechDroneNodes.FOR_EACH_COORDINATE, 100, 0);
        DroneProgramNode action = DroneProgramNode.create(DrTechDroneNodes.BREAK_BLOCK_AT, 200, 0);
        DroneProgramNode end = DroneProgramNode.create(DrTechDroneNodes.END, 300, 0);
        DroneProgramNode area = area(3, 20, 7, 4, 20, 8);
        for (DroneProgramNode node : Arrays.asList(start, loop, action, end, area)) graph.addNode(node);
        graph.addEdge(DroneProgramEdge.create(start.getId(), "next", loop.getId(), "in"));
        graph.addEdge(DroneProgramEdge.create(loop.getId(), "body", action.getId(), "in"));
        graph.addEdge(DroneProgramEdge.create(action.getId(), "next", loop.getId(), "in"));
        graph.addEdge(DroneProgramEdge.create(loop.getId(), "done", end.getId(), "in"));
        graph.addEdge(DroneProgramEdge.create(area.getId(), "value", loop.getId(), "area"));
        graph.addEdge(DroneProgramEdge.create(loop.getId(), "coordinate", action.getId(), "target"));
        CompiledDroneProgram compiled = new DroneProgramCompiler(DrTechDroneNodes.createDefaultRegistry())
                .compile(graph).getProgram().orElseThrow(AssertionError::new);
        RecordingEnvironment firstEnvironment = new RecordingEnvironment();
        DroneProgramRuntime first = new DroneProgramRuntime(compiled, DrTechDroneExecutors.createDefaultRegistry(),
                DrTechDroneValueEvaluators.createDefaultRegistry(), firstEnvironment);

        first.tick();
        first.tick();
        NBTTagCompound saved = first.writeToNbt();
        RecordingEnvironment resumedEnvironment = new RecordingEnvironment();
        DroneProgramRuntime resumed = new DroneProgramRuntime(compiled, DrTechDroneExecutors.createDefaultRegistry(),
                DrTechDroneValueEvaluators.createDefaultRegistry(), resumedEnvironment);
        resumed.readFromNbt(saved);
        for (int tick = 0; tick < 10 && resumed.getStatus() != DroneRuntimeStatus.COMPLETED; tick++) resumed.tick();

        assertEquals(Arrays.asList(new BlockPos(3, 20, 7)), firstEnvironment.brokenTargets);
        assertEquals(Arrays.asList(new BlockPos(4, 20, 7), new BlockPos(4, 20, 8), new BlockPos(3, 20, 8)),
                resumedEnvironment.brokenTargets);
        assertEquals(DroneRuntimeStatus.COMPLETED, resumed.getStatus());
    }

    @Test
    void divisionByZeroBecomesRuntimeError() {
        DroneProgramGraph graph = new DroneProgramGraph("division error");
        DroneProgramNode start = DroneProgramNode.create(DrTechDroneNodes.START, 0, 0);
        DroneProgramNode set = configured(DrTechDroneNodes.SET_NUMBER_VARIABLE, "Name", "result");
        DroneProgramNode end = DroneProgramNode.create(DrTechDroneNodes.END, 200, 0);
        DroneProgramNode one = number(1.0D);
        DroneProgramNode zero = number(0.0D);
        DroneProgramNode math = configured(DrTechDroneNodes.NUMBER_MATH, "Operator", "/");
        for (DroneProgramNode node : Arrays.asList(start, set, end, one, zero, math)) graph.addNode(node);
        graph.addEdge(DroneProgramEdge.create(start.getId(), "next", set.getId(), "in"));
        graph.addEdge(DroneProgramEdge.create(set.getId(), "next", end.getId(), "in"));
        graph.addEdge(DroneProgramEdge.create(one.getId(), "value", math.getId(), "left"));
        graph.addEdge(DroneProgramEdge.create(zero.getId(), "value", math.getId(), "right"));
        graph.addEdge(DroneProgramEdge.create(math.getId(), "result", set.getId(), "value"));
        CompiledDroneProgram compiled = new DroneProgramCompiler(DrTechDroneNodes.createDefaultRegistry())
                .compile(graph).getProgram().orElseThrow(AssertionError::new);
        DroneProgramRuntime runtime = new DroneProgramRuntime(compiled, DrTechDroneExecutors.createDefaultRegistry());

        runtime.tick();

        assertEquals(DroneRuntimeStatus.ERROR, runtime.getStatus());
        assertTrue(runtime.getError().contains("Division by zero"));
    }

    @Test
    void routesSemanticInventoryFailureAndBranchesOnLastStatus() {
        DroneProgramGraph graph = new DroneProgramGraph("failure status");
        DroneProgramNode start = DroneProgramNode.create(DrTechDroneNodes.START, 0, 0);
        DroneProgramNode transfer = DroneProgramNode.create(DrTechDroneNodes.IMPORT_ITEMS, 100, 0);
        DroneProgramNode branch = DroneProgramNode.create(DrTechDroneNodes.BRANCH, 200, 0);
        DroneProgramNode successEnd = DroneProgramNode.create(DrTechDroneNodes.END, 300, 0);
        DroneProgramNode matchedEnd = DroneProgramNode.create(DrTechDroneNodes.END, 300, 80);
        DroneProgramNode otherEnd = DroneProgramNode.create(DrTechDroneNodes.END, 300, 160);
        DroneProgramNode target = coordinate(1, 2, 3);
        DroneProgramNode lastStatus = DroneProgramNode.create(DrTechDroneNodes.LAST_ACTION_STATUS, 100, 100);
        DroneProgramNode compare = configured(DrTechDroneNodes.COMPARE_ACTION_STATUS, "Status", "NOT_FOUND");
        for (DroneProgramNode node : Arrays.asList(start, transfer, branch, successEnd, matchedEnd, otherEnd, target,
                lastStatus, compare)) graph.addNode(node);
        graph.addEdge(DroneProgramEdge.create(start.getId(), "next", transfer.getId(), "in"));
        graph.addEdge(DroneProgramEdge.create(transfer.getId(), "next", successEnd.getId(), "in"));
        graph.addEdge(DroneProgramEdge.create(transfer.getId(), "failed", branch.getId(), "in"));
        graph.addEdge(DroneProgramEdge.create(target.getId(), "value", transfer.getId(), "target"));
        graph.addEdge(DroneProgramEdge.create(lastStatus.getId(), "value", compare.getId(), "status"));
        graph.addEdge(DroneProgramEdge.create(compare.getId(), "result", branch.getId(), "condition"));
        graph.addEdge(DroneProgramEdge.create(branch.getId(), "true", matchedEnd.getId(), "in"));
        graph.addEdge(DroneProgramEdge.create(branch.getId(), "false", otherEnd.getId(), "in"));
        CompiledDroneProgram compiled = new DroneProgramCompiler(DrTechDroneNodes.createDefaultRegistry())
                .compile(graph).getProgram().orElseThrow(AssertionError::new);
        RecordingEnvironment environment = new RecordingEnvironment();
        environment.importFailure = true;
        DroneProgramRuntime runtime = new DroneProgramRuntime(compiled, DrTechDroneExecutors.createDefaultRegistry(),
                DrTechDroneValueEvaluators.createDefaultRegistry(), environment);

        runtime.tick();

        assertEquals(DroneRuntimeStatus.COMPLETED, runtime.getStatus());
        assertEquals(matchedEnd.getId(), runtime.getCurrentNodeId());
    }

    @Test
    void dispatchesEmptyHandAndFilteredItemBlockInteractions() {
        DroneProgramGraph graph = new DroneProgramGraph("block interactions");
        DroneProgramNode start = DroneProgramNode.create(DrTechDroneNodes.START, 0, 0);
        NBTTagCompound emptyConfig = new NBTTagCompound();
        emptyConfig.setString("Direction", "NORTH");
        DroneProgramNode emptyHand = DroneProgramNode.create(DrTechDroneNodes.INTERACT_BLOCK, 100, 0)
                .withConfiguration(emptyConfig);
        NBTTagCompound heldConfig = new NBTTagCompound();
        heldConfig.setString("Direction", "UP");
        heldConfig.setBoolean("Sneaking", true);
        DroneProgramNode heldItem = DroneProgramNode.create(DrTechDroneNodes.USE_ITEM_ON_BLOCK, 200, 0)
                .withConfiguration(heldConfig);
        DroneProgramNode end = DroneProgramNode.create(DrTechDroneNodes.END, 300, 0);
        DroneProgramNode firstTarget = coordinate(2, 4, 6);
        DroneProgramNode secondTarget = coordinate(3, 5, 7);
        NBTTagCompound filterConfig = new NBTTagCompound();
        filterConfig.setString("Item", "minecraft:stone");
        filterConfig.setInteger("Meta", 0);
        DroneProgramNode filter = DroneProgramNode.create(DrTechDroneNodes.ITEM_FILTER, 100, 100)
                .withConfiguration(filterConfig);
        for (DroneProgramNode node : Arrays.asList(start, emptyHand, heldItem, end, firstTarget, secondTarget, filter)) {
            graph.addNode(node);
        }
        graph.addEdge(DroneProgramEdge.create(start.getId(), "next", emptyHand.getId(), "in"));
        graph.addEdge(DroneProgramEdge.create(emptyHand.getId(), "next", heldItem.getId(), "in"));
        graph.addEdge(DroneProgramEdge.create(heldItem.getId(), "next", end.getId(), "in"));
        graph.addEdge(DroneProgramEdge.create(firstTarget.getId(), "value", emptyHand.getId(), "target"));
        graph.addEdge(DroneProgramEdge.create(secondTarget.getId(), "value", heldItem.getId(), "target"));
        graph.addEdge(DroneProgramEdge.create(filter.getId(), "filter", heldItem.getId(), "filter"));
        CompiledDroneProgram compiled = new DroneProgramCompiler(DrTechDroneNodes.createDefaultRegistry())
                .compile(graph).getProgram().orElseThrow(AssertionError::new);
        RecordingEnvironment environment = new RecordingEnvironment();
        DroneProgramRuntime runtime = new DroneProgramRuntime(compiled, DrTechDroneExecutors.createDefaultRegistry(),
                DrTechDroneValueEvaluators.createDefaultRegistry(), environment);

        runtime.tick();

        assertEquals(DroneRuntimeStatus.COMPLETED, runtime.getStatus());
        assertEquals(2, environment.interactions.size());
        assertEquals(new BlockPos(2, 4, 6), environment.interactions.get(0).getTarget());
        assertEquals(EnumFacing.NORTH, environment.interactions.get(0).getSide());
        assertEquals(false, environment.interactions.get(0).isUseHeldItem());
        assertEquals(new BlockPos(3, 5, 7), environment.interactions.get(1).getTarget());
        assertEquals(EnumFacing.UP, environment.interactions.get(1).getSide());
        assertEquals(true, environment.interactions.get(1).isUseHeldItem());
        assertEquals(true, environment.interactions.get(1).isSneaking());
        assertEquals("minecraft:stone", environment.interactions.get(1).getHeldItemFilter().getRules()
                .get(0).getItemId().toString());
    }

    @Test
    void dispatchesPickupDropAndMatureCropHarvestActions() {
        DroneProgramGraph graph = new DroneProgramGraph("world item actions");
        DroneProgramNode start = DroneProgramNode.create(DrTechDroneNodes.START, 0, 0);
        NBTTagCompound pickupConfig = new NBTTagCompound();
        pickupConfig.setDouble("Radius", 5.0D);
        pickupConfig.setInteger("MaxAmount", 12);
        DroneProgramNode pickup = DroneProgramNode.create(DrTechDroneNodes.PICKUP_DROPPED_ITEMS, 100, 0)
                .withConfiguration(pickupConfig);
        NBTTagCompound dropConfig = new NBTTagCompound();
        dropConfig.setInteger("MaxAmount", 7);
        DroneProgramNode drop = DroneProgramNode.create(DrTechDroneNodes.DROP_ITEMS, 200, 0)
                .withConfiguration(dropConfig);
        DroneProgramNode harvest = DroneProgramNode.create(DrTechDroneNodes.HARVEST_CROP, 300, 0);
        DroneProgramNode end = DroneProgramNode.create(DrTechDroneNodes.END, 400, 0);
        DroneProgramNode target = coordinate(9, 64, 9);
        for (DroneProgramNode node : Arrays.asList(start, pickup, drop, harvest, end, target)) graph.addNode(node);
        graph.addEdge(DroneProgramEdge.create(start.getId(), "next", pickup.getId(), "in"));
        graph.addEdge(DroneProgramEdge.create(pickup.getId(), "next", drop.getId(), "in"));
        graph.addEdge(DroneProgramEdge.create(drop.getId(), "next", harvest.getId(), "in"));
        graph.addEdge(DroneProgramEdge.create(harvest.getId(), "next", end.getId(), "in"));
        graph.addEdge(DroneProgramEdge.create(target.getId(), "value", harvest.getId(), "target"));
        CompiledDroneProgram compiled = new DroneProgramCompiler(DrTechDroneNodes.createDefaultRegistry())
                .compile(graph).getProgram().orElseThrow(AssertionError::new);
        RecordingEnvironment environment = new RecordingEnvironment();
        DroneProgramRuntime runtime = new DroneProgramRuntime(compiled, DrTechDroneExecutors.createDefaultRegistry(),
                DrTechDroneValueEvaluators.createDefaultRegistry(), environment);

        runtime.tick();

        assertEquals(DroneRuntimeStatus.COMPLETED, runtime.getStatus());
        assertEquals(5.0D, environment.pickupRequest.getRadius());
        assertEquals(12, environment.pickupRequest.getMaximumAmount());
        assertEquals(7, environment.dropRequest.getMaximumAmount());
        assertEquals(new BlockPos(9, 64, 9), environment.harvestTarget);
    }

    @Test
    void dispatchesClampedProgrammableRedstoneOutput() {
        DroneProgramGraph graph = new DroneProgramGraph("redstone output");
        DroneProgramNode start = DroneProgramNode.create(DrTechDroneNodes.START, 0, 0);
        NBTTagCompound outputConfig = new NBTTagCompound();
        outputConfig.setInteger("Strength", 4);
        DroneProgramNode output = DroneProgramNode.create(DrTechDroneNodes.SET_REDSTONE_OUTPUT, 100, 0)
                .withConfiguration(outputConfig);
        DroneProgramNode end = DroneProgramNode.create(DrTechDroneNodes.END, 200, 0);
        DroneProgramNode target = coordinate(14, 65, -2);
        DroneProgramNode strength = number(99.0D);
        for (DroneProgramNode node : Arrays.asList(start, output, end, target, strength)) graph.addNode(node);
        graph.addEdge(DroneProgramEdge.create(start.getId(), "next", output.getId(), "in"));
        graph.addEdge(DroneProgramEdge.create(output.getId(), "next", end.getId(), "in"));
        graph.addEdge(DroneProgramEdge.create(target.getId(), "value", output.getId(), "target"));
        graph.addEdge(DroneProgramEdge.create(strength.getId(), "value", output.getId(), "strength"));
        CompiledDroneProgram compiled = new DroneProgramCompiler(DrTechDroneNodes.createDefaultRegistry())
                .compile(graph).getProgram().orElseThrow(AssertionError::new);
        RecordingEnvironment environment = new RecordingEnvironment();
        DroneProgramRuntime runtime = new DroneProgramRuntime(compiled, DrTechDroneExecutors.createDefaultRegistry(),
                DrTechDroneValueEvaluators.createDefaultRegistry(), environment);

        runtime.tick();

        assertEquals(DroneRuntimeStatus.COMPLETED, runtime.getStatus());
        assertEquals(new BlockPos(14, 65, -2), environment.redstoneOutputTarget);
        assertEquals(15, environment.redstoneOutputStrength);
    }

    @Test
    void searchesAreaAndExposesPersistedTransferAmountAsNumberOutput() {
        DroneProgramGraph graph = new DroneProgramGraph("area transfer output");
        DroneProgramNode start = DroneProgramNode.create(DrTechDroneNodes.START, 0, 0);
        NBTTagCompound transferConfig = new NBTTagCompound();
        transferConfig.setInteger("MaxAmount", 20);
        transferConfig.setInteger("BatchSize", 5);
        transferConfig.setString("Direction", "DOWN");
        transferConfig.setString("SearchMode", "NEAREST");
        transferConfig.setBoolean("SkipUnavailable", true);
        DroneProgramNode transfer = DroneProgramNode.create(DrTechDroneNodes.IMPORT_ITEMS, 100, 0)
                .withConfiguration(transferConfig);
        DroneProgramNode set = configured(DrTechDroneNodes.SET_NUMBER_VARIABLE, "Name", "moved");
        DroneProgramNode end = DroneProgramNode.create(DrTechDroneNodes.END, 300, 0);
        DroneProgramNode area = area(0, 64, 0, 2, 64, 2);
        for (DroneProgramNode node : Arrays.asList(start, transfer, set, end, area)) graph.addNode(node);
        graph.addEdge(DroneProgramEdge.create(start.getId(), "next", transfer.getId(), "in"));
        graph.addEdge(DroneProgramEdge.create(transfer.getId(), "next", set.getId(), "in"));
        graph.addEdge(DroneProgramEdge.create(set.getId(), "next", end.getId(), "in"));
        graph.addEdge(DroneProgramEdge.create(area.getId(), "value", transfer.getId(), "area"));
        graph.addEdge(DroneProgramEdge.create(transfer.getId(), "amount", set.getId(), "value"));
        CompiledDroneProgram compiled = new DroneProgramCompiler(DrTechDroneNodes.createDefaultRegistry())
                .compile(graph).getProgram().orElseThrow(AssertionError::new);
        RecordingEnvironment environment = new RecordingEnvironment();
        environment.incrementalAreaImport = true;
        DroneProgramRuntime runtime = new DroneProgramRuntime(compiled, DrTechDroneExecutors.createDefaultRegistry(),
                DrTechDroneValueEvaluators.createDefaultRegistry(), environment);

        runtime.tick();
        assertEquals(DroneRuntimeStatus.RUNNING, runtime.getStatus());
        runtime.tick();

        assertEquals(DroneRuntimeStatus.COMPLETED, runtime.getStatus());
        assertEquals(5.0D, runtime.getNumberVariable("moved"));
        assertEquals(5L, runtime.getActionAmount(transfer.getId()));
        assertEquals(DroneSearchMode.NEAREST, environment.importRequest.getSearchMode());
        assertEquals(true, environment.importRequest.isSkipUnavailable());
        assertEquals(9L, environment.importRequest.getArea().getVolume());

        DroneProgramRuntime restored = new DroneProgramRuntime(compiled, DrTechDroneExecutors.createDefaultRegistry());
        restored.readFromNbt(runtime.writeToNbt());
        assertEquals(5L, restored.getActionAmount(transfer.getId()));
    }

    private static DroneProgramNode coordinate(int x, int y, int z) {
        NBTTagCompound configuration = new NBTTagCompound();
        configuration.setInteger("X", x);
        configuration.setInteger("Y", y);
        configuration.setInteger("Z", z);
        return DroneProgramNode.create(DrTechDroneNodes.COORDINATE, 0, 80).withConfiguration(configuration);
    }

    private static DroneProgramNode area(int x1, int y1, int z1, int x2, int y2, int z2) {
        NBTTagCompound configuration = new NBTTagCompound();
        configuration.setInteger("X1", x1);
        configuration.setInteger("Y1", y1);
        configuration.setInteger("Z1", z1);
        configuration.setInteger("X2", x2);
        configuration.setInteger("Y2", y2);
        configuration.setInteger("Z2", z2);
        return DroneProgramNode.create(DrTechDroneNodes.AREA, 0, 80).withConfiguration(configuration);
    }

    private static DroneProgramNode number(double value) {
        NBTTagCompound configuration = new NBTTagCompound();
        configuration.setDouble("Value", value);
        return DroneProgramNode.create(DrTechDroneNodes.NUMBER, 0, 100).withConfiguration(configuration);
    }

    private static DroneProgramNode bool(boolean value) {
        NBTTagCompound configuration = new NBTTagCompound();
        configuration.setBoolean("Value", value);
        return DroneProgramNode.create(DrTechDroneNodes.BOOLEAN, 0, 100).withConfiguration(configuration);
    }

    private static DroneProgramNode configured(net.minecraft.util.ResourceLocation type, String key, String value) {
        NBTTagCompound configuration = new NBTTagCompound();
        configuration.setString(key, value);
        return DroneProgramNode.create(type, 0, 0).withConfiguration(configuration);
    }

    private static ProgramFixture waitProgram(int ticks) {
        DroneProgramGraph graph = new DroneProgramGraph("wait");
        DroneProgramNode start = DroneProgramNode.create(DrTechDroneNodes.START, 0, 0);
        NBTTagCompound waitConfig = new NBTTagCompound();
        waitConfig.setInteger("Ticks", ticks);
        DroneProgramNode wait = DroneProgramNode.create(DrTechDroneNodes.WAIT, 100, 0).withConfiguration(waitConfig);
        DroneProgramNode end = DroneProgramNode.create(DrTechDroneNodes.END, 200, 0);
        graph.addNode(start);
        graph.addNode(wait);
        graph.addNode(end);
        graph.addEdge(DroneProgramEdge.create(start.getId(), "next", wait.getId(), "in"));
        graph.addEdge(DroneProgramEdge.create(wait.getId(), "next", end.getId(), "in"));
        CompiledDroneProgram compiled = new DroneProgramCompiler(DrTechDroneNodes.createDefaultRegistry())
                .compile(graph).getProgram().orElseThrow(AssertionError::new);
        return new ProgramFixture(compiled, wait, end,
                new DroneProgramRuntime(compiled, DrTechDroneExecutors.createDefaultRegistry()));
    }

    private static final class ProgramFixture {
        private final CompiledDroneProgram program;
        private final DroneProgramNode waitNode;
        private final DroneProgramNode endNode;
        private final DroneProgramRuntime runtime;

        private ProgramFixture(CompiledDroneProgram program, DroneProgramNode waitNode, DroneProgramNode endNode,
                DroneProgramRuntime runtime) {
            this.program = program;
            this.waitNode = waitNode;
            this.endNode = endNode;
            this.runtime = runtime;
        }
    }

    private static final class RecordingEnvironment implements DroneRuntimeEnvironment {
        private BlockPos broken;
        private BlockPos placed;
        private BlockPos dock;
        private double requestedChargePercent;
        private int chargeTicks;
        private BlockPos importTarget;
        private BlockPos exportTarget;
        private DroneItemFilter itemFilter;
        private DroneTransferRequest importRequest;
        private DroneTransferRequest exportRequest;
        private final List<BlockPos> brokenTargets = new ArrayList<>();
        private final List<BlockPos> placedTargets = new ArrayList<>();
        private boolean redstonePowered;
        private boolean ownerNearby;
        private double ownerRadius;
        private boolean importFailure;
        private final List<DroneInteractionRequest> interactions = new ArrayList<>();
        private DroneItemWorldRequest pickupRequest;
        private DroneItemWorldRequest dropRequest;
        private BlockPos harvestTarget;
        private BlockPos redstoneOutputTarget;
        private int redstoneOutputStrength = -1;
        private boolean incrementalAreaImport;
        private int areaImportCalls;
        private BlockPos boundDock;
        private boolean unboundDock;
        private int returnAtPercent;
        private int resumeAtPercent;
        private BlockPos importEuTarget;
        private BlockPos exportEuTarget;
        private long importedEu;
        private long exportedEu;
        private double targetChargePercent;
        private int chargeTargetTicks;

        @Override
        public double getEnergyPercent() {
            return 100.0D;
        }

        @Override
        public DroneExecutionResult moveTo(BlockPos target) {
            return DroneExecutionResult.success();
        }

        @Override
        public DroneExecutionResult breakBlock(BlockPos target) {
            broken = target;
            brokenTargets.add(target);
            return DroneExecutionResult.success();
        }

        @Override
        public DroneExecutionResult placeBlock(BlockPos target) {
            placed = target;
            return DroneExecutionResult.success();
        }

        @Override
        public DroneExecutionResult placeBlockInArea(BlockPos target, DroneItemFilter filter) {
            placedTargets.add(target);
            return DroneExecutionResult.success();
        }

        @Override
        public DroneExecutionResult returnToDock(BlockPos target) {
            dock = target;
            return DroneExecutionResult.success();
        }

        @Override
        public DroneExecutionResult chargeUntil(double percent) {
            requestedChargePercent = percent;
            return ++chargeTicks >= 2 ? DroneExecutionResult.success() : DroneExecutionResult.running();
        }

        @Override
        public DroneExecutionResult bindDock(BlockPos target) {
            boundDock = target;
            return DroneExecutionResult.success();
        }

        @Override
        public DroneExecutionResult unbindDock() {
            unboundDock = true;
            return DroneExecutionResult.success();
        }

        @Override
        public DroneExecutionResult configureSafety(int returnAtPercent, int resumeAtPercent) {
            this.returnAtPercent = returnAtPercent;
            this.resumeAtPercent = resumeAtPercent;
            return DroneExecutionResult.success();
        }

        @Override
        public DroneExecutionResult importEnergy(BlockPos target, long maximumEu) {
            importEuTarget = target;
            importedEu = Math.min(128L, maximumEu);
            return DroneExecutionResult.success(importedEu);
        }

        @Override
        public DroneExecutionResult exportEnergy(BlockPos target, long maximumEu) {
            exportEuTarget = target;
            exportedEu = Math.min(64L, maximumEu);
            return DroneExecutionResult.success(exportedEu);
        }

        @Override
        public DroneExecutionResult chargeTargetUntil(BlockPos target, double percent, long maximumEu) {
            targetChargePercent = percent;
            return ++chargeTargetTicks < 2 ? DroneExecutionResult.running(32L) : DroneExecutionResult.success(32L);
        }

        @Override
        public DroneExecutionResult importItems(BlockPos target, DroneItemFilter filter) {
            importTarget = target;
            itemFilter = filter;
            return DroneExecutionResult.success();
        }

        @Override
        public DroneExecutionResult exportItems(BlockPos target, DroneItemFilter filter) {
            exportTarget = target;
            itemFilter = filter;
            return DroneExecutionResult.success();
        }

        @Override
        public DroneExecutionResult importItems(DroneTransferRequest request) {
            importRequest = request;
            if (importFailure) return DroneExecutionResult.failure(DroneActionStatus.NOT_FOUND, "failed",
                    "Inventory is missing");
            if (incrementalAreaImport && request.getArea() != null) {
                return areaImportCalls++ == 0 ? DroneExecutionResult.running(5L) : DroneExecutionResult.success();
            }
            return importItems(request.getTarget(), DroneItemFilter.fromSpec(request.getFilter()));
        }

        @Override
        public DroneExecutionResult exportItems(DroneTransferRequest request) {
            exportRequest = request;
            return exportItems(request.getTarget(), DroneItemFilter.fromSpec(request.getFilter()));
        }

        @Override
        public boolean isRedstonePowered(BlockPos target) {
            return redstonePowered;
        }

        @Override
        public boolean isOwnerWithin(double radius) {
            ownerRadius = radius;
            return ownerNearby;
        }

        @Override
        public DroneExecutionResult interactBlock(DroneInteractionRequest request) {
            interactions.add(request);
            return DroneExecutionResult.success();
        }

        @Override
        public DroneExecutionResult pickupDroppedItems(DroneItemWorldRequest request) {
            pickupRequest = request;
            return DroneExecutionResult.success(3L);
        }

        @Override
        public DroneExecutionResult dropItems(DroneItemWorldRequest request) {
            dropRequest = request;
            return DroneExecutionResult.success(2L);
        }

        @Override
        public DroneExecutionResult harvestCrop(BlockPos target) {
            harvestTarget = target;
            return DroneExecutionResult.success();
        }

        @Override
        public DroneExecutionResult setRedstoneOutput(BlockPos target, int strength) {
            redstoneOutputTarget = target;
            redstoneOutputStrength = strength;
            return DroneExecutionResult.success();
        }
    }
}
