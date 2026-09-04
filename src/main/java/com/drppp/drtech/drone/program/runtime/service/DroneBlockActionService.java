package com.drppp.drtech.drone.program.runtime.service;

import com.drppp.drtech.drone.inventory.DroneItemFilter;
import com.drppp.drtech.drone.action.DroneInteractionRequest;
import com.drppp.drtech.drone.program.runtime.DroneExecutionResult;
import net.minecraft.util.math.BlockPos;

public interface DroneBlockActionService {
    default DroneExecutionResult breakBlock(BlockPos target) {
        return DroneExecutionResult.error("Block breaking is unavailable in this runtime environment");
    }

    default DroneExecutionResult placeBlock(BlockPos target) {
        return DroneExecutionResult.error("Block placement is unavailable in this runtime environment");
    }

    default DroneExecutionResult placeBlock(BlockPos target, DroneItemFilter filter) {
        return placeBlock(target);
    }

    default DroneExecutionResult placeBlockInArea(BlockPos target, DroneItemFilter filter) {
        return placeBlock(target, filter);
    }

    default DroneExecutionResult interactBlock(DroneInteractionRequest request) {
        return DroneExecutionResult.error("Block interaction is unavailable in this runtime environment");
    }

    default DroneExecutionResult useItem(DroneItemFilter filter, boolean sneaking) {
        return DroneExecutionResult.error("Item use is unavailable in this runtime environment");
    }

    default DroneExecutionResult harvestCrop(BlockPos target) {
        return DroneExecutionResult.error("Crop harvesting is unavailable in this runtime environment");
    }

    default DroneExecutionResult fellTreeBlock(BlockPos target) {
        return DroneExecutionResult.error("Tree felling is unavailable in this runtime environment");
    }

    default DroneExecutionResult replant(BlockPos target, DroneItemFilter filter) {
        return DroneExecutionResult.error("Crop replanting is unavailable in this runtime environment");
    }

    default DroneExecutionResult setRedstoneOutput(BlockPos target, int strength) {
        return DroneExecutionResult.error("Programmable redstone output is unavailable in this runtime environment");
    }
}
