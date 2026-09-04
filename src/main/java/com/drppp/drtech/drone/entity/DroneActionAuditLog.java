package com.drppp.drtech.drone.entity;

import com.drppp.drtech.DrTechMain;
import com.drppp.drtech.DrtConfig;
import com.drppp.drtech.drone.program.registry.DrTechDroneNodes;
import com.drppp.drtech.drone.program.runtime.DroneExecutionResult;
import net.minecraft.util.ResourceLocation;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/** Structured server audit records for terminal, state-changing drone actions. */
final class DroneActionAuditLog {

    private static final Set<ResourceLocation> AUDITED_ACTIONS = new HashSet<>(Arrays.asList(
            DrTechDroneNodes.MOVE_TO, DrTechDroneNodes.RETURN_TO_DOCK, DrTechDroneNodes.BIND_DOCK,
            DrTechDroneNodes.UNBIND_DOCK, DrTechDroneNodes.CONFIGURE_SAFETY,
            DrTechDroneNodes.BREAK_BLOCK, DrTechDroneNodes.BREAK_BLOCK_AT,
            DrTechDroneNodes.PLACE_BLOCK, DrTechDroneNodes.PLACE_AREA,
            DrTechDroneNodes.INTERACT_BLOCK, DrTechDroneNodes.USE_ITEM_ON_BLOCK, DrTechDroneNodes.USE_ITEM,
            DrTechDroneNodes.HARVEST_CROP, DrTechDroneNodes.PICKUP_DROPPED_ITEMS, DrTechDroneNodes.DROP_ITEMS,
            DrTechDroneNodes.IMPORT_ITEMS, DrTechDroneNodes.EXPORT_ITEMS,
            DrTechDroneNodes.IMPORT_FLUID, DrTechDroneNodes.EXPORT_FLUID, DrTechDroneNodes.DRAIN_FLUID,
            DrTechDroneNodes.IMPORT_EU, DrTechDroneNodes.EXPORT_EU, DrTechDroneNodes.CHARGE_TARGET_PERCENT,
            DrTechDroneNodes.CRAFT_ITEMS, DrTechDroneNodes.CRAFT_GRID,
            DrTechDroneNodes.SET_MACHINE_WORKING, DrTechDroneNodes.REPAIR_MACHINE,
            DrTechDroneNodes.SET_REDSTONE_OUTPUT, DrTechDroneNodes.REMOTE_ALERT));

    private DroneActionAuditLog() {}

    static void record(EntityProgrammableDrone drone, ResourceLocation action, DroneExecutionResult result) {
        if (!DrtConfig.drone.EnableDroneAuditLog || drone == null || action == null || result == null
                || !AUDITED_ACTIONS.contains(action)) return;
        UUID owner = drone.getOwnerId();
        UUID program = drone.getProgramId();
        String error = result.getError() == null ? "" : result.getError().replace('\n', ' ').replace('\r', ' ');
        if (error.length() > 256) error = error.substring(0, 256);
        DrTechMain.LOGGER.info("drone_action action={} state={} status={} amount={} droneId={} owner={} "
                        + "programId={} revision={} dimension={} position={} error={}",
                action, result.getState(), result.getStatus(), result.getAmount(), drone.getDroneId(),
                owner == null ? "unowned" : owner, program == null ? "none" : program,
                drone.getProgramRevision(), drone.world.provider.getDimension(), drone.getPosition(), error);
    }
}
