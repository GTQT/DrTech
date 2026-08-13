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
import net.minecraft.util.math.BlockPos;

public interface DroneRuntimeEnvironment extends DroneMovementService, DroneBlockActionService, DroneItemService,
        DroneFluidService, DroneEnergyService, DroneEntityService, DroneSensorService, DroneDockService,
        DroneSafetyService, DroneMachineService {

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
