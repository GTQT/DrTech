package com.drppp.drtech.common.drone.program.runtime.service;

import com.drppp.drtech.common.drone.action.DroneTransferRequest;
import com.drppp.drtech.common.drone.action.DroneItemWorldRequest;
import com.drppp.drtech.common.drone.inventory.DroneItemFilter;
import com.drppp.drtech.common.drone.program.runtime.DroneExecutionResult;
import net.minecraft.util.math.BlockPos;

public interface DroneItemService {
    default DroneExecutionResult importItems(BlockPos target, DroneItemFilter filter) {
        return DroneExecutionResult.error("Item import is unavailable in this runtime environment");
    }

    default DroneExecutionResult exportItems(BlockPos target, DroneItemFilter filter) {
        return DroneExecutionResult.error("Item export is unavailable in this runtime environment");
    }

    default DroneExecutionResult importItems(DroneTransferRequest request) {
        if (request.getTarget() == null) return DroneExecutionResult.error("Area inventory search is unavailable");
        return importItems(request.getTarget(), DroneItemFilter.fromSpec(request.getFilter()));
    }

    default DroneExecutionResult exportItems(DroneTransferRequest request) {
        if (request.getTarget() == null) return DroneExecutionResult.error("Area inventory search is unavailable");
        return exportItems(request.getTarget(), DroneItemFilter.fromSpec(request.getFilter()));
    }

    default DroneExecutionResult pickupDroppedItems(DroneItemWorldRequest request) {
        return DroneExecutionResult.error("Dropped-item pickup is unavailable in this runtime environment");
    }

    default DroneExecutionResult dropItems(DroneItemWorldRequest request) {
        return DroneExecutionResult.error("Item dropping is unavailable in this runtime environment");
    }
}
