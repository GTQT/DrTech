package com.drppp.drtech.common.drone.program.runtime;

import com.drppp.drtech.common.drone.program.registry.DrTechDroneNodes;
import com.drppp.drtech.common.drone.inventory.DroneItemFilter;
import com.drppp.drtech.common.drone.filter.DroneBlockFilterSpec;
import com.drppp.drtech.common.drone.filter.DroneFluidFilterSpec;
import com.drppp.drtech.common.drone.filter.DroneEntityFilterSpec;
import com.drppp.drtech.common.drone.filter.DroneFilterMode;
import com.drppp.drtech.common.drone.program.model.DroneArea;
import com.drppp.drtech.common.drone.program.model.DroneProgramReference;
import com.drppp.drtech.common.drone.program.runtime.service.DroneSensorService;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

public final class DrTechDroneValueEvaluators {

    private DrTechDroneValueEvaluators() {}

    public static DroneValueEvaluatorRegistry createDefaultRegistry() {
        DroneValueEvaluatorRegistry registry = new DroneValueEvaluatorRegistry();
        registry.register(DrTechDroneNodes.NUMBER, (context, output) ->
                context.getConfiguration().getDouble("Value"));
        registry.register(DrTechDroneNodes.BOOLEAN, (context, output) ->
                context.getConfiguration().getBoolean("Value"));
        registry.register(DrTechDroneNodes.COORDINATE, (context, output) -> coordinate(context.getConfiguration()));
        registry.register(DrTechDroneNodes.DOCK_REFERENCE, (context, output) -> {
            NBTTagCompound dock = context.getConfiguration().getCompoundTag("Dock");
            if (!dock.hasKey("Position", 4)) {
                throw new IllegalStateException("Dock reference is not selected");
            }
            return BlockPos.fromLong(dock.getLong("Position"));
        });
        registry.register(DrTechDroneNodes.PROGRAM_REFERENCE, (context, output) -> {
            NBTTagCompound program = context.getConfiguration().getCompoundTag("Program");
            if (!program.hasKey("ProgramId", 8) || !program.hasKey("Revision", 99)) {
                throw new IllegalStateException("Program reference is not selected");
            }
            try {
                return new DroneProgramReference(java.util.UUID.fromString(program.getString("ProgramId")),
                        program.getLong("Revision"), program.getString("Name"));
            } catch (IllegalArgumentException exception) {
                throw new IllegalStateException("Program reference id is invalid", exception);
            }
        });
        registry.register(DrTechDroneNodes.AREA, (context, output) -> area(context.getConfiguration()));
        registry.register(DrTechDroneNodes.ITEM_FILTER, (context, output) ->
                DroneItemFilter.fromConfiguration(context.getConfiguration()));
        registry.register(DrTechDroneNodes.BLOCK_FILTER, (context, output) ->
                DroneBlockFilterSpec.readFromNbt(context.getConfiguration().hasKey("FilterSpec", 10)
                        ? context.getConfiguration().getCompoundTag("FilterSpec") : null));
        registry.register(DrTechDroneNodes.FLUID_FILTER, (context, output) -> {
            String fluid = context.getConfiguration().getString("Fluid").trim();
            java.util.List<String> names = fluid.isEmpty() ? java.util.Collections.emptyList()
                    : java.util.Collections.singletonList(fluid);
            return new DroneFluidFilterSpec(
                    DroneFilterMode.fromName(context.getConfiguration().getString("Mode")), names);
        });
        registry.register(DrTechDroneNodes.ENTITY_FILTER, (context, output) ->
                DroneEntityFilterSpec.readFromNbt(context.getConfiguration().hasKey("FilterSpec", 10)
                        ? context.getConfiguration().getCompoundTag("FilterSpec") : null));
        DroneValueEvaluator actionAmount = (context, output) ->
                context.getMemory().getActionAmount(context.getNode().getId());
        registry.register(DrTechDroneNodes.IMPORT_ITEMS, actionAmount);
        registry.register(DrTechDroneNodes.EXPORT_ITEMS, actionAmount);
        registry.register(DrTechDroneNodes.PICKUP_DROPPED_ITEMS, actionAmount);
        registry.register(DrTechDroneNodes.DROP_ITEMS, actionAmount);
        registry.register(DrTechDroneNodes.IMPORT_FLUID, actionAmount);
        registry.register(DrTechDroneNodes.EXPORT_FLUID, actionAmount);
        registry.register(DrTechDroneNodes.DRAIN_FLUID, actionAmount);
        registry.register(DrTechDroneNodes.CRAFT_ITEMS, actionAmount);
        registry.register(DrTechDroneNodes.CRAFT_GRID, actionAmount);
        registry.register(DrTechDroneNodes.CAN_CRAFT, (context, output) -> {
            Number input = context.getOptionalInput("count", Number.class);
            int count = input == null ? context.getConfiguration().hasKey("Count", 99)
                    ? context.getConfiguration().getInteger("Count") : 1 : input.intValue();
            Number reserveInput = context.getOptionalInput("reserve_count", Number.class);
            int reserveCount = reserveInput == null ? context.getConfiguration().getInteger("ReserveCount")
                    : reserveInput.intValue();
            return context.getEnvironment().getCraftableCount(
                    context.requireInput("output", DroneItemFilter.class), Math.max(1, Math.min(64, count)),
                    context.getOptionalInput("reserve_filter", DroneItemFilter.class),
                    Math.max(0, Math.min(64, reserveCount)))
                    >= Math.max(1, Math.min(64, count));
        });
        registry.register(DrTechDroneNodes.CRAFTABLE_COUNT, (context, output) -> {
            int limit = context.getConfiguration().hasKey("Limit", 99)
                    ? context.getConfiguration().getInteger("Limit") : 64;
            Number reserveInput = context.getOptionalInput("reserve_count", Number.class);
            int reserveCount = reserveInput == null ? context.getConfiguration().getInteger("ReserveCount")
                    : reserveInput.intValue();
            return context.getEnvironment().getCraftableCount(
                    context.requireInput("output", DroneItemFilter.class), Math.max(1, Math.min(64, limit)),
                    context.getOptionalInput("reserve_filter", DroneItemFilter.class),
                    Math.max(0, Math.min(64, reserveCount)));
        });
        registry.register(DrTechDroneNodes.MACHINE_ACTIVE, (context, output) ->
                context.getEnvironment().isMachineActive(context.requireInput("target", BlockPos.class)));
        registry.register(DrTechDroneNodes.MACHINE_ENABLED, (context, output) ->
                context.getEnvironment().isMachineWorkingEnabled(context.requireInput("target", BlockPos.class)));
        registry.register(DrTechDroneNodes.MACHINE_PROGRESS, (context, output) ->
                context.getEnvironment().getMachineProgressPercent(context.requireInput("target", BlockPos.class)));
        registry.register(DrTechDroneNodes.MACHINE_WAITING_INPUT, (context, output) ->
                context.getEnvironment().isMachineWaitingForInput(context.requireInput("target", BlockPos.class)));
        registry.register(DrTechDroneNodes.MACHINE_OUTPUT_BLOCKED, (context, output) ->
                context.getEnvironment().isMachineOutputBlocked(context.requireInput("target", BlockPos.class)));
        registry.register(DrTechDroneNodes.MACHINE_LOW_ENERGY, (context, output) ->
                context.getEnvironment().isMachineLowEnergy(context.requireInput("target", BlockPos.class)));
        registry.register(DrTechDroneNodes.MACHINE_DIAGNOSTIC, (context, output) ->
                context.getEnvironment().getMachineDiagnostic(context.requireInput("target", BlockPos.class)));
        registry.register(DrTechDroneNodes.REPAIR_MACHINE, actionAmount);
        registry.register(DrTechDroneNodes.MACHINE_NEEDS_MAINTENANCE, (context, output) ->
                context.getEnvironment().needsMachineMaintenance(context.requireInput("target", BlockPos.class)));
        registry.register(DrTechDroneNodes.MACHINE_MAINTENANCE_PROBLEMS, (context, output) ->
                (double) context.getEnvironment().getMachineMaintenanceProblemCount(
                        context.requireInput("target", BlockPos.class)));
        registry.register(DrTechDroneNodes.ENERGY_LEVEL, (context, output) ->
                context.getEnvironment().getEnergyPercent());
        DroneValueEvaluator energyAmount = (context, output) ->
                context.getMemory().getActionAmount(context.getNode().getId());
        registry.register(DrTechDroneNodes.IMPORT_EU, energyAmount);
        registry.register(DrTechDroneNodes.EXPORT_EU, energyAmount);
        registry.register(DrTechDroneNodes.CHARGE_TARGET_PERCENT, energyAmount);
        registry.register(DrTechDroneNodes.TARGET_ENERGY, (context, output) ->
                context.getEnvironment().getTargetEnergy(context.requireInput("target", BlockPos.class)));
        registry.register(DrTechDroneNodes.TARGET_ENERGY_CAPACITY, (context, output) ->
                context.getEnvironment().getTargetEnergyCapacity(context.requireInput("target", BlockPos.class)));
        registry.register(DrTechDroneNodes.TARGET_ENERGY_PERCENT, (context, output) ->
                context.getEnvironment().getTargetEnergyPercent(context.requireInput("target", BlockPos.class)));
        registry.register(DrTechDroneNodes.CARGO_ITEM_COUNT, (context, output) ->
                context.getEnvironment().getCargoItemCount(optionalFilter(context)));
        registry.register(DrTechDroneNodes.CARGO_FREE_SLOTS, (context, output) ->
                context.getEnvironment().getCargoFreeSlots());
        registry.register(DrTechDroneNodes.CARGO_USED_PERCENT, (context, output) ->
                context.getEnvironment().getCargoUsedPercent());
        registry.register(DrTechDroneNodes.INVENTORY_ITEM_COUNT, (context, output) ->
                context.getEnvironment().getInventoryItemCount(context.requireInput("target", BlockPos.class),
                        parseDirection(context.getConfiguration().getString("Direction")), optionalFilter(context)));
        registry.register(DrTechDroneNodes.DRONE_FLUID_AMOUNT, (context, output) ->
                context.getEnvironment().getDroneFluidAmount(optionalFluidFilter(context)));
        registry.register(DrTechDroneNodes.DRONE_FLUID_PERCENT, (context, output) -> {
            int capacity = context.getEnvironment().getDroneFluidCapacity();
            return capacity <= 0 ? 0.0D : context.getEnvironment().getDroneFluidAmount(
                    new DroneFluidFilterSpec(DroneFilterMode.WHITELIST, java.util.Collections.emptyList()))
                    * 100.0D / capacity;
        });
        registry.register(DrTechDroneNodes.CONTAINER_FLUID_AMOUNT, (context, output) ->
                context.getEnvironment().getContainerFluidAmount(
                        context.requireInput("target", BlockPos.class),
                        parseDirection(context.getConfiguration().getString("Direction")),
                        optionalFluidFilter(context)));
        registry.register(DrTechDroneNodes.FIND_FLUID_CONTAINER, (context, output) ->
                context.getMemory().getActionPosition(context.getNode().getId()));
        registry.register(DrTechDroneNodes.REDSTONE_STRENGTH, (context, output) ->
                context.getEnvironment().getRedstoneStrength(context.requireInput("target", BlockPos.class)));
        registry.register(DrTechDroneNodes.REDSTONE_OUTPUT_LEVEL, (context, output) ->
                context.getEnvironment().getRedstoneOutputStrength(
                        context.requireInput("target", BlockPos.class)));
        registry.register(DrTechDroneNodes.LIGHT_LEVEL, (context, output) ->
                context.getEnvironment().getLightLevel(context.requireInput("target", BlockPos.class),
                        parseLightType(context.getConfiguration().getString("LightType"))));
        registry.register(DrTechDroneNodes.LAST_ACTION_STATUS, (context, output) ->
                context.getMemory().getLastActionStatus());
        registry.register(DrTechDroneNodes.LAST_ACTION_ERROR, (context, output) ->
                context.getMemory().getLastActionError());
        registry.register(DrTechDroneNodes.COMPARE_ACTION_STATUS, (context, output) -> {
            DroneActionStatus actual = context.requireInput("status", DroneActionStatus.class);
            try {
                return actual == DroneActionStatus.valueOf(context.getConfiguration().getString("Status"));
            } catch (IllegalArgumentException ignored) {
                return actual == DroneActionStatus.SUCCESS;
            }
        });
        registry.register(DrTechDroneNodes.BLOCK_MATCHES, (context, output) ->
                context.getEnvironment().matchesBlock(context.requireInput("target", BlockPos.class),
                        optionalBlockFilter(context)));
        registry.register(DrTechDroneNodes.COORDINATE_REACHABLE, (context, output) ->
                context.getEnvironment().isCoordinateReachable(context.requireInput("target", BlockPos.class)));
        registry.register(DrTechDroneNodes.DOCK_AVAILABLE, (context, output) ->
                context.getEnvironment().isDockAvailable(context.requireInput("target", BlockPos.class)));
        registry.register(DrTechDroneNodes.FIND_NEAREST_DOCK, (context, output) ->
                context.getEnvironment().findNearestDock());
        registry.register(DrTechDroneNodes.AREA_BLOCK_COUNT, (context, output) -> {
            int limit = context.getConfiguration().hasKey("Limit", 99)
                    ? context.getConfiguration().getInteger("Limit") : DroneArea.MAX_BLOCKS;
            return context.getEnvironment().countMatchingBlocks(context.requireInput("area", DroneArea.class),
                    optionalBlockFilter(context), Math.max(1, Math.min(DroneArea.MAX_BLOCKS, limit)));
        });
        registry.register(DrTechDroneNodes.COMPARE_NUMBER, DrTechDroneValueEvaluators::compareNumbers);
        registry.register(DrTechDroneNodes.GET_NUMBER_VARIABLE, (context, output) -> {
            String name = context.getConfiguration().getString("Name");
            return context.getMemory().getNumber(name.isEmpty() ? "value" : name);
        });
        registry.register(DrTechDroneNodes.NUMBER_MATH, DrTechDroneValueEvaluators::numberMath);
        registry.register(DrTechDroneNodes.BOOLEAN_LOGIC, DrTechDroneValueEvaluators::booleanLogic);
        registry.register(DrTechDroneNodes.BOOLEAN_NOT, (context, output) ->
                !context.requireInput("value", Boolean.class));
        registry.register(DrTechDroneNodes.COORDINATE_OFFSET, DrTechDroneValueEvaluators::coordinateOffset);
        registry.register(DrTechDroneNodes.AREA_FROM_CORNERS, (context, output) ->
                DroneArea.between(context.requireInput("first", BlockPos.class),
                        context.requireInput("second", BlockPos.class)));
        registry.register(DrTechDroneNodes.FOR_EACH_COORDINATE, (context, output) -> {
            DroneArea area = context.requireInput("area", DroneArea.class);
            int nextIndex = context.getMemory().getLoopIteration(context.getNode().getId());
            if (nextIndex <= 0 || nextIndex > area.getVolume()) {
                throw new IllegalStateException("For Each coordinate is only available inside its body branch");
            }
            return area.positionAt(nextIndex - 1,
                    parseTraversalOrder(context.getConfiguration().getString("Order")));
        });
        registry.register(DrTechDroneNodes.SPHERE_AREA, (context, output) -> {
            NBTTagCompound config = context.getConfiguration();
            Number radiusInput = context.getOptionalInput("radius", Number.class);
            int radius = radiusInput == null ? config.getInteger("Radius") : radiusInput.intValue();
            return DroneArea.sphere(context.requireInput("center", BlockPos.class),
                    Math.max(1, Math.min(9, radius)), config.getBoolean("Hollow"));
        });
        registry.register(DrTechDroneNodes.CYLINDER_AREA, (context, output) -> {
            NBTTagCompound config = context.getConfiguration();
            Number radiusInput = context.getOptionalInput("radius", Number.class);
            Number heightInput = context.getOptionalInput("height", Number.class);
            int radius = radiusInput == null ? config.getInteger("Radius") : radiusInput.intValue();
            int height = heightInput == null ? config.getInteger("Height") : heightInput.intValue();
            return DroneArea.cylinder(context.requireInput("center", BlockPos.class),
                    Math.max(1, Math.min(8, radius)), Math.max(1, Math.min(16, height)),
                    config.getBoolean("Hollow"));
        });
        registry.register(DrTechDroneNodes.PATH_AREA, (context, output) -> {
            Number input = context.getOptionalInput("radius", Number.class);
            int radius = input == null ? context.getConfiguration().getInteger("Radius") : input.intValue();
            return DroneArea.path(context.requireInput("first", BlockPos.class),
                    context.requireInput("second", BlockPos.class), Math.max(0, Math.min(3, radius)));
        });
        registry.register(DrTechDroneNodes.AREA_UNION, (context, output) ->
                context.requireInput("first", DroneArea.class).union(
                        context.requireInput("second", DroneArea.class)));
        registry.register(DrTechDroneNodes.AREA_INTERSECTION, (context, output) ->
                context.requireInput("first", DroneArea.class).intersection(
                        context.requireInput("second", DroneArea.class)));
        registry.register(DrTechDroneNodes.AREA_DIFFERENCE, (context, output) ->
                context.requireInput("first", DroneArea.class).difference(
                        context.requireInput("second", DroneArea.class)));
        registry.register(DrTechDroneNodes.AREA_OFFSET, (context, output) -> {
            NBTTagCompound config = context.getConfiguration();
            return context.requireInput("area", DroneArea.class).offset(
                    offset(context, config, "x", "X"), offset(context, config, "y", "Y"),
                    offset(context, config, "z", "Z"));
        });
        registry.register(DrTechDroneNodes.AREA_EXPAND, (context, output) -> {
            Number input = context.getOptionalInput("radius", Number.class);
            int radius = input == null ? context.getConfiguration().getInteger("Radius") : input.intValue();
            return context.requireInput("area", DroneArea.class).expand(Math.max(0, Math.min(4, radius)));
        });
        registry.register(DrTechDroneNodes.AREA_INSET, (context, output) -> {
            Number input = context.getOptionalInput("radius", Number.class);
            int radius = input == null ? context.getConfiguration().getInteger("Radius") : input.intValue();
            return context.requireInput("area", DroneArea.class).inset(Math.max(0, Math.min(4, radius)));
        });
        registry.register(DrTechDroneNodes.AREA_CONTAINS, (context, output) ->
                context.requireInput("area", DroneArea.class).contains(
                        context.requireInput("coordinate", BlockPos.class)));
        registry.register(DrTechDroneNodes.AREA_VOLUME, (context, output) ->
                (double) context.requireInput("area", DroneArea.class).getVolume());
        registry.register(DrTechDroneNodes.PLANE_AREA, (context, output) ->
                DroneArea.plane(context.requireInput("origin", BlockPos.class),
                        context.requireInput("first", BlockPos.class),
                        context.requireInput("second", BlockPos.class)));
        registry.freeze();
        return registry;
    }

