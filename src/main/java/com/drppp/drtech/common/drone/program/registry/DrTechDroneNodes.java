package com.drppp.drtech.common.drone.program.registry;

import com.drppp.drtech.common.drone.program.model.DroneNodeDefinition;
import com.drppp.drtech.common.drone.program.model.DroneNodePropertyDefinition;
import com.drppp.drtech.common.drone.program.model.DroneNodePropertyType;
import com.drppp.drtech.common.drone.program.model.DronePortDefinition;
import com.drppp.drtech.common.drone.program.model.DronePortType;
import net.minecraft.util.ResourceLocation;

/**
 * Built-in node catalogue for the first editor/runtime slice.
 *
 * <p>Definitions contain only stable schema. Runtime executors will be bound separately, so clients may render and
 * validate a graph without loading server action implementations.</p>
 */
public final class DrTechDroneNodes {

    public static final ResourceLocation START = id("start");
    public static final ResourceLocation END = id("end");
    public static final ResourceLocation WAIT = id("wait");
    public static final ResourceLocation BRANCH = id("branch");
    public static final ResourceLocation MOVE_TO = id("move_to");
    public static final ResourceLocation RETURN_TO_DOCK = id("return_to_dock");
    public static final ResourceLocation CHARGE_UNTIL = id("charge_until");
    public static final ResourceLocation FIND_NEAREST_DOCK = id("find_nearest_dock");
    public static final ResourceLocation BIND_DOCK = id("bind_dock");
    public static final ResourceLocation UNBIND_DOCK = id("unbind_dock");
    public static final ResourceLocation CONFIGURE_SAFETY = id("configure_safety");
    public static final ResourceLocation IMPORT_EU = id("import_eu");
    public static final ResourceLocation EXPORT_EU = id("export_eu");
    public static final ResourceLocation CHARGE_TARGET_PERCENT = id("charge_target_percent");
    public static final ResourceLocation BREAK_BLOCK = id("break_block");
    public static final ResourceLocation BREAK_BLOCK_AT = id("break_block_at");
    public static final ResourceLocation PLACE_BLOCK = id("place_block");
    public static final ResourceLocation PLACE_AREA = id("place_area");
    public static final ResourceLocation IMPORT_ITEMS = id("import_items");
    public static final ResourceLocation EXPORT_ITEMS = id("export_items");
    public static final ResourceLocation IMPORT_FLUID = id("import_fluid");
    public static final ResourceLocation EXPORT_FLUID = id("export_fluid");
    public static final ResourceLocation DRAIN_FLUID = id("drain_fluid");
    public static final ResourceLocation NUMBER = id("number");
    public static final ResourceLocation BOOLEAN = id("boolean");
    public static final ResourceLocation COORDINATE = id("coordinate");
    public static final ResourceLocation AREA = id("area");
    public static final ResourceLocation ITEM_FILTER = id("item_filter");
    public static final ResourceLocation BLOCK_FILTER = id("block_filter");
    public static final ResourceLocation FLUID_FILTER = id("fluid_filter");
    public static final ResourceLocation ENERGY_LEVEL = id("energy_level");
    public static final ResourceLocation TARGET_ENERGY = id("target_energy");
    public static final ResourceLocation TARGET_ENERGY_CAPACITY = id("target_energy_capacity");
    public static final ResourceLocation TARGET_ENERGY_PERCENT = id("target_energy_percent");
    public static final ResourceLocation CARGO_ITEM_COUNT = id("cargo_item_count");
    public static final ResourceLocation CARGO_FREE_SLOTS = id("cargo_free_slots");
    public static final ResourceLocation CARGO_USED_PERCENT = id("cargo_used_percent");
    public static final ResourceLocation INVENTORY_ITEM_COUNT = id("inventory_item_count");
    public static final ResourceLocation DRONE_FLUID_AMOUNT = id("drone_fluid_amount");
    public static final ResourceLocation DRONE_FLUID_PERCENT = id("drone_fluid_percent");
    public static final ResourceLocation CONTAINER_FLUID_AMOUNT = id("container_fluid_amount");
    public static final ResourceLocation FIND_FLUID_CONTAINER = id("find_fluid_container");
    public static final ResourceLocation WAIT_FOR_FLUID_AMOUNT = id("wait_for_fluid_amount");
    public static final ResourceLocation REDSTONE_STRENGTH = id("redstone_strength");
    public static final ResourceLocation LIGHT_LEVEL = id("light_level");
    public static final ResourceLocation LAST_ACTION_STATUS = id("last_action_status");
    public static final ResourceLocation LAST_ACTION_ERROR = id("last_action_error");
    public static final ResourceLocation COMPARE_ACTION_STATUS = id("compare_action_status");
    public static final ResourceLocation BLOCK_MATCHES = id("block_matches");
    public static final ResourceLocation COORDINATE_REACHABLE = id("coordinate_reachable");
    public static final ResourceLocation DOCK_AVAILABLE = id("dock_available");
    public static final ResourceLocation INTERACT_BLOCK = id("interact_block");
    public static final ResourceLocation USE_ITEM_ON_BLOCK = id("use_item_on_block");
    public static final ResourceLocation AREA_BLOCK_COUNT = id("area_block_count");
    public static final ResourceLocation PICKUP_DROPPED_ITEMS = id("pickup_dropped_items");
    public static final ResourceLocation DROP_ITEMS = id("drop_items");
    public static final ResourceLocation HARVEST_CROP = id("harvest_crop");
    public static final ResourceLocation SET_REDSTONE_OUTPUT = id("set_redstone_output");
    public static final ResourceLocation REDSTONE_OUTPUT_LEVEL = id("redstone_output_level");
    public static final ResourceLocation COMPARE_NUMBER = id("compare_number");
    public static final ResourceLocation GET_NUMBER_VARIABLE = id("get_number_variable");
    public static final ResourceLocation SET_NUMBER_VARIABLE = id("set_number_variable");
    public static final ResourceLocation ADD_NUMBER_VARIABLE = id("add_number_variable");
    public static final ResourceLocation REPEAT = id("repeat");
    public static final ResourceLocation WAIT_FOR_REDSTONE = id("wait_for_redstone");
    public static final ResourceLocation WAIT_FOR_OWNER = id("wait_for_owner");
    public static final ResourceLocation NUMBER_MATH = id("number_math");
    public static final ResourceLocation BOOLEAN_LOGIC = id("boolean_logic");
    public static final ResourceLocation BOOLEAN_NOT = id("boolean_not");
    public static final ResourceLocation COORDINATE_OFFSET = id("coordinate_offset");
    public static final ResourceLocation AREA_FROM_CORNERS = id("area_from_corners");
    public static final ResourceLocation WHILE = id("while");
    public static final ResourceLocation FOR_EACH_COORDINATE = id("for_each_coordinate");
    public static final ResourceLocation SPHERE_AREA = id("sphere_area");
    public static final ResourceLocation CYLINDER_AREA = id("cylinder_area");
    public static final ResourceLocation PATH_AREA = id("path_area");
    public static final ResourceLocation AREA_UNION = id("area_union");
    public static final ResourceLocation AREA_INTERSECTION = id("area_intersection");
    public static final ResourceLocation AREA_DIFFERENCE = id("area_difference");
    public static final ResourceLocation AREA_OFFSET = id("area_offset");
    public static final ResourceLocation AREA_CONTAINS = id("area_contains");
    public static final ResourceLocation AREA_VOLUME = id("area_volume");
    public static final ResourceLocation PLANE_AREA = id("plane_area");

