package com.drppp.drtech.drone.program.runtime.service;

import com.drppp.drtech.drone.program.runtime.DroneExecutionResult;
import net.minecraft.util.math.BlockPos;
import com.drppp.drtech.drone.inventory.DroneItemFilter;
import com.drppp.drtech.drone.filter.DroneEntityFilterSpec;
import com.drppp.drtech.drone.program.model.DroneArea;

public interface DroneEntityService {
    default DroneExecutionResult interactWithNearestEntity(BlockPos target) {
        return interactWithNearestEntity(target, null);
    }

    default DroneExecutionResult interactWithNearestEntity(BlockPos target, DroneEntityFilterSpec entityFilter) {
        return DroneExecutionResult.error("Entity interaction is unavailable in this runtime environment");
    }

    default DroneExecutionResult useItemOnEntity(BlockPos target) {
        return useItemOnEntity(target, DroneItemFilter.ANY);
    }

    default DroneExecutionResult useItemOnEntity(BlockPos target, DroneItemFilter filter) {
        return useItemOnEntity(target, filter, null);
    }

    default DroneExecutionResult useItemOnEntity(BlockPos target, DroneItemFilter filter,
            DroneEntityFilterSpec entityFilter) {
        return DroneExecutionResult.error("Entity item use is unavailable in this runtime environment");
    }

    default DroneExecutionResult attackEntity(BlockPos target) {
        return attackEntity(target, null);
    }

    default DroneExecutionResult attackEntity(BlockPos target, DroneEntityFilterSpec filter) {
        return DroneExecutionResult.error("Entity attack is unavailable in this runtime environment");
    }

    default DroneExecutionResult attackEntity(BlockPos target, DroneEntityFilterSpec filter,
            boolean untilDefeated) {
        return attackEntity(target, filter);
    }

    default DroneExecutionResult attackEntity(BlockPos target, DroneEntityFilterSpec filter,
            boolean untilDefeated, String weaponMode, int attackIntervalTicks) {
        return attackEntity(target, filter, untilDefeated);
    }

    default DroneExecutionResult attackEntityInArea(BlockPos target, DroneEntityFilterSpec filter,
            boolean untilDefeated, DroneArea patrolArea, int maxChaseDistance) {
        return attackEntity(target, filter, untilDefeated);
    }

    default DroneExecutionResult attackEntityInArea(BlockPos target, DroneEntityFilterSpec filter,
            boolean untilDefeated, DroneArea patrolArea, int maxChaseDistance,
            String expectedEntityUuid, boolean hostileOnly) {
        return attackEntityInArea(target, filter, untilDefeated, patrolArea, maxChaseDistance);
    }

    default DroneExecutionResult attackEntityInArea(BlockPos target, DroneEntityFilterSpec filter,
            boolean untilDefeated, DroneArea patrolArea, int maxChaseDistance,
            String expectedEntityUuid, boolean hostileOnly, String weaponMode, int attackIntervalTicks) {
        return attackEntityInArea(target, filter, untilDefeated, patrolArea, maxChaseDistance,
                expectedEntityUuid, hostileOnly);
    }

    default void clearAttackTarget() {}

    default DroneExecutionResult followEntity(BlockPos target, double distance) {
        return followEntity(target, distance, null);
    }

    default DroneExecutionResult followEntity(BlockPos target, double distance, DroneEntityFilterSpec filter) {
        if (distance < 1.0D || distance > 64.0D) return DroneExecutionResult.error("Follow distance must be 1..64");
        return DroneExecutionResult.error("Entity following is unavailable in this runtime environment");
    }

    default DroneExecutionResult moveAwayFromEntity(BlockPos target, double distance) {
        return moveAwayFromEntity(target, distance, null);
    }

    default DroneExecutionResult moveAwayFromEntity(BlockPos target, double distance, DroneEntityFilterSpec filter) {
        if (distance < 1.0D || distance > 64.0D) return DroneExecutionResult.error("Retreat distance must be 1..64");
        return DroneExecutionResult.error("Entity retreat is unavailable in this runtime environment");
    }

    default DroneExecutionResult loadEntity(BlockPos target) {
        return loadEntity(target, null);
    }

    default DroneExecutionResult loadEntity(BlockPos target, DroneEntityFilterSpec filter) {
        return DroneExecutionResult.error("Entity loading is unavailable in this runtime environment");
    }

    default DroneExecutionResult releaseEntity(BlockPos target) {
        return DroneExecutionResult.error("Entity release is unavailable in this runtime environment");
    }

    default DroneExecutionResult fishAt(BlockPos target) {
        return DroneExecutionResult.error("Fishing is unavailable in this runtime environment");
    }

    default DroneExecutionResult renameDrone(String name) {
        if (name == null || name.trim().isEmpty() || name.trim().length() > 32) {
            return DroneExecutionResult.error("Drone name must contain 1..32 characters");
        }
        return DroneExecutionResult.error("Drone renaming is unavailable in this runtime environment");
    }

    default DroneExecutionResult setStatusLabel(String label) {
        if (label != null && label.length() > 64) return DroneExecutionResult.error("Status label is limited to 64 characters");
        return DroneExecutionResult.error("Drone status labels are unavailable in this runtime environment");
    }

    default DroneExecutionResult setRotorMode(String mode) {
        if (mode == null || !("ACTIVE".equalsIgnoreCase(mode) || "STANDBY".equalsIgnoreCase(mode))) {
            return DroneExecutionResult.error("Rotor mode must be ACTIVE or STANDBY");
        }
        return DroneExecutionResult.error("Drone rotor control is unavailable in this runtime environment");
    }

    default DroneExecutionResult setStatusLight(String mode) {
        if (mode == null || !("AUTO".equalsIgnoreCase(mode) || "GREEN".equalsIgnoreCase(mode)
                || "YELLOW".equalsIgnoreCase(mode) || "RED".equalsIgnoreCase(mode)
                || "OFF".equalsIgnoreCase(mode))) {
            return DroneExecutionResult.error("Status light mode must be AUTO, GREEN, YELLOW, RED or OFF");
        }
        return DroneExecutionResult.error("Drone status light control is unavailable in this runtime environment");
    }

    default DroneExecutionResult editSign(BlockPos target, String[] lines) {
        if (target == null) return DroneExecutionResult.error("Sign target is required");
        if (lines == null || lines.length != 4) return DroneExecutionResult.error("A sign requires exactly four lines");
        for (String line : lines) {
            if (line != null && line.length() > 64) return DroneExecutionResult.error("Each sign line is limited to 64 characters");
        }
        return DroneExecutionResult.error("Sign editing is unavailable in this runtime environment");
    }
}
