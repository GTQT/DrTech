package com.drppp.drtech.common.Blocks;

import com.drppp.drtech.DrTechMain;
import com.drppp.drtech.Tags;
import com.drppp.drtech.Tile.TileEntityStoneAxle;
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

public class BlockStoneAxle extends Block {
    private static final AxisAlignedBB hitboxEW = new AxisAlignedBB(0, 0, 0, 1.5, .5, .5).offset(0, 0.25, 0.25);
    private static final AxisAlignedBB hitboxSN = new AxisAlignedBB(0, 0, 0, .5, .5, 1.5).offset(0.25, 0.25, 0);
    public static final PropertyDirection FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);
    public BlockStoneAxle() {
        super(Material.ROCK);
        this.setHardness(2);
        this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
        this.setHarvestLevel("pickaxe", 0);
        this.setRegistryName(Tags.MODID,"stone_axle");
        this.setCreativeTab(DrTechMain.DrTechTab);
        this.setTranslationKey(Tags.MODID+".stone_axle");
    }
    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING);
    }
    @Override
    public IBlockState getStateFromMeta(int meta) {
        EnumFacing facing = EnumFacing.byHorizontalIndex(meta);
        return this.getDefaultState().withProperty(FACING, facing);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(FACING).getHorizontalIndex();
    }
    @org.jetbrains.annotations.Nullable
    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileEntityStoneAxle();
    }

    @Override
    public boolean hasTileEntity() {
        return true;
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    private TileEntityStoneAxle getTe(World world, BlockPos pos) {
        return (TileEntityStoneAxle) world.getTileEntity(pos);
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
      if(!worldIn.isRemote)
      {
          TileEntityStoneAxle te = getTe(worldIn, pos);
          te.setFacing(placer.getHorizontalFacing());
      }
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
        if (te instanceof TileEntityStoneAxle) {
            switch (((TileEntityStoneAxle) te).getFacing()) {

                case DOWN:
                case UP:
                case NORTH:
                case SOUTH:
                    return hitboxSN;
                case WEST:
                case EAST:
                    return hitboxEW;
            }
        }
        return FULL_BLOCK_AABB;
    }

    @SuppressWarnings("deprecation")
    @Override
    @Nonnull
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        TileEntity te = source.getTileEntity(pos);
        if (te instanceof TileEntityStoneAxle) {
            switch (((TileEntityStoneAxle) te).getFacing()) {

                case DOWN:
                case UP:
                case NORTH:
                case SOUTH:
                    return hitboxSN;
                case WEST:
                case EAST:
                    return hitboxEW;
            }
        }
        return FULL_BLOCK_AABB;
    }

}
