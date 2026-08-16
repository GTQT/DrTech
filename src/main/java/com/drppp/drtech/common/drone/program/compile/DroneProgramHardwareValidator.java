package com.drppp.drtech.common.drone.program.compile;

import com.drppp.drtech.common.drone.hardware.DroneChassisTier;
import com.drppp.drtech.common.drone.hardware.DroneUpgradeDataCodec;
import com.drppp.drtech.common.drone.hardware.DroneUpgradeType;
import com.drppp.drtech.common.drone.item.DroneItemData;
import com.drppp.drtech.common.drone.item.ItemProgrammableDrone;
import com.drppp.drtech.common.drone.program.model.DroneProgramEdge;
import com.drppp.drtech.common.drone.program.model.DroneProgramGraph;
import com.drppp.drtech.common.drone.program.model.DroneProgramNode;
import com.drppp.drtech.common.drone.program.registry.DrTechDroneNodes;
import gregtech.api.GTValues;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IEnergyContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** Target-drone preflight checks. These diagnostics are never persisted into the reusable program card. */
public final class DroneProgramHardwareValidator {

    private static final Map<DroneUpgradeType, ResourceLocation[]> REQUIRED_UPGRADES = requiredUpgrades();

    private DroneProgramHardwareValidator() {}

    /**
     * Returns the dedicated modules required by one node type.  The editor uses this as display-only
     * catalogue metadata; validation remains authoritative when the program is written to a drone.
     */
    public static List<DroneUpgradeType> getRequiredUpgrades(ResourceLocation nodeType) {
        if (nodeType == null) return Collections.emptyList();
        List<DroneUpgradeType> result = new ArrayList<>();
        for (Map.Entry<DroneUpgradeType, ResourceLocation[]> requirement : REQUIRED_UPGRADES.entrySet()) {
            if (contains(requirement.getValue(), nodeType)) result.add(requirement.getKey());
        }
        return result.isEmpty() ? Collections.emptyList() : Collections.unmodifiableList(result);
    }

    public static List<DroneProgramDiagnostic> validate(DroneProgramGraph graph, ItemStack drone,
            @Nullable World world) {
        if (graph == null || drone.isEmpty() || !(drone.getItem() instanceof ItemProgrammableDrone)) {
            return new ArrayList<>();
        }
        return validate(graph, DroneItemData.getChassis(drone), DroneItemData.getUpgrades(drone), world);
    }

    public static List<DroneProgramDiagnostic> validate(DroneProgramGraph graph, DroneChassisTier chassis,
            net.minecraft.nbt.NBTTagCompound upgrades, @Nullable World world) {
        List<DroneProgramDiagnostic> diagnostics = new ArrayList<>();
        for (Map.Entry<DroneUpgradeType, ResourceLocation[]> requirement : REQUIRED_UPGRADES.entrySet()) {
            if (DroneUpgradeDataCodec.getLevel(upgrades, requirement.getKey()) > 0) continue;
            for (DroneProgramNode node : graph.getNodes()) {
                if (contains(requirement.getValue(), node.getType())) {
                    diagnostics.add(new DroneProgramDiagnostic(DroneDiagnosticSeverity.ERROR,
                            DroneDiagnosticCode.REQUIRED_UPGRADE_MISSING, node.getId(), null,
                            requirement.getKey().getSerializedName()));
                }
            }
        }

        long executableNodes = graph.getNodes().stream()
                .filter(node -> !DrTechDroneNodes.isEditorOnly(node.getType())).count();
        int recommendedTier = executableNodes > 96 ? GTValues.IV
                : executableNodes > 32 ? GTValues.EV : GTValues.HV;
        if (chassis.getVoltageTier() < recommendedTier) {
            diagnostics.add(new DroneProgramDiagnostic(DroneDiagnosticSeverity.WARNING,
                    DroneDiagnosticCode.CHASSIS_TIER_RECOMMENDED, null, null,
                    GTValues.VNF[chassis.getVoltageTier()], GTValues.VNF[recommendedTier],
                    Long.toString(executableNodes)));
        }
        if (world != null) validateConstantVoltageTargets(graph, chassis, world, diagnostics);
        return diagnostics;
    }