    private static BlockPos coordinate(NBTTagCompound configuration) {
        return new BlockPos(configuration.getInteger("X"), configuration.getInteger("Y"),
                configuration.getInteger("Z"));
    }

    private static DroneArea area(NBTTagCompound configuration) {
        BlockPos first = new BlockPos(configuration.getInteger("X1"), configuration.getInteger("Y1"),
                configuration.getInteger("Z1"));
        BlockPos second = new BlockPos(configuration.getInteger("X2"), configuration.getInteger("Y2"),
                configuration.getInteger("Z2"));
        return DroneArea.between(first, second);
    }

    private static Object compareNumbers(DroneValueEvaluationContext context, String outputPort) {
        double left = context.requireInput("left", Number.class).doubleValue();
        double right = context.requireInput("right", Number.class).doubleValue();
        String operator = context.getConfiguration().getString("Operator");
        return switch (operator) {
            case "<" -> left < right;
            case "<=" -> left <= right;
            case ">" -> left > right;
            case ">=" -> left >= right;
            case "!=" -> Double.compare(left, right) != 0;
            default -> Double.compare(left, right) == 0;
        };
    }

    private static Object numberMath(DroneValueEvaluationContext context, String outputPort) {
        double left = context.requireInput("left", Number.class).doubleValue();
        double right = context.requireInput("right", Number.class).doubleValue();
        String operator = context.getConfiguration().getString("Operator");
        double result = switch (operator) {
            case "-" -> left - right;
            case "*" -> left * right;
            case "/" -> {
                if (right == 0.0D) throw new IllegalStateException("Division by zero");
                yield left / right;
            }
            case "%" -> {
                if (right == 0.0D) throw new IllegalStateException("Modulo by zero");
                yield left % right;
            }
            case "min" -> Math.min(left, right);
            case "max" -> Math.max(left, right);
            default -> left + right;
        };
        if (!Double.isFinite(result)) throw new IllegalStateException("Math result is not finite");
        return result;
    }

