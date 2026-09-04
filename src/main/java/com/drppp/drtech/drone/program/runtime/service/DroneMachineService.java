package com.drppp.drtech.drone.program.runtime.service;

import com.drppp.drtech.drone.program.runtime.DroneExecutionResult;
import com.drppp.drtech.drone.program.model.DroneArea;
import net.minecraft.util.math.BlockPos;

/** GregTech machine control and read-only workable state exposed to visual programs. */
public interface DroneMachineService {
    default DroneExecutionResult setMachineWorking(BlockPos target, boolean enabled) {
        return DroneExecutionResult.error("GregTech machine control is unavailable in this runtime environment");
    }

    default DroneExecutionResult waitForMachineIdle(BlockPos target) {
        return DroneExecutionResult.error("GregTech machine monitoring is unavailable in this runtime environment");
    }

    /**
     * Waits for one real recipe cycle. Implementations must not report success until the caller has first observed
     * an active recipe and then observed its progress return to the beginning or the machine become idle.
     */
    default DroneExecutionResult waitForMachineCycle(BlockPos target, boolean observedActive,
            double previousProgressPercent) {
        return DroneExecutionResult.error("GregTech machine cycle monitoring is unavailable in this runtime environment");
    }

    default boolean isMachineActive(BlockPos target) { return false; }
    default boolean isMachineWorkingEnabled(BlockPos target) { return false; }
    /** Returns 0..100, or -1 when the target does not expose GregTech workable capability. */
    default double getMachineProgressPercent(BlockPos target) { return -1.0D; }
    /** True when the recipe logic reports that no input recipe currently matches. */
    default boolean isMachineWaitingForInput(BlockPos target) { return false; }
    /** True when the recipe logic reports a full or otherwise unusable output. */
    default boolean isMachineOutputBlocked(BlockPos target) { return false; }
    default boolean isMachineLowEnergy(BlockPos target) { return false; }
    /** Human-readable GregTech recipe diagnostic; empty when none is available. */
    default String getMachineDiagnostic(BlockPos target) { return ""; }
    /** Repairs the target with matching GregTech tools carried in drone cargo. */
    default DroneExecutionResult repairMachine(BlockPos target, boolean requireAllTools) {
        return DroneExecutionResult.error("GregTech machine maintenance is unavailable in this runtime environment");
    }
    default boolean needsMachineMaintenance(BlockPos target) { return false; }
    default int getMachineMaintenanceProblemCount(BlockPos target) { return 0; }

    default DroneExecutionResult transferThaumcraftEssentia(BlockPos smelter, DroneArea tubeArea, int maxAmount) {
        return DroneExecutionResult.error("Thaumcraft essentia transfer is unavailable in this runtime environment");
    }
}
