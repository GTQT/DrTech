package com.drppp.drtech.common.Blocks;

import com.drppp.drtech.DrTechMain;
import com.drppp.drtech.Tags;
import com.drppp.drtech.Tile.TileEntityWoodAxle;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockWoodAxle extends Block {
    public static final PropertyDirection FACING = PropertyDirection.create("facing");
    private static final AxisAlignedBB hitboxEW = new AxisAlignedBB(0, 0, 0, 1.5, .5, .5).offset(0, 0.25, 0.25);
    private static final AxisAlignedBB hitboxSN = new AxisAlignedBB(0, 0, 0, .5, .5, 1.5).offset(0.25, 0.25, 0);
    // 在类顶部添加垂直方向的AABB定义
    private static final AxisAlignedBB hitboxUD = new AxisAlignedBB(0.25, 0, 0.25, 0.75, 1.5, 0.75).offset(0, -0.25, 0);

    public BlockWoodAxle() {
        super(Material.ROCK);
        this.setHardness(2);
        this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
        this.setHarvestLevel("pickaxe", 0);
        this.setRegistryName(Tags.MODID, "wood_axle");
        this.setCreativeTab(DrTechMain.DrTechTab);
        this.setTranslationKey(Tags.MODID + ".wood_axle");
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        // 使用完整方向索引（0-5对应所有方向）
        return this.getDefaultState().withProperty(FACING, EnumFacing.byIndex(meta));
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        // 获取完整方向索引
        return state.getValue(FACING).getIndex();
    }

    @org.jetbrains.annotations.Nullable
    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileEntityWoodAxle();
    }

    @Override
    public boolean hasTileEntity() {
        return true;
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    private TileEntityWoodAxle getTe(World world, BlockPos pos) {
        return (TileEntityWoodAxle) world.getTileEntity(pos);
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        if (!worldIn.isRemote) {
            TileEntityWoodAxle te = getTe(worldIn, pos);
            // 获取包含垂直方向的完整朝向
            EnumFacing facing = calculateProperFacing(placer);
            te.setFacing(facing);
        }
    }

    // 新增方向计算方法
    private EnumFacing calculateProperFacing(EntityLivingBase placer) {
        // 当玩家潜行时允许放置垂直方向
        if(placer.isSneaking()){
            float pitch = placer.rotationPitch;
            // 根据俯仰角判断上下方向
            if(pitch > 45) return EnumFacing.DOWN;
            if(pitch < -45) return EnumFacing.UP;
        }
        // 默认水平方向（保持原有逻辑）
        return placer.getHorizontalFacing().getOpposite();
    }

    @Nonnull
    @Deprecated
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.INVISIBLE;
    }

    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    public boolean isFullCube(IBlockState state) {
        return false;
    }

    public boolean isPassable(IBlockAccess worldIn, BlockPos pos) {
        return true;
    }

    @SuppressWarnings("deprecation")
    @Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, @Nonnull IBlockAccess worldIn, @Nonnull BlockPos pos) {
        TileEntity te = worldIn.getTileEntity(pos);
        if (te instanceof TileEntityWoodAxle) {
            return switch (((TileEntityWoodAxle) te).getFacing()) {
                case NORTH, SOUTH -> hitboxSN;
                case EAST, WEST -> hitboxEW;
                case UP, DOWN -> hitboxUD;
            };
        }
        return FULL_BLOCK_AABB;
    }

    @SuppressWarnings("deprecation")
    @Override
    @Nonnull
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        TileEntity te = source.getTileEntity(pos);
        if (te instanceof TileEntityWoodAxle) {
            return switch (((TileEntityWoodAxle) te).getFacing()) {
                case NORTH, SOUTH -> hitboxSN;
                case EAST, WEST -> hitboxEW;
                case UP, DOWN -> hitboxUD;
            };
        }
        return FULL_BLOCK_AABB;
    }
}
