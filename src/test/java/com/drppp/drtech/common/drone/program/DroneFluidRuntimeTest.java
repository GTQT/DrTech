package com.drppp.drtech.common.drone.program;

import com.drppp.drtech.common.drone.filter.DroneFluidFilterSpec;
import com.drppp.drtech.common.drone.program.compile.CompiledDroneProgram;
import com.drppp.drtech.common.drone.program.compile.DroneCompileResult;
import com.drppp.drtech.common.drone.program.compile.DroneProgramCompiler;
import com.drppp.drtech.common.drone.program.model.DroneProgramEdge;
import com.drppp.drtech.common.drone.program.model.DroneProgramGraph;
import com.drppp.drtech.common.drone.program.model.DroneProgramNode;
import com.drppp.drtech.common.drone.program.model.DroneArea;
import com.drppp.drtech.common.drone.program.registry.DrTechDroneNodes;
import com.drppp.drtech.common.drone.program.runtime.DrTechDroneExecutors;
import com.drppp.drtech.common.drone.program.runtime.DrTechDroneValueEvaluators;
import com.drppp.drtech.common.drone.program.runtime.DroneExecutionResult;
import com.drppp.drtech.common.drone.program.runtime.DroneProgramRuntime;
import com.drppp.drtech.common.drone.program.runtime.DroneRuntimeEnvironment;
import com.drppp.drtech.common.drone.program.runtime.DroneRuntimeStatus;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.Test;

