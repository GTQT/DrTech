package com.drppp.drtech.common.drone.program;

import com.drppp.drtech.common.drone.program.model.DroneArea;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashSet;
import java.util.Set;

class DroneAreaTest {

    @Test
    void normalizesCornersAndTraversesInSerpentineOrder() {
        DroneArea area = DroneArea.between(new BlockPos(1, 1, 1), BlockPos.ORIGIN);

        assertEquals(BlockPos.ORIGIN, area.getMin());
        assertEquals(new BlockPos(1, 1, 1), area.getMax());
        assertEquals(8L, area.getVolume());
        assertEquals(new BlockPos(0, 0, 0), area.positionAt(0));
        assertEquals(new BlockPos(1, 0, 0), area.positionAt(1));
        assertEquals(new BlockPos(1, 0, 1), area.positionAt(2));
        assertEquals(new BlockPos(0, 0, 1), area.positionAt(3));
        assertEquals(new BlockPos(1, 1, 0), area.positionAt(4));
        assertTrue(area.isWithinRuntimeLimits());
    }

    @Test
    void rejectsOversizedAxisOrVolume() {
        assertFalse(DroneArea.between(BlockPos.ORIGIN, new BlockPos(32, 0, 0)).isWithinRuntimeLimits());
        assertFalse(DroneArea.between(BlockPos.ORIGIN, new BlockPos(16, 16, 16)).isWithinRuntimeLimits());
    }

    @Test
    void createsBoundedSphereShellAndVerticalCylinderShell() {
        DroneArea sphere = DroneArea.sphere(new BlockPos(10, 20, 30), 1, false);
        DroneArea sphereShell = DroneArea.sphere(new BlockPos(10, 20, 30), 1, true);
        DroneArea cylinder = DroneArea.cylinder(BlockPos.ORIGIN, 1, 3, false);
        DroneArea cylinderShell = DroneArea.cylinder(BlockPos.ORIGIN, 1, 3, true);

        assertEquals(7L, sphere.getVolume());
        assertEquals(6L, sphereShell.getVolume());
        assertEquals(15L, cylinder.getVolume());
        assertEquals(14L, cylinderShell.getVolume());
        assertEquals(new BlockPos(9, 19, 29), sphere.getMin());
        assertEquals(new BlockPos(11, 21, 31), sphere.getMax());
        assertTrue(sphere.isWithinRuntimeLimits());
        assertTrue(cylinderShell.isWithinRuntimeLimits());
    }

    @Test
    void createsVoxelPathAndCombinesOffsetsAndQueriesRegions() {
        DroneArea path = DroneArea.path(BlockPos.ORIGIN, new BlockPos(3, 3, 0), 0);
        DroneArea first = DroneArea.between(BlockPos.ORIGIN, new BlockPos(2, 0, 0));
        DroneArea second = DroneArea.between(new BlockPos(1, 0, 0), new BlockPos(3, 0, 0));

        assertEquals(4L, path.getVolume());
        assertEquals(new BlockPos(2, 2, 0), path.positionAt(2));
        assertEquals(4L, first.union(second).getVolume());
        assertEquals(2L, first.intersection(second).getVolume());
        assertEquals(1L, first.difference(second).getVolume());
        assertEquals(0L, first.intersection(DroneArea.between(new BlockPos(8, 0, 0),
                new BlockPos(9, 0, 0))).getVolume());
        DroneArea shifted = first.offset(5, 2, -1);
        assertTrue(shifted.contains(new BlockPos(6, 2, -1)));
        assertFalse(shifted.contains(new BlockPos(1, 0, 0)));
    }

    @Test
    void rejectsOversizedPathAndUnionBeforeRuntimeTraversal() {
        assertThrows(IllegalArgumentException.class,
                () -> DroneArea.path(BlockPos.ORIGIN, new BlockPos(32, 0, 0), 0));
        DroneArea first = DroneArea.between(BlockPos.ORIGIN, new BlockPos(15, 15, 15));
        DroneArea second = DroneArea.between(new BlockPos(16, 0, 0), new BlockPos(31, 15, 15));
        assertThrows(IllegalArgumentException.class, () -> first.union(second));
    }

    @Test
    void voxelizesArbitrarilyOrientedPlaneFromTwoEdges() {
        DroneArea plane = DroneArea.plane(BlockPos.ORIGIN, new BlockPos(2, 0, 0), new BlockPos(0, 2, 2));

        assertEquals(9L, plane.getVolume());
        assertTrue(plane.contains(new BlockPos(1, 1, 1)));
        assertTrue(plane.contains(new BlockPos(2, 2, 2)));
        assertFalse(plane.contains(new BlockPos(1, 1, 0)));
    }

    @Test
    void expandsAndInsetsAreasWithoutExceedingRuntimeLimits() {
        DroneArea single = DroneArea.between(BlockPos.ORIGIN, BlockPos.ORIGIN);
        DroneArea expanded = single.expand(1);
        DroneArea inset = expanded.inset(1);

        assertEquals(27L, expanded.getVolume());
        assertEquals(new BlockPos(-1, -1, -1), expanded.getMin());
        assertEquals(new BlockPos(1, 1, 1), expanded.getMax());
        assertEquals(single, inset);
        assertEquals(0L, single.inset(1).getVolume());
        assertThrows(IllegalArgumentException.class,
                () -> DroneArea.between(BlockPos.ORIGIN, new BlockPos(31, 0, 0)).expand(1));
    }

    @Test
    void supportsDeterministicAlternativeTraversalOrders() {
        DroneArea area = DroneArea.between(BlockPos.ORIGIN, new BlockPos(1, 1, 0));

        assertEquals(new BlockPos(0, 1, 0), area.positionAt(0, DroneArea.TraversalOrder.REVERSE));
        assertEquals(new BlockPos(1, 1, 0), area.positionAt(0, DroneArea.TraversalOrder.TOP_DOWN));
        Set<BlockPos> randomized = new HashSet<>();
        for (int index = 0; index < area.getVolume(); index++) {
            BlockPos first = area.positionAt(index, DroneArea.TraversalOrder.RANDOMIZED);
            assertEquals(first, area.positionAt(index, DroneArea.TraversalOrder.RANDOMIZED));
            randomized.add(first);
        }
        assertEquals(area.getVolume(), randomized.size());
    }
}
