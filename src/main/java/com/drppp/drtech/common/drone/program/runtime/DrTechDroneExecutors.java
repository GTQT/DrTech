package com.drppp.drtech.common.drone.program.runtime;

import com.drppp.drtech.common.drone.action.DroneFailurePolicy;
import com.drppp.drtech.common.drone.action.DroneTransferRequest;
import com.drppp.drtech.common.drone.action.DroneInteractionRequest;
import com.drppp.drtech.common.drone.action.DroneItemWorldRequest;
import com.drppp.drtech.common.drone.action.DroneSearchMode;
import com.drppp.drtech.common.drone.program.registry.DrTechDroneNodes;
import com.drppp.drtech.common.drone.inventory.DroneItemFilter;
import com.drppp.drtech.common.drone.filter.DroneFluidFilterSpec;
import com.drppp.drtech.common.drone.filter.DroneFilterMode;
import com.drppp.drtech.common.drone.filter.DroneItemFilterSpec;
import com.drppp.drtech.common.drone.filter.DroneBlockFilterSpec;
import com.drppp.drtech.common.drone.filter.DroneEntityFilterSpec;
import com.drppp.drtech.common.drone.program.model.DroneArea;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;

import java.util.Collections;

public final class DrTechDroneExecutors {

    private static final int DEFAULT_WAIT_TICKS = 20;
    private static final int MAX_WAIT_TICKS = 20 * 60 * 60;
    public static final int MAX_REPEAT_COUNT = 1_000_000;

    private DrTechDroneExecutors() {}