    private static Object booleanLogic(DroneValueEvaluationContext context, String outputPort) {
        boolean left = context.requireInput("left", Boolean.class);
        boolean right = context.requireInput("right", Boolean.class);
        return switch (context.getConfiguration().getString("Operator")) {
            case "OR" -> left || right;
            case "XOR" -> left ^ right;
            default -> left && right;
        };
    }

    private static Object coordinateOffset(DroneValueEvaluationContext context, String outputPort) {
        BlockPos base = context.requireInput("base", BlockPos.class);
        NBTTagCompound configuration = context.getConfiguration();
        int x = clamp((long) base.getX() + offset(context, configuration, "x", "X"), -30_000_000, 30_000_000);
        int y = clamp((long) base.getY() + offset(context, configuration, "y", "Y"), -2_048, 2_047);
        int z = clamp((long) base.getZ() + offset(context, configuration, "z", "Z"), -30_000_000, 30_000_000);
        return new BlockPos(x, y, z);
    }

    private static int offset(DroneValueEvaluationContext context, NBTTagCompound config, String port, String key) {
        Number input = context.getOptionalInput(port, Number.class);
        return input == null ? config.getInteger(key) : input.intValue();
    }

    private static int clamp(long value, int min, int max) {
        return (int) Math.max(min, Math.min(max, value));
    }

