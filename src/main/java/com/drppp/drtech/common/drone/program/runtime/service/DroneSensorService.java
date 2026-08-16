package com.drppp.drtech.common.drone.program.runtime.service;

import com.drppp.drtech.common.drone.inventory.DroneItemFilter;
import com.drppp.drtech.common.drone.filter.DroneBlockFilterSpec;
import com.drppp.drtech.common.drone.program.model.DroneArea;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;

public interface DroneSensorService {
    enum LightType { MAX, BLOCK, SKY }

    default boolean isRedstonePowered(BlockPos target) { return false; }
    default boolean isOwnerWithin(double radius) { return false; }
    default int getCargoItemCount(DroneItemFilter filter) { return 0; }
    default int getCargoFreeSlots() { return 0; }
    default double getCargoUsedPercent() { return 0.0D; }
    default int getInventoryItemCount(BlockPos target, @Nullable EnumFacing side, DroneItemFilter filter) { return 0; }
    default int getRedstoneStrength(BlockPos target) { return isRedstonePowered(target) ? 15 : 0; }
    default int getRedstoneOutputStrength(BlockPos target) { return 0; }
    default int getLightLevel(BlockPos target, LightType type) { return 0; }
    default boolean matchesBlock(BlockPos target, DroneBlockFilterSpec filter) { return false; }
    default boolean isAirBlock(BlockPos target) { return false; }
    default boolean isCoordinateReachable(BlockPos target) { return false; }
    default boolean isDockAvailable(BlockPos target) { return false; }
    default int countMatchingBlocks(DroneArea area, DroneBlockFilterSpec filter, int limit) { return 0; }
    default int countEntities(DroneArea area, @Nullable com.drppp.drtech.common.drone.filter.DroneEntityFilterSpec filter, int limit) { return 0; }
    default DroneEntitySensorResult senseNearestEntity(DroneArea area, @Nullable com.drppp.drtech.common.drone.filter.DroneEntityFilterSpec filter) { return DroneEntitySensorResult.EMPTY; }
    default DroneEntitySensorResult senseDroneDamage() { return DroneEntitySensorResult.EMPTY; }
}