    public static DroneExecutorRegistry createDefaultRegistry() {
        DroneExecutorRegistry registry = new DroneExecutorRegistry();
        registry.register(DrTechDroneNodes.WAIT, DrTechDroneExecutors::tickWait);
        registry.register(DrTechDroneNodes.BRANCH, context -> DroneExecutionResult.success(
                context.requireInput("condition", Boolean.class) ? "true" : "false"));
        registry.register(DrTechDroneNodes.MOVE_TO, context ->
                context.getEnvironment().moveTo(context.requireInput("target", BlockPos.class)));
        registry.register(DrTechDroneNodes.BREAK_BLOCK_AT, context ->
                context.getEnvironment().breakBlock(context.requireInput("target", BlockPos.class)));
        registry.register(DrTechDroneNodes.BREAK_BLOCK, context -> tickArea(context, false));
        registry.register(DrTechDroneNodes.PLACE_BLOCK, context ->
                context.getEnvironment().placeBlock(context.requireInput("target", BlockPos.class),
                        optionalFilter(context)));
        registry.register(DrTechDroneNodes.PLACE_AREA, context -> tickArea(context, true));
        registry.register(DrTechDroneNodes.RETURN_TO_DOCK, context ->
                context.getEnvironment().returnToDock(context.requireInput("target", BlockPos.class)));
        registry.register(DrTechDroneNodes.CHARGE_UNTIL, context -> {
            Number input = context.getOptionalInput("percent", Number.class);
            double configured = input == null ? context.getConfiguration().hasKey("Percent")
                    ? context.getConfiguration().getDouble("Percent") : 100.0D : input.doubleValue();
            return context.getEnvironment().chargeUntil(Math.max(1.0D, Math.min(100.0D, configured)));
        });
        registry.register(DrTechDroneNodes.BIND_DOCK, context ->
                context.getEnvironment().bindDock(context.requireInput("target", BlockPos.class)));
        registry.register(DrTechDroneNodes.UNBIND_DOCK, context -> context.getEnvironment().unbindDock());
        registry.register(DrTechDroneNodes.CONFIGURE_SAFETY, context -> {
            Number returnInput = context.getOptionalInput("return_percent", Number.class);
            Number resumeInput = context.getOptionalInput("resume_percent", Number.class);
            int returnPercent = returnInput == null ? context.getConfiguration().hasKey("ReturnPercent")
                    ? context.getConfiguration().getInteger("ReturnPercent") : 20
                    : returnInput.intValue();
            int resumePercent = resumeInput == null ? context.getConfiguration().hasKey("ResumePercent")
                    ? context.getConfiguration().getInteger("ResumePercent") : 90
                    : resumeInput.intValue();
            return context.getEnvironment().configureSafety(returnPercent, resumePercent);
        });
        registry.register(DrTechDroneNodes.IMPORT_EU, context -> energyTransfer(context, true));
        registry.register(DrTechDroneNodes.EXPORT_EU, context -> energyTransfer(context, false));
        registry.register(DrTechDroneNodes.CHARGE_TARGET_PERCENT, DrTechDroneExecutors::chargeTargetPercent);
        registry.register(DrTechDroneNodes.IMPORT_ITEMS, context -> tickItemTransfer(context, true));
        registry.register(DrTechDroneNodes.EXPORT_ITEMS, context -> tickItemTransfer(context, false));
        registry.register(DrTechDroneNodes.IMPORT_FLUID, context -> tickFluidTransfer(context, 0));
        registry.register(DrTechDroneNodes.EXPORT_FLUID, context -> tickFluidTransfer(context, 1));
        registry.register(DrTechDroneNodes.DRAIN_FLUID, context -> tickFluidTransfer(context, 2));
        registry.register(DrTechDroneNodes.FIND_FLUID_CONTAINER, DrTechDroneExecutors::findFluidContainer);
        registry.register(DrTechDroneNodes.WAIT_FOR_FLUID_AMOUNT, DrTechDroneExecutors::waitForFluidAmount);
        registry.register(DrTechDroneNodes.CRAFT_ITEMS, context -> {
            Number input = context.getOptionalInput("count", Number.class);
            int count = input == null ? context.getConfiguration().hasKey("Count", 99)
                    ? context.getConfiguration().getInteger("Count") : 1 : input.intValue();
            count = Math.max(1, Math.min(64, count));
            DroneItemFilter output = context.requireInput("output", DroneItemFilter.class);
            DroneItemFilter reserve = context.getOptionalInput("reserve_filter", DroneItemFilter.class);
            Number reserveInput = context.getOptionalInput("reserve_count", Number.class);
            int reserveCount = reserveInput == null ? context.getConfiguration().getInteger("ReserveCount")
                    : reserveInput.intValue();
            boolean exact = !"AS_MANY_AS_POSSIBLE".equals(context.getConfiguration().getString("Mode"));
            DroneExecutionResult result = context.getEnvironment().craftItems(output, count,
                    context.getConfiguration().getBoolean("Simulate"), exact, reserve,
                    Math.max(0, Math.min(64, reserveCount)));
            context.getMemory().setActionAmount(context.getNode().getId(), result.getAmount());
            return result;
        });
        registry.register(DrTechDroneNodes.CRAFT_GRID, context -> {
            DroneItemFilter[] grid = new DroneItemFilter[9];
            for (int slot = 0; slot < grid.length; slot++) {
                grid[slot] = context.getOptionalInput("slot_" + (slot + 1), DroneItemFilter.class);
            }
            Number countInput = context.getOptionalInput("count", Number.class);
            int count = countInput == null ? context.getConfiguration().getInteger("Count") : countInput.intValue();
            Number reserveInput = context.getOptionalInput("reserve_count", Number.class);
            int reserveCount = reserveInput == null ? context.getConfiguration().getInteger("ReserveCount")
                    : reserveInput.intValue();
            DroneExecutionResult result = context.getEnvironment().craftGrid(
                    context.requireInput("output", DroneItemFilter.class), grid,
                    Math.max(1, Math.min(64, count)),
                    !"AS_MANY_AS_POSSIBLE".equals(context.getConfiguration().getString("Mode")),
                    context.getOptionalInput("reserve_filter", DroneItemFilter.class),
                    Math.max(0, Math.min(64, reserveCount)));
            context.getMemory().setActionAmount(context.getNode().getId(), result.getAmount());
            return result;
        });
        registry.register(DrTechDroneNodes.SET_MACHINE_WORKING, context -> {
            Boolean input = context.getOptionalInput("enabled", Boolean.class);
            boolean enabled = input == null ? context.getConfiguration().getBoolean("Enabled") : input;
            return context.getEnvironment().setMachineWorking(
                    context.requireInput("target", BlockPos.class), enabled);
        });
        registry.register(DrTechDroneNodes.WAIT_MACHINE_IDLE, context ->
                context.getEnvironment().waitForMachineIdle(context.requireInput("target", BlockPos.class)));
        registry.register(DrTechDroneNodes.WAIT_MACHINE_CYCLE, DrTechDroneExecutors::tickWaitMachineCycle);
        registry.register(DrTechDroneNodes.REPAIR_MACHINE, context -> {
            DroneExecutionResult result = context.getEnvironment().repairMachine(
                    context.requireInput("target", BlockPos.class),
                    context.getConfiguration().getBoolean("RequireAll"));
            context.getMemory().setActionAmount(context.getNode().getId(), result.getAmount());
            return result;
        });
        registry.register(DrTechDroneNodes.INTERACT_BLOCK, context -> interactBlock(context, false));
        registry.register(DrTechDroneNodes.USE_ITEM_ON_BLOCK, context -> interactBlock(context, true));
        registry.register(DrTechDroneNodes.USE_ITEM, context -> context.getEnvironment().useItem(
                optionalFilter(context), context.getConfiguration().getBoolean("Sneaking")));
        registry.register(DrTechDroneNodes.INTERACT_ENTITY, context -> context.getEnvironment()
                .interactWithNearestEntity(context.requireInput("target", BlockPos.class),
                        optionalEntityFilter(context)));
        registry.register(DrTechDroneNodes.USE_ITEM_ON_ENTITY, context -> context.getEnvironment()
                .useItemOnEntity(context.requireInput("target", BlockPos.class), optionalFilter(context),
                        optionalEntityFilter(context, "entity_filter")));
        registry.register(DrTechDroneNodes.FOLLOW_ENTITY, context -> context.getEnvironment().followEntity(
                context.requireInput("target", BlockPos.class), entityDistance(context), optionalEntityFilter(context)));
        registry.register(DrTechDroneNodes.AVOID_ENTITY, context -> context.getEnvironment().moveAwayFromEntity(
                context.requireInput("target", BlockPos.class), entityDistance(context), optionalEntityFilter(context)));
        registry.register(DrTechDroneNodes.RENAME_DRONE, context -> context.getEnvironment()
                .renameDrone(context.requireInput("name", String.class)));
        registry.register(DrTechDroneNodes.SET_STATUS_LABEL, context -> context.getEnvironment()
                .setStatusLabel(context.requireInput("label", String.class)));
        registry.register(DrTechDroneNodes.SET_ROTOR_MODE, context -> context.getEnvironment()
                .setRotorMode(context.getConfiguration().getString("Mode")));
        registry.register(DrTechDroneNodes.SET_STATUS_LIGHT, context -> context.getEnvironment()
                .setStatusLight(context.getConfiguration().getString("Mode")));
        registry.register(DrTechDroneNodes.EDIT_SIGN, DrTechDroneExecutors::editSign);
        registry.register(DrTechDroneNodes.ATTACK_ENTITY, context -> context.getEnvironment()
                .attackEntity(context.requireInput("target", BlockPos.class), optionalEntityFilter(context)));
        registry.register(DrTechDroneNodes.LOAD_ENTITY, context -> context.getEnvironment()
                .loadEntity(context.requireInput("target", BlockPos.class), optionalEntityFilter(context)));
        registry.register(DrTechDroneNodes.RELEASE_ENTITY, context -> context.getEnvironment()
                .releaseEntity(context.requireInput("target", BlockPos.class)));
        registry.register(DrTechDroneNodes.PICKUP_DROPPED_ITEMS, context -> itemWorldAction(context, true));
        registry.register(DrTechDroneNodes.DROP_ITEMS, context -> itemWorldAction(context, false));
        registry.register(DrTechDroneNodes.HARVEST_CROP, context ->
                context.getEnvironment().harvestCrop(context.requireInput("target", BlockPos.class)));
        registry.register(DrTechDroneNodes.SET_REDSTONE_OUTPUT, context -> {
            Number input = context.getOptionalInput("strength", Number.class);
            int configured = input == null ? context.getConfiguration().hasKey("Strength")
                    ? context.getConfiguration().getInteger("Strength") : 15 : input.intValue();
            return context.getEnvironment().setRedstoneOutput(
                    context.requireInput("target", BlockPos.class), Math.max(0, Math.min(15, configured)));
        });
        registry.register(DrTechDroneNodes.SET_NUMBER_VARIABLE, context -> {
            context.getMemory().setNumber(variableName(context), context.requireInput("value", Number.class).doubleValue(),
                    isLocalVariable(context));
            return DroneExecutionResult.success();
        });
        registry.register(DrTechDroneNodes.ADD_NUMBER_VARIABLE, context -> {
            context.getMemory().addNumber(variableName(context), context.requireInput("amount", Number.class).doubleValue(),
                    isLocalVariable(context));
            return DroneExecutionResult.success();
        });
        registry.register(DrTechDroneNodes.SET_STRING_VARIABLE, context -> {
            context.getMemory().setString(variableName(context), context.requireInput("value", String.class),
                    isLocalVariable(context));
            return DroneExecutionResult.success();
        });
        registry.register(DrTechDroneNodes.DISPLAY_STRING, context -> {
            context.getMemory().setString("_display", context.requireInput("value", String.class));
            return DroneExecutionResult.success();
        });
        registry.register(DrTechDroneNodes.REMOTE_ALERT, context -> {
            String message = context.requireInput("message", String.class);
            context.getMemory().setString("_alert", message);
            return context.getEnvironment().remoteAlert(message);
        });
        registry.register(DrTechDroneNodes.REPEAT, DrTechDroneExecutors::tickRepeat);
        registry.register(DrTechDroneNodes.WAIT_FOR_REDSTONE, context -> {
            BlockPos target = context.getOptionalInput("target", BlockPos.class);
            return context.getEnvironment().isRedstonePowered(target)
                    ? DroneExecutionResult.success() : DroneExecutionResult.running();
        });
        registry.register(DrTechDroneNodes.WAIT_FOR_OWNER, context -> {
            Number input = context.getOptionalInput("radius", Number.class);
            double configured = input == null ? context.getConfiguration().hasKey("Radius")
                    ? context.getConfiguration().getDouble("Radius") : 16.0D : input.doubleValue();
            double radius = Math.max(1.0D, Math.min(128.0D, configured));
            return context.getEnvironment().isOwnerWithin(radius)
                    ? DroneExecutionResult.success() : DroneExecutionResult.running();
        });
        registry.register(DrTechDroneNodes.WHILE, context -> DroneExecutionResult.success(
                context.requireInput("condition", Boolean.class) ? "body" : "done"));
        registry.register(DrTechDroneNodes.FOR_EACH_COORDINATE, DrTechDroneExecutors::tickForEachCoordinate);
        registry.register(DrTechDroneNodes.FOR_EACH_ITEM_FILTER, DrTechDroneExecutors::tickForEachItemFilter);
        registry.freeze();
        return registry;
    }

