package com.drppp.drtech.common.drone.program.runtime;

import com.drppp.drtech.common.drone.program.runtime.service.DroneBlockActionService;
import com.drppp.drtech.common.drone.program.runtime.service.DroneDockService;
import com.drppp.drtech.common.drone.program.runtime.service.DroneEnergyService;
import com.drppp.drtech.common.drone.program.runtime.service.DroneEntityService;
import com.drppp.drtech.common.drone.program.runtime.service.DroneFluidService;
import com.drppp.drtech.common.drone.program.runtime.service.DroneItemService;
import com.drppp.drtech.common.drone.program.runtime.service.DroneMovementService;
import com.drppp.drtech.common.drone.program.runtime.service.DroneMachineService;
import com.drppp.drtech.common.drone.program.runtime.service.DroneSensorService;
import com.drppp.drtech.common.drone.program.runtime.service.DroneSafetyService;
import com.drppp.drtech.common.drone.program.compile.CompiledDroneProgram;
import com.drppp.drtech.common.drone.program.model.DroneProgramReference;
import com.drppp.drtech.common.drone.program.model.DroneArea;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.ResourceLocation;

public interface DroneRuntimeEnvironment extends DroneMovementService, DroneBlockActionService, DroneItemService,
        DroneFluidService, DroneEnergyService, DroneEntityService, DroneSensorService, DroneDockService,
        DroneSafetyService, DroneMachineService {

    /** Resolves only a server-owned, exact-revision subprogram. Null means unavailable or unauthorized. */
    default CompiledDroneProgram resolveProgram(DroneProgramReference reference) {
        return null;
    }

    default DroneExecutionResult remoteAlert(String message) {
        return DroneExecutionResult.success();
    }

    default void auditAction(ResourceLocation action, DroneExecutionResult result) {}

    /** Current drone block position, used as the fixed origin for nearest-first traversal. */
    default BlockPos getCurrentPosition() {
        return BlockPos.ORIGIN;
    }

    /** Runtime area capacity of the physical chassis. */
    default int getAreaBlockLimit() {
        return DroneArea.MAX_BLOCKS;
    }

    DroneRuntimeEnvironment EMPTY = new DroneRuntimeEnvironment() {
        @Override
        public double getEnergyPercent() {
            return 0.0D;
        }

        @Override
        public DroneExecutionResult moveTo(BlockPos target) {
            return DroneExecutionResult.error("Movement is unavailable in this runtime environment");
        }
    };

}
