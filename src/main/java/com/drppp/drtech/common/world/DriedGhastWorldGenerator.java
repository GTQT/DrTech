package com.drppp.drtech.common.world;

import com.drppp.drtech.common.Blocks.BlockDriedGhast;
import com.drppp.drtech.common.Blocks.BlocksInit;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.fml.common.IWorldGenerator;

import java.util.Random;

public class DriedGhastWorldGenerator implements IWorldGenerator {
    private static final int NETHER_CHUNK_CHANCE = 512;

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
        if (world.provider.getDimension() != -1 || random.nextInt(NETHER_CHUNK_CHANCE) != 0) {
            return;
        }

        for (int attempt = 0; attempt < 6; attempt++) {
            int x = chunkX * 16 + random.nextInt(16);
            int z = chunkZ * 16 + random.nextInt(16);
            BlockPos pos = findPlacementPos(world, x, z);
            if (pos != null) {
                world.setBlockState(pos, BlocksInit.BLOCK_DRIED_GHAST.getDefaultState()
                        .withProperty(BlockDriedGhast.FACING, EnumFacing.Plane.HORIZONTAL.random(random)), 2);
                return;
            }
        }
    }

    private BlockPos findPlacementPos(World world, int x, int z) {
        int topY = Math.min(110, world.getActualHeight() - 2);
        for (int y = topY; y >= 20; y--) {
            BlockPos groundPos = new BlockPos(x, y, z);
            IBlockState groundState = world.getBlockState(groundPos);
            BlockPos placePos = groundPos.up();
            if (!groundState.isSideSolid(world, groundPos, EnumFacing.UP)) {
                continue;
            }
            if (groundState.getMaterial() == Material.LAVA || groundState.getMaterial() == Material.AIR) {
                continue;
            }
            if (!world.isAirBlock(placePos) || !world.isAirBlock(placePos.up())) {
                continue;
            }
            return placePos;
        }
        return null;
    }
}
