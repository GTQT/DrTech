package com.drppp.drtech.common.drone.program.runtime;

import com.drppp.drtech.common.drone.inventory.DroneItemFilter;
import com.drppp.drtech.common.drone.filter.DroneBlockFilterSpec;
import com.drppp.drtech.common.drone.filter.DroneFilterMode;
import com.drppp.drtech.common.drone.program.model.DroneProgramNode;
import com.drppp.drtech.common.drone.program.model.DroneArea;
import com.drppp.drtech.common.drone.program.registry.DrTechDroneNodes;
import com.drppp.drtech.common.drone.program.runtime.service.DroneSensorService;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class DroneSensorValueEvaluatorTest {

    private final DroneValueEvaluatorRegistry evaluators = DrTechDroneValueEvaluators.createDefaultRegistry();
    private final RecordingSensors environment = new RecordingSensors();
    private final DroneRuntimeMemory memory = new DroneRuntimeMemory();

    @Test
    void evaluatesCargoInventoryRedstoneAndLightSensors() throws Exception {
        assertEquals(37, evaluate(DrTechDroneNodes.CARGO_ITEM_COUNT, new NBTTagCompound(), map()));
        assertEquals(4, evaluate(DrTechDroneNodes.CARGO_FREE_SLOTS, new NBTTagCompound(), map()));
        assertEquals(25.0D, evaluate(DrTechDroneNodes.CARGO_USED_PERCENT, new NBTTagCompound(), map()));

        NBTTagCompound inventoryConfig = new NBTTagCompound();
        inventoryConfig.setString("Direction", "DOWN");
        BlockPos target = new BlockPos(3, 4, 5);
        Map<String, Object> inventoryInputs = map();
        inventoryInputs.put("target", target);
        inventoryInputs.put("filter", DroneItemFilter.ANY);
        assertEquals(64, evaluate(DrTechDroneNodes.INVENTORY_ITEM_COUNT, inventoryConfig, inventoryInputs));
        assertEquals(target, environment.lastTarget);
        assertEquals(EnumFacing.DOWN, environment.lastSide);
        assertSame(DroneItemFilter.ANY, environment.lastFilter);

        Map<String, Object> worldInputs = map();
        worldInputs.put("target", target);
        assertEquals(11, evaluate(DrTechDroneNodes.REDSTONE_STRENGTH, new NBTTagCompound(), worldInputs));
        assertEquals(9, evaluate(DrTechDroneNodes.REDSTONE_OUTPUT_LEVEL, new NBTTagCompound(), worldInputs));
        NBTTagCompound lightConfig = new NBTTagCompound();
        lightConfig.setString("LightType", "SKY");
        assertEquals(14, evaluate(DrTechDroneNodes.LIGHT_LEVEL, lightConfig, worldInputs));
        assertEquals(DroneSensorService.LightType.SKY, environment.lastLightType);
    }

    @Test
    void exposesAndPersistsLastActionDetails() throws Exception {
        memory.setLastAction(DroneActionStatus.NOT_FOUND, "missing inventory");
        assertEquals(DroneActionStatus.NOT_FOUND,
                evaluate(DrTechDroneNodes.LAST_ACTION_STATUS, new NBTTagCompound(), map()));
        assertEquals("missing inventory", evaluate(DrTechDroneNodes.LAST_ACTION_ERROR, new NBTTagCompound(), map()));

        NBTTagCompound compareConfig = new NBTTagCompound();
        compareConfig.setString("Status", "NOT_FOUND");
        Map<String, Object> compareInputs = map();
        compareInputs.put("status", DroneActionStatus.NOT_FOUND);
        assertEquals(true, evaluate(DrTechDroneNodes.COMPARE_ACTION_STATUS, compareConfig, compareInputs));

        DroneRuntimeMemory restored = new DroneRuntimeMemory();
        restored.readFromNbt(memory.writeToNbt());
        assertEquals(DroneActionStatus.NOT_FOUND, restored.getLastActionStatus());
        assertEquals("missing inventory", restored.getLastActionError());
    }

    @Test
    void evaluatesBlockFilterReachabilityAndDockAvailability() throws Exception {
        DroneBlockFilterSpec expected = new DroneBlockFilterSpec(DroneFilterMode.WHITELIST,
                Collections.singletonList(new DroneBlockFilterSpec.Rule(
                        new net.minecraft.util.ResourceLocation("minecraft:stone"), 0)));
        NBTTagCompound filterConfig = new NBTTagCompound();
        filterConfig.setTag("FilterSpec", expected.writeToNbt());
        DroneBlockFilterSpec decoded = (DroneBlockFilterSpec) evaluate(
                DrTechDroneNodes.BLOCK_FILTER, filterConfig, map());
        assertEquals(1, decoded.getRules().size());

        BlockPos target = new BlockPos(7, 8, 9);
        Map<String, Object> inputs = map();
        inputs.put("target", target);
        inputs.put("filter", decoded);
        assertEquals(true, evaluate(DrTechDroneNodes.BLOCK_MATCHES, new NBTTagCompound(), inputs));
        assertSame(decoded, environment.lastBlockFilter);
        assertEquals(true, evaluate(DrTechDroneNodes.COORDINATE_REACHABLE, new NBTTagCompound(), inputs));
        assertEquals(true, evaluate(DrTechDroneNodes.DOCK_AVAILABLE, new NBTTagCompound(), inputs));
        assertEquals(target, environment.lastTarget);

        NBTTagCompound areaConfig = new NBTTagCompound();
        areaConfig.setInteger("Limit", 12);
        Map<String, Object> areaInputs = map();
        DroneArea area = DroneArea.between(BlockPos.ORIGIN, new BlockPos(2, 0, 2));
        areaInputs.put("area", area);
        areaInputs.put("filter", decoded);
        assertEquals(7, evaluate(DrTechDroneNodes.AREA_BLOCK_COUNT, areaConfig, areaInputs));
        assertEquals(area, environment.lastArea);
        assertEquals(12, environment.lastLimit);
    }

    @Test
    void evaluatesAreaBooleanOffsetContainsAndVolumeNodes() throws Exception {
        DroneArea first = DroneArea.between(BlockPos.ORIGIN, new BlockPos(2, 0, 0));
        DroneArea second = DroneArea.between(new BlockPos(1, 0, 0), new BlockPos(3, 0, 0));
        Map<String, Object> binary = map();
        binary.put("first", first);
        binary.put("second", second);
        DroneArea union = (DroneArea) evaluate(DrTechDroneNodes.AREA_UNION, new NBTTagCompound(), binary);
        assertEquals(4L, union.getVolume());

        NBTTagCompound offset = new NBTTagCompound();
        offset.setInteger("X", 4);
        Map<String, Object> areaInput = map();
        areaInput.put("area", union);
        DroneArea shifted = (DroneArea) evaluate(DrTechDroneNodes.AREA_OFFSET, offset, areaInput);
        Map<String, Object> contains = map();
        contains.put("area", shifted);
        contains.put("coordinate", new BlockPos(7, 0, 0));
        assertEquals(true, evaluate(DrTechDroneNodes.AREA_CONTAINS, new NBTTagCompound(), contains));
        assertEquals(4.0D, evaluate(DrTechDroneNodes.AREA_VOLUME, new NBTTagCompound(), areaInput));
    }

    @Test
    void evaluatesNearestDockCoordinate() throws Exception {
        assertEquals(new BlockPos(12, 65, -7),
                evaluate(DrTechDroneNodes.FIND_NEAREST_DOCK, new NBTTagCompound(), map()));
    }

    @Test
    void evaluatesTargetEuSensors() throws Exception {
        BlockPos target = new BlockPos(-8, 70, 3);
        Map<String, Object> inputs = map();
        inputs.put("target", target);

        assertEquals(4_000L, evaluate(DrTechDroneNodes.TARGET_ENERGY, new NBTTagCompound(), inputs));
        assertEquals(16_000L, evaluate(DrTechDroneNodes.TARGET_ENERGY_CAPACITY, new NBTTagCompound(), inputs));
        assertEquals(25.0D, evaluate(DrTechDroneNodes.TARGET_ENERGY_PERCENT, new NBTTagCompound(), inputs));
        assertEquals(target, environment.lastTarget);
    }

    private Object evaluate(net.minecraft.util.ResourceLocation type, NBTTagCompound configuration,
            Map<String, Object> inputs) throws Exception {
        DroneProgramNode node = DroneProgramNode.create(type, 0, 0).withConfiguration(configuration);
        DroneNodeExecutionContext.InputResolver resolver = new DroneNodeExecutionContext.InputResolver() {
            @Override
            public <T> T resolve(String portId, Class<T> expectedType) {
                Object value = inputs.get(portId);
                return value == null ? null : expectedType.cast(value);
            }
        };
        return evaluators.get(type).evaluate(new DroneValueEvaluationContext(node, environment, memory, resolver),
                "value");
    }

    private static Map<String, Object> map() {
        return new HashMap<>();
    }

    private static final class RecordingSensors implements DroneRuntimeEnvironment {
        private BlockPos lastTarget;
        private EnumFacing lastSide;
        private DroneItemFilter lastFilter;
        private DroneSensorService.LightType lastLightType;
        private DroneBlockFilterSpec lastBlockFilter;
        private DroneArea lastArea;
        private int lastLimit;

        @Override public double getEnergyPercent() { return 100.0D; }
        @Override public DroneExecutionResult moveTo(BlockPos target) { return DroneExecutionResult.success(); }
        @Override public int getCargoItemCount(DroneItemFilter filter) { lastFilter = filter; return 37; }
        @Override public int getCargoFreeSlots() { return 4; }
        @Override public double getCargoUsedPercent() { return 25.0D; }
        @Override public int getInventoryItemCount(BlockPos target, EnumFacing side, DroneItemFilter filter) {
            lastTarget = target;
            lastSide = side;
            lastFilter = filter;
            return 64;
        }
        @Override public int getRedstoneStrength(BlockPos target) { lastTarget = target; return 11; }
        @Override public int getRedstoneOutputStrength(BlockPos target) { lastTarget = target; return 9; }
        @Override public int getLightLevel(BlockPos target, DroneSensorService.LightType type) {
            lastTarget = target;
            lastLightType = type;
            return 14;
        }
        @Override public boolean matchesBlock(BlockPos target, DroneBlockFilterSpec filter) {
            lastTarget = target;
            lastBlockFilter = filter;
            return true;
        }
        @Override public boolean isCoordinateReachable(BlockPos target) { lastTarget = target; return true; }
        @Override public boolean isDockAvailable(BlockPos target) { lastTarget = target; return true; }
        @Override public BlockPos findNearestDock() { return new BlockPos(12, 65, -7); }
        @Override public long getTargetEnergy(BlockPos target) { lastTarget = target; return 4_000L; }
        @Override public long getTargetEnergyCapacity(BlockPos target) { lastTarget = target; return 16_000L; }
        @Override public int countMatchingBlocks(DroneArea area, DroneBlockFilterSpec filter, int limit) {
            lastArea = area;
            lastBlockFilter = filter;
            lastLimit = limit;
            return 7;
        }
    }
}
