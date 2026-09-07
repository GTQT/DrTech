package com.drppp.drtech.common.drone.navigation;

import net.minecraft.util.math.BlockPos;

@FunctionalInterface
public interface DroneNavigationWorld {

    boolean isPassable(BlockPos position);
}
