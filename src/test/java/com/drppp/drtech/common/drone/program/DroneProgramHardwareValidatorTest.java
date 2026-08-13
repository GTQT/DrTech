package com.drppp.drtech.common.drone.program;

import com.drppp.drtech.common.drone.hardware.DroneChassisTier;
import com.drppp.drtech.common.drone.hardware.DroneUpgradeDataCodec;
import com.drppp.drtech.common.drone.hardware.DroneUpgradeType;
import com.drppp.drtech.common.drone.program.compile.DroneDiagnosticCode;
import com.drppp.drtech.common.drone.program.compile.DroneDiagnosticSeverity;
import com.drppp.drtech.common.drone.program.compile.DroneProgramDiagnostic;
import com.drppp.drtech.common.drone.program.compile.DroneProgramHardwareValidator;
import com.drppp.drtech.common.drone.program.model.DroneProgramGraph;
import com.drppp.drtech.common.drone.program.model.DroneProgramNode;
import com.drppp.drtech.common.drone.program.registry.DrTechDroneNodes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DroneProgramHardwareValidatorTest {

    @Test
    void exposesDedicatedModuleRequirementsForTheNodeLibrary() {
        assertEquals(java.util.Collections.singletonList(DroneUpgradeType.FLUID_CARGO),
                DroneProgramHardwareValidator.getRequiredUpgrades(DrTechDroneNodes.IMPORT_FLUID));
        assertEquals(java.util.Collections.singletonList(DroneUpgradeType.CRAFTING),
                DroneProgramHardwareValidator.getRequiredUpgrades(DrTechDroneNodes.CRAFT_GRID));
        assertTrue(DroneProgramHardwareValidator.getRequiredUpgrades(DrTechDroneNodes.WAIT).isEmpty());
    }

    @Test
    void requiresDedicatedModulesForFluidAndCraftingNodes() {
        DroneProgramGraph graph = new DroneProgramGraph("hardware");
        graph.addNode(DroneProgramNode.create(DrTechDroneNodes.IMPORT_FLUID, 0, 0));
        graph.addNode(DroneProgramNode.create(DrTechDroneNodes.CRAFT_ITEMS, 0, 0));
        List<DroneProgramDiagnostic> diagnostics = DroneProgramHardwareValidator.validate(
                graph, DroneChassisTier.HV, upgrades(), null);

        assertTrue(has(diagnostics, DroneDiagnosticCode.REQUIRED_UPGRADE_MISSING,
                DroneDiagnosticSeverity.ERROR, "fluid_cargo"));
        assertTrue(has(diagnostics, DroneDiagnosticCode.REQUIRED_UPGRADE_MISSING,
                DroneDiagnosticSeverity.ERROR, "crafting"));
    }

    @Test
    void acceptsInstalledDedicatedModules() {
        DroneProgramGraph graph = new DroneProgramGraph("hardware ready");
        graph.addNode(DroneProgramNode.create(DrTechDroneNodes.DRONE_FLUID_AMOUNT, 0, 0));
        graph.addNode(DroneProgramNode.create(DrTechDroneNodes.CRAFT_GRID, 0, 0));
        List<DroneProgramDiagnostic> diagnostics = DroneProgramHardwareValidator.validate(graph,
                DroneChassisTier.HV, upgrades(DroneUpgradeType.FLUID_CARGO, DroneUpgradeType.CRAFTING), null);

        assertFalse(diagnostics.stream()
                .anyMatch(value -> value.getCode() == DroneDiagnosticCode.REQUIRED_UPGRADE_MISSING));
    }

    @Test
    void recommendsChassisTierForLargeGraphsWithoutBlockingWrite() {
        DroneProgramGraph graph = new DroneProgramGraph("large graph");
        for (int index = 0; index < 33; index++) {
            graph.addNode(DroneProgramNode.create(DrTechDroneNodes.NUMBER, index, 0));
        }

        List<DroneProgramDiagnostic> diagnostics = DroneProgramHardwareValidator.validate(
                graph, DroneChassisTier.HV, upgrades(), null);

        assertTrue(diagnostics.stream().anyMatch(value -> value.getCode()
                == DroneDiagnosticCode.CHASSIS_TIER_RECOMMENDED
                && value.getSeverity() == DroneDiagnosticSeverity.WARNING));
    }

    @Test
    void editorOnlyCommentsDoNotRaiseChassisRecommendation() {
        DroneProgramGraph graph = new DroneProgramGraph("comments");
        for (int index = 0; index < 100; index++) {
            graph.addNode(DroneProgramNode.create(DrTechDroneNodes.COMMENT, index, 0));
        }

        List<DroneProgramDiagnostic> diagnostics = DroneProgramHardwareValidator.validate(
                graph, DroneChassisTier.HV, upgrades(), null);

        assertFalse(diagnostics.stream()
                .anyMatch(value -> value.getCode() == DroneDiagnosticCode.CHASSIS_TIER_RECOMMENDED));
    }

    private static NBTTagCompound upgrades(DroneUpgradeType... types) {
        NBTTagCompound result = new NBTTagCompound();
        result.setInteger("Version", DroneUpgradeDataCodec.CURRENT_VERSION);
        NBTTagList entries = new NBTTagList();
        for (DroneUpgradeType type : types) {
            NBTTagCompound entry = new NBTTagCompound();
            entry.setString("Id", type.getId().toString());
            entry.setInteger("Level", 1);
            entries.appendTag(entry);
        }
        result.setTag("Entries", entries);
        return result;
    }

    private static boolean has(List<DroneProgramDiagnostic> diagnostics, DroneDiagnosticCode code,
            DroneDiagnosticSeverity severity, String detail) {
        return diagnostics.stream().anyMatch(value -> value.getCode() == code && value.getSeverity() == severity
                && !value.getArguments().isEmpty() && detail.equals(value.getArguments().get(0)));
    }
}