import javax.annotation.Nullable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DroneFluidRuntimeTest {

    @Test
    void dispatchesFilteredFluidImportExportAndDrain() {
        DroneProgramGraph graph = new DroneProgramGraph("fluid logistics");
        DroneProgramNode start = DroneProgramNode.create(DrTechDroneNodes.START, 0, 0);
        DroneProgramNode importFluid = action(DrTechDroneNodes.IMPORT_FLUID, "DOWN");
        DroneProgramNode exportFluid = action(DrTechDroneNodes.EXPORT_FLUID, "UP");
        DroneProgramNode drainFluid = action(DrTechDroneNodes.DRAIN_FLUID, "AUTO");
        DroneProgramNode end = DroneProgramNode.create(DrTechDroneNodes.END, 400, 0);
        DroneProgramNode source = coordinate(1, 64, 2);
        DroneProgramNode destination = coordinate(8, 64, 9);
        NBTTagCompound filterConfig = new NBTTagCompound();
        filterConfig.setString("Fluid", "water");
        filterConfig.setString("Mode", "WHITELIST");
        DroneProgramNode importFilter = DroneProgramNode.create(DrTechDroneNodes.FLUID_FILTER, 0, 100)
                .withConfiguration(filterConfig);
        DroneProgramNode exportFilter = DroneProgramNode.create(DrTechDroneNodes.FLUID_FILTER, 100, 100)
                .withConfiguration(filterConfig);
        DroneProgramNode drainFilter = DroneProgramNode.create(DrTechDroneNodes.FLUID_FILTER, 200, 100)
                .withConfiguration(filterConfig);
        for (DroneProgramNode node : new DroneProgramNode[] {
                start, importFluid, exportFluid, drainFluid, end, source, destination,
                importFilter, exportFilter, drainFilter }) graph.addNode(node);
        graph.addEdge(DroneProgramEdge.create(start.getId(), "next", importFluid.getId(), "in"));
        graph.addEdge(DroneProgramEdge.create(importFluid.getId(), "next", exportFluid.getId(), "in"));
        graph.addEdge(DroneProgramEdge.create(exportFluid.getId(), "next", drainFluid.getId(), "in"));
        graph.addEdge(DroneProgramEdge.create(drainFluid.getId(), "next", end.getId(), "in"));
        graph.addEdge(DroneProgramEdge.create(source.getId(), "value", importFluid.getId(), "target"));
        graph.addEdge(DroneProgramEdge.create(destination.getId(), "value", exportFluid.getId(), "target"));
        graph.addEdge(DroneProgramEdge.create(importFilter.getId(), "filter", importFluid.getId(), "filter"));
        graph.addEdge(DroneProgramEdge.create(exportFilter.getId(), "filter", exportFluid.getId(), "filter"));
        graph.addEdge(DroneProgramEdge.create(drainFilter.getId(), "filter", drainFluid.getId(), "filter"));

        DroneCompileResult compileResult = new DroneProgramCompiler(DrTechDroneNodes.createDefaultRegistry())
                .compile(graph);
        assertFalse(compileResult.hasErrors(), () -> compileResult.getDiagnostics().toString());
        CompiledDroneProgram compiled = compileResult.getProgram().orElseThrow(AssertionError::new);
        RecordingFluidEnvironment environment = new RecordingFluidEnvironment();
        DroneProgramRuntime runtime = new DroneProgramRuntime(compiled, DrTechDroneExecutors.createDefaultRegistry(),
                DrTechDroneValueEvaluators.createDefaultRegistry(), environment);
        runtime.tick();

        assertEquals(DroneRuntimeStatus.COMPLETED, runtime.getStatus());
        assertEquals(new BlockPos(1, 64, 2), environment.importTarget);
        assertEquals(new BlockPos(8, 64, 9), environment.exportTarget);
        assertEquals(EnumFacing.DOWN, environment.importSide);
        assertEquals(EnumFacing.UP, environment.exportSide);
        assertEquals(1_000, environment.drained);
        assertTrue(environment.filter.getFluidNames().contains("water"));
    }

    @Test
    void findsContainerAndPersistsTargetWhileWaitingForFluid() {
        DroneProgramGraph graph = new DroneProgramGraph("fluid discovery");
        DroneProgramNode start = DroneProgramNode.create(DrTechDroneNodes.START, 0, 0);
        NBTTagCompound findConfig = new NBTTagCompound();
        findConfig.setString("Direction", "DOWN");
        findConfig.setInteger("MinimumAmount", 500);
        DroneProgramNode find = DroneProgramNode.create(DrTechDroneNodes.FIND_FLUID_CONTAINER, 100, 0)
                .withConfiguration(findConfig);
        NBTTagCompound waitConfig = new NBTTagCompound();
        waitConfig.setString("Direction", "DOWN");
        waitConfig.setInteger("Amount", 1_000);
        waitConfig.setString("Operator", "AT_LEAST");
        DroneProgramNode wait = DroneProgramNode.create(DrTechDroneNodes.WAIT_FOR_FLUID_AMOUNT, 200, 0)
                .withConfiguration(waitConfig);
        DroneProgramNode end = DroneProgramNode.create(DrTechDroneNodes.END, 300, 0);
        DroneProgramNode area = area(0, 60, 0, 10, 70, 10);
        NBTTagCompound filterConfig = new NBTTagCompound();
        filterConfig.setString("Fluid", "water");
        filterConfig.setString("Mode", "WHITELIST");
        DroneProgramNode findFilter = DroneProgramNode.create(DrTechDroneNodes.FLUID_FILTER, 50, 100)
                .withConfiguration(filterConfig);
        DroneProgramNode waitFilter = DroneProgramNode.create(DrTechDroneNodes.FLUID_FILTER, 150, 100)
                .withConfiguration(filterConfig);
        for (DroneProgramNode node : new DroneProgramNode[] {
                start, find, wait, end, area, findFilter, waitFilter }) graph.addNode(node);
        graph.addEdge(DroneProgramEdge.create(start.getId(), "next", find.getId(), "in"));
        graph.addEdge(DroneProgramEdge.create(find.getId(), "next", wait.getId(), "in"));
        graph.addEdge(DroneProgramEdge.create(wait.getId(), "next", end.getId(), "in"));
        graph.addEdge(DroneProgramEdge.create(area.getId(), "value", find.getId(), "area"));
        graph.addEdge(DroneProgramEdge.create(findFilter.getId(), "filter", find.getId(), "filter"));
        graph.addEdge(DroneProgramEdge.create(find.getId(), "target", wait.getId(), "target"));
        graph.addEdge(DroneProgramEdge.create(waitFilter.getId(), "filter", wait.getId(), "filter"));

        DroneCompileResult compileResult = new DroneProgramCompiler(DrTechDroneNodes.createDefaultRegistry())
                .compile(graph);
        assertFalse(compileResult.hasErrors(), () -> compileResult.getDiagnostics().toString());
        CompiledDroneProgram compiled = compileResult.getProgram().orElseThrow(AssertionError::new);
        RecordingFluidEnvironment environment = new RecordingFluidEnvironment();
        environment.foundTarget = new BlockPos(4, 64, 6);
        environment.containerAmount = 999;
        DroneProgramRuntime runtime = new DroneProgramRuntime(compiled, DrTechDroneExecutors.createDefaultRegistry(),
                DrTechDroneValueEvaluators.createDefaultRegistry(), environment);
        runtime.tick();

        assertEquals(DroneRuntimeStatus.RUNNING, runtime.getStatus());
        assertEquals(500, environment.minimumAmount);
        assertEquals(EnumFacing.DOWN, environment.findSide);
        assertEquals(environment.foundTarget, environment.amountTarget);
        assertTrue(environment.findFilter.getFluidNames().contains("water"));

        NBTTagCompound savedRuntime = runtime.writeToNbt();
        RecordingFluidEnvironment resumedEnvironment = new RecordingFluidEnvironment();
        resumedEnvironment.containerAmount = 1_000;
        DroneProgramRuntime resumed = new DroneProgramRuntime(compiled, DrTechDroneExecutors.createDefaultRegistry(),
                DrTechDroneValueEvaluators.createDefaultRegistry(), resumedEnvironment);
        resumed.readFromNbt(savedRuntime);
        resumed.tick();

        assertEquals(DroneRuntimeStatus.COMPLETED, resumed.getStatus());
        assertEquals(environment.foundTarget, resumedEnvironment.amountTarget,
                "the discovered target must survive runtime serialization");
    }

    private static DroneProgramNode action(net.minecraft.util.ResourceLocation type, String direction) {
        NBTTagCompound config = new NBTTagCompound();
        config.setString("Direction", direction);
        config.setInteger("MaxAmount", 1_000);
        return DroneProgramNode.create(type, 100, 0).withConfiguration(config);
    }

    private static DroneProgramNode coordinate(int x, int y, int z) {
        NBTTagCompound config = new NBTTagCompound();
        config.setInteger("X", x);
        config.setInteger("Y", y);
        config.setInteger("Z", z);
        return DroneProgramNode.create(DrTechDroneNodes.COORDINATE, 0, 80).withConfiguration(config);
    }

    private static DroneProgramNode area(int x1, int y1, int z1, int x2, int y2, int z2) {
        NBTTagCompound config = new NBTTagCompound();
        config.setInteger("X1", x1);
        config.setInteger("Y1", y1);
        config.setInteger("Z1", z1);
        config.setInteger("X2", x2);
        config.setInteger("Y2", y2);
        config.setInteger("Z2", z2);
        return DroneProgramNode.create(DrTechDroneNodes.AREA, 0, 120).withConfiguration(config);
    }

    private static final class RecordingFluidEnvironment implements DroneRuntimeEnvironment {
        private BlockPos importTarget;
        private BlockPos exportTarget;
        private EnumFacing importSide;
        private EnumFacing exportSide;
        private DroneFluidFilterSpec filter;
        private int drained;
        private BlockPos foundTarget;
        private BlockPos amountTarget;
        private EnumFacing findSide;
        private DroneFluidFilterSpec findFilter;
        private int minimumAmount;
        private int containerAmount;

        @Override
        public double getEnergyPercent() { return 100.0D; }

        @Override
        public DroneExecutionResult moveTo(BlockPos target) { return DroneExecutionResult.success(); }

        @Override
        public DroneExecutionResult importFluid(BlockPos target, @Nullable EnumFacing side, int maximumAmount,
                DroneFluidFilterSpec filter) {
            importTarget = target;
            importSide = side;
            this.filter = filter;
            return DroneExecutionResult.success(maximumAmount);
        }

        @Override
        public DroneExecutionResult exportFluid(BlockPos target, @Nullable EnumFacing side, int maximumAmount,
                DroneFluidFilterSpec filter) {
            exportTarget = target;
            exportSide = side;
            this.filter = filter;
            return DroneExecutionResult.success(maximumAmount);
        }

        @Override
        public DroneExecutionResult drainFluid(int maximumAmount, DroneFluidFilterSpec filter) {
            drained = maximumAmount;
            this.filter = filter;
            return DroneExecutionResult.success(maximumAmount);
        }

        @Override
        @Nullable
        public BlockPos findFluidContainer(DroneArea area, @Nullable EnumFacing side,
                DroneFluidFilterSpec filter, int minimumAmount) {
            this.findSide = side;
            this.findFilter = filter;
            this.minimumAmount = minimumAmount;
            return foundTarget;
        }

        @Override
        public int getContainerFluidAmount(BlockPos target, @Nullable EnumFacing side,
                DroneFluidFilterSpec filter) {
            amountTarget = target;
            return containerAmount;
        }
    }
}