    private static DroneItemFilter optionalFilter(DroneNodeExecutionContext context) {
        DroneItemFilter filter = context.getOptionalInput("filter", DroneItemFilter.class);
        return filter == null ? DroneItemFilter.ANY : filter;
    }

    private static DroneEntityFilterSpec optionalEntityFilter(DroneNodeExecutionContext context) {
        return optionalEntityFilter(context, "filter");
    }

    private static DroneEntityFilterSpec optionalEntityFilter(DroneNodeExecutionContext context, String port) {
        return context.getOptionalInput(port, DroneEntityFilterSpec.class);
    }

    private static double entityDistance(DroneNodeExecutionContext context) {
        Number input = context.getOptionalInput("distance", Number.class);
        double value = input == null ? context.getConfiguration().hasKey("Distance")
                ? context.getConfiguration().getDouble("Distance") : 4.0D : input.doubleValue();
        return Math.max(1.0D, Math.min(64.0D, value));
    }

    private static DroneExecutionResult editSign(DroneNodeExecutionContext context) {
        String[] lines = new String[4];
        for (int index = 0; index < lines.length; index++) {
            String input = context.getOptionalInput("line_" + (index + 1), String.class);
            lines[index] = input == null ? context.getConfiguration().getString("Line" + (index + 1)) : input;
        }
        return context.getEnvironment().editSign(context.requireInput("target", BlockPos.class), lines);
    }