    private static DroneItemFilter optionalFilter(DroneValueEvaluationContext context) {
        DroneItemFilter filter = context.getOptionalInput("filter", DroneItemFilter.class);
        return filter == null ? DroneItemFilter.ANY : filter;
    }

    private static DroneBlockFilterSpec optionalBlockFilter(DroneValueEvaluationContext context) {
        DroneBlockFilterSpec filter = context.getOptionalInput("filter", DroneBlockFilterSpec.class);
        return filter == null ? DroneBlockFilterSpec.ANY : filter;
    }

    private static DroneFluidFilterSpec optionalFluidFilter(DroneValueEvaluationContext context) {
        DroneFluidFilterSpec filter = context.getOptionalInput("filter", DroneFluidFilterSpec.class);
        return filter == null ? new DroneFluidFilterSpec(DroneFilterMode.WHITELIST,
                java.util.Collections.emptyList()) : filter;
    }

    private static EnumFacing parseDirection(String value) {
        if (value == null || value.isEmpty() || "AUTO".equals(value)) return null;
        try {
            return EnumFacing.valueOf(value);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private static DroneSensorService.LightType parseLightType(String value) {
        try {
            return DroneSensorService.LightType.valueOf(value);
        } catch (IllegalArgumentException ignored) {
            return DroneSensorService.LightType.MAX;
        }
    }

    private static DroneArea.TraversalOrder parseTraversalOrder(String value) {
        try {
            return DroneArea.TraversalOrder.valueOf(value);
        } catch (IllegalArgumentException ignored) {
            return DroneArea.TraversalOrder.SERPENTINE;
        }
    }
}
