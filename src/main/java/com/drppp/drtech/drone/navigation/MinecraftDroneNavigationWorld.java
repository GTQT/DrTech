package com.drppp.drtech.drone.navigation;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class MinecraftDroneNavigationWorld implements DroneNavigationWorld {

    private final World world;

    public MinecraftDroneNavigationWorld(World world) {
        this.world = world;
    }

    @Override
    public boolean isPassable(BlockPos position) {
        if (!world.getWorldBorder().contains(position) || !world.isBlockLoaded(position)) return false;
        IBlockState state = world.getBlockState(position);
        if (state.getMaterial().isLiquid()) return false;
        AxisAlignedBB collision = state.getCollisionBoundingBox(world, position);
        return collision == null || collision == Block.NULL_AABB;
    }
}