    private static DroneExecutionResult energyTransfer(DroneNodeExecutionContext context, boolean importing) {
        DroneExecutionResult result = importing
                ? context.getEnvironment().importEnergy(context.requireInput("target", BlockPos.class), energyAmount(context))
                : context.getEnvironment().exportEnergy(context.requireInput("target", BlockPos.class), energyAmount(context));
        context.getMemory().setActionAmount(context.getNode().getId(), result.getAmount());
        return result;
    }

    private static long energyAmount(DroneNodeExecutionContext context) {
        Number input = context.getOptionalInput("amount", Number.class);
        long configured = input == null ? context.getConfiguration().hasKey("MaxEU")
                ? context.getConfiguration().getInteger("MaxEU") : 1L : input.longValue();
        return Math.max(1L, Math.min(1_000_000L, configured));
    }

    private static DroneExecutionResult chargeTargetPercent(DroneNodeExecutionContext context) {
        Number input = context.getOptionalInput("percent", Number.class);
        double percent = input == null ? context.getConfiguration().hasKey("Percent")
                ? context.getConfiguration().getInteger("Percent") : 100.0D : input.doubleValue();
        DroneExecutionResult result = context.getEnvironment().chargeTargetUntil(
                context.requireInput("target", BlockPos.class), Math.max(1.0D, Math.min(100.0D, percent)),
                energyAmount(context));
        NBTTagCompound state = context.getState();
        long transferred = Math.max(0L, state.getLong("Transferred")) + Math.max(0L, result.getAmount());
        state.setLong("Transferred", transferred);
        context.getMemory().setActionAmount(context.getNode().getId(), transferred);
        if (result.getState() == DroneActionState.SUCCESS) return DroneExecutionResult.success(transferred);
        if (result.getState() == DroneActionState.RUNNING) return DroneExecutionResult.running(transferred);
        return result;
    }

