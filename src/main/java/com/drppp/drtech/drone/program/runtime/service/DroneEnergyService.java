package com.drppp.drtech.drone.program.runtime.service;

import com.drppp.drtech.drone.program.runtime.DroneExecutionResult;
import net.minecraft.util.math.BlockPos;

public interface DroneEnergyService {
    double getEnergyPercent();

    default long getTargetEnergy(BlockPos target) {
        return -1L;
    }

    default long getTargetEnergyCapacity(BlockPos target) {
        return -1L;
    }

    default double getTargetEnergyPercent(BlockPos target) {
        long capacity = getTargetEnergyCapacity(target);
        long stored = getTargetEnergy(target);
        return capacity <= 0L || stored < 0L ? -1.0D : stored * 100.0D / capacity;
    }

    default DroneExecutionResult importEnergy(BlockPos target, long maximumEu) {
        return DroneExecutionResult.error("EU import is unavailable in this runtime environment");
    }

    default DroneExecutionResult exportEnergy(BlockPos target, long maximumEu) {
        return DroneExecutionResult.error("EU export is unavailable in this runtime environment");
    }

    default DroneExecutionResult chargeTargetUntil(BlockPos target, double percent, long maximumEu) {
        return DroneExecutionResult.error("Target EU charging is unavailable in this runtime environment");
    }
}