    private static void validateConstantVoltageTargets(DroneProgramGraph graph, DroneChassisTier chassis, World world,
            List<DroneProgramDiagnostic> diagnostics) {
        for (DroneProgramNode action : graph.getNodes()) {
            boolean importing = action.getType().equals(DrTechDroneNodes.IMPORT_EU);
            boolean exporting = action.getType().equals(DrTechDroneNodes.EXPORT_EU)
                    || action.getType().equals(DrTechDroneNodes.CHARGE_TARGET_PERCENT);
            if (!importing && !exporting) continue;
            DroneProgramNode coordinate = constantTargetCoordinate(graph, action);
            if (coordinate == null) continue;
            net.minecraft.nbt.NBTTagCompound config = coordinate.getConfiguration();
            BlockPos position = new BlockPos(config.getInteger("X"), config.getInteger("Y"), config.getInteger("Z"));
            if (!world.isBlockLoaded(position)) continue;
            TileEntity tile = world.getTileEntity(position);
            IEnergyContainer energy = tile == null ? null
                    : tile.getCapability(GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER, null);
            if (energy == null) continue;
            long droneVoltage = GTValues.V[chassis.getVoltageTier()];
            long endpointVoltage = importing ? energy.getOutputVoltage() : energy.getInputVoltage();
            boolean incompatible = importing ? endpointVoltage > droneVoltage
                    : endpointVoltage > 0L && endpointVoltage < droneVoltage;
            if (incompatible) {
                diagnostics.add(new DroneProgramDiagnostic(DroneDiagnosticSeverity.ERROR,
                        DroneDiagnosticCode.VOLTAGE_TIER_MISMATCH, action.getId(), "target",
                        Long.toString(droneVoltage), Long.toString(endpointVoltage),
                        importing ? "import" : "export"));
            }
        }
    }

    private static DroneProgramNode constantTargetCoordinate(DroneProgramGraph graph, DroneProgramNode action) {
        for (DroneProgramEdge edge : graph.getEdges()) {
            if (!edge.getTargetNodeId().equals(action.getId()) || !"target".equals(edge.getTargetPortId())) continue;
            DroneProgramNode source = graph.getNode(edge.getSourceNodeId());
            if (source != null && source.getType().equals(DrTechDroneNodes.COORDINATE)) return source;
        }
        return null;
    }

    private static boolean contains(ResourceLocation[] values, ResourceLocation value) {
        for (ResourceLocation candidate : values) if (candidate.equals(value)) return true;
        return false;
    }

    private static Map<DroneUpgradeType, ResourceLocation[]> requiredUpgrades() {
        Map<DroneUpgradeType, ResourceLocation[]> requirements = new EnumMap<>(DroneUpgradeType.class);
        requirements.put(DroneUpgradeType.FLUID_CARGO, new ResourceLocation[] {
                DrTechDroneNodes.IMPORT_FLUID, DrTechDroneNodes.EXPORT_FLUID, DrTechDroneNodes.DRAIN_FLUID,
                DrTechDroneNodes.DRONE_FLUID_AMOUNT, DrTechDroneNodes.DRONE_FLUID_PERCENT
        });
        requirements.put(DroneUpgradeType.CRAFTING, new ResourceLocation[] {
                DrTechDroneNodes.CRAFT_ITEMS, DrTechDroneNodes.CAN_CRAFT,
                DrTechDroneNodes.CRAFTABLE_COUNT, DrTechDroneNodes.CRAFT_GRID
        });
        requirements.put(DroneUpgradeType.TOOL_ARM, new ResourceLocation[] {
                DrTechDroneNodes.INTERACT_ENTITY, DrTechDroneNodes.USE_ITEM_ON_ENTITY,
                DrTechDroneNodes.EDIT_SIGN
        });
        requirements.put(DroneUpgradeType.ENTITY_SCANNER, new ResourceLocation[] {
                DrTechDroneNodes.ENTITY_COUNT, DrTechDroneNodes.ENTITY_SENSOR,
                DrTechDroneNodes.FOLLOW_ENTITY, DrTechDroneNodes.AVOID_ENTITY
        });
        requirements.put(DroneUpgradeType.COMBAT, new ResourceLocation[] {
                DrTechDroneNodes.ATTACK_ENTITY
        });
        requirements.put(DroneUpgradeType.ENTITY_CONTAINMENT, new ResourceLocation[] {
                DrTechDroneNodes.LOAD_ENTITY, DrTechDroneNodes.RELEASE_ENTITY
        });
        return requirements;
    }
}