    private static DroneExecutionResult tickItemTransfer(DroneNodeExecutionContext context, boolean importing) {
        NBTTagCompound config = context.getConfiguration();
        NBTTagCompound state = context.getState();
        int maximum = config.hasKey("MaxAmount") ? config.getInteger("MaxAmount") : 64;
        int batch = config.hasKey("BatchSize") ? config.getInteger("BatchSize") : maximum;
        maximum = Math.max(1, Math.min(1_000_000, maximum));
        batch = Math.max(1, Math.min(maximum, batch));
        long transferred = Math.max(0L, state.getLong("Transferred"));
        if (transferred >= maximum) return DroneExecutionResult.success(transferred);
        int remaining = (int) Math.min(Integer.MAX_VALUE, maximum - transferred);
        EnumFacing side = parseDirection(config.getString("Direction"));
        BlockPos target = context.getOptionalInput("target", BlockPos.class);
        DroneArea area = context.getOptionalInput("area", DroneArea.class);
        DroneTransferRequest request;
        if (target != null && area == null) {
            request = DroneTransferRequest.at(target, side, remaining, Math.min(batch, remaining),
                    DroneFailurePolicy.ERROR, optionalFilter(context).getSpec());
        } else if (target == null && area != null) {
            request = DroneTransferRequest.within(area, side, remaining, Math.min(batch, remaining),
                    parseSearchMode(config.getString("SearchMode")),
                    !config.hasKey("SkipUnavailable") || config.getBoolean("SkipUnavailable"),
                    DroneFailurePolicy.ERROR, optionalFilter(context).getSpec());
        } else {
            return DroneExecutionResult.error("Item transfer requires exactly one coordinate or area input");
        }
        DroneExecutionResult result = importing ? context.getEnvironment().importItems(request)
                : context.getEnvironment().exportItems(request);
        if (result.getAmount() > 0L) {
            transferred += result.getAmount();
            state.setLong("Transferred", transferred);
            if (transferred >= maximum) return DroneExecutionResult.success(transferred);
        }
        if (result.getState() == DroneActionState.SUCCESS) return DroneExecutionResult.success(transferred);
        if (result.getState() == DroneActionState.FAILURE && transferred > 0L) {
            return DroneExecutionResult.success(transferred);
        }
        return result;
    }

