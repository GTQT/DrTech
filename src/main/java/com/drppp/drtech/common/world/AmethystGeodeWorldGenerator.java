package com.drppp.drtech.common.world;

import com.drppp.drtech.common.Blocks.BlockAmethystCluster;
import com.drppp.drtech.common.Blocks.BlocksInit;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.fml.common.IWorldGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AmethystGeodeWorldGenerator implements IWorldGenerator {
    private static final int CHUNK_CHANCE = 24;
    private static final double BUDDING_CHANCE = 0.083D;
    private static final double SURFACE_GROWTH_CHANCE = 0.35D;
    private static final double CRACK_CHANCE = 0.95D;
    private static final double CRACK_RADIUS = 2.0D;

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
        if (world.provider.getDimension() != 0 || random.nextInt(CHUNK_CHANCE) != 0) {
            return;
        }

        int x = chunkX * 16 + random.nextInt(16);
        int y = random.nextInt(31);
        int z = chunkZ * 16 + random.nextInt(16);
        generateGeode(world, random, new BlockPos(x, y, z));
    }

    private void generateGeode(World world, Random random, BlockPos center) {
        double baseRadius = 3.2D + random.nextDouble() * 1.6D;
        double airRadius = baseRadius;
        double innerRadius = airRadius + 1.1D;
        double middleRadius = innerRadius + 1.15D;
        double outerRadius = middleRadius + 1.25D;

        double xScale = 0.9D + random.nextDouble() * 0.35D;
        double yScale = 0.75D + random.nextDouble() * 0.35D;
        double zScale = 0.9D + random.nextDouble() * 0.35D;

        boolean hasCrack = random.nextDouble() < CRACK_CHANCE;
        double crackYaw = random.nextDouble() * Math.PI * 2.0D;
        double crackPitch = (random.nextDouble() - 0.35D) * 0.65D;
        double crackX = Math.cos(crackYaw) * Math.cos(crackPitch);
        double crackY = Math.sin(crackPitch);
        double crackZ = Math.sin(crackYaw) * Math.cos(crackPitch);

        int radius = (int) Math.ceil(outerRadius + 2.0D);
        List<BlockPos> buddingBlocks = new ArrayList<>();

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    BlockPos pos = center.add(dx, dy, dz);
                    if (pos.getY() <= 0 || pos.getY() >= world.getActualHeight() - 1) {
                        continue;
                    }
                    if (!canReplace(world.getBlockState(pos))) {
                        continue;
                    }

                    double distance = scaledDistance(dx, dy, dz, xScale, yScale, zScale);
                    if (distance > outerRadius) {
                        continue;
                    }

                    if (hasCrack && isInsideCrack(dx, dy, dz, crackX, crackY, crackZ, outerRadius)) {
                        world.setBlockState(pos, Blocks.AIR.getDefaultState(), 2);
                        continue;
                    }

                    if (distance <= airRadius) {
                        world.setBlockState(pos, Blocks.AIR.getDefaultState(), 2);
                    } else if (distance <= innerRadius) {
                        IBlockState state = random.nextDouble() < BUDDING_CHANCE
                                ? BlocksInit.BLOCK_BUDDING_AMETHYST.getDefaultState()
                                : BlocksInit.BLOCK_AMETHYST_BLOCK.getDefaultState();
                        world.setBlockState(pos, state, 2);
                        if (state.getBlock() == BlocksInit.BLOCK_BUDDING_AMETHYST) {
                            buddingBlocks.add(pos);
                        }
                    } else if (distance <= middleRadius) {
                        world.setBlockState(pos, BlocksInit.BLOCK_CALCITE.getDefaultState(), 2);
                    } else {
                        world.setBlockState(pos, BlocksInit.BLOCK_SMOOTH_BASALT.getDefaultState(), 2);
                    }
                }
            }
        }

        for (BlockPos buddingPos : buddingBlocks) {
            if (random.nextDouble() >= SURFACE_GROWTH_CHANCE) {
                continue;
            }
            List<EnumFacing> possibleFaces = new ArrayList<>();
            for (EnumFacing facing : EnumFacing.values()) {
                BlockPos targetPos = buddingPos.offset(facing);
                if (world.isAirBlock(targetPos)) {
                    possibleFaces.add(facing);
                }
            }
            if (possibleFaces.isEmpty()) {
                continue;
            }

            EnumFacing facing = possibleFaces.get(random.nextInt(possibleFaces.size()));
            BlockPos targetPos = buddingPos.offset(facing);
            world.setBlockState(targetPos, getRandomBud(random)
                    .withProperty(BlockAmethystCluster.FACING, facing), 2);
        }
    }

    private double scaledDistance(int dx, int dy, int dz, double xScale, double yScale, double zScale) {
        double x = dx / xScale;
        double y = dy / yScale;
        double z = dz / zScale;
        return Math.sqrt(x * x + y * y + z * z);
    }

    private boolean isInsideCrack(int dx, int dy, int dz, double crackX, double crackY, double crackZ, double outerRadius) {
        double projection = dx * crackX + dy * crackY + dz * crackZ;
        if (projection < -1.5D || projection > outerRadius + 2.0D) {
            return false;
        }
        double perpendicularX = dx - crackX * projection;
        double perpendicularY = dy - crackY * projection;
        double perpendicularZ = dz - crackZ * projection;
        return perpendicularX * perpendicularX + perpendicularY * perpendicularY + perpendicularZ * perpendicularZ <= CRACK_RADIUS * CRACK_RADIUS;
    }

    private IBlockState getRandomBud(Random random) {
        int roll = random.nextInt(10);
        if (roll <= 3) {
            return BlocksInit.BLOCK_SMALL_AMETHYST_BUD.getDefaultState();
        }
        if (roll <= 6) {
            return BlocksInit.BLOCK_MEDIUM_AMETHYST_BUD.getDefaultState();
        }
        if (roll <= 8) {
            return BlocksInit.BLOCK_LARGE_AMETHYST_BUD.getDefaultState();
        }
        return BlocksInit.BLOCK_AMETHYST_CLUSTER.getDefaultState();
    }

    private boolean canReplace(IBlockState state) {
        Block block = state.getBlock();
        return block != Blocks.BEDROCK
                && block != Blocks.END_PORTAL_FRAME
                && block != Blocks.END_PORTAL
                && block != Blocks.END_GATEWAY
                && block != Blocks.COMMAND_BLOCK
                && block != Blocks.REPEATING_COMMAND_BLOCK
                && block != Blocks.CHAIN_COMMAND_BLOCK;
    }
}