    private DrTechDroneNodes() {}

    public static DroneNodeRegistry createDefaultRegistry() {
        DroneNodeRegistry registry = new DroneNodeRegistry();
        registerDefaults(registry);
        registry.freeze();
        return registry;
    }

    public static void registerDefaults(DroneNodeRegistry registry) {
        registry.register(DroneNodeDefinition.builder(START, DroneNodeDefinition.FlowRole.ENTRY)
                .category("flow")
                .port(DronePortDefinition.output("next", DronePortType.FLOW, true))
                .build());
        registry.register(DroneNodeDefinition.builder(END, DroneNodeDefinition.FlowRole.TERMINAL)
                .category("flow")
                .port(DronePortDefinition.input("in", DronePortType.FLOW, true))
                .build());
        registry.register(action(WAIT, "flow")
                .port(DronePortDefinition.input("duration", DronePortType.NUMBER, false))
                .property(DroneNodePropertyDefinition.integer("Ticks", 1, 20 * 60 * 60))
                .build());
        registry.register(DroneNodeDefinition.builder(BRANCH, DroneNodeDefinition.FlowRole.NORMAL)
                .category("flow")
                .port(DronePortDefinition.input("in", DronePortType.FLOW, true))
                .port(DronePortDefinition.input("condition", DronePortType.BOOLEAN, true))
                .port(DronePortDefinition.output("true", DronePortType.FLOW, true))
                .port(DronePortDefinition.output("false", DronePortType.FLOW, true))
                .build());
        registry.register(DroneNodeDefinition.builder(FOR_EACH_COORDINATE, DroneNodeDefinition.FlowRole.NORMAL)
                .category("flow")
                .port(DronePortDefinition.multiInput("in", DronePortType.FLOW, true))
                .port(DronePortDefinition.input("area", DronePortType.AREA, true))
                .port(DronePortDefinition.output("body", DronePortType.FLOW, true))
                .port(DronePortDefinition.output("done", DronePortType.FLOW, true))
                .port(DronePortDefinition.output("coordinate", DronePortType.COORDINATE, false))
                .build());

        registry.register(action(MOVE_TO, "movement")
                .port(DronePortDefinition.input("target", DronePortType.COORDINATE, true))
                .build());
        registry.register(action(RETURN_TO_DOCK, "dock")
                .port(DronePortDefinition.input("target", DronePortType.COORDINATE, true))
                .build());
        registry.register(action(CHARGE_UNTIL, "dock")
                .port(DronePortDefinition.input("percent", DronePortType.NUMBER, false))
                .property(DroneNodePropertyDefinition.number("Percent", 1.0D, 100.0D))
                .build());
        registry.register(DroneNodeDefinition.builder(FIND_NEAREST_DOCK, DroneNodeDefinition.FlowRole.VALUE)
                .category("dock")
                .port(DronePortDefinition.output("value", DronePortType.COORDINATE, false))
                .build());
        registry.register(action(BIND_DOCK, "dock")
                .port(DronePortDefinition.input("target", DronePortType.COORDINATE, true))
                .build());
        registry.register(action(UNBIND_DOCK, "dock").build());
        registry.register(action(CONFIGURE_SAFETY, "dock")
                .port(DronePortDefinition.input("return_percent", DronePortType.NUMBER, false))
                .port(DronePortDefinition.input("resume_percent", DronePortType.NUMBER, false))
                .property(DroneNodePropertyDefinition.integer("ReturnPercent", 1, 95))
                .property(DroneNodePropertyDefinition.integer("ResumePercent", 2, 100))
                .build());
        registry.register(action(IMPORT_EU, "energy")
                .port(DronePortDefinition.input("target", DronePortType.COORDINATE, true))
                .port(DronePortDefinition.input("amount", DronePortType.NUMBER, false))
                .port(DronePortDefinition.output("transferred", DronePortType.NUMBER, false))
                .property(DroneNodePropertyDefinition.integer("MaxEU", 1, 1_000_000))
                .build());
        registry.register(action(EXPORT_EU, "energy")
                .port(DronePortDefinition.input("target", DronePortType.COORDINATE, true))
                .port(DronePortDefinition.input("amount", DronePortType.NUMBER, false))
                .port(DronePortDefinition.output("transferred", DronePortType.NUMBER, false))
                .property(DroneNodePropertyDefinition.integer("MaxEU", 1, 1_000_000))
                .build());
        registry.register(action(CHARGE_TARGET_PERCENT, "energy")
                .port(DronePortDefinition.input("target", DronePortType.COORDINATE, true))
                .port(DronePortDefinition.input("percent", DronePortType.NUMBER, false))
                .port(DronePortDefinition.output("transferred", DronePortType.NUMBER, false))
                .property(DroneNodePropertyDefinition.integer("Percent", 1, 100))
                .property(DroneNodePropertyDefinition.integer("MaxEU", 1, 1_000_000))
                .build());
        registry.register(action(BREAK_BLOCK, "blocks")
                .port(DronePortDefinition.input("area", DronePortType.AREA, true))
                .build());
        registry.register(action(BREAK_BLOCK_AT, "blocks")
                .port(DronePortDefinition.input("target", DronePortType.COORDINATE, true))
                .build());
        registry.register(action(PLACE_BLOCK, "blocks")
                .port(DronePortDefinition.input("target", DronePortType.COORDINATE, true))
                .port(DronePortDefinition.input("filter", DronePortType.ITEM_FILTER, false))
                .build());
        registry.register(action(PLACE_AREA, "blocks")
                .port(DronePortDefinition.input("area", DronePortType.AREA, true))
                .port(DronePortDefinition.input("filter", DronePortType.ITEM_FILTER, false))
                .build());
        registry.register(action(IMPORT_ITEMS, "items")
                .port(DronePortDefinition.input("target", DronePortType.COORDINATE, false))
                .port(DronePortDefinition.input("area", DronePortType.AREA, false))
                .port(DronePortDefinition.input("filter", DronePortType.ITEM_FILTER, false))
                .port(DronePortDefinition.output("amount", DronePortType.NUMBER, false))
                .property(DroneNodePropertyDefinition.enumeration("Direction", "AUTO", "DOWN", "UP", "NORTH",
                        "SOUTH", "WEST", "EAST"))
                .property(DroneNodePropertyDefinition.integer("MaxAmount", 1, 1_000_000))
                .property(DroneNodePropertyDefinition.integer("BatchSize", 1, 1_000_000))
                .property(DroneNodePropertyDefinition.enumeration("SearchMode", "NEAREST", "ORDERED", "RANDOM"))
                .property(DroneNodePropertyDefinition.bool("SkipUnavailable"))
                .build());
        registry.register(action(EXPORT_ITEMS, "items")
                .port(DronePortDefinition.input("target", DronePortType.COORDINATE, false))
                .port(DronePortDefinition.input("area", DronePortType.AREA, false))
                .port(DronePortDefinition.input("filter", DronePortType.ITEM_FILTER, false))
                .port(DronePortDefinition.output("amount", DronePortType.NUMBER, false))
                .property(DroneNodePropertyDefinition.enumeration("Direction", "AUTO", "DOWN", "UP", "NORTH",
                        "SOUTH", "WEST", "EAST"))
                .property(DroneNodePropertyDefinition.integer("MaxAmount", 1, 1_000_000))
                .property(DroneNodePropertyDefinition.integer("BatchSize", 1, 1_000_000))
                .property(DroneNodePropertyDefinition.enumeration("SearchMode", "NEAREST", "ORDERED", "RANDOM"))
                .property(DroneNodePropertyDefinition.bool("SkipUnavailable"))
                .build());
        registry.register(action(IMPORT_FLUID, "fluids")
                .port(DronePortDefinition.input("target", DronePortType.COORDINATE, true))
                .port(DronePortDefinition.input("filter", DronePortType.FLUID_FILTER, false))
                .port(DronePortDefinition.input("amount", DronePortType.NUMBER, false))
                .port(DronePortDefinition.output("transferred", DronePortType.NUMBER, false))
                .property(DroneNodePropertyDefinition.enumeration("Direction", "AUTO", "DOWN", "UP", "NORTH",
                        "SOUTH", "WEST", "EAST"))
                .property(DroneNodePropertyDefinition.integer("MaxAmount", 1, 1_000_000))
                .build());
        registry.register(action(EXPORT_FLUID, "fluids")
                .port(DronePortDefinition.input("target", DronePortType.COORDINATE, true))
                .port(DronePortDefinition.input("filter", DronePortType.FLUID_FILTER, false))
                .port(DronePortDefinition.input("amount", DronePortType.NUMBER, false))
                .port(DronePortDefinition.output("transferred", DronePortType.NUMBER, false))
                .property(DroneNodePropertyDefinition.enumeration("Direction", "AUTO", "DOWN", "UP", "NORTH",
                        "SOUTH", "WEST", "EAST"))
                .property(DroneNodePropertyDefinition.integer("MaxAmount", 1, 1_000_000))
                .build());
        registry.register(action(DRAIN_FLUID, "fluids")
                .port(DronePortDefinition.input("filter", DronePortType.FLUID_FILTER, false))
                .port(DronePortDefinition.input("amount", DronePortType.NUMBER, false))
                .port(DronePortDefinition.output("transferred", DronePortType.NUMBER, false))
                .property(DroneNodePropertyDefinition.integer("MaxAmount", 1, 1_000_000))
                .build());
        registry.register(action(FIND_FLUID_CONTAINER, "fluids")
                .port(DronePortDefinition.input("area", DronePortType.AREA, true))
                .port(DronePortDefinition.input("filter", DronePortType.FLUID_FILTER, false))
                .port(DronePortDefinition.input("minimum", DronePortType.NUMBER, false))
                .port(DronePortDefinition.output("target", DronePortType.COORDINATE, false))
                .property(DroneNodePropertyDefinition.enumeration("Direction", "AUTO", "DOWN", "UP", "NORTH",
                        "SOUTH", "WEST", "EAST"))
                .property(DroneNodePropertyDefinition.integer("MinimumAmount", 1, 1_000_000))
                .build());
        registry.register(action(WAIT_FOR_FLUID_AMOUNT, "fluids")
                .port(DronePortDefinition.input("target", DronePortType.COORDINATE, false))
                .port(DronePortDefinition.input("filter", DronePortType.FLUID_FILTER, false))
                .port(DronePortDefinition.input("amount", DronePortType.NUMBER, false))
                .property(DroneNodePropertyDefinition.enumeration("Direction", "AUTO", "DOWN", "UP", "NORTH",
                        "SOUTH", "WEST", "EAST"))
                .property(DroneNodePropertyDefinition.integer("Amount", 0, 1_000_000))
                .property(DroneNodePropertyDefinition.enumeration("Operator", "AT_LEAST", "AT_MOST"))
                .build());
        registry.register(action(INTERACT_BLOCK, "blocks")
                .port(DronePortDefinition.input("target", DronePortType.COORDINATE, true))
                .property(DroneNodePropertyDefinition.enumeration("Direction", "AUTO", "DOWN", "UP", "NORTH",
                        "SOUTH", "WEST", "EAST"))
                .property(DroneNodePropertyDefinition.bool("Sneaking"))
                .build());
        registry.register(action(USE_ITEM_ON_BLOCK, "blocks")
                .port(DronePortDefinition.input("target", DronePortType.COORDINATE, true))
                .port(DronePortDefinition.input("filter", DronePortType.ITEM_FILTER, false))
                .property(DroneNodePropertyDefinition.enumeration("Direction", "AUTO", "DOWN", "UP", "NORTH",
                        "SOUTH", "WEST", "EAST"))
                .property(DroneNodePropertyDefinition.bool("Sneaking"))
                .build());
        registry.register(action(PICKUP_DROPPED_ITEMS, "items")
                .port(DronePortDefinition.input("filter", DronePortType.ITEM_FILTER, false))
                .port(DronePortDefinition.output("amount", DronePortType.NUMBER, false))
                .property(DroneNodePropertyDefinition.number("Radius", 0.5D, 32.0D))
                .property(DroneNodePropertyDefinition.integer("MaxAmount", 1, 1_000_000))
                .build());
        registry.register(action(DROP_ITEMS, "items")
                .port(DronePortDefinition.input("filter", DronePortType.ITEM_FILTER, false))
                .port(DronePortDefinition.output("amount", DronePortType.NUMBER, false))
                .property(DroneNodePropertyDefinition.integer("MaxAmount", 1, 1_000_000))
                .build());
        registry.register(action(HARVEST_CROP, "blocks")
                .port(DronePortDefinition.input("target", DronePortType.COORDINATE, true))
                .build());
        registry.register(action(SET_REDSTONE_OUTPUT, "events")
                .port(DronePortDefinition.input("target", DronePortType.COORDINATE, true))
                .port(DronePortDefinition.input("strength", DronePortType.NUMBER, false))
                .property(DroneNodePropertyDefinition.integer("Strength", 0, 15))
                .build());

        registry.register(valueBuilder(NUMBER, "values", "value", DronePortType.NUMBER)
                .property(DroneNodePropertyDefinition.number("Value", -Double.MAX_VALUE, Double.MAX_VALUE)).build());
        registry.register(valueBuilder(BOOLEAN, "values", "value", DronePortType.BOOLEAN)
                .property(DroneNodePropertyDefinition.bool("Value")).build());
        registry.register(valueBuilder(COORDINATE, "values", "value", DronePortType.COORDINATE)
                .property(DroneNodePropertyDefinition.integer("X", -30_000_000, 30_000_000))
                .property(DroneNodePropertyDefinition.integer("Y", -2_048, 2_047))
                .property(DroneNodePropertyDefinition.integer("Z", -30_000_000, 30_000_000)).build());
        registry.register(valueBuilder(AREA, "values", "value", DronePortType.AREA)
                .property(DroneNodePropertyDefinition.integer("X1", -30_000_000, 30_000_000))
                .property(DroneNodePropertyDefinition.integer("Y1", -2_048, 2_047))
                .property(DroneNodePropertyDefinition.integer("Z1", -30_000_000, 30_000_000))
                .property(DroneNodePropertyDefinition.integer("X2", -30_000_000, 30_000_000))
                .property(DroneNodePropertyDefinition.integer("Y2", -2_048, 2_047))
                .property(DroneNodePropertyDefinition.integer("Z2", -30_000_000, 30_000_000)).build());
        registry.register(valueBuilder(ITEM_FILTER, "filters", "filter", DronePortType.ITEM_FILTER)
                .property(DroneNodePropertyDefinition.selector("FilterSpec", DroneNodePropertyType.ITEM_SELECTOR))
                .build());
        registry.register(valueBuilder(BLOCK_FILTER, "filters", "filter", DronePortType.BLOCK_FILTER)
                .property(DroneNodePropertyDefinition.selector("FilterSpec", DroneNodePropertyType.BLOCK_SELECTOR))
                .build());
        registry.register(valueBuilder(FLUID_FILTER, "filters", "filter", DronePortType.FLUID_FILTER)
                .property(DroneNodePropertyDefinition.selector("Fluid", DroneNodePropertyType.FLUID_SELECTOR))
                .property(DroneNodePropertyDefinition.enumeration("Mode", "WHITELIST", "BLACKLIST"))
                .build());
        registry.register(value(ENERGY_LEVEL, "conditions", "value", DronePortType.NUMBER));
        registry.register(DroneNodeDefinition.builder(TARGET_ENERGY, DroneNodeDefinition.FlowRole.VALUE)
                .category("energy")
                .port(DronePortDefinition.input("target", DronePortType.COORDINATE, true))
                .port(DronePortDefinition.output("value", DronePortType.NUMBER, false))
                .build());
        registry.register(DroneNodeDefinition.builder(TARGET_ENERGY_CAPACITY, DroneNodeDefinition.FlowRole.VALUE)
                .category("energy")
                .port(DronePortDefinition.input("target", DronePortType.COORDINATE, true))
                .port(DronePortDefinition.output("value", DronePortType.NUMBER, false))
                .build());
        registry.register(DroneNodeDefinition.builder(TARGET_ENERGY_PERCENT, DroneNodeDefinition.FlowRole.VALUE)
                .category("energy")
                .port(DronePortDefinition.input("target", DronePortType.COORDINATE, true))
                .port(DronePortDefinition.output("value", DronePortType.NUMBER, false))
                .build());
        registry.register(DroneNodeDefinition.builder(CARGO_ITEM_COUNT, DroneNodeDefinition.FlowRole.VALUE)
                .category("sensors")
                .port(DronePortDefinition.input("filter", DronePortType.ITEM_FILTER, false))
                .port(DronePortDefinition.output("value", DronePortType.NUMBER, false))
                .build());
        registry.register(value(CARGO_FREE_SLOTS, "sensors", "value", DronePortType.NUMBER));
        registry.register(value(CARGO_USED_PERCENT, "sensors", "value", DronePortType.NUMBER));
        registry.register(DroneNodeDefinition.builder(INVENTORY_ITEM_COUNT, DroneNodeDefinition.FlowRole.VALUE)
                .category("sensors")
                .port(DronePortDefinition.input("target", DronePortType.COORDINATE, true))
                .port(DronePortDefinition.input("filter", DronePortType.ITEM_FILTER, false))
                .port(DronePortDefinition.output("value", DronePortType.NUMBER, false))
                .property(DroneNodePropertyDefinition.enumeration("Direction", "AUTO", "DOWN", "UP", "NORTH",
                        "SOUTH", "WEST", "EAST"))
                .build());
        registry.register(DroneNodeDefinition.builder(DRONE_FLUID_AMOUNT, DroneNodeDefinition.FlowRole.VALUE)
                .category("sensors")
                .port(DronePortDefinition.input("filter", DronePortType.FLUID_FILTER, false))
                .port(DronePortDefinition.output("value", DronePortType.NUMBER, false))
                .build());
        registry.register(value(DRONE_FLUID_PERCENT, "sensors", "value", DronePortType.NUMBER));
        registry.register(DroneNodeDefinition.builder(CONTAINER_FLUID_AMOUNT, DroneNodeDefinition.FlowRole.VALUE)
                .category("sensors")
                .port(DronePortDefinition.input("target", DronePortType.COORDINATE, true))
                .port(DronePortDefinition.input("filter", DronePortType.FLUID_FILTER, false))
                .port(DronePortDefinition.output("value", DronePortType.NUMBER, false))
                .property(DroneNodePropertyDefinition.enumeration("Direction", "AUTO", "DOWN", "UP", "NORTH",
                        "SOUTH", "WEST", "EAST"))
                .build());
        registry.register(DroneNodeDefinition.builder(REDSTONE_STRENGTH, DroneNodeDefinition.FlowRole.VALUE)
                .category("sensors")
                .port(DronePortDefinition.input("target", DronePortType.COORDINATE, true))
                .port(DronePortDefinition.output("value", DronePortType.NUMBER, false))
                .build());
        registry.register(DroneNodeDefinition.builder(REDSTONE_OUTPUT_LEVEL, DroneNodeDefinition.FlowRole.VALUE)
                .category("sensors")
                .port(DronePortDefinition.input("target", DronePortType.COORDINATE, true))
                .port(DronePortDefinition.output("value", DronePortType.NUMBER, false))
                .build());
        registry.register(DroneNodeDefinition.builder(LIGHT_LEVEL, DroneNodeDefinition.FlowRole.VALUE)
                .category("sensors")
                .port(DronePortDefinition.input("target", DronePortType.COORDINATE, true))
                .port(DronePortDefinition.output("value", DronePortType.NUMBER, false))
                .property(DroneNodePropertyDefinition.enumeration("LightType", "MAX", "BLOCK", "SKY"))
                .build());
        registry.register(value(LAST_ACTION_STATUS, "conditions", "value", DronePortType.ACTION_STATUS));
        registry.register(value(LAST_ACTION_ERROR, "conditions", "value", DronePortType.STRING));
        registry.register(DroneNodeDefinition.builder(COMPARE_ACTION_STATUS, DroneNodeDefinition.FlowRole.VALUE)
                .category("conditions")
                .port(DronePortDefinition.input("status", DronePortType.ACTION_STATUS, true))
                .port(DronePortDefinition.output("result", DronePortType.BOOLEAN, false))
                .property(DroneNodePropertyDefinition.enumeration("Status", "SUCCESS", "NOT_FOUND", "NO_SPACE",
                        "NO_RESOURCE", "NO_ENERGY", "OUT_OF_RANGE", "UNLOADED", "DENIED", "UNREACHABLE",
                        "INVALID_TARGET", "FAILURE", "ERROR"))
                .build());
        registry.register(DroneNodeDefinition.builder(BLOCK_MATCHES, DroneNodeDefinition.FlowRole.VALUE)
                .category("sensors")
                .port(DronePortDefinition.input("target", DronePortType.COORDINATE, true))
                .port(DronePortDefinition.input("filter", DronePortType.BLOCK_FILTER, false))
                .port(DronePortDefinition.output("result", DronePortType.BOOLEAN, false))
                .build());
        registry.register(DroneNodeDefinition.builder(COORDINATE_REACHABLE, DroneNodeDefinition.FlowRole.VALUE)
                .category("sensors")
                .port(DronePortDefinition.input("target", DronePortType.COORDINATE, true))
                .port(DronePortDefinition.output("result", DronePortType.BOOLEAN, false))
                .build());
        registry.register(DroneNodeDefinition.builder(DOCK_AVAILABLE, DroneNodeDefinition.FlowRole.VALUE)
                .category("sensors")
                .port(DronePortDefinition.input("target", DronePortType.COORDINATE, true))
                .port(DronePortDefinition.output("result", DronePortType.BOOLEAN, false))
                .build());
        registry.register(DroneNodeDefinition.builder(AREA_BLOCK_COUNT, DroneNodeDefinition.FlowRole.VALUE)
                .category("sensors")
                .port(DronePortDefinition.input("area", DronePortType.AREA, true))
                .port(DronePortDefinition.input("filter", DronePortType.BLOCK_FILTER, false))
                .port(DronePortDefinition.output("value", DronePortType.NUMBER, false))
                .property(DroneNodePropertyDefinition.integer("Limit", 1, 4_096))
                .build());
        registry.register(DroneNodeDefinition.builder(COMPARE_NUMBER, DroneNodeDefinition.FlowRole.VALUE)
                .category("conditions")
                .port(DronePortDefinition.input("left", DronePortType.NUMBER, true))
                .port(DronePortDefinition.input("right", DronePortType.NUMBER, true))
                .port(DronePortDefinition.output("result", DronePortType.BOOLEAN, false))
                .property(DroneNodePropertyDefinition.enumeration("Operator", "", "==", "!=", "<", "<=", ">", ">="))
                .build());
        registry.register(valueBuilder(GET_NUMBER_VARIABLE, "variables", "value", DronePortType.NUMBER)
                .property(DroneNodePropertyDefinition.string("Name", 32)).build());
        registry.register(action(SET_NUMBER_VARIABLE, "variables")
                .port(DronePortDefinition.input("value", DronePortType.NUMBER, true))
                .property(DroneNodePropertyDefinition.string("Name", 32))
                .build());
        registry.register(action(ADD_NUMBER_VARIABLE, "variables")
                .port(DronePortDefinition.input("amount", DronePortType.NUMBER, true))
                .property(DroneNodePropertyDefinition.string("Name", 32))
                .build());
        registry.register(DroneNodeDefinition.builder(REPEAT, DroneNodeDefinition.FlowRole.NORMAL)
                .category("flow")
                .port(DronePortDefinition.multiInput("in", DronePortType.FLOW, true))
                .port(DronePortDefinition.input("count", DronePortType.NUMBER, false))
                .port(DronePortDefinition.output("body", DronePortType.FLOW, true))
                .port(DronePortDefinition.output("done", DronePortType.FLOW, true))
                .property(DroneNodePropertyDefinition.integer("Count", 0, 1_000_000))
                .build());
        registry.register(action(WAIT_FOR_REDSTONE, "events")
                .port(DronePortDefinition.input("target", DronePortType.COORDINATE, false))
                .build());
        registry.register(action(WAIT_FOR_OWNER, "events")
                .port(DronePortDefinition.input("radius", DronePortType.NUMBER, false))
                .property(DroneNodePropertyDefinition.number("Radius", 1.0D, 128.0D))
                .build());
        registry.register(DroneNodeDefinition.builder(NUMBER_MATH, DroneNodeDefinition.FlowRole.VALUE)
                .category("math")
                .port(DronePortDefinition.input("left", DronePortType.NUMBER, true))
                .port(DronePortDefinition.input("right", DronePortType.NUMBER, true))
                .port(DronePortDefinition.output("result", DronePortType.NUMBER, false))
                .property(DroneNodePropertyDefinition.enumeration("Operator", "", "+", "-", "*", "/", "%",
                        "min", "max"))
                .build());
        registry.register(DroneNodeDefinition.builder(BOOLEAN_LOGIC, DroneNodeDefinition.FlowRole.VALUE)
                .category("conditions")
                .port(DronePortDefinition.input("left", DronePortType.BOOLEAN, true))
                .port(DronePortDefinition.input("right", DronePortType.BOOLEAN, true))
                .port(DronePortDefinition.output("result", DronePortType.BOOLEAN, false))
                .property(DroneNodePropertyDefinition.enumeration("Operator", "", "AND", "OR", "XOR"))
                .build());
        registry.register(DroneNodeDefinition.builder(BOOLEAN_NOT, DroneNodeDefinition.FlowRole.VALUE)
                .category("conditions")
                .port(DronePortDefinition.input("value", DronePortType.BOOLEAN, true))
                .port(DronePortDefinition.output("result", DronePortType.BOOLEAN, false))
                .build());
        registry.register(DroneNodeDefinition.builder(COORDINATE_OFFSET, DroneNodeDefinition.FlowRole.VALUE)
                .category("movement")
                .port(DronePortDefinition.input("base", DronePortType.COORDINATE, true))
                .port(DronePortDefinition.input("x", DronePortType.NUMBER, false))
                .port(DronePortDefinition.input("y", DronePortType.NUMBER, false))
                .port(DronePortDefinition.input("z", DronePortType.NUMBER, false))
                .port(DronePortDefinition.output("value", DronePortType.COORDINATE, false))
                .property(DroneNodePropertyDefinition.integer("X", -30_000_000, 30_000_000))
                .property(DroneNodePropertyDefinition.integer("Y", -2_048, 2_047))
                .property(DroneNodePropertyDefinition.integer("Z", -30_000_000, 30_000_000))
                .build());
        registry.register(DroneNodeDefinition.builder(AREA_FROM_CORNERS, DroneNodeDefinition.FlowRole.VALUE)
                .category("values")
                .port(DronePortDefinition.input("first", DronePortType.COORDINATE, true))
                .port(DronePortDefinition.input("second", DronePortType.COORDINATE, true))
                .port(DronePortDefinition.output("value", DronePortType.AREA, false))
                .build());
        registry.register(DroneNodeDefinition.builder(SPHERE_AREA, DroneNodeDefinition.FlowRole.VALUE)
                .category("values")
                .port(DronePortDefinition.input("center", DronePortType.COORDINATE, true))
                .port(DronePortDefinition.input("radius", DronePortType.NUMBER, false))
                .port(DronePortDefinition.output("value", DronePortType.AREA, false))
                .property(DroneNodePropertyDefinition.integer("Radius", 1, 9))
                .property(DroneNodePropertyDefinition.bool("Hollow"))
                .build());
        registry.register(DroneNodeDefinition.builder(CYLINDER_AREA, DroneNodeDefinition.FlowRole.VALUE)
                .category("values")
                .port(DronePortDefinition.input("center", DronePortType.COORDINATE, true))
                .port(DronePortDefinition.input("radius", DronePortType.NUMBER, false))
                .port(DronePortDefinition.input("height", DronePortType.NUMBER, false))
                .port(DronePortDefinition.output("value", DronePortType.AREA, false))
                .property(DroneNodePropertyDefinition.integer("Radius", 1, 8))
                .property(DroneNodePropertyDefinition.integer("Height", 1, 16))
                .property(DroneNodePropertyDefinition.bool("Hollow"))
                .build());
        registry.register(DroneNodeDefinition.builder(PATH_AREA, DroneNodeDefinition.FlowRole.VALUE)
                .category("values")
                .port(DronePortDefinition.input("first", DronePortType.COORDINATE, true))
                .port(DronePortDefinition.input("second", DronePortType.COORDINATE, true))
                .port(DronePortDefinition.input("radius", DronePortType.NUMBER, false))
                .port(DronePortDefinition.output("value", DronePortType.AREA, false))
                .property(DroneNodePropertyDefinition.integer("Radius", 0, 3))
                .build());
        registry.register(areaBinary(AREA_UNION));
        registry.register(areaBinary(AREA_INTERSECTION));
        registry.register(areaBinary(AREA_DIFFERENCE));
        registry.register(DroneNodeDefinition.builder(AREA_OFFSET, DroneNodeDefinition.FlowRole.VALUE)
                .category("values")
                .port(DronePortDefinition.input("area", DronePortType.AREA, true))
                .port(DronePortDefinition.input("x", DronePortType.NUMBER, false))
                .port(DronePortDefinition.input("y", DronePortType.NUMBER, false))
                .port(DronePortDefinition.input("z", DronePortType.NUMBER, false))
                .port(DronePortDefinition.output("value", DronePortType.AREA, false))
                .property(DroneNodePropertyDefinition.integer("X", -32, 32))
                .property(DroneNodePropertyDefinition.integer("Y", -32, 32))
                .property(DroneNodePropertyDefinition.integer("Z", -32, 32))
                .build());
        registry.register(DroneNodeDefinition.builder(AREA_CONTAINS, DroneNodeDefinition.FlowRole.VALUE)
                .category("conditions")
                .port(DronePortDefinition.input("area", DronePortType.AREA, true))
                .port(DronePortDefinition.input("coordinate", DronePortType.COORDINATE, true))
                .port(DronePortDefinition.output("result", DronePortType.BOOLEAN, false))
                .build());
        registry.register(DroneNodeDefinition.builder(AREA_VOLUME, DroneNodeDefinition.FlowRole.VALUE)
                .category("sensors")
                .port(DronePortDefinition.input("area", DronePortType.AREA, true))
                .port(DronePortDefinition.output("value", DronePortType.NUMBER, false))
                .build());
        registry.register(DroneNodeDefinition.builder(PLANE_AREA, DroneNodeDefinition.FlowRole.VALUE)
                .category("values")
                .port(DronePortDefinition.input("origin", DronePortType.COORDINATE, true))
                .port(DronePortDefinition.input("first", DronePortType.COORDINATE, true))
                .port(DronePortDefinition.input("second", DronePortType.COORDINATE, true))
                .port(DronePortDefinition.output("value", DronePortType.AREA, false))
                .build());
        registry.register(DroneNodeDefinition.builder(WHILE, DroneNodeDefinition.FlowRole.NORMAL)
                .category("flow")
                .port(DronePortDefinition.multiInput("in", DronePortType.FLOW, true))
                .port(DronePortDefinition.input("condition", DronePortType.BOOLEAN, true))
                .port(DronePortDefinition.output("body", DronePortType.FLOW, true))
                .port(DronePortDefinition.output("done", DronePortType.FLOW, true))
                .build());
    }

