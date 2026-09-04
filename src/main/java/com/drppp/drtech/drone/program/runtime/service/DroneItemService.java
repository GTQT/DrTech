package com.drppp.drtech.drone.program.runtime.service;

import com.drppp.drtech.drone.action.DroneTransferRequest;
import com.drppp.drtech.drone.action.DroneItemWorldRequest;
import com.drppp.drtech.drone.inventory.DroneItemFilter;
import com.drppp.drtech.drone.program.runtime.DroneExecutionResult;
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

    default DroneExecutionResult craftItems(DroneItemFilter outputFilter, int maximumCrafts,
            boolean simulate, boolean requireExactCount) {
        return DroneExecutionResult.error("Cargo crafting is unavailable in this runtime environment");
    }

    default DroneExecutionResult craftItems(DroneItemFilter outputFilter, int maximumCrafts,
            boolean simulate, boolean requireExactCount, DroneItemFilter reserveFilter, int reserveAmount) {
        return craftItems(outputFilter, maximumCrafts, simulate, requireExactCount);
    }

    default int getCraftableCount(DroneItemFilter outputFilter, int limit) {
        return 0;
    }

    default int getCraftableCount(DroneItemFilter outputFilter, int limit,
            DroneItemFilter reserveFilter, int reserveAmount) {
        return getCraftableCount(outputFilter, limit);
    }

    default DroneExecutionResult craftGrid(DroneItemFilter outputFilter, DroneItemFilter[] gridFilters,
            int maximumCrafts, boolean requireExactCount, DroneItemFilter reserveFilter, int reserveAmount) {
        return DroneExecutionResult.error("Explicit cargo crafting is unavailable in this runtime environment");
    }
}