    private static DroneExecutionResult tickFluidTransfer(DroneNodeExecutionContext context, int operation) {
        NBTTagCompound config = context.getConfiguration();
        NBTTagCompound state = context.getState();
        Number input = context.getOptionalInput("amount", Number.class);
        int maximum = input == null ? config.hasKey("MaxAmount", 99)
                ? config.getInteger("MaxAmount") : 1_000 : input.intValue();
        maximum = Math.max(1, Math.min(1_000_000, maximum));
        long transferred = Math.max(0L, state.getLong("Transferred"));
        if (transferred >= maximum) return DroneExecutionResult.success(transferred);
        int remaining = (int) Math.min(Integer.MAX_VALUE, maximum - transferred);
        DroneFluidFilterSpec filter = optionalFluidFilter(context);
        DroneExecutionResult result;
        if (operation == 2) {
            result = context.getEnvironment().drainFluid(remaining, filter);
        } else {
            BlockPos target = context.requireInput("target", BlockPos.class);
            EnumFacing side = parseDirection(config.getString("Direction"));
            result = operation == 0
                    ? context.getEnvironment().importFluid(target, side, remaining, filter)
                    : context.getEnvironment().exportFluid(target, side, remaining, filter);
        }
        if (result.getAmount() > 0L) {
            transferred += result.getAmount();
            state.setLong("Transferred", transferred);
            context.getMemory().setActionAmount(context.getNode().getId(), transferred);
            if (transferred >= maximum) return DroneExecutionResult.success(transferred);
        }
        if (result.getState() == DroneActionState.SUCCESS) return DroneExecutionResult.success(transferred);
        if (result.getState() == DroneActionState.FAILURE && transferred > 0L) {
            return DroneExecutionResult.success(transferred);
        }
        return result;
    }

    private static DroneFluidFilterSpec optionalFluidFilter(DroneNodeExecutionContext context) {
        DroneFluidFilterSpec filter = context.getOptionalInput("filter", DroneFluidFilterSpec.class);
        return filter == null ? new DroneFluidFilterSpec(DroneFilterMode.WHITELIST,
                java.util.Collections.emptyList()) : filter;
    }

    private static DroneExecutionResult findFluidContainer(DroneNodeExecutionContext context) {
        Number input = context.getOptionalInput("minimum", Number.class);
        int minimum = input == null ? context.getConfiguration().hasKey("MinimumAmount", 99)
                ? context.getConfiguration().getInteger("MinimumAmount") : 1_000 : input.intValue();
        minimum = Math.max(1, Math.min(1_000_000, minimum));
        BlockPos target = context.getEnvironment().findFluidContainer(
                context.requireInput("area", DroneArea.class),
                parseDirection(context.getConfiguration().getString("Direction")),
                optionalFluidFilter(context), minimum);
        if (target == null) {
            return DroneExecutionResult.failure(DroneActionStatus.NOT_FOUND, "failed",
                    "No loaded matching fluid container was found in the area");
        }
        context.getMemory().setActionPosition(context.getNode().getId(), target);
        return DroneExecutionResult.success();
    }

    private static DroneExecutionResult waitForFluidAmount(DroneNodeExecutionContext context) {
        Number input = context.getOptionalInput("amount", Number.class);
        int threshold = input == null ? context.getConfiguration().hasKey("Amount", 99)
                ? context.getConfiguration().getInteger("Amount") : 1_000 : input.intValue();
        threshold = Math.max(0, Math.min(1_000_000, threshold));
        DroneFluidFilterSpec filter = optionalFluidFilter(context);
        BlockPos target = context.getOptionalInput("target", BlockPos.class);
        int current = target == null
                ? context.getEnvironment().getDroneFluidAmount(filter)
                : context.getEnvironment().getContainerFluidAmount(target,
                        parseDirection(context.getConfiguration().getString("Direction")), filter);
        boolean atMost = "AT_MOST".equals(context.getConfiguration().getString("Operator"));
        boolean complete = atMost ? current <= threshold : current >= threshold;
        return complete ? DroneExecutionResult.success() : DroneExecutionResult.running();
    }

