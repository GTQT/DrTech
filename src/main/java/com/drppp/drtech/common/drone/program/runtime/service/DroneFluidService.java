package com.drppp.drtech.common.drone.program.runtime.service;

import com.drppp.drtech.common.drone.program.runtime.DroneExecutionResult;
import com.drppp.drtech.common.drone.filter.DroneFluidFilterSpec;
import com.drppp.drtech.common.drone.program.model.DroneArea;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;

public interface DroneFluidService {
    default DroneExecutionResult importFluid(BlockPos target, @Nullable EnumFacing side, int maximumAmount,
            DroneFluidFilterSpec filter) {
        return DroneExecutionResult.error("Fluid import is unavailable in this runtime environment");
    }

    default DroneExecutionResult exportFluid(BlockPos target, @Nullable EnumFacing side, int maximumAmount,
            DroneFluidFilterSpec filter) {
        return DroneExecutionResult.error("Fluid export is unavailable in this runtime environment");
    }

    default DroneExecutionResult drainFluid(int maximumAmount, DroneFluidFilterSpec filter) {
        return DroneExecutionResult.error("Fluid drain is unavailable in this runtime environment");
    }

    default int getDroneFluidAmount(DroneFluidFilterSpec filter) { return 0; }
    default int getDroneFluidCapacity() { return 0; }
    default int getContainerFluidAmount(BlockPos target, @Nullable EnumFacing side,
            DroneFluidFilterSpec filter) { return 0; }

    /** Returns the nearest loaded matching fluid container, without forcing chunks to load. */
    @Nullable
    default BlockPos findFluidContainer(DroneArea area, @Nullable EnumFacing side,
            DroneFluidFilterSpec filter, int minimumAmount) { return null; }
}
