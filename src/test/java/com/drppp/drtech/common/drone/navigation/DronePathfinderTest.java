package com.drppp.drtech.common.drone.navigation;

import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DronePathfinderTest {

    private final DronePathfinder pathfinder = new DronePathfinder();

    @Test
    void routesAroundAThreeBlockWall() {
        Set<BlockPos> blocked = new HashSet<>();
        blocked.add(new BlockPos(1, 0, -1));
        blocked.add(new BlockPos(1, 0, 0));
        blocked.add(new BlockPos(1, 0, 1));
        DroneNavigationWorld world = pos -> pos.getY() == 0 && Math.abs(pos.getX()) <= 4 && Math.abs(pos.getZ()) <= 4
                && !blocked.contains(pos);

        DronePathResult result = pathfinder.findPath(new BlockPos(0, 0, 0), new BlockPos(2, 0, 0), world, 16, 256);

        assertTrue(result.isFound());
        assertEquals(new BlockPos(0, 0, 0), result.getPath().get(0));
        assertEquals(new BlockPos(2, 0, 0), result.getPath().get(result.getPath().size() - 1));
        assertTrue(result.getPath().stream().noneMatch(blocked::contains));
        assertTrue(result.getPath().stream().anyMatch(pos -> Math.abs(pos.getZ()) >= 2));
    }

    @Test
    void rejectsTargetOutsideConfiguredRadius() {
        DronePathResult result = pathfinder.findPath(BlockPos.ORIGIN, new BlockPos(65, 0, 0), pos -> true, 64, 4096);
        assertEquals(DronePathStatus.OUT_OF_RANGE, result.getStatus());
    }

    @Test
    void stopsWhenDiscoveryBudgetIsReached() {
        DronePathResult result = pathfinder.findPath(BlockPos.ORIGIN, new BlockPos(8, 0, 0),
                pos -> true, 16, 8);
        assertEquals(DronePathStatus.NODE_LIMIT_REACHED, result.getStatus());
    }
}
