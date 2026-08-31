package com.drppp.futuremc.blocks;

import com.drppp.futuremc.FutureMCMain;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.BlockLiquid;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Random;

public class BlockBuddingAmethyst extends BlockSimpleDrTech {
    public BlockBuddingAmethyst() {
        super("budding_amethyst", Material.ROCK, SoundType.GLASS, 1.5F, 1.5F);
        setTickRandomly(true);
    }

    @Override
    public void randomTick(World worldIn, BlockPos pos, IBlockState state, Random random) {
        for (EnumFacing facing : EnumFacing.values()) {
            if (random.nextInt(5) != 0) {
                continue;
            }

            BlockPos targetPos = pos.offset(facing);
            IBlockState targetState = worldIn.getBlockState(targetPos);
            Block targetBlock = targetState.getBlock();

            if (canGrowInto(targetState)) {
                worldIn.setBlockState(targetPos, FutureMCMain.SMALL_AMETHYST_BUD.getDefaultState()
                        .withProperty(BlockAmethystCluster.FACING, facing), 2);
                continue;
            }

            if (targetBlock == FutureMCMain.SMALL_AMETHYST_BUD && targetState.getValue(BlockAmethystCluster.FACING) == facing) {
                worldIn.setBlockState(targetPos, FutureMCMain.MEDIUM_AMETHYST_BUD.getDefaultState()
                        .withProperty(BlockAmethystCluster.FACING, facing), 2);
                continue;
            }

            if (targetBlock == FutureMCMain.MEDIUM_AMETHYST_BUD && targetState.getValue(BlockAmethystCluster.FACING) == facing) {
                worldIn.setBlockState(targetPos, FutureMCMain.LARGE_AMETHYST_BUD.getDefaultState()
                        .withProperty(BlockAmethystCluster.FACING, facing), 2);
                continue;
            }

            if (targetBlock == FutureMCMain.LARGE_AMETHYST_BUD && targetState.getValue(BlockAmethystCluster.FACING) == facing) {
                worldIn.setBlockState(targetPos, FutureMCMain.AMETHYST_CLUSTER.getDefaultState()
                        .withProperty(BlockAmethystCluster.FACING, facing), 2);
            }
        }
    }

    private boolean canGrowInto(IBlockState state) {
        Block block = state.getBlock();
        if (block == Blocks.AIR) {
            return true;
        }
        return (block == Blocks.WATER || block == Blocks.FLOWING_WATER)
                && state.getPropertyKeys().contains(BlockLiquid.LEVEL)
                && state.getValue(BlockLiquid.LEVEL) == 0;
    }
}


