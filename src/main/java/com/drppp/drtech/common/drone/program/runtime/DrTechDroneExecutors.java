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
import com.drppp.drtech.common.drone.program.model.DroneArea;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;

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
        registry.register(DrTechDroneNodes.INTERACT_BLOCK, context -> interactBlock(context, false));
        registry.register(DrTechDroneNodes.USE_ITEM_ON_BLOCK, context -> interactBlock(context, true));
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
            context.getMemory().setNumber(variableName(context), context.requireInput("value", Number.class).doubleValue());
            return DroneExecutionResult.success();
        });
        registry.register(DrTechDroneNodes.ADD_NUMBER_VARIABLE, context -> {
            context.getMemory().addNumber(variableName(context), context.requireInput("amount", Number.class).doubleValue());
            return DroneExecutionResult.success();
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
        registry.freeze();
        return registry;
    }

    private static DroneItemFilter optionalFilter(DroneNodeExecutionContext context) {
        DroneItemFilter filter = context.getOptionalInput("filter", DroneItemFilter.class);
        return filter == null ? DroneItemFilter.ANY : filter;
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
        if (index >= area.getVolume()) {
            context.getMemory().clearLoop(context.getNode().getId());
            return DroneExecutionResult.success("done");
        }
        context.getMemory().setLoopIteration(context.getNode().getId(), index + 1);
        return DroneExecutionResult.success("body");
    }

    private static String variableName(DroneNodeExecutionContext context) {
        String name = context.getConfiguration().getString("Name");
        return name.isEmpty() ? "value" : name;
    }
}
