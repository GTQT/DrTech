package com.drppp.drtech.common.Blocks;

import com.drppp.drtech.Tags;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Random;

public class BlockBubbleColumn extends Block {
    public static final PropertyBool DRAG_DOWN = PropertyBool.create("drag_down");

    public BlockBubbleColumn() {
        super(Material.WATER);
        setRegistryName(Tags.MODID, "bubble_column");
        setTranslationKey(Tags.MODID + ".bubble_column");
        setTickRandomly(true);
        setDefaultState(blockState.getBaseState()
                .withProperty(DRAG_DOWN, false)
                .withProperty(BlockLiquid.LEVEL, 0));
    }

    @Override
    public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
        if (!world.isRemote) {
            world.scheduleUpdate(pos, this, 1);
        }
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos) {
        if (!world.isRemote) {
            world.scheduleUpdate(pos, this, 1);
        }
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
        refreshAround(world, pos);
    }

    @Override
    public void onEntityCollision(World world, BlockPos pos, IBlockState state, Entity entityIn) {
        if (entityIn.isSneaking()) {
            return;
        }
        if (state.getValue(DRAG_DOWN)) {
            entityIn.motionY = Math.max(-0.35D, entityIn.motionY - 0.03D);
        } else {
            entityIn.motionY = Math.min(0.8D, entityIn.motionY + 0.08D);
            if (entityIn.motionY > 0.0D) {
                entityIn.motionY += 0.02D;
            }
            entityIn.fallDistance = 0.0F;
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random rand) {
        double x = pos.getX() + 0.2D + rand.nextDouble() * 0.6D;
        double y = pos.getY() + rand.nextDouble();
        double z = pos.getZ() + 0.2D + rand.nextDouble() * 0.6D;
        double speedY = state.getValue(DRAG_DOWN) ? -0.02D : 0.04D;
        world.spawnParticle(EnumParticleTypes.WATER_BUBBLE, x, y, z, 0.0D, speedY, 0.0D);
        if (!state.getValue(DRAG_DOWN) && rand.nextInt(3) == 0) {
            world.spawnParticle(EnumParticleTypes.WATER_WAKE, x, pos.getY() + 1.0D, z, 0.0D, 0.0D, 0.0D);
        }
    }

    public static void refreshAround(World world, BlockPos pos) {
        if (world == null || world.isRemote) {
            return;
        }
        rebuildFromSupport(world, pos);
        rebuildFromSupport(world, pos.down());
        rebuildFromNearbyColumn(world, pos);
        rebuildFromNearbyColumn(world, pos.up());
        rebuildFromNearbyColumn(world, pos.down());
    }

    private static void rebuildFromSupport(World world, BlockPos supportPos) {
        IBlockState supportState = world.getBlockState(supportPos);
        if (!isColumnSupport(supportState)) {
            return;
        }
        BlockPos startPos = supportPos.up();
        IBlockState startState = world.getBlockState(startPos);
        if (isWaterSource(startState) || isBubbleColumn(startState)) {
            rebuildColumn(world, startPos, isDragDownSupport(supportState));
        }
    }

    private static void rebuildFromNearbyColumn(World world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        if (!isWaterSource(state) && !isBubbleColumn(state)) {
            return;
        }
        BlockPos basePos = pos;
        while (basePos.getY() > 0) {
            IBlockState belowState = world.getBlockState(basePos.down());
            if (!isWaterSource(belowState) && !isBubbleColumn(belowState)) {
                break;
            }
            basePos = basePos.down();
        }

        IBlockState supportState = world.getBlockState(basePos.down());
        if (isColumnSupport(supportState)) {
            rebuildColumn(world, basePos, isDragDownSupport(supportState));
        } else if (isBubbleColumn(state)) {
            cleanupColumn(world, pos);
        }
    }

    private static void rebuildColumn(World world, BlockPos startPos, boolean dragDown) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos(startPos);
        while (cursor.getY() < world.getHeight()) {
            IBlockState state = world.getBlockState(cursor);
            if (isWaterSource(state) || isBubbleColumn(state)) {
                IBlockState bubbleState = getBubbleState(dragDown);
                if (state != bubbleState) {
                    world.setBlockState(cursor, bubbleState, 2);
                }
                cursor.move(EnumFacing.UP);
                continue;
            }
            break;
        }
    }

    private static void cleanupColumn(World world, BlockPos startPos) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos(startPos);
        while (cursor.getY() < world.getHeight()) {
            IBlockState state = world.getBlockState(cursor);
            if (!isBubbleColumn(state)) {
                break;
            }
            world.setBlockState(cursor, getWaterSourceState(), 2);
            cursor.move(EnumFacing.UP);
        }
    }

    private static IBlockState getBubbleState(boolean dragDown) {
        return BlocksInit.BLOCK_BUBBLE_COLUMN.getDefaultState().withProperty(DRAG_DOWN, dragDown);
    }

    private static IBlockState getWaterSourceState() {
        return Blocks.WATER.getDefaultState().withProperty(BlockLiquid.LEVEL, 0);
    }

    private static boolean isBubbleColumn(IBlockState state) {
        return state.getBlock() instanceof BlockBubbleColumn;
    }

    private static boolean isWaterSource(IBlockState state) {
        Block block = state.getBlock();
        return (block == Blocks.WATER || block == Blocks.FLOWING_WATER)
                && state.getValue(BlockLiquid.LEVEL) == 0;
    }

    private static boolean isColumnSupport(IBlockState state) {
        Block block = state.getBlock();
        return block == Blocks.SOUL_SAND || block == Blocks.MAGMA;
    }

    private static boolean isDragDownSupport(IBlockState state) {
        return state.getBlock() == Blocks.MAGMA;
    }

    @Override
    public boolean isReplaceable(IBlockAccess worldIn, BlockPos pos) {
        return true;
    }

    @Override
    public boolean canCollideCheck(IBlockState state, boolean hitIfLiquid) {
        return hitIfLiquid;
    }

    @Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
        return NULL_AABB;
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return FULL_BLOCK_AABB;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.TRANSLUCENT;
    }

    @Override
    public int quantityDropped(Random random) {
        return 0;
    }

    @Nullable
    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return null;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, DRAG_DOWN, BlockLiquid.LEVEL);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(DRAG_DOWN) ? 1 : 0;
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState()
                .withProperty(DRAG_DOWN, meta == 1)
                .withProperty(BlockLiquid.LEVEL, 0);
    }

}
