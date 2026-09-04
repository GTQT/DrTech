package com.drppp.drtech.drone.navigation;

import net.minecraft.util.math.BlockPos;

@FunctionalInterface
public interface DroneNavigationWorld {

    boolean isPassable(BlockPos position);
}