    private static DroneNodeDefinition.Builder action(ResourceLocation id, String category) {
        return DroneNodeDefinition.builder(id, DroneNodeDefinition.FlowRole.NORMAL)
                .category(category)
                .port(DronePortDefinition.input("in", DronePortType.FLOW, true))
                .port(DronePortDefinition.output("next", DronePortType.FLOW, true))
                .port(DronePortDefinition.output("failed", DronePortType.FLOW, false));
    }

    private static DroneNodeDefinition areaBinary(ResourceLocation id) {
        return DroneNodeDefinition.builder(id, DroneNodeDefinition.FlowRole.VALUE)
                .category("values")
                .port(DronePortDefinition.input("first", DronePortType.AREA, true))
                .port(DronePortDefinition.input("second", DronePortType.AREA, true))
                .port(DronePortDefinition.output("value", DronePortType.AREA, false))
                .build();
    }

    private static DroneNodeDefinition value(ResourceLocation id, String category, String portId,
            DronePortType portType) {
        return valueBuilder(id, category, portId, portType).build();
    }

    private static DroneNodeDefinition.Builder valueBuilder(ResourceLocation id, String category, String portId,
            DronePortType portType) {
        return DroneNodeDefinition.builder(id, DroneNodeDefinition.FlowRole.VALUE)
                .category(category)
                .port(DronePortDefinition.output(portId, portType, false));
    }

    private static ResourceLocation id(String path) {
        return new ResourceLocation("drtech", path);
    }
}