    private static EnumFacing parseDirection(String value) {
        if (value == null || value.isEmpty() || "AUTO".equals(value)) return null;
        try {
            return EnumFacing.valueOf(value);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private static DroneSearchMode parseSearchMode(String value) {
        try {
            DroneSearchMode mode = DroneSearchMode.valueOf(value);
            return mode == DroneSearchMode.EXACT ? DroneSearchMode.NEAREST : mode;
        } catch (IllegalArgumentException ignored) {
            return DroneSearchMode.NEAREST;
        }
    }

    private static DroneExecutionResult interactBlock(DroneNodeExecutionContext context, boolean useHeldItem) {
        NBTTagCompound config = context.getConfiguration();
        DroneInteractionRequest request = new DroneInteractionRequest(
                context.requireInput("target", BlockPos.class), parseDirection(config.getString("Direction")),
                optionalFilter(context).getSpec(), DroneFailurePolicy.ERROR, useHeldItem,
                config.getBoolean("Sneaking"));
        return context.getEnvironment().interactBlock(request);
    }

    private static DroneExecutionResult itemWorldAction(DroneNodeExecutionContext context, boolean pickup) {
        NBTTagCompound config = context.getConfiguration();
        double radius = pickup && config.hasKey("Radius", 99) ? config.getDouble("Radius") : 1.5D;
        int maximum = config.hasKey("MaxAmount", 99) ? config.getInteger("MaxAmount") : 64;
        DroneItemWorldRequest request = new DroneItemWorldRequest(
                Math.max(0.5D, Math.min(32.0D, radius)), Math.max(1, Math.min(1_000_000, maximum)),
                optionalFilter(context).getSpec(), DroneFailurePolicy.ERROR);
        return pickup ? context.getEnvironment().pickupDroppedItems(request)
                : context.getEnvironment().dropItems(request);
    }

    private static DroneExecutionResult tickArea(DroneNodeExecutionContext context, boolean placing) {
        DroneArea area = context.requireInput("area", DroneArea.class);
        if (!area.isWithinRuntimeLimits()) {
            return DroneExecutionResult.error("Area exceeds " + DroneArea.MAX_BLOCKS
                    + " blocks or " + DroneArea.MAX_AXIS_LENGTH + " blocks on one axis");
        }
        NBTTagCompound state = context.getState();
        int index = Math.max(0, state.getInteger("AreaIndex"));
        int total = (int) area.getVolume();
        state.setInteger("AreaTotal", total);
        if (index >= total) return DroneExecutionResult.success();
        BlockPos target = area.positionAt(index);
        DroneExecutionResult result = placing
                ? context.getEnvironment().placeBlockInArea(target, optionalFilter(context))
                : context.getEnvironment().breakBlock(target);
        if (result.getState() != DroneActionState.SUCCESS) return result;
        index++;
        state.setInteger("AreaIndex", index);
        return index >= total ? DroneExecutionResult.success() : DroneExecutionResult.running();
    }

    private static DroneExecutionResult tickWait(DroneNodeExecutionContext context) {
        NBTTagCompound state = context.getState();
        if (!state.hasKey("Remaining")) {
            Number input = context.getOptionalInput("duration", Number.class);
            int configured = input != null ? input.intValue() : context.getConfiguration().hasKey("Ticks")
                    ? context.getConfiguration().getInteger("Ticks") : DEFAULT_WAIT_TICKS;
            state.setInteger("Remaining", Math.max(1, Math.min(MAX_WAIT_TICKS, configured)));
        }
        int remaining = state.getInteger("Remaining") - 1;
        state.setInteger("Remaining", remaining);
        return remaining <= 0 ? DroneExecutionResult.success() : DroneExecutionResult.running();
    }

    private static DroneExecutionResult tickWaitMachineCycle(DroneNodeExecutionContext context) {
        BlockPos target = context.requireInput("target", BlockPos.class);
        NBTTagCompound state = context.getState();
        boolean observedActive = state.getBoolean("ObservedActive");
        double previousProgress = state.hasKey("PreviousProgress", 99)
                ? state.getDouble("PreviousProgress") : -1.0D;
        DroneExecutionResult result = context.getEnvironment().waitForMachineCycle(
                target, observedActive, previousProgress);
        if (result.getState() == DroneActionState.RUNNING) {
            double currentProgress = context.getEnvironment().getMachineProgressPercent(target);
            if (context.getEnvironment().isMachineActive(target) || currentProgress > 0.0D) {
                state.setBoolean("ObservedActive", true);
            }
            if (currentProgress >= 0.0D) state.setDouble("PreviousProgress", currentProgress);
        }
        return result;
    }

    private static DroneExecutionResult tickRepeat(DroneNodeExecutionContext context) {
        Number input = context.getOptionalInput("count", Number.class);
        int configured = input == null ? context.getConfiguration().hasKey("Count")
                ? context.getConfiguration().getInteger("Count") : 1 : input.intValue();
        int count = Math.max(0, Math.min(MAX_REPEAT_COUNT, configured));
        int iteration = context.getMemory().getLoopIteration(context.getNode().getId());
        if (iteration >= count) {
            context.getMemory().clearLoop(context.getNode().getId());
            return DroneExecutionResult.success("done");
        }
        context.getMemory().setLoopIteration(context.getNode().getId(), iteration + 1);
        return DroneExecutionResult.success("body");
    }

    private static DroneExecutionResult tickForEachCoordinate(DroneNodeExecutionContext context) {
        DroneArea area = context.requireInput("area", DroneArea.class);
        if (!area.isWithinRuntimeLimits()) return DroneExecutionResult.error("Area exceeds runtime limits");
        int index = context.getMemory().getLoopIteration(context.getNode().getId());
        if (index == 0) {
            context.getMemory().setLoopTraversalIndices(context.getNode().getId(), area.traversalIndices(
                    DroneArea.TraversalOrder.fromName(context.getConfiguration().getString("Order")),
                    context.getEnvironment().getCurrentPosition()));
        }
        boolean skipAir = context.getConfiguration().getBoolean("SkipAir");
        boolean skipNonMatching = context.getConfiguration().getBoolean("SkipNonMatching");
        DroneBlockFilterSpec blockFilter = context.getOptionalInput("block_filter", DroneBlockFilterSpec.class);
        if (skipNonMatching && blockFilter == null) {
            return DroneExecutionResult.error("Skip non-matching blocks requires a block filter");
        }
        while (index < area.getVolume()) {
            int selectedIndex = context.getMemory().getLoopTraversalIndex(context.getNode().getId(), index,
                    (int) area.getVolume());
            BlockPos target = area.positionAt(selectedIndex >= 0 ? selectedIndex : index);
            if (!(skipAir && context.getEnvironment().isAirBlock(target))
                    && !(skipNonMatching && !context.getEnvironment().matchesBlock(target, blockFilter))) break;
            index++;
            context.getMemory().setLoopIteration(context.getNode().getId(), index);
        }
        if (index >= area.getVolume()) {
            context.getMemory().clearLoop(context.getNode().getId());
            return DroneExecutionResult.success("done");
        }
        int selectedIndex = context.getMemory().getLoopTraversalIndex(context.getNode().getId(), index,
                (int) area.getVolume());
        context.getMemory().setActionPosition(context.getNode().getId(),
                area.positionAt(selectedIndex >= 0 ? selectedIndex : index));
        context.getMemory().setLoopIteration(context.getNode().getId(), index + 1);
        return DroneExecutionResult.success("body");
    }

    private static DroneExecutionResult tickForEachItemFilter(DroneNodeExecutionContext context) {
        DroneItemFilter source = context.requireInput("filter", DroneItemFilter.class);
        java.util.List<DroneItemFilterSpec.Rule> rules = source.getSpec().getRules();
        int index = context.getMemory().getLoopIteration(context.getNode().getId());
        if (index >= rules.size()) {
            context.getMemory().clearLoop(context.getNode().getId());
            context.getMemory().clearCurrentItemFilter(context.getNode().getId());
            return DroneExecutionResult.success("done");
        }
        DroneItemFilter current = DroneItemFilter.fromSpec(new DroneItemFilterSpec(DroneFilterMode.WHITELIST,
                Collections.singletonList(rules.get(index))));
        context.getMemory().setCurrentItemFilter(context.getNode().getId(), current);
        context.getMemory().setLoopIteration(context.getNode().getId(), index + 1);
        return DroneExecutionResult.success("body");
    }

    private static String variableName(DroneNodeExecutionContext context) {
        String name = context.getConfiguration().getString("Name");
        return name.isEmpty() ? "value" : name;
    }

    private static boolean isLocalVariable(DroneNodeExecutionContext context) {
        return "LOCAL".equalsIgnoreCase(context.getConfiguration().getString("Scope"));
    }
}
